package raft;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public List<LogEntry> getLog() {
        return log;
    }

    public int getLastLogIndex(){
        if(this.getLog().isEmpty()){
            return 0;
        }

        return this.getLog().size() - 1;
    }
    public int getLastLogTerm(){
        if(this.getLog().isEmpty()){
            return 0;
        }

        return this.getLog().get(this.getLog().size() - 1).getTerm();
    }

    public boolean hasLogAtLeastAsUpToDate(int otherLastLogIndex, int otherLastLogTerm){
        int thisLastLogIndex = this.getLastLogIndex();
        int thisLastLogTerm = this.getLastLogTerm();
        if(thisLastLogTerm != otherLastLogTerm){
            if(thisLastLogTerm > otherLastLogTerm){
                return true;
            }else{
                return false;
            }
        }else{
            if(thisLastLogIndex >= otherLastLogIndex){
                return true;
            }else{
                return false;
            }
        }
    }

    public void resolveConflictsWithNewEntries(List<LogEntry> newEntries){
        List<Integer> conflictingIndexes = new ArrayList<>();
        for(LogEntry oldEntry : this.getLog()){
            for(LogEntry newEntry : newEntries){
                if(oldEntry.conflictsWith(newEntry)){
                    conflictingIndexes.add(oldEntry.getIndex());
                }
            }
        }

        int minIndex = conflictingIndexes.stream().min(Integer::compareTo).orElse(-1);

        if(minIndex == -1){
            return;
        }

        this.log = this.log.subList(0, minIndex);
    }

    public void appendEntries(List<LogEntry> newEntries){
        for(LogEntry newEntry : newEntries){
            if(!this.getLog().contains(newEntry)){
                this.getLog().add(newEntry);
            }
        }
    }
}
