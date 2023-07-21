package raft.tasks;

import raft.RaftNode;
import raft.roles.Role;

import java.util.TimerTask;

public class HeartbeatTask extends TimerTask {
    private Role role;
    private RaftNode node;

    public HeartbeatTask(Role role, RaftNode node) {
        this.role = role;
        this.node = node;
    }

    @Override
    public void run() {
        this.role.onHeartbeatTimeoutElapsed(node);
    }
}
