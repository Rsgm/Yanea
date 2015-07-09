package network.nodes;

public interface Node {
    double calculate();

    void connect(Node input, double weight);
}
