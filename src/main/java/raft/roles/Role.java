package raft.roles;

import org.example.*;
import raft.RaftNode;
import raft.State;
import raft.request.RPCAppendEntriesRequest;
import raft.request.RPCRequest;
import raft.request.RPCVoteRequestRequest;
import raft.response.RPCAppendEntriesResponse;
import raft.response.RPCResponse;
import raft.response.RPCVoteRequestResponse;

public abstract class Role {

    public void handleRPCRequest(RaftNode node, RPCRequest request) {

    }

    public void handleRPCResponse(RaftNode node, RPCResponse response) {
        if(response != null){
            int term = response.getTerm();
            if (term > node.getCurrentTerm()) {
                node.setState(new State(term, -1));
                this.transitionToFollower(node);
            }
        }

    }

    public void handleRPCVoteRequestResponse(RaftNode node, RPCVoteRequestResponse response) {
        this.handleRPCResponse(node, response);
    }

    public void handleRPCAppendEntriesResponse(RaftNode node, RPCAppendEntriesResponse response) {
        this.handleRPCResponse(node, response);
    }

    public RPCVoteRequestResponse handleRPCVoteRequest(RaftNode node, RPCVoteRequestRequest request) {
        int term = request.getTerm();
        if (term > node.getCurrentTerm()) {
            node.setState(new State(term, request.getCandidateId()));
            this.transitionToFollower(node);
        }

        if (term < node.getCurrentTerm()) {
            return new RPCVoteRequestResponse(node.getCurrentTerm(), false);
        }

        if (node.getVotedFor() == -1 || node.getVotedFor() == request.getCandidateId()) {
            node.setState(new State(term, request.getCandidateId()));
            return new RPCVoteRequestResponse(node.getCurrentTerm(), true);
        }

        return new RPCVoteRequestResponse(node.getCurrentTerm(), false);
    }

    public RPCAppendEntriesResponse handleRPCAppendEntriesRequest(RaftNode node, RPCAppendEntriesRequest request) {
        int term = request.getTerm();
        if (term >= node.getCurrentTerm()) {
            node.setState(new State(term, request.getLeaderId()));
            this.transitionToFollower(node);
        }

        if (term < node.getCurrentTerm()) {
            return new RPCAppendEntriesResponse(node.getCurrentTerm(), false);
        }

        //this.transitionToFollower(node); // I added this
        return new RPCAppendEntriesResponse(node.getCurrentTerm(), true);
    }

    public abstract void transitionToFollower(RaftNode node);


    public void onElectionTimeoutElapsed(RaftNode node) {
    }

    public void onHeartbeatTimeoutElapsed(RaftNode node) {

    }

}
