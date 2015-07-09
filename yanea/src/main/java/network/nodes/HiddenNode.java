package network.nodes;

import lombok.Data;
import network.Connection;
import network.Parameters;

import java.util.ArrayList;

@Data
public class HiddenNode implements Node {
    private ArrayList<Connection> inputs = new ArrayList<Connection>();


    double lastValue;
    boolean equilibrium;
    boolean undefined;

    @Override
    public double calculate() {
        undefined = true;
        if (equilibrium) {
            return lastValue;
        }

        double total = 0;
        for (Connection c : inputs) {
            if (c.getNode() != this) {
                if (!(c.getNode() instanceof HiddenNode) || !((HiddenNode) c.getNode()).isUndefined()) {
                    total += c.getNode().calculate() * c.getWeight();
                }
            }
        }

        equilibrium = total <= lastValue + Parameters.epsilon || total >= lastValue - Parameters.epsilon;
        lastValue = total;

        return total / inputs.size() > Parameters.fireNeuron ? 1 : 0;
    }

    @Override
    public void connect(Node input, double weight) {
        inputs.add(new Connection(input, weight));
    }
}
