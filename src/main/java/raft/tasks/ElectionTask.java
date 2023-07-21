package raft.tasks;

import raft.RaftNode;
import raft.roles.Role;

import java.util.TimerTask;

public class ElectionTask extends TimerTask {
    private Role role;
    private RaftNode node;

    public ElectionTask(Role role, RaftNode node) {
        this.role = role;
        this.node = node;
    }

    @Override
    public void run() {
        this.role.onElectionTimeoutElapsed(node);
    }
}
