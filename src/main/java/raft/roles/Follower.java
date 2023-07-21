package raft.roles;

import raft.RaftNode;
import raft.tasks.ElectionTask;

import java.util.Timer;

public class Follower extends Role {

    Timer timer;

    public Follower(RaftNode node) {
        this.timer = new Timer();
        this.timer.schedule(new ElectionTask(this, node), node.getElectionInterval());
    }

    @Override
    public void onElectionTimeoutElapsed(RaftNode node) {
        System.out.println("elapsed timeout follower for node " + node.getId());
        this.transitionToCandidate(node);
    }

    public void transitionToFollower(RaftNode node){
        this.timer.cancel();
        // System.out.println("Node " + node.getId() + " passing from " + this.getClass().toString() + " to Follower");
        node.setRole(new Follower(node));
    }

    public void transitionToCandidate(RaftNode node){
        this.timer.cancel();
        System.out.println("Node " + node.getId() + " passing from " + this.getClass().getSimpleName() + " to Candidate");
        Candidate candidate = new Candidate(node);
        node.setRole(candidate);
        candidate.startElection(node);

    }

}
