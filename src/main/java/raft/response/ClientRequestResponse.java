package raft.response;

import raft.request.ClientRequest;

public class ClientRequestResponse {

    boolean success;
    public ClientRequestResponse(){}

    public ClientRequestResponse(boolean success){
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
