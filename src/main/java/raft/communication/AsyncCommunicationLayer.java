package raft.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.*;
import raft.request.RPCAppendEntriesRequest;
import raft.request.RPCVoteRequestRequest;
import raft.response.RPCAppendEntriesResponse;
import raft.response.RPCVoteRequestResponse;

import java.time.Duration;
import java.time.Instant;

public class AsyncCommunicationLayer implements CommunicationLayer {

    AsyncHttpClient client;

    public AsyncCommunicationLayer() {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config()
                .setConnectTimeout(50)
                .setRequestTimeout(50);
        this.client = Dsl.asyncHttpClient(clientBuilder);
    }

    @Override
    public RPCVoteRequestResponse sendRPCVoteRequest(RPCVoteRequestRequest request, String address) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = "";
        try {
            jsonPayload = objectMapper.writeValueAsString(request);

            BoundRequestBuilder postRequest = client.preparePost(address + "/requestVote").setBody(jsonPayload);
            ListenableFuture<RPCVoteRequestResponse> responsePromise = postRequest.execute(new AsyncCompletionHandler<RPCVoteRequestResponse>() {
                @Override
                public RPCVoteRequestResponse onCompleted(Response response) throws Exception {
                    return objectMapper.readValue(response.getResponseBody(), RPCVoteRequestResponse.class);
                }
            });
            return responsePromise.get();
        } catch (Exception e) {
            return new RPCVoteRequestResponse();
        }
    }

    @Override
    public RPCAppendEntriesResponse sendRPCAppendEntriesRequest(RPCAppendEntriesRequest request, String address) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = "";
        Instant start = Instant.now();
        try {
            jsonPayload = objectMapper.writeValueAsString(request);

            BoundRequestBuilder postRequest = client.preparePost(address + "/appendEntries").setBody(jsonPayload);

            ListenableFuture<RPCAppendEntriesResponse> responsePromise = postRequest.execute(new AsyncCompletionHandler<RPCAppendEntriesResponse>() {
                @Override
                public RPCAppendEntriesResponse onCompleted(Response response) throws Exception {
                    return objectMapper.readValue(response.getResponseBody(), RPCAppendEntriesResponse.class);
                }
            });
            RPCAppendEntriesResponse response = responsePromise.get();
            Instant end = Instant.now();
            Duration timeElapsed = Duration.between(start, end);
            // System.out.println("Time taken: " + timeElapsed.toMillis() + " milliseconds");
            return response;
        } catch (Exception e) {
            Instant end = Instant.now();
            Duration timeElapsed = Duration.between(start, end);
            // System.out.println("Time taken: " + timeElapsed.toMillis() + " milliseconds");
            return new RPCAppendEntriesResponse();
        }
    }
}
