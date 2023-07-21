package raft.communication;

import raft.request.RPCAppendEntriesRequest;
import raft.request.RPCVoteRequestRequest;
import raft.response.RPCAppendEntriesResponse;
import raft.response.RPCVoteRequestResponse;

public interface CommunicationLayer {

    public RPCVoteRequestResponse sendRPCVoteRequest(RPCVoteRequestRequest request, String address);

    public RPCAppendEntriesResponse sendRPCAppendEntriesRequest(RPCAppendEntriesRequest request, String address);

}
