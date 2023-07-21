package raft.request;

public class RPCVoteRequestRequest extends RPCRequest {
    int candidateId;

    public RPCVoteRequestRequest(){}

    public RPCVoteRequestRequest(int term, int candidateId) {
        super(term);
        this.candidateId = candidateId;
    }

    public int getCandidateId() {
        return this.candidateId;
    }

}