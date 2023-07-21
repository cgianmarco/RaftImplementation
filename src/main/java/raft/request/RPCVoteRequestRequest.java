package raft.request;

public class RPCVoteRequestRequest extends RPCRequest {
    int candidateId;
    int lastLogIndex;
    int lastLogTerm;

    public RPCVoteRequestRequest(){}

    public RPCVoteRequestRequest(int term, int candidateId, int lastLogIndex, int lastLogTerm) {
        super(term);
        this.candidateId = candidateId;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }

    public int getCandidateId() {
        return this.candidateId;
    }

    public int getLastLogIndex() {
        return lastLogIndex;
    }

    public int getLastLogTerm() {
        return lastLogTerm;
    }
}