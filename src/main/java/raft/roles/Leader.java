package raft.roles;

import raft.LogEntry;
import raft.RaftNode;
import raft.request.ClientRequest;
import raft.request.RPCAppendEntriesRequest;
import raft.response.ClientRequestResponse;
import raft.response.RPCAppendEntriesResponse;
import raft.tasks.HeartbeatTask;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;

public class Leader extends Role {

    Timer timer;
    List<Integer> nextIndex;
    List<Integer>  matchIndex;

    public Leader(RaftNode node) {
        this.initializeLeaderState(node);
        List<RPCAppendEntriesResponse> responses = node
                .sendRPCAppendEntriesRequests(new RPCAppendEntriesRequest(node.getCurrentTerm(), node.getId()));
        responses.forEach(response -> this.handleRPCAppendEntriesResponse(node, response));
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new HeartbeatTask(this, node), 0, node.getHeartbeatInterval());
    }

    void initializeLeaderState(RaftNode node){

        int numberOfNodes = node.getConfig().getNodeAddresses().size();

        int initialValue = 0; // To be determined
        this.nextIndex = Arrays.asList(new Integer[numberOfNodes]);
        Arrays.fill(this.nextIndex.toArray(), initialValue);

        this.nextIndex = Arrays.asList(new Integer[numberOfNodes]);
        Arrays.fill(this.nextIndex.toArray(), 0);

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

    @Override
    public ClientRequestResponse handleClientRequest(RaftNode node, ClientRequest request) {
        node.appendEntryToLog(new LogEntry(node.getCurrentTerm(), request.getCommand()));
        return null;
    }



}
