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
import java.util.concurrent.CompletableFuture;

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
        node.assignVoteForTerm(node.getId(), node.getCurrentTerm() + 1);
        this.resetElectionTimer();
        List<CompletableFuture<RPCVoteRequestResponse>> responses = node.forAllOtherNodes(nodeId -> node.sendRPCVoteRequest(nodeId));

        responses.forEach(future -> future
                .thenAccept(response -> this.handleRPCVoteRequestResponse(response)));

        int voteCount = responses.stream()
                .map(future -> future.join())
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
            node.assignVoteForTerm(request.getLeaderId(), term);
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
