package raft.storage;

import raft.RaftNode;
import raft.State;

import java.util.Optional;

public interface StorageLayer {
    void persistToDisk(State state);

    Optional<State> recoverFromDisk();


}
