package raft.request;

public class RPCAppendEntriesRequest extends RPCRequest {
    int leaderId;

    public RPCAppendEntriesRequest(){}

    public RPCAppendEntriesRequest(int term, int leaderId) {
        super(term);
        this.leaderId = leaderId;
    }

    public int getLeaderId() {
        return this.leaderId;
    }
}
