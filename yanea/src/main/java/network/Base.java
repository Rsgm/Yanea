package network;

import lombok.Value;
import network.nodes.NodeType;

@Value
public class Base {
    int node;
    NodeType type;
}
