package raft.request;

public class ClientRequest {
    String command;

    public ClientRequest(){}

    public ClientRequest(String command){
        this.command = command;
    }

    public String getCommand(){
        return this.command;
    }

}
