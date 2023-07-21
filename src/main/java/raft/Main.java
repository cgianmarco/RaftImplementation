package raft;

import java.io.*;
import java.net.InetSocketAddress;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import raft.communication.CommunicationLayer;
import raft.communication.CommunicationLayerImpl;
import raft.request.RPCAppendEntriesRequest;
import raft.request.RPCVoteRequestRequest;
import raft.response.RPCAppendEntriesResponse;
import raft.response.RPCVoteRequestResponse;
import raft.storage.StorageLayer;
import raft.storage.StorageLayerImpl;


public class Main {

    public static String getRequestBody(HttpExchange httpExchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody());
        BufferedReader br = new BufferedReader(isr);
        StringBuilder requestBody = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            requestBody.append(line);
        }
        br.close();
        return requestBody.toString();
    }


    public static void handleRequestVoteRequestForNode(RaftNode node, HttpExchange exchange) throws IOException {
        try {

            String requestBody = getRequestBody(exchange);

            // Convert JSON to Java Map
            ObjectMapper objectMapper = new ObjectMapper();
            RPCVoteRequestRequest request = objectMapper.readValue(requestBody, RPCVoteRequestRequest.class);

            RPCVoteRequestResponse response = node.handleRPCVoteRequest(request);

            System.out.println("Node " + node.getId() + " has " + (response.isVoteGranted() ? "" : "not ") + "granted vote for term " + node.getCurrentTerm() );

            String responseBody = objectMapper.writeValueAsString(response);

            // Send the response back to the client
            exchange.sendResponseHeaders(200, responseBody.length());
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(responseBody.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void handleAppendEntriesRequestForNode(RaftNode node, HttpExchange exchange) {

        try {
            String requestBody = getRequestBody(exchange);

            // Convert JSON to Java Map
            ObjectMapper objectMapper = new ObjectMapper();
            RPCAppendEntriesRequest request = objectMapper.readValue(requestBody, RPCAppendEntriesRequest.class);

            RPCAppendEntriesResponse response = node.handleAppendEntriesRequest(request);

            String responseBody = objectMapper.writeValueAsString(response);

            // Send the response back to the client
            exchange.sendResponseHeaders(200, responseBody.length());
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(responseBody.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) throws IOException {

        RaftNetworkConfig config = new RaftNetworkConfig("http://localhost:3000/node1", "http://localhost:3000/node2",
                "http://localhost:3000/node3","http://localhost:3000/node4");

        CommunicationLayer communicationLayer = new CommunicationLayerImpl();
        StorageLayer storageLayer = new StorageLayerImpl("dbnode.db");

        RaftNode node1 = new RaftNode(0, config, communicationLayer, storageLayer);
        RaftNode node2 = new RaftNode(1, config, communicationLayer, storageLayer);
        RaftNode node3 = new RaftNode(2, config, communicationLayer, storageLayer);
        RaftNode node4 = new RaftNode(3, config, communicationLayer, storageLayer);

        int port = 3000; // Port on which the server will listen

        // Create the HTTP server and bind it to the specified port
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);


        server.createContext("/node1/requestVote", exchange -> handleRequestVoteRequestForNode(node1, exchange));
        server.createContext("/node1/appendEntries", exchange -> handleAppendEntriesRequestForNode(node1, exchange));

        server.createContext("/node2/requestVote", exchange -> handleRequestVoteRequestForNode(node2, exchange));
        server.createContext("/node2/appendEntries", exchange -> handleAppendEntriesRequestForNode(node2, exchange));

        server.createContext("/node3/requestVote", exchange -> handleRequestVoteRequestForNode(node3, exchange));
        server.createContext("/node3/appendEntries", exchange -> handleAppendEntriesRequestForNode(node3, exchange));

        server.createContext("/node4/requestVote", exchange -> handleRequestVoteRequestForNode(node4, exchange));
        server.createContext("/node4/appendEntries", exchange -> handleAppendEntriesRequestForNode(node4, exchange));


        // // Start the server
        server.start();

    }
}
