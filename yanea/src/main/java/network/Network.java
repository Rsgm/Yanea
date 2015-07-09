package network;

import lombok.Value;
import network.nodes.HiddenNode;
import network.nodes.Node;

import java.util.ArrayList;

@Value
public class Network {
    Genome genome;
    ArrayList<Node> nodes; // input nodes, output nodes, hidden nodes

    public Network(Genome genome, ArrayList<Node> nodes) {
        this.genome = genome;
        this.nodes = new ArrayList<Node>(nodes);

        for (int i = 0; i < genome.getHiddenNodes(); i++) {
            nodes.add(new HiddenNode());
        }

        for (Gene gene : genome.getGenes()) {
            if (!gene.isDisabled()) {
                nodes.get(gene.getOut()).connect(nodes.get(gene.getIn()), gene.getWeight());
            }
        }
    }
}
