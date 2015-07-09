package network;

import lombok.Value;
import network.nodes.Node;

@Value
public class Connection {
    Node node;
    double weight;
}
