package raft.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import raft.request.RPCAppendEntriesRequest;
import raft.request.RPCVoteRequestRequest;
import raft.response.RPCAppendEntriesResponse;
import raft.response.RPCVoteRequestResponse;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface CommunicationLayer {

    public RPCVoteRequestResponse sendRPCVoteRequest(RPCVoteRequestRequest request, String address);

    public CompletableFuture<RPCAppendEntriesResponse> sendRPCAppendEntriesRequest(RPCAppendEntriesRequest request, String address);

}
