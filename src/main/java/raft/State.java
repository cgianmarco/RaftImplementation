package raft;

import java.util.List;

public class State {

    int currentTerm;
    int votedFor;

    List<LogEntry> log;

    public State(int currentTerm, int votedFor, List<LogEntry> log){
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

    public List<LogEntry> getLog(){
        return this.log;
    }
}
