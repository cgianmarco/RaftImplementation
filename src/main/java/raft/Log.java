package raft;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import raft.response.RPCAppendEntriesResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Log {
    List<LogEntry> entries;

    @JsonIgnore
    @JsonProperty(defaultValue = "0")
    int commitIndex;

    @JsonIgnore
    @JsonProperty(defaultValue = "0")
    int lastApplied;

    public Log(){

        this.entries = new ArrayList<>();
        this.commitIndex = 0;
        this.lastApplied = 0;

    }

    public int getCommitIndex() {
        return commitIndex;
    }

    public int getLastApplied() {
        return lastApplied;
    }

    public List<LogEntry> getEntries() {
        return entries;
    }

    public void setCommitIndex(int commitIndex) {
        int oldCommitIndex = this.commitIndex;
        this.commitIndex = commitIndex;

        if (this.commitIndex > this.lastApplied) {
            this.lastApplied = this.lastApplied + 1;
            this.applyToStateMachine(this.getLastCommandAtIndex(this.lastApplied));
        }

        if(oldCommitIndex != this.commitIndex)
            this.onLogChanged();
    }

    @JsonIgnore
    public int getLastLogIndex(){
        if(this.entries.isEmpty()){
            return 0;
        }
        return this.entries.stream().mapToInt(entry -> entry.getIndex()).max().orElse(0);
    }

    @JsonIgnore
    public int getLastLogTerm(){
        if(this.entries.isEmpty()){
            return 0;
        }

        return this.getEntryByIndex(this.getLastLogIndex()).getTerm();
    }

    public List<LogEntry> getEntriesStartingFromIndex(int index){
        int lastIndex = this.getLastLogIndex();
        if(index > lastIndex || index < 0) return List.of();
        return this.entries.stream().filter(entry -> entry.getIndex() >= index && entry.getIndex() <= lastIndex).toList();
    }

    public boolean isAtLeastAsUpToDate(int otherLastLogIndex, int otherLastLogTerm){
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
        for(LogEntry oldEntry : this.entries){
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

        this.entries = this.entries.subList(0, minIndex);
    }

    public void appendNonExistingEntries(List<LogEntry> newEntries){
        for(LogEntry newEntry : newEntries){
            if(!this.entries.contains(newEntry)){
                this.entries.add(newEntry);
            }
        }

        if(!newEntries.isEmpty())
            this.onLogChanged();
    }

    public void appendEntry(int term, String command){
        int nextIndex = this.getSize() + 1;
        this.entries.add(new LogEntry(nextIndex, term, command));
        this.onLogChanged();
    }

    public String getLastCommandAtIndex(int index){
        return this.getEntryByIndex(index).getCommand();
    }

    public LogEntry getEntryByIndex(int index){
        if(index < 1 || index > this.getLastLogIndex()) {
            return null;
        }
        return this.entries.stream().filter(entry -> entry.getIndex() == index).findFirst().orElse(null);
    }

    @JsonIgnore
    public int getSize(){
        return this.entries.size();
    }

    public boolean checkConsistency(int prevLogIndex, int prevLogTerm){
        LogEntry logEntry = this.getEntryByIndex(prevLogIndex);

        if(logEntry == null) {
            return true;
        }

        if (logEntry.getTerm() != prevLogTerm) {
            return false;
        }else{
            return true;
        }
    }

    public void applyToStateMachine(String command) {
        // TODO
    }

    public void addNonConflictingEntries(List<LogEntry> newEntries){

        if(!newEntries.isEmpty()){
            System.out.println("Requested to add entries:");
            newEntries.forEach(System.out::println);
        }

        // If an existing entry conflicts with a new one (same index but different terms)
        // delete the existing entry and all that follow it
        this.resolveConflictsWithNewEntries(newEntries);

        // Append new entries not already in the log
        this.appendNonExistingEntries(newEntries);
    }

    public void commitEntriesUpTo(int leaderCommit) {
        // If leaderCommit > commitIndex,
        // set commitIndex = min(leaderCommit, index of last new entry)
        if(leaderCommit > this.getCommitIndex()){
            int maxNewIndex = this.getLastLogIndex();
            this.setCommitIndex(Math.min(leaderCommit, maxNewIndex));
        }
    }

    public static int getLastIndexOfEntries(List<LogEntry> newEntries){
        return newEntries.stream().mapToInt(entry -> entry.getIndex()).max().orElse(0);
    }

    public void onLogChanged(){
        System.out.println("--------Log---------");
        System.out.println("commitIndex: " + commitIndex);
        this.entries.forEach(entry -> System.out.println(entry));
        System.out.println("--------End---------");
    }

    public void updateCommitIndex(List<Integer> matchIndex, int currentTerm){
        int oldCommitIndex = this.commitIndex;
        int N = this.commitIndex + 1;

        List<Integer> sortedMatchIndex = new ArrayList<>(matchIndex);

        Collections.sort(sortedMatchIndex);
        int majority = sortedMatchIndex.get(sortedMatchIndex.size() / 2);

        // Check if there's an N such that N > commitIndex,
        // a majority of matchIndex[i] >= N, and log[N].term == currentTerm
        while (majority >= N && this.getEntryByIndex(N).getTerm() == currentTerm) {
            N++;
        }

        // Set commitIndex to N - 1 (since we looped beyond the valid index)
        this.commitIndex = N - 1;

        if(this.commitIndex != oldCommitIndex)
            this.onLogChanged();
    }

}
