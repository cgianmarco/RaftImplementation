package raft;

import java.util.ArrayList;
import java.util.List;

public class Log {
    List<LogEntry> entries;

    public Log(){
        this.entries = new ArrayList<>();
    }

    public int getLastLogIndex(){
        if(this.entries.isEmpty()){
            return 0;
        }

        return this.entries.size() - 1;
    }
    public int getLastLogTerm(){
        if(this.entries.isEmpty()){
            return 0;
        }

        return this.entries.get(this.entries.size() - 1).getTerm();
    }

    public List<LogEntry> getEntriesStartingFromIndex(int index){
        int logSize = this.entries.size();
        if(index >= logSize || index < 0) return List.of();
        return this.entries.subList(index, logSize);
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

    public void appendEntries(List<LogEntry> newEntries){
        for(LogEntry newEntry : newEntries){
            if(!this.entries.contains(newEntry)){
                this.entries.add(newEntry);
            }
        }
    }

    public void appendEntry(int term, String command){
        int nextIndex = this.getSize();
        this.entries.add(new LogEntry(nextIndex, term, command));
    }

    public String getLastCommandAtIndex(int index){
        return this.entries.get(index).getCommand();
    }

    public LogEntry getEntryByIndex(int index){
        return this.entries.get(index);
    }

    public int getSize(){
        return this.entries.size();
    }

}
