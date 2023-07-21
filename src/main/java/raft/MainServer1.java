package raft;

import com.sun.net.httpserver.HttpServer;
import raft.communication.AsyncCommunicationLayer;
import raft.communication.CommunicationLayer;
import raft.storage.StorageLayer;
import raft.storage.StorageLayerImpl;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MainServer1 {

    public static void main(String[] args) throws IOException {

        RaftNetworkConfig config = new RaftNetworkConfig(
                "http://localhost:3001",
                "http://localhost:3002",
                "http://localhost:3003"
        );

        CommunicationLayer communicationLayer = new AsyncCommunicationLayer();
        StorageLayer storageLayer = new StorageLayerImpl("dbnode1.db");

        RaftNode node = new RaftNode(0, config, communicationLayer, storageLayer);

        int port = 3001; // Port on which the server will listen

        // Create the HTTP server and bind it to the specified port
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);


        server.createContext("/requestVote", exchange -> Utils.handleRequestVoteRequestForNode(node, exchange));
        server.createContext("/appendEntries", exchange -> Utils.handleAppendEntriesRequestForNode(node, exchange));


        // // Start the server
        server.start();

    }
}
