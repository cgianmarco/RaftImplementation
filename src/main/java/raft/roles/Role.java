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

public abstract class Role {

    public void handleRPCRequest(RaftNode node, RPCRequest request) {

    }

    public void handleRPCResponse(RaftNode node, RPCResponse response) {
        if (response != null) {
            int term = response.getTerm();
            if (term > node.getCurrentTerm()) {
                node.setCurrentTerm(term);
                node.setVotedFor(-1);
                this.transitionToFollower(node);
            }
        }

    }

    public abstract ClientRequestResponse handleClientRequest(RaftNode node, ClientRequest request);

    public void handleRPCVoteRequestResponse(RaftNode node, RPCVoteRequestResponse response) {
        this.handleRPCResponse(node, response);
    }

    public void handleRPCAppendEntriesResponse(RaftNode node, RPCAppendEntriesResponse response) {
        this.handleRPCResponse(node, response);
    }

    public RPCVoteRequestResponse handleRPCVoteRequest(RaftNode node, RPCVoteRequestRequest request) {
        int term = request.getTerm();
        if (term > node.getCurrentTerm()) {
            node.setCurrentTerm(term);
            node.setVotedFor(request.getCandidateId());
            this.transitionToFollower(node);
        }

        if (term < node.getCurrentTerm()) {
            return new RPCVoteRequestResponse(node.getCurrentTerm(), false);
        }

        if ((node.getVotedFor() == -1 || node.getVotedFor() == request.getCandidateId()) &&
                node.getLog().hasLogAtLeastAsUpToDate(request.getLastLogIndex(), request.getLastLogTerm())) {

            node.setCurrentTerm(term);
            node.setVotedFor(request.getCandidateId());
            return new RPCVoteRequestResponse(node.getCurrentTerm(), true);
        }

        return new RPCVoteRequestResponse(node.getCurrentTerm(), false);
    }

    public RPCAppendEntriesResponse handleRPCAppendEntriesRequest(RaftNode node, RPCAppendEntriesRequest request) {
        int term = request.getTerm();
        if (term >= node.getCurrentTerm()) {
            node.setCurrentTerm(term);
            node.setVotedFor(request.getLeaderId());
            this.transitionToFollower(node);
        }

        // Reply false if term < currentTerm
        if (term < node.getCurrentTerm()) {
            return new RPCAppendEntriesResponse(node.getCurrentTerm(), false);
        }

        // Reply false if log doesn't contain an entry at prevLogIndex whose term matches prevLogTerm
        try {
            LogEntry logEntry = node.getLog().getEntryByIndex(request.getPrevLogIndex());
            if (logEntry.getTerm() != request.getPrevLogTerm()) {
                return new RPCAppendEntriesResponse(node.getCurrentTerm(), false);
            }
        } catch (IndexOutOfBoundsException e) {
            return new RPCAppendEntriesResponse(node.getCurrentTerm(), false);
        }

        // If an existing entry conflicts with a new one (same index but different terms)
        // delete the existing entry and all that follow it
        node.getLog().resolveConflictsWithNewEntries(request.getEntries());

        // Append new entries not already in the log
        node.getLog().appendEntries(request.getEntries());

        // If leaderCommit > commitIndex,
        // set commitIndex = min(leaderCommit, index of last new entry)
        int leaderCommit = request.getLeaderCommit();
        if(leaderCommit > node.getCommitIndex()){
            node.setCommitIndex(Math.min(leaderCommit, request.getEntries().stream().mapToInt(entry -> entry.getIndex()).max().orElse(Integer.MAX_VALUE)));
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
