package network;

import lombok.Value;
import network.nodes.HiddenNode;
import network.nodes.Memory;
import network.nodes.Node;

import java.util.ArrayList;

@Value
public class Network {
    Genome genome;
    private final ArrayList<Node> inputNodes;
    private final ArrayList<Node> outputNodes;
    private final ArrayList<HiddenNode> hiddenNodes = new ArrayList<HiddenNode>();
    private final ArrayList<Memory> memoryNodes = new ArrayList<Memory>();

    public Network(Genome genome, ArrayList<Node> inputNodes, ArrayList<Node> outputNodes) {
        this.genome = genome;
        this.inputNodes = inputNodes;
        this.outputNodes = outputNodes;

        for (int i = 0; i < genome.getHiddenNodes(); i++) {
            hiddenNodes.add(new HiddenNode());
        }

        for (int i = 0; i < genome.getMemoryNodes(); i++) {
            memoryNodes.add(new Memory());
        }

        for (Gene gene : genome.getGenes()) {
            if (!gene.isDisabled()) {
                Node inNode = null;
                Node outNode = null;

                switch (gene.getIn().getType()) {
                    case INPUT:
                        inNode = inputNodes.get(gene.getIn().getNode());
                        break;
                    case HIDDEN:
                        inNode = hiddenNodes.get(gene.getIn().getNode());
                        break;
                    case MEMORY:
                        inNode = memoryNodes.get(gene.getIn().getNode());
                        break;
                }

                switch (gene.getOut().getType()) {
                    case OUTPUT:
                        outNode = outputNodes.get(gene.getOut().getNode());
                        break;
                    case HIDDEN:
                        outNode = hiddenNodes.get(gene.getOut().getNode());
                        break;
                    case MEMORY:
                        outNode = memoryNodes.get(gene.getOut().getNode());
                        break;
                }

                if (outNode != null) {
                    outNode.connect(inNode, gene.getWeight(), gene.getOffset());
                }
            }
        }
    }

    public void clearMemory() {
        memoryNodes.forEach(Memory::clear);
    }

    public void resetMemory() {
        memoryNodes.forEach(Memory::reset);
    }
}
