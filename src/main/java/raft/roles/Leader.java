package raft.roles;

import raft.Log;
import raft.LogEntry;
import raft.RaftNode;
import raft.request.ClientRequest;
import raft.request.RPCAppendEntriesRequest;
import raft.response.ClientRequestResponse;
import raft.response.RPCAppendEntriesResponse;
import raft.tasks.HeartbeatTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.function.Consumer;
import java.util.function.Function;

public class Leader extends Role {

    Timer timer;
    List<Integer> nextIndex;
    List<Integer>  matchIndex;

    public Leader(RaftNode node) {
        super(node);
        this.initializeLeaderState();
        List<RPCAppendEntriesResponse> responses = node.sendRPCAppendEntriesRequests(new ArrayList<>());
        responses.forEach(response -> this.handleRPCAppendEntriesResponse(response));
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new HeartbeatTask(this), 0, node.getHeartbeatInterval());
    }

    void initializeLeaderState(){

        int numberOfNodes = node.getConfig().getNodeAddresses().size();

        this.nextIndex = Arrays.asList(new Integer[numberOfNodes]);
        for(int i = 0; i < this.nextIndex.size(); i++){
            this.nextIndex.set(i, node.getLog().getLastLogIndex() + 1);
        }

        this.matchIndex = Arrays.asList(new Integer[numberOfNodes]);
        for(int i = 0; i < this.matchIndex.size(); i++){
            this.matchIndex.set(i, 0);
        }

    }

    public void sendHeartBeat(int nodeId){
        RPCAppendEntriesResponse response = node.sendRPCAppendEntriesRequest(new ArrayList<>(), nodeId);
        this.handleRPCAppendEntriesResponse(response);
    }

    public void sendEntriesOrHeartbeat(int nodeId){
        if(this.node.getLog().getLastLogIndex() >= this.nextIndex.get(nodeId)){
            sendRequest(nodeId);
        }else{
            sendHeartBeat(nodeId);
        }
    }


    @Override
    public void onHeartbeatTimeoutElapsed() {
        this.node.forAllOtherNodes(nodeId -> sendEntriesOrHeartbeat(nodeId));
    }
    public void transitionToLeader(){
        this.timer.cancel();
        // System.out.println("Node " + node.getId() + " passing from " + this.getClass().toString() + " to Follower");
        node.setRole(new Leader(node));
    }


    public void transitionToFollower(){
        this.timer.cancel();
        System.out.println("Node " + node.getId() + " passing from " + this.getClass().getSimpleName() + " to Follower");
        Follower follower = new Follower(node);
        node.setRole(follower);
    }

    void updateNextIndex(List<LogEntry> newEntries, int nodeId){
        this.nextIndex.set(nodeId, Log.getLastIndexOfEntries(newEntries) + 1);
    }
    private void updateMatchIndex(List<LogEntry> newEntries, int nodeId) {
        this.matchIndex.set(nodeId, Log.getLastIndexOfEntries(newEntries));
    }
    private void decrementNextIndex(int nodeId) {
        this.nextIndex.set(nodeId, this.nextIndex.get(nodeId) - 1);
    }


    public void sendRequest(int nodeId) {
        //while (this.nextIndex.get(nodeId) >= 0) {
            List<LogEntry> newEntries = node.getLog().getEntriesStartingFromIndex(nextIndex.get(nodeId));

            RPCAppendEntriesResponse response = node.sendRPCAppendEntriesRequest(newEntries, nodeId);

            this.handleRPCAppendEntriesResponse(response);


            if (response != null && response.isSuccess()) {
                this.updateNextIndex(newEntries, nodeId);
                this.updateMatchIndex(newEntries, nodeId);
                //break;
            }
//            else {
//                if (this.nextIndex.get(nodeId) == 0) {
//                    //break;
//                }
//                this.decrementNextIndex(nodeId);
//            }

        }
    // }

    @Override
    public ClientRequestResponse handleClientRequest(ClientRequest request) {
        node.appendEntryToLog(request.getCommand());
        nextIndex.forEach(nextIndexForId -> System.out.println(nextIndexForId));
        node.forAllOtherNodes(nodeId -> sendRequest(nodeId));
        return new ClientRequestResponse(true); // Will become ClientRequestResponse(result)
    }



}
