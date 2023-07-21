package raft;

import java.util.Objects;

public class LogEntry {

    int index;
    int term;
    String command;

    public LogEntry(int index, int term, String command) {
        this.index = index;
        this.term = term;
        this.command = command;
    }

    @Override
    public boolean equals(Object other) {
        if(this == other) return true;
        if(other == null || this.getClass() != other.getClass()) return false;
        LogEntry otherLogEntry = (LogEntry) other;
        return this.getIndex() == otherLogEntry.getIndex() &&
                this.getTerm() == otherLogEntry.getTerm() &&
                this.getCommand().equals(otherLogEntry.getCommand());
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, term, command);
    }

    public int getIndex(){
        return this.index;
    }

    public int getTerm() {
        return term;
    }

    public String getCommand() {
        return command;
    }

    public boolean conflictsWith(LogEntry other){
        return this.getIndex() == other.getIndex() && this.getTerm() != other.getTerm();
    }

}
