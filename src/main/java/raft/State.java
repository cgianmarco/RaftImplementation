package raft;

import java.util.Optional;

public class State {
    int currentTerm;
    int votedFor;
    Log log;

    public State() {}

    public State(int currentTerm, int votedFor, Log log) {
        this.currentTerm = currentTerm;
        this.votedFor = votedFor;
        this.log = log;
    }


    public int getCurrentTerm() {
        return currentTerm;
    }

    public int getVotedFor() {
        return votedFor;
    }

    public Log getLog() {
        return log;
    }
}
