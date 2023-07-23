package raft;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import raft.communication.AsyncCommunicationLayer;
import raft.communication.CommunicationLayer;
import raft.storage.StorageLayer;
import raft.storage.StorageLayerImpl;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MainServer2 {

    static class requestVoteHttpHandler implements HttpHandler {

        RaftNode node;

        public requestVoteHttpHandler(RaftNode node) {
            this.node = node;
        }

        @Override
        public void handle(HttpExchange exchange) {
            try {
                Utils.handleRequestVoteRequestForNode(node, exchange);
            } catch (Exception e) { }
        }
    }
    static class appendEntriesHttpHandler implements HttpHandler {

        RaftNode node;

        public appendEntriesHttpHandler(RaftNode node) {
            this.node = node;
        }

        @Override
        public void handle(HttpExchange exchange) {
            try {
                Utils.handleAppendEntriesRequestForNode(node, exchange);
            } catch (Exception e) { }
        }
    }


    public static void main(String[] args) throws IOException {

        RaftNetworkConfig config = new RaftNetworkConfig(
                "http://localhost:3001",
                "http://localhost:3002",
                "http://localhost:3003"
        );

        CommunicationLayer communicationLayer = new AsyncCommunicationLayer();
        StorageLayer storageLayer = new StorageLayerImpl("dbnode2.db");

        RaftNode node = new RaftNode(1, config, communicationLayer, storageLayer);

        int port = 3002; // Port on which the server will listen

        // Create the HTTP server and bind it to the specified port
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);


        server.createContext("/requestVote", new requestVoteHttpHandler(node));
        server.createContext("/appendEntries", new appendEntriesHttpHandler(node));


        // // Start the server
        server.start();

    }
}

