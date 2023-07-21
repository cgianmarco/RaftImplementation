package raft;

public class State {

    int currentTerm;
    int votedFor;

    public State(int currentTerm, int votedFor){
        this.currentTerm = currentTerm;
        this.votedFor = votedFor;
    }

    public int getCurrentTerm() {
        return currentTerm;
    }

    public int getVotedFor() {
        return votedFor;
    }
}
