package raft.storage;

import raft.State;

import java.util.Optional;

public interface StorageLayer {
    void persistToDisk(State state);

    Optional<State> recoverFromDisk();


}
