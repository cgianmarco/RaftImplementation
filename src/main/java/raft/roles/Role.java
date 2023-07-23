package raft.roles;

import raft.LogEntry;
import raft.RaftNode;
import raft.request.ClientRequest;
import raft.request.RPCAppendEntriesRequest;
import raft.request.RPCRequest;
import raft.request.RPCVoteRequestRequest;
import raft.response.ClientRequestResponse;
import raft.response.RPCAppendEntriesResponse;
import raft.response.RPCResponse;
import raft.response.RPCVoteRequestResponse;

import java.util.Collections;

public abstract class Role {

    RaftNode node;

    public Role(RaftNode node){
        this.node = node;
    }

    public void handleRPCRequest(RPCRequest request) {

    }

    public void handleRPCResponse(RPCResponse response) {
        if (response != null) {
            int term = response.getTerm();
            if (term > node.getCurrentTerm()) {
                node.assignVoteForTerm(-1, term);
                this.transitionToFollower();
            }
        }

    }

    public abstract ClientRequestResponse handleClientRequest(ClientRequest request);

    public void handleRPCVoteRequestResponse(RPCVoteRequestResponse response) {
        this.handleRPCResponse(response);
    }

    public void handleRPCAppendEntriesResponse(RPCAppendEntriesResponse response) {
        this.handleRPCResponse(response);
    }

    public RPCVoteRequestResponse handleRPCVoteRequest(RPCVoteRequestRequest request) {
        int term = request.getTerm();
        if (term > node.getCurrentTerm()) {
            node.assignVoteForTerm(request.getCandidateId(), term);
            this.transitionToFollower();
        }

        if (term < node.getCurrentTerm()) {
            return new RPCVoteRequestResponse(node.getCurrentTerm(), false);
        }

        if ((node.getVotedFor() == -1 || node.getVotedFor() == request.getCandidateId()) &&
                node.getLog().isAtLeastAsUpToDate(request.getLastLogIndex(), request.getLastLogTerm())) {

            node.assignVoteForTerm(request.getCandidateId(), term);
            return new RPCVoteRequestResponse(node.getCurrentTerm(), true);
        }

        return new RPCVoteRequestResponse(node.getCurrentTerm(), false);
    }

    public RPCAppendEntriesResponse handleRPCAppendEntriesRequest(RPCAppendEntriesRequest request) {
        int term = request.getTerm();
        if (term >= node.getCurrentTerm()) {
            node.assignVoteForTerm(request.getLeaderId(), term);
            this.transitionToFollower();
        }

        // Reply false if term < currentTerm
        if (term < node.getCurrentTerm()) {
            return new RPCAppendEntriesResponse(node.getCurrentTerm(), false);
        }

        // Reply false if log doesn't contain an entry at prevLogIndex whose term matches prevLogTerm
        if(!node.getLog().checkConsistency(request.getPrevLogIndex(), request.getPrevLogTerm())){
            return new RPCAppendEntriesResponse(node.getCurrentTerm(), false);
        }

        node.getLog().addNonConflictingEntries(request.getEntries());
        node.getLog().commitEntriesUpTo(request.getLeaderCommit());

        node.onStateChanged();

        return new RPCAppendEntriesResponse(node.getCurrentTerm(), true);
    }

    public abstract void transitionToFollower();


    public void onElectionTimeoutElapsed() {
    }

    public void onHeartbeatTimeoutElapsed() {

    }

}
