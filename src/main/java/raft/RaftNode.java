package raft;

import raft.communication.CommunicationLayer;
import raft.request.ClientRequest;
import raft.request.RPCAppendEntriesRequest;
import raft.request.RPCVoteRequestRequest;
import raft.response.ClientRequestResponse;
import raft.response.RPCAppendEntriesResponse;
import raft.response.RPCVoteRequestResponse;
import raft.roles.Follower;
import raft.roles.Role;
import raft.storage.StorageLayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class RaftNode {

    CommunicationLayer communicationLayer;
    StorageLayer storageLayer;
    RaftNetworkConfig config;
    ExecutorService executorService;
    int currentTerm;
    int votedFor;
    Log log;
    int id;

    private Role role;

    Integer electionInterval;

    public RaftNode(int id, RaftNetworkConfig config, CommunicationLayer communicationLayer, StorageLayer storageLayer) {
        this(id, config, communicationLayer, storageLayer, null);
    }


    public RaftNode(int id, RaftNetworkConfig config, CommunicationLayer communicationLayer, StorageLayer storageLayer, Integer electionInterval) {
        this.executorService = Executors.newFixedThreadPool(3);
        this.communicationLayer = communicationLayer;
        this.storageLayer = storageLayer;
        this.config = config;
        this.id = id;
        this.electionInterval = electionInterval;
        this.log = new Log();
        this.start();

    }

    public void start() {
        this.role = new Follower(this);
    }

    public void stop() {
        // TODO
    }

    public int getCurrentTerm() {
        return this.currentTerm;
    }

    public int getVotedFor() {
        return votedFor;
    }

    public Log getLog() {
        return log;
    }

    public void setCurrentTerm(int currentTerm) {
        this.currentTerm = currentTerm;
    }

    public void setVotedFor(int votedFor) {
        this.votedFor = votedFor;
    }

    public RaftNetworkConfig getConfig() {
        return this.config;
    }


    public int getId() {
        return this.id;
    }

    public void setRole(Role role) {
        this.role = role;
    }


    public String getAddress() {
        return this.getConfig().getAddressFromId(this.getId());
    }

    public List<RPCVoteRequestResponse> sendRPCVoteRequests() {
        RPCVoteRequestRequest request = new RPCVoteRequestRequest(
                this.getCurrentTerm(),
                this.getId(),
                this.getLog().getLastLogIndex(),
                this.getLog().getLastLogTerm()
        );
        return this.getConfig()
                .getNodeAddresses()
                .stream()
                .filter(address -> !address.equals(this.getAddress()))
                .map(address -> this.communicationLayer.sendRPCVoteRequest(request, address))
                .toList();
    }

    public List<RPCAppendEntriesResponse> sendRPCAppendEntriesRequests(List<LogEntry> entries) {
        RPCAppendEntriesRequest request = new RPCAppendEntriesRequest(
                this.getCurrentTerm(),
                this.getId(),
                this.getPrevLogIndex(),
                this.getPrevLogTerm(),
                entries,
                this.getLog().getCommitIndex()
        );
        return this.getConfig()
                .getNodeAddresses()
                .stream()
                .filter(address -> !address.equals(this.getAddress()))
                .map(address -> this.communicationLayer.sendRPCAppendEntriesRequest(request, address)).toList();
    }

    private int getPrevLogIndex() {
        return 0; //TODO
    }
    private int getPrevLogTerm() {
        return 0;  //TODO
    }


    public int getMajorityCount() {
        return this.getConfig().getMajority();
    }

    public int getElectionInterval() {
        if (this.electionInterval == null) {
            int min = 150;
            int max = 300;
            Random random = new Random();
            return random.nextInt((max - min) + 1) + min;
        }

        return this.electionInterval;

    }

    public int getHeartbeatInterval() {
        return 20;
    }

    public RPCVoteRequestResponse handleRPCVoteRequest(RPCVoteRequestRequest request) {
        return this.role.handleRPCVoteRequest(request);
    }

    public RPCAppendEntriesResponse handleAppendEntriesRequest(RPCAppendEntriesRequest request) {
        return this.role.handleRPCAppendEntriesRequest(request);
    }

    public ClientRequestResponse handleClientRequest(ClientRequest request) {
        return this.role.handleClientRequest(request);
    }

    public void appendEntryToLog(String command) {
        this.getLog().appendEntry(this.getCurrentTerm(), command);
    }

    public void forAllOtherNodes(Consumer<Integer> func){
        for(int nodeId = 0; nodeId < this.getConfig().getNodeAddresses().size(); nodeId++) {
            if (nodeId != this.getId()) {
                final int copyNodeId = nodeId;
                executorService.execute(() -> func.accept(copyNodeId));
            }
        }
    }



    public RPCAppendEntriesResponse sendRPCAppendEntriesRequest(List<LogEntry> newEntries, int nodeId) {
        RPCAppendEntriesRequest request = new RPCAppendEntriesRequest(
                this.getCurrentTerm(),
                this.getId(),
                this.getPrevLogIndex(),
                this.getPrevLogTerm(),
                newEntries,
                this.getLog().getCommitIndex()
        );

        String address = this.getConfig().getAddressFromId(nodeId);
        return this.communicationLayer.sendRPCAppendEntriesRequest(request, address);
    }
}
