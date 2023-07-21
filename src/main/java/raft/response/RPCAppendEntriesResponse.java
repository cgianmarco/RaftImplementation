package raft.response;

public class RPCAppendEntriesResponse extends RPCResponse {
    boolean success;

    public RPCAppendEntriesResponse(){}

    public RPCAppendEntriesResponse(int term, boolean success) {
        super(term);
        this.success = success;
    }

}