package raft.roles;

import raft.RaftNode;
import raft.request.ClientRequest;
import raft.response.ClientRequestResponse;
import raft.tasks.ElectionTask;

import java.util.Timer;

public class Follower extends Role {

    Timer timer;

    public Follower(RaftNode node) {
        super(node);
        this.timer = new Timer();
        this.timer.schedule(new ElectionTask(this), node.getElectionInterval());
    }

    @Override
    public void onElectionTimeoutElapsed() {
        System.out.println("elapsed timeout follower for node " + node.getId());
        this.transitionToCandidate();
    }

    public void transitionToFollower(){
        this.timer.cancel();
        // System.out.println("Node " + node.getId() + " passing from " + this.getClass().toString() + " to Follower");
        node.setRole(new Follower(node));
    }

    public void transitionToCandidate(){
        this.timer.cancel();
        System.out.println("Node " + node.getId() + " passing from " + this.getClass().getSimpleName() + " to Candidate");
        Candidate candidate = new Candidate(node);
        node.setRole(candidate);
        candidate.startElection();

    }

    @Override
    public ClientRequestResponse handleClientRequest(ClientRequest request) {
        return null;
    }

}
