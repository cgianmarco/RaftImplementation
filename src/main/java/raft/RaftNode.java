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
import java.util.List;
import java.util.Random;

public class RaftNode {

    CommunicationLayer communicationLayer;
    StorageLayer storageLayer;
    RaftNetworkConfig config;
    State state;
    int id;
    int commitIndex;
    int lastApplied;
    Role role;

    Integer electionInterval;
    public RaftNode(int id, RaftNetworkConfig config, CommunicationLayer communicationLayer,StorageLayer storageLayer) {
        this.communicationLayer = communicationLayer;
        this.storageLayer = storageLayer;
        this.config = config;
        this.id = id;
        this.electionInterval = null;
        this.commitIndex = 0;
        this.lastApplied = 0;

        this.start();
    }


    public RaftNode(int id, RaftNetworkConfig config, CommunicationLayer communicationLayer, StorageLayer storageLayer, Integer electionInterval) {
        this.communicationLayer = communicationLayer;
        this.storageLayer = storageLayer;
        this.config = config;
        this.id = id;
        this.electionInterval = electionInterval;
        this.start();

    }

    public void start(){
        State state = this.storageLayer
                .recoverFromDisk()
                .orElse(new State(0, -1, new ArrayList<>()));
        this.setState(state);

        this.role = new Follower(this);
    }

    public void stop(){
        // TODO
    }


    public int getCommitIndex() {
        return commitIndex;
    }

    public int getLastApplied() {
        return lastApplied;
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

    public int getCurrentTerm() {
        return this.getState().getCurrentTerm();
    }

    public void setState(State state){
        this.state = state;
        this.storageLayer.persistToDisk(this.state);
    }

    public State getState(){
        return this.state;
    }

    public int getVotedFor() {
        return this.getState().getVotedFor();
    }
    public String getAddress(){
        return this.getConfig().getNodeAddresses().get(this.getId());
    }

    public List<RPCVoteRequestResponse> sendRPCVoteRequests(RPCVoteRequestRequest request) {
        return this.getConfig()
                .getNodeAddresses()
                .stream()
                .filter(address -> !address.equals(this.getAddress()))
                .map(address -> this.communicationLayer.sendRPCVoteRequest(request, address))
                .toList();
    }

    public List<RPCAppendEntriesResponse> sendRPCAppendEntriesRequests(RPCAppendEntriesRequest request) {
        return this.getConfig()
                .getNodeAddresses()
                .stream()
                .filter(address -> !address.equals(this.getAddress()))
                .map(address -> this.communicationLayer.sendRPCAppendEntriesRequest(request, address)).toList();
    }

    public int getMajorityCount() {
        return this.getConfig().getMajority();
    }

    public int getElectionInterval() {
        if(this.electionInterval == null){
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

    public RPCVoteRequestResponse handleRPCVoteRequest(RPCVoteRequestRequest request){
        return this.role.handleRPCVoteRequest(this, request);
    }
    public RPCAppendEntriesResponse handleAppendEntriesRequest(RPCAppendEntriesRequest request){
        return this.role.handleRPCAppendEntriesRequest(this, request);
    }

    public ClientRequestResponse handleClientRequest(ClientRequest request){
        this.role.handleClientRequest(this, request);
    }

    public void appendEntryToLog(LogEntry logEntry) {
        this.getState().getLog().add(logEntry);
    }
}
