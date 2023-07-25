package raft;

import raft.communication.CommunicationLayer;
import raft.request.ClientRequest;
import raft.request.RPCAppendEntriesRequest;
import raft.request.RPCVoteRequestRequest;
import raft.response.ClientRequestResponse;
import raft.response.RPCAppendEntriesResponse;
import raft.response.RPCResponse;
import raft.response.RPCVoteRequestResponse;
import raft.roles.Follower;
import raft.roles.Role;
import raft.storage.StorageLayer;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class RaftNode {

    CommunicationLayer communicationLayer;
    StorageLayer storageLayer;
    RaftNetworkConfig config;
    ExecutorService executorService;
    ExecutorService storageExecutorService;
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
        this.storageExecutorService = Executors.newFixedThreadPool(2);
        this.communicationLayer = communicationLayer;
        this.storageLayer = storageLayer;
        this.config = config;
        this.id = id;
        this.electionInterval = electionInterval;
        this.start();

    }

    public void start() {
        Optional<State> state = this.storageLayer.recoverFromDisk();

        this.log = state.map(State::getLog).orElse(new Log());
        this.currentTerm = state.map(State::getCurrentTerm).orElse(0);
        this.votedFor = state.map(State::getVotedFor).orElse(-1);
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

    public void assignVoteForTerm(int votedFor, int currentTerm) {
        this.currentTerm = currentTerm;
        this.votedFor = votedFor;
        this.onStateChanged();
    }

    public void onStateChanged(){
        storageExecutorService.execute(() -> this.storageLayer.persistToDisk(
                new State(
                        this.getCurrentTerm(),
                        this.getVotedFor(),
                        this.getLog()
                )));
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

    public List<CompletableFuture<RPCAppendEntriesResponse>> sendRPCAppendEntriesRequests(List<LogEntry> newEntries) {
        RPCAppendEntriesRequest request = new RPCAppendEntriesRequest(
                this.getCurrentTerm(),
                this.getId(),
                this.getPrevLogIndex(),
                this.getPrevLogTerm(),
                newEntries,
                this.getLog().getCommitIndex()
        );

        return this.getConfig()
                .getNodeAddresses()
                .stream()
                .filter(address -> !address.equals(this.getAddress()))
                .map(address -> this.communicationLayer.sendRPCAppendEntriesRequest(request, address)).toList();


    }

    private int getPrevLogIndex() {
        return 0; // TODO
    }

    private int getPrevLogTerm() {
        return 0; //TODO
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
        return 30;
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
        this.onStateChanged();
    }

    public <T extends RPCResponse> List<CompletableFuture<T>> forAllOtherNodes(Function<Integer, CompletableFuture<T>> func) {
        List<CompletableFuture<T>> futures = new ArrayList<>();
        for (int nodeId = 0; nodeId < this.getConfig().getNodeAddresses().size(); nodeId++) {
            if (nodeId != this.getId()) {
                futures.add(func.apply(nodeId));
            }
        }

        return futures;
    }




    public CompletableFuture<RPCAppendEntriesResponse> sendRPCAppendEntriesRequest(List<LogEntry> newEntries, int nodeId) {
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
