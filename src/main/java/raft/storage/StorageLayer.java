package raft.storage;

import raft.RaftNode;

import java.util.Optional;

public interface StorageLayer {
    void persistToDisk(RaftNode node);

    Optional<Object> recoverFromDisk();


}
