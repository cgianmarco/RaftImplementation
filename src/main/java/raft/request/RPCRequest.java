package raft.request;

public abstract class RPCRequest {
    int term;

    public RPCRequest(){

    }


    public RPCRequest(int term) {
        this.term = term;
    }

    public int getTerm() {
        return this.term;
    }
}
