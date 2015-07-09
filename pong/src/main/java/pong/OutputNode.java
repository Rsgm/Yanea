package pong;

import network.Connection;
import network.Parameters;
import network.nodes.Node;

import java.util.ArrayList;

public class OutputNode implements Node {
    private ArrayList<Connection> inputs = new ArrayList<Connection>();

    @Override
    public double calculate() {
        double total = 0;
        for (Connection c : inputs) {
            if (c.getNode() != this) { // an output node had its self as an input
                double nodeValue = c.getNode().calculate() * c.getWeight();
                total += nodeValue;
            }
        }

        return total / inputs.size() > Parameters.fireNeuron ? 1 : 0;
    }

    @Override
    public void connect(Node input, double weight) {
        inputs.add(new Connection(input, weight));
    }

    public void clear() {
        inputs.clear();
    }
}
