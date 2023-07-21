package raft.storage;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import raft.State;

import java.io.File;
import java.util.Optional;

public class StorageLayerImpl implements StorageLayer{

    DB.HashMapMaker<String, Integer> hashMapMaker;
    public StorageLayerImpl(String filename){
        File dbFile = new File(filename);
        DB db = DBMaker.fileDB(dbFile).make();

        this.hashMapMaker = db.hashMap("keyValueMap", Serializer.STRING, Serializer.INTEGER);
    }
    @Override
    public void persistToDisk(State state) {
//        HTreeMap<String, Integer> keyValueMap = hashMapMaker.createOrOpen();
//        keyValueMap.put("votedFor", state.getVotedFor());
//        keyValueMap.put("currentTerm", state.getCurrentTerm());
//        keyValueMap.close();
    }

    @Override
    public Optional<State> recoverFromDisk() {
//        HTreeMap<String, Integer> keyValueMap = hashMapMaker.createOrOpen();
//        Integer votedFor = keyValueMap.get("votedFor");
//        Integer currentTerm = keyValueMap.get("currentTerm");
//        keyValueMap.close();
//
//        if((votedFor != null) && (currentTerm != null))
//            return Optional.of(new State(currentTerm, votedFor));

        return Optional.empty();
    }
}
