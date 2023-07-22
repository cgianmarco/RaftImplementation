package raft.tasks;

import raft.RaftNode;
import raft.roles.Role;

import java.util.TimerTask;

public class HeartbeatTask extends TimerTask {
    private Role role;

    public HeartbeatTask(Role role) {
        this.role = role;
    }

    @Override
    public void run() {
        this.role.onHeartbeatTimeoutElapsed();
    }
}
