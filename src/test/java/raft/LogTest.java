package raft;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import raft.request.RPCAppendEntriesRequest;

import java.util.ArrayList;
import java.util.List;

public class LogTest extends TestCase {

    private Log log;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        log = new Log();
    }

    public void testGetLastLogIndex() {
        log.appendEntry(2, "WRITE");
        log.appendEntry(3, "WRITE");
        log.appendEntry(3, "WRITE");

        assertEquals(2, log.getLastLogIndex());
    }

    public void testGetLastLogTerm() {
        log.appendEntry(2, "WRITE");
        log.appendEntry(3, "WRITE");
        log.appendEntry(3, "WRITE");

        assertEquals(3, log.getLastLogTerm());
    }

    public void testGetEntriesStartingFromIndex() {
        log.appendEntry(2, "WRITE");
        log.appendEntry(3, "WRITE");
        log.appendEntry(3, "WRITE");
        log.appendEntry(4, "WRITE");
        log.appendEntry(5, "WRITE");

        List<LogEntry> entries = log.getEntriesStartingFromIndex(2);
        assertEquals(3, entries.size());

        assertEquals(2, entries.get(0).getIndex());
        assertEquals(3, entries.get(0).getTerm());
        assertEquals("WRITE", entries.get(0).getCommand());

        assertEquals(3, entries.get(1).getIndex());
        assertEquals(4, entries.get(1).getTerm());
        assertEquals("WRITE", entries.get(1).getCommand());

        assertEquals(4, entries.get(2).getIndex());
        assertEquals(5, entries.get(2).getTerm());
        assertEquals("WRITE", entries.get(2).getCommand());
    }

    public void testGetEntriesStartingFromIndexWhenLogIsEmpty() {
        List<LogEntry> entries = log.getEntriesStartingFromIndex(2);
        assertEquals(0, entries.size());
    }

    public void testIsAtLeastAsUpToDate() {
        log.appendEntry(2, "WRITE");
        log.appendEntry(3, "WRITE");
        log.appendEntry(3, "WRITE");

        assertEquals(true, log.isAtLeastAsUpToDate(0, 2));
        assertEquals(true, log.isAtLeastAsUpToDate(1, 3));
        assertEquals(true, log.isAtLeastAsUpToDate(2, 3));
        assertEquals(false, log.isAtLeastAsUpToDate(2, 4));
        assertEquals(false, log.isAtLeastAsUpToDate(4, 3));
    }

    public void testResolveConflictsWithNewEntries() {
        log.appendEntry(2, "WRITE");
        log.appendEntry(3, "WRITE");
        log.appendEntry(3, "WRITE");

        List<LogEntry> newEntries = new ArrayList<>();
        LogEntry entry1 = new LogEntry(0, 2, "WRITE");
        LogEntry entry2 = new LogEntry(1, 3, "WRITE");
        LogEntry entry3 = new LogEntry(2, 4, "WRITE");

        newEntries.add(entry1);
        newEntries.add(entry2);
        newEntries.add(entry3);

        log.resolveConflictsWithNewEntries(newEntries);

        assertEquals(2, log.getSize());
        assertEquals(entry1, log.getEntryByIndex(0));
        assertEquals(entry2, log.getEntryByIndex(1));
    }

    public void testAppendNonExistingEntries() {
        log.appendEntry(2, "WRITE");
        log.appendEntry(3, "WRITE");
        log.appendEntry(3, "WRITE");

        List<LogEntry> newEntries = new ArrayList<>();
        LogEntry entry1 = new LogEntry(0, 2, "WRITE");
        LogEntry entry2 = new LogEntry(1, 3, "WRITE");
        LogEntry entry3 = new LogEntry(3, 4, "WRITE");

        newEntries.add(entry1);
        newEntries.add(entry2);
        newEntries.add(entry3);

        log.appendNonExistingEntries(newEntries);
        assertEquals(4, log.getSize());
        assertEquals(entry3, log.getEntryByIndex(3));
    }

    public void testAppendEntry() {
    }

    public void testGetLastCommandAtIndex() {
    }

    public void testGetEntryByIndex() {
    }

    public void testGetSize() {
    }

    public void testCheckConsistency() {
    }
}