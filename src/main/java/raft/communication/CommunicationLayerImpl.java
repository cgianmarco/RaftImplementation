package raft.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import raft.request.RPCAppendEntriesRequest;
import raft.request.RPCVoteRequestRequest;
import raft.response.RPCAppendEntriesResponse;
import raft.response.RPCVoteRequestResponse;

import java.io.InputStream;

public class CommunicationLayerImpl implements CommunicationLayer{

    CloseableHttpClient httpClient;

    public CommunicationLayerImpl(){
        httpClient = HttpClients.createDefault();
    }

    @Override
    public RPCVoteRequestResponse sendRPCVoteRequest(RPCVoteRequestRequest request, String address) {

        // Define the URL for the POST request
        String url = address + "/requestVote";

        ObjectMapper objectMapper = new ObjectMapper();

        String jsonPayload = "";
        RPCVoteRequestResponse response = null;

        try{
            jsonPayload = objectMapper.writeValueAsString(request);

            // Create an instance of HttpPost with the URL
            HttpPost httpPost = new HttpPost(url);

            // Set the JSON payload as the request entity
            StringEntity entity = new StringEntity(jsonPayload);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");
            InputStream responseBody = httpClient.execute(httpPost).getEntity().getContent();

            response = objectMapper.readValue(responseBody, RPCVoteRequestResponse.class);


        }catch(Exception e){
            System.out.println("Got error Vote request parsing request");
        }

        return response;
    }

    @Override
    public RPCAppendEntriesResponse sendRPCAppendEntriesRequest(RPCAppendEntriesRequest request, String address) {
        int requestTimeout = 40;
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(requestTimeout)
                .setConnectTimeout(requestTimeout)
                .build();

        // Define the URL for the POST request
        String url = address + "/appendEntries";

        ObjectMapper objectMapper = new ObjectMapper();

        String jsonPayload = "";
        RPCAppendEntriesResponse response = null;

        try{
            jsonPayload = objectMapper.writeValueAsString(request);

            // Create an instance of HttpPost with the URL
            HttpPost httpPost = new HttpPost(url);
            httpPost.setConfig(requestConfig);

            // Set the JSON payload as the request entity
            StringEntity entity = new StringEntity(jsonPayload);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");
            InputStream responseBody = httpClient.execute(httpPost).getEntity().getContent();

            response = objectMapper.readValue(responseBody, RPCAppendEntriesResponse.class);


        }catch(Exception e){
            System.out.println("Got error Append entries parsing request");
        }

        return response;
    }
}