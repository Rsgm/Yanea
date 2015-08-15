package pong;

import network.Connection;
import network.nodes.Node;

import java.util.ArrayList;

public class OutputNode implements Node {
    private ArrayList<Connection> inputs = new ArrayList<Connection>();

    @Override
    public double calculate() {
        double total = 0;
        for (Connection c : inputs) {
            double nodeValue = c.getNode().calculate() * c.getWeight() + c.getOffset();
            total += nodeValue;
        }

        if (inputs.isEmpty()) {
            return 0;
        }

        return total / inputs.size();
    }

    @Override
    public void connect(Node input, double weight, double offset) {
        inputs.add(new Connection(input, weight, offset));
    }

    public void clear() {
        inputs.clear();
    }
}
