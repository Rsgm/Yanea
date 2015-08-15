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
        } else if (inputs.isEmpty()) {
            return 0;
        }

        for (int i = 0; i < Parameters.recurrentIterations; i++) {
            double total = 0;
            for (Connection c : inputs) {
                boolean undefinedHidden = c.getNode() instanceof HiddenNode && ((HiddenNode) c.getNode()).isUndefined();

                if (!undefinedHidden) {
                    total += c.getNode().calculate() * c.getWeight() + c.getOffset();
                }
            }

            equilibrium = total / inputs.size() <= lastValue + Parameters.epsilon || total / inputs.size() >= lastValue - Parameters.epsilon;
            lastValue = total / inputs.size();
        }

        undefined = false;
        return lastValue;
    }

    @Override
    public void connect(Node input, double weight, double offset) {
        inputs.add(new Connection(input, weight, offset));
    }

    public void reset() {

    }
}
