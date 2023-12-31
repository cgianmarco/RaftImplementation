package raft;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import raft.request.RPCAppendEntriesRequest;
import raft.request.RPCVoteRequestRequest;
import raft.response.RPCAppendEntriesResponse;
import raft.response.RPCVoteRequestResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class Utils {
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

            System.out.println("Node " + node.getId() + " has " + (response.isVoteGranted() ? "" : "not ") + "granted vote for term " + node.getCurrentTerm());

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


}
