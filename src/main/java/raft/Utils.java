package raft;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import raft.request.ClientRequest;
import raft.request.RPCAppendEntriesRequest;
import raft.request.RPCVoteRequestRequest;
import raft.response.ClientRequestResponse;
import raft.response.RPCAppendEntriesResponse;
import raft.response.RPCVoteRequestResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class Utils {

    static class clientRequestHttpHandler implements HttpHandler {

        RaftNode node;

        public clientRequestHttpHandler(RaftNode node) {
            this.node = node;
        }

        @Override
        public void handle(HttpExchange exchange) {
            try {
                Utils.handleRequestForNode(node, exchange, ClientRequest.class, node::handleClientRequest);
            } catch (Exception e) { }
        }
    }
    static class requestVoteHttpHandler implements HttpHandler {

        RaftNode node;

        public requestVoteHttpHandler(RaftNode node) {
            this.node = node;
        }

        @Override
        public void handle(HttpExchange exchange) {
            try {
                Utils.handleRequestForNode(node, exchange, RPCVoteRequestRequest.class, node::handleRPCVoteRequest);
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
                Utils.handleRequestForNode(node, exchange, RPCAppendEntriesRequest.class, node::handleAppendEntriesRequest);
            } catch (Exception e) { }
        }
    }

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


//    public static void handleRequestVoteRequestForNode(RaftNode node, HttpExchange exchange) throws IOException {
//
//            String requestBody = getRequestBody(exchange);
//
//            // Convert JSON to Java Map
//            ObjectMapper objectMapper = new ObjectMapper();
//            RPCVoteRequestRequest request = objectMapper.readValue(requestBody, RPCVoteRequestRequest.class);
//
//            RPCVoteRequestResponse response = node.handleRPCVoteRequest(request);
//
//            System.out.println("Node " + node.getId() + " has " + (response.isVoteGranted() ? "" : "not ") + "granted vote for term " + node.getCurrentTerm());
//
//            String responseBody = objectMapper.writeValueAsString(response);
//
//            // Send the response back to the client
//            exchange.sendResponseHeaders(200, responseBody.length());
//            OutputStream outputStream = exchange.getResponseBody();
//            outputStream.write(responseBody.getBytes());
//            outputStream.close();
//    }
//
//    public static void handleAppendEntriesRequestForNode(RaftNode node, HttpExchange exchange) throws IOException {
//
//            String requestBody = getRequestBody(exchange);
//
//            // Convert JSON to Java Map
//            ObjectMapper objectMapper = new ObjectMapper();
//            RPCAppendEntriesRequest request = objectMapper.readValue(requestBody, RPCAppendEntriesRequest.class);
//
//            RPCAppendEntriesResponse response = node.handleAppendEntriesRequest(request);
//
//            String responseBody = objectMapper.writeValueAsString(response);
//
//            // Send the response back to the client
//            exchange.sendResponseHeaders(200, responseBody.length());
//            OutputStream outputStream = exchange.getResponseBody();
//            outputStream.write(responseBody.getBytes());
//            outputStream.close();
//
//    }


    public static <RQ, RS> void handleRequestForNode(RaftNode node, HttpExchange exchange, Class<RQ> requestClass, Function<RQ, RS> handler) throws IOException {

            String requestBody = getRequestBody(exchange);

            // Convert JSON to Java Map
            ObjectMapper objectMapper = new ObjectMapper();

            RQ request = objectMapper.readValue(requestBody, requestClass);

            RS response = handler.apply(request);

            String responseBody = objectMapper.writeValueAsString(response);

            // Send the response back to the client
            exchange.sendResponseHeaders(200, responseBody.length());
            OutputStream outputStream = exchange.getResponseBody();
            outputStream.write(responseBody.getBytes());
            outputStream.close();


    }

    public static <T> CompletableFuture<List<T>> atLeastN(List<CompletableFuture<T>> futures, int N) {
        CompletableFuture<List<T>> resultFuture = new CompletableFuture<>();
        AtomicInteger successfulCount = new AtomicInteger(0);
        AtomicInteger failedCount = new AtomicInteger(0);

        for (CompletableFuture<T> future : futures) {
            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    if (failedCount.incrementAndGet() >= (futures.size() - N + 1)) {
                        resultFuture.completeExceptionally(new RuntimeException("At least N futures failed"));
                    }
                } else {
                    if (successfulCount.incrementAndGet() >= N) {
                        resultFuture.complete(null);
                    }
                }
            });
        }

        return resultFuture;
    }
}
