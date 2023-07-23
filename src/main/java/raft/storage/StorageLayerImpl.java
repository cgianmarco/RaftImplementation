package raft.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import raft.RaftNode;
import raft.State;

import java.io.File;
import java.util.Optional;

public class StorageLayerImpl implements StorageLayer{

    DB.HashMapMaker<String, String> hashMapMaker;
    ObjectMapper objectMapper;

    DB db;

    public StorageLayerImpl(String filename){
        File dbFile = new File(filename);
        db = DBMaker.fileDB(dbFile).transactionEnable().make();
        this.objectMapper = new ObjectMapper();
        this.hashMapMaker = db.hashMap("keyValueMap", Serializer.STRING, Serializer.STRING);
    }
    @Override
    public synchronized void persistToDisk(State state) {
        HTreeMap<String, String> keyValueMap = hashMapMaker.createOrOpen();
        String jsonState = "";
        try {
            jsonState = objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        keyValueMap.put("state", jsonState);
        db.commit();
    }

    @Override
    public Optional<State> recoverFromDisk() {
        HTreeMap<String, String> keyValueMap = hashMapMaker.createOrOpen();
        String jsonState = keyValueMap.get("state");

        if(jsonState != null){
            try {
                State state = objectMapper.readValue(jsonState, State.class);
                return Optional.of(state);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }
}
