package raft.request;

import raft.LogEntry;

import java.util.List;

public class RPCAppendEntriesRequest extends RPCRequest {
    int leaderId;
    int prevLogIndex;
    int prevLogTerm;
    List<LogEntry> entries;
    int leaderCommit;

    public RPCAppendEntriesRequest(){}

    public RPCAppendEntriesRequest(int term, int leaderId, int prevLogIndex, int prevLogTerm, List<LogEntry> entries, int leaderCommit) {
        super(term);
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.prevLogTerm = prevLogTerm;
        this.entries = entries;
        this.leaderCommit = leaderCommit;
    }

    public int getLeaderId() {
        return this.leaderId;
    }

    public int getPrevLogIndex() {
        return prevLogIndex;
    }

    public int getPrevLogTerm() {
        return prevLogTerm;
    }

    public List<LogEntry> getEntries() {
        return entries;
    }

    public int getLeaderCommit() {
        return leaderCommit;
    }
}
