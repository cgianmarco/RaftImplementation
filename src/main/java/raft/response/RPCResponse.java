package raft.response;

public abstract class RPCResponse {
    int term;

    public RPCResponse(){}

    public RPCResponse(int term) {
        this.term = term;
    }

    public int getTerm() {
        return this.term;
    }

}
