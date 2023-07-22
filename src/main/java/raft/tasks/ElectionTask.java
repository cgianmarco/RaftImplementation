package raft.tasks;

import raft.RaftNode;
import raft.roles.Role;

import java.util.TimerTask;

public class ElectionTask extends TimerTask {
    private Role role;

    public ElectionTask(Role role) {
        this.role = role;
    }

    @Override
    public void run() {
        this.role.onElectionTimeoutElapsed();
    }
}
