package raft;

import java.util.Arrays;
import java.util.List;

public class RaftNetworkConfig {
    List<String> nodes;
    int majority;

    public RaftNetworkConfig(String... nodes) {
        this.nodes = Arrays.asList(nodes);
        this.majority = (this.nodes.size() + 1) / 2;
    }

    public int getMajority() {
        return this.majority;
    }

    public List<String> getNodeAddresses() {
        return this.nodes;
    }
}