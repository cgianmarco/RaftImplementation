package raft.roles;

import raft.RaftNode;
import raft.request.ClientRequest;
import raft.request.RPCAppendEntriesRequest;
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
        super(node);
        this.timer = new Timer();
    }

    @Override
    public void onElectionTimeoutElapsed() {
        this.startElection();
    }

    public void resetElectionTimer() {
        this.timer.schedule(new ElectionTask(this), node.getElectionInterval());
    }

    public void startElection() {
        node.setCurrentTerm(node.getCurrentTerm() + 1);
        node.setVotedFor(node.getId());
        this.resetElectionTimer();
        List<RPCVoteRequestResponse> responses = node
                .sendRPCVoteRequests();

        responses.forEach(response -> this.handleRPCVoteRequestResponse(response));

        int voteCount = responses.stream()
                .filter(Objects::nonNull)
                .mapToInt(response -> response.isVoteGranted() ? 1 : 0)
                .sum() + 1;

        System.out.println("Node " + node.getId() + " received " + voteCount + " votes");

        boolean hasReceivedMajority = voteCount >= node.getMajorityCount();

        if (hasReceivedMajority) {
            this.transitionToLeader();
        }
    }

    @Override
    public ClientRequestResponse handleClientRequest(ClientRequest request) {
        return null;
    }

    public RPCAppendEntriesResponse handleRPCAppendEntriesRequest(RPCAppendEntriesRequest request) {

        int term = request.getTerm();
        if (term >= node.getCurrentTerm()) {
            node.setCurrentTerm(term);
            node.setVotedFor(request.getLeaderId());
            node.setRole(new Follower(node));
        }

        return super.handleRPCAppendEntriesRequest(request);

    }


    public void transitionToLeader(){
        this.timer.cancel();
        System.out.println("Node " + node.getId() + " passing from " + this.getClass().getSimpleName() + " to Leader");
        Leader leader = new Leader(node);
        node.setRole(leader);
    }

    public void transitionToFollower(){
        this.timer.cancel();
        System.out.println("Node " + node.getId() + " passing from " + this.getClass().getSimpleName() + " to Follower");
        Follower follower = new Follower(node);
        node.setRole(follower);
    }

}
