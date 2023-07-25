package raft.roles;

import raft.Log;
import raft.LogEntry;
import raft.RaftNode;
import raft.Utils;
import raft.request.ClientRequest;
import raft.request.RPCAppendEntriesRequest;
import raft.response.ClientRequestResponse;
import raft.response.RPCAppendEntriesResponse;
import raft.tasks.HeartbeatTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public class Leader extends Role {

    Timer timer;
    List<Integer> nextIndex;
    List<Integer> matchIndex;

    public Leader(RaftNode node) {
        super(node);
        this.initializeLeaderState();

        List<CompletableFuture<RPCAppendEntriesResponse>> responses = node.sendRPCAppendEntriesRequests(new ArrayList<>());
        responses.forEach(future -> future
                .thenAccept(response -> this.handleRPCAppendEntriesResponse(response)));
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(new HeartbeatTask(this), 0, node.getHeartbeatInterval());
    }

    void initializeLeaderState() {

        int numberOfNodes = node.getConfig().getNodeAddresses().size();

        this.nextIndex = Arrays.asList(new Integer[numberOfNodes]);
        for (int i = 0; i < this.nextIndex.size(); i++) {
            this.nextIndex.set(i, node.getLog().getLastLogIndex() + 1);
        }

        this.matchIndex = Arrays.asList(new Integer[numberOfNodes]);
        for (int i = 0; i < this.matchIndex.size(); i++) {
            this.matchIndex.set(i, 0);
        }

    }

    public CompletableFuture sendHeartBeat(int nodeId) {
        return node.sendRPCAppendEntriesRequest(new ArrayList<>(), nodeId)
                .thenAccept(response -> this.handleRPCAppendEntriesResponse(response));
    }

    public CompletableFuture sendEntriesOrHeartbeat(int nodeId) {
        if (this.node.getLog().getLastLogIndex() >= this.nextIndex.get(nodeId)) {
            return sendRequest(nodeId);
        } else {
            return sendHeartBeat(nodeId);
        }
    }


    @Override
    public void onHeartbeatTimeoutElapsed() {
        this.node.getLog().updateCommitIndex(this.matchIndex, this.node.getCurrentTerm());
        this.node.forAllOtherNodes(nodeId -> sendEntriesOrHeartbeat(nodeId));
    }

    public void transitionToLeader() {
        this.timer.cancel();
        // System.out.println("Node " + node.getId() + " passing from " + this.getClass().toString() + " to Follower");
        node.setRole(new Leader(node));
    }


    public void transitionToFollower() {
        this.timer.cancel();
        System.out.println("Node " + node.getId() + " passing from " + this.getClass().getSimpleName() + " to Follower");
        Follower follower = new Follower(node);
        node.setRole(follower);
    }

    void setNextIndex(int nextIndex, int nodeId) {
        this.nextIndex.set(nodeId, nextIndex);
    }

    private void setMatchIndex(int matchIndex, int nodeId) {
        this.matchIndex.set(nodeId, matchIndex);
    }

    private void decrementNextIndex(int nodeId) {
        this.nextIndex.set(nodeId, this.nextIndex.get(nodeId) - 1);
    }




    public CompletableFuture<RPCAppendEntriesResponse> sendRequest(int nodeId) {
        List<LogEntry> newEntries = node.getLog().getEntriesStartingFromIndex(nextIndex.get(nodeId));

        return node.sendRPCAppendEntriesRequest(newEntries, nodeId).thenApply(response -> {
            this.handleRPCAppendEntriesResponse(response);


            if (response != null && response.isSuccess()) {
                this.setNextIndex(Log.getLastIndexOfEntries(newEntries) + 1, nodeId);
                this.setMatchIndex(Log.getLastIndexOfEntries(newEntries), nodeId);
            }

            return response;
        });

    }

    @Override
    public ClientRequestResponse handleClientRequest(ClientRequest request) {
        node.appendEntryToLog(request.getCommand());
        this.setNextIndex(this.node.getLog().getLastLogIndex() + 1, this.node.getId());
        this.setMatchIndex(this.node.getLog().getLastLogIndex(), this.node.getId());
        CompletableFuture future = Utils.atLeastN(node.forAllOtherNodes(nodeId -> sendRequest(nodeId)), node.getConfig().getMajority() - 1);
        try {
            future.join();
            return new ClientRequestResponse(!future.isCompletedExceptionally()); // Return true if completed normally
        } catch (Exception e) {
            return new ClientRequestResponse(false);
        }

        //return new ClientRequestResponse(true); // Will become ClientRequestResponse(result)
    }


}
