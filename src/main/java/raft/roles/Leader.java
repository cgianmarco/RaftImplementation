package raft.roles;

import raft.RaftNode;
import raft.request.RPCAppendEntriesRequest;
import raft.response.RPCAppendEntriesResponse;
import raft.tasks.HeartbeatTask;

import java.util.List;
import java.util.Timer;

public class Leader extends Role {

    Timer timer;

    public Leader(RaftNode node) {
        List<RPCAppendEntriesResponse> responses = node
                .sendRPCAppendEntriesRequests(new RPCAppendEntriesRequest(node.getCurrentTerm(), node.getId()));
        responses.forEach(response -> this.handleRPCAppendEntriesResponse(node, response));
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new HeartbeatTask(this, node), 0, node.getHeartbeatInterval());
    }

    @Override
    public void onHeartbeatTimeoutElapsed(RaftNode node) {
        List<RPCAppendEntriesResponse> responses = node
                .sendRPCAppendEntriesRequests(new RPCAppendEntriesRequest(node.getCurrentTerm(), node.getId()));
        responses.forEach(response -> this.handleRPCAppendEntriesResponse(node, response));
    }
    public void transitionToLeader(RaftNode node){
        this.timer.cancel();
        // System.out.println("Node " + node.getId() + " passing from " + this.getClass().toString() + " to Follower");
        node.setRole(new Leader(node));
    }


    public void transitionToFollower(RaftNode node){
        this.timer.cancel();
        System.out.println("Node " + node.getId() + " passing from " + this.getClass().getSimpleName() + " to Follower");
        Follower follower = new Follower(node);
        node.setRole(follower);
    }



}
