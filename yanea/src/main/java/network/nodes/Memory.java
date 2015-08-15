package network.nodes;

import network.Connection;
import network.Parameters;

import java.util.ArrayList;
import java.util.LinkedList;

public class Memory implements Node {
    private ArrayList<Connection> inputs = new ArrayList<Connection>();
    private LinkedList<Double> memory = new LinkedList<Double>();
    private boolean writable = true;
    private boolean calculating;


    @Override
    public double calculate() {
        double total = 0;
        for (Double d : memory) {
            total += d;
        }

        if (calculating && memory.isEmpty()) {
            return 0;
        } else if (calculating) {
            return total / memory.size();
        } else {
            calculating = true;
        }

        if (writable && !inputs.isEmpty()) {
            for (int i = 0; i < Parameters.recurrentIterations; i++) {
                total = 0;
                for (Connection c : inputs) {
                    boolean undefinedHidden = c.getNode() instanceof HiddenNode && ((HiddenNode) c.getNode()).isUndefined();

                    if (!undefinedHidden) {
                        total += c.getNode().calculate() * c.getWeight() + c.getOffset();
                    }
                }
            }

            if (memory.size() < Parameters.MemoryLength) {
                memory.add(total / inputs.size());
            } else {
                memory.remove(0);
                memory.add(total / inputs.size());
            }
        }

        calculating = false;

        if (memory.isEmpty()) {
            return 0;
        }

        total = 0;
        for (Double d : memory) {
            total += d;
        }

        return total / memory.size();
    }

    @Override
    public void connect(Node input, double weight, double offset) {
        inputs.add(new Connection(input, weight, offset));
    }

    public void clear() {
        memory.clear();
    }

    public void reset() {
        writable = true;
    }
}
