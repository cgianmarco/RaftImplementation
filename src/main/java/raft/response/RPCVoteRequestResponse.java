package raft.response;

public class RPCVoteRequestResponse extends RPCResponse {
    boolean voteGranted;

    public RPCVoteRequestResponse(){

    }

    public RPCVoteRequestResponse(int term, boolean voteGranted) {
        super(term);
        this.voteGranted = voteGranted;
    }

    public boolean isVoteGranted() {
        return this.voteGranted;
    }

}
