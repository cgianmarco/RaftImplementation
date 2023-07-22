package raft.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.asynchttpclient.*;
import raft.request.RPCAppendEntriesRequest;
import raft.request.RPCVoteRequestRequest;
import raft.response.RPCAppendEntriesResponse;
import raft.response.RPCVoteRequestResponse;

public class AsyncCommunicationLayer implements CommunicationLayer {

    AsyncHttpClient client;

    public AsyncCommunicationLayer() {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config()
                .setConnectTimeout(500);
        this.client = Dsl.asyncHttpClient(clientBuilder);
    }

    @Override
    public RPCVoteRequestResponse sendRPCVoteRequest(RPCVoteRequestRequest request, String address) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = "";
        try {
            jsonPayload = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
        }

        BoundRequestBuilder postRequest = client.preparePost(address + "/requestVote").setBody(jsonPayload);
        ListenableFuture<RPCVoteRequestResponse> responsePromise = postRequest.execute(new AsyncCompletionHandler<RPCVoteRequestResponse>() {
            @Override
            public RPCVoteRequestResponse onCompleted(Response response) throws Exception {
                return objectMapper.readValue(response.getResponseBody(), RPCVoteRequestResponse.class);
            }
        });
        try {
            return responsePromise.get();
        } catch (Exception e) {
            return new RPCVoteRequestResponse();
        }
    }

    @Override
    public RPCAppendEntriesResponse sendRPCAppendEntriesRequest(RPCAppendEntriesRequest request, String address) {
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = "";
        try {
            jsonPayload = objectMapper.writeValueAsString(request);
        } catch (Exception e) {
        }

        BoundRequestBuilder postRequest = client.preparePost(address + "/appendEntries").setBody(jsonPayload);
        ListenableFuture<RPCAppendEntriesResponse> responsePromise = postRequest.execute(new AsyncCompletionHandler<RPCAppendEntriesResponse>() {
            @Override
            public RPCAppendEntriesResponse onCompleted(Response response) throws Exception {
                return objectMapper.readValue(response.getResponseBody(), RPCAppendEntriesResponse.class);
            }
        });
        try {
            return responsePromise.get();
        } catch (Exception e) {
            return new RPCAppendEntriesResponse();
        }
    }
}
