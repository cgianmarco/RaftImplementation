package raft;

import com.sun.net.httpserver.HttpServer;
import raft.communication.AsyncCommunicationLayer;
import raft.communication.CommunicationLayer;
import raft.storage.StorageLayer;
import raft.storage.StorageLayerImpl;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MainServer3 {

    public static void main(String[] args) throws IOException {

        RaftNetworkConfig config = new RaftNetworkConfig(
                "http://localhost:3001",
                "http://localhost:3002",
                "http://localhost:3003"
        );

        CommunicationLayer communicationLayer = new AsyncCommunicationLayer();
        StorageLayer storageLayer = new StorageLayerImpl("dbnode3.db");

        RaftNode node = new RaftNode(2, config, communicationLayer, storageLayer);

        int port = 3003; // Port on which the server will listen

        // Create the HTTP server and bind it to the specified port
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);


        server.createContext("/requestVote", new Utils.requestVoteHttpHandler(node));
        server.createContext("/appendEntries", new Utils.appendEntriesHttpHandler(node));
        server.createContext("/clientRequest", new Utils.clientRequestHttpHandler(node));


        // // Start the server
        server.start();

    }
}

