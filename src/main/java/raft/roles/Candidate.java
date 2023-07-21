package raft.roles;

import raft.RaftNode;
import raft.State;
import raft.request.ClientRequest;
import raft.request.RPCAppendEntriesRequest;
import raft.request.RPCVoteRequestRequest;
import raft.response.ClientRequestResponse;
import raft.response.RPCAppendEntriesResponse;
import raft.response.RPCVoteRequestResponse;
import raft.tasks.ElectionTask;

import java.util.List;
import java.util.Objects;
import java.util.Timer;

public class Candidate extends Role {

    Timer timer;

    public Candidate(RaftNode node) {
        this.timer = new Timer();
    }

    @Override
    public void onElectionTimeoutElapsed(RaftNode node) {
        this.startElection(node);
    }

    public void resetElectionTimer(RaftNode node) {
        this.timer.schedule(new ElectionTask(this, node), node.getElectionInterval());
    }

    public void startElection(RaftNode node) {
        node.setState(new State(node.getCurrentTerm() + 1, node.getId(), node.getState().getLog()));
        this.resetElectionTimer(node);
        List<RPCVoteRequestResponse> responses = node
                .sendRPCVoteRequests();

        responses.forEach(response -> this.handleRPCVoteRequestResponse(node, response));

        int voteCount = responses.stream()
                .filter(Objects::nonNull)
                .mapToInt(response -> response.isVoteGranted() ? 1 : 0)
                .sum() + 1;

        System.out.println("Node " + node.getId() + " received " + voteCount + " votes");

        boolean hasReceivedMajority = voteCount >= node.getMajorityCount();

        if (hasReceivedMajority) {
            this.transitionToLeader(node);
        }
    }

    @Override
    public ClientRequestResponse handleClientRequest(RaftNode node, ClientRequest request) {
        return null;
    }

    public RPCAppendEntriesResponse handleRPCAppendEntriesRequest(RaftNode node, RPCAppendEntriesRequest request) {

        int term = request.getTerm();
        if (term >= node.getCurrentTerm()) {
            node.setState(new State(term, request.getLeaderId(), node.getState().getLog()));
            node.setRole(new Follower(node));
        }

        return super.handleRPCAppendEntriesRequest(node, request);

    }


    public void transitionToLeader(RaftNode node){
        this.timer.cancel();
        System.out.println("Node " + node.getId() + " passing from " + this.getClass().getSimpleName() + " to Leader");
        Leader leader = new Leader(node);
        node.setRole(leader);
    }

    public void transitionToFollower(RaftNode node){
        this.timer.cancel();
        System.out.println("Node " + node.getId() + " passing from " + this.getClass().getSimpleName() + " to Follower");
        Follower follower = new Follower(node);
        node.setRole(follower);
    }

}
