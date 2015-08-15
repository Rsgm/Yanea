package network;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;
import network.nodes.NodeType;

import java.util.ArrayList;

@Value
@Builder
public class Genome implements Comparable<Genome> {
    @Wither
    ArrayList<Gene> genes;
    @Wither
    int hiddenNodes;
    @Wither
    int memoryNodes;

    @NonFinal
    @Wither
    double fitness;

    /**
     * Breed two genomes by crossing the genes
     *
     * @param other the less fit genome
     * @return the resulting genome
     */
    public Genome breed(Genome other) {
        Genome newGenome;

        if (fitness < other.fitness) {
            return other.breed(this);
        }

        // cross or mutate genes
        if (Parameters.noCrossing > Math.random()) { // ignore mutating weights here
            newGenome = mutate();
        } else {
            newGenome = cross(other);
        }

        newGenome = newGenome.mutate();

        return newGenome;
    }

    private Genome cross(Genome genome) {
        ArrayList<Gene> newGenes = new ArrayList<Gene>();

        ArrayList<Gene> a = new ArrayList<Gene>(genes);
        ArrayList<Gene> b = new ArrayList<Gene>(genome.getGenes());

        // fill same ancestral genes
        newGenes.addAll(ancestralGenes(a, b));

        // fill with disjoint genes by ascending innovations
        newGenes.addAll(disjointGenes(a, b));

        // fill the excess genes
        newGenes.addAll(excessGenes(a, b));

        return builder()
                .genes(newGenes)
                .hiddenNodes(Math.max(hiddenNodes, genome.getHiddenNodes()))
                .memoryNodes(Math.max(memoryNodes, genome.getMemoryNodes()))
                .build();
    }

    public Genome mutate() {
        Genome mutated = this;

        // chance of mutating weights
        if (Parameters.weightMutation > Math.random()) {
            if (Parameters.randomMutate > Math.random()) { // random weights
                ArrayList<Gene> newGenes = new ArrayList<Gene>();

                for (Gene g : genes) {
                    double weight = Math.random();
                    newGenes.add(g.withWeight(weight).withOffset(Math.random() * weight));
                }

                mutated.withGenes(newGenes);
            } else { // uniformly mutate weights
                ArrayList<Gene> newGenes = new ArrayList<Gene>();
                double uniformModifier = (Math.random() * 2 - 1) * Parameters.uniformWeightModifier;

                for (Gene g : mutated.getGenes()) {
                    double weight = g.getWeight() + uniformModifier;
                    double offset = g.getOffset() + uniformModifier;
                    weight = Math.min(1, weight);
                    weight = Math.max(0, weight);
                    offset = Math.min(1 - weight, offset);
                    offset = Math.max(0, offset);

                    newGenes.add(g.withWeight(weight).withOffset(offset));
                }

                mutated.withGenes(newGenes);
            }
        }

        // chance of adding a gene
        for (int i = 0; i < Parameters.newGeneCount; i++) {
            if (Parameters.newGene > Math.random()) {
                mutated = mutated.mutateAddGene();
            }
        }

        for (int i = 0; i < Parameters.newNodeCount; i++) {
            if (Parameters.newNode > Math.random()) {
                mutated = mutated.mutateAddNode();
            }
        }

        return mutated;
    }

    private Genome mutateAddNode() {
        ArrayList<Gene> genesCopy = new ArrayList<Gene>(genes);
        int geneIndex = (int) (Math.random() * genes.size());
        Gene oldGene = genesCopy.get(geneIndex).withDisabled(true);

        genesCopy.set(geneIndex, oldGene);

        int memoryNodes = this.memoryNodes;
        int hiddenNodes = this.hiddenNodes;

        int nodeIndex;
        NodeType type;
        if (Parameters.memoryNodes > Math.random()) {
            nodeIndex = memoryNodes;
            type = NodeType.MEMORY;
            memoryNodes++;
        } else {
            nodeIndex = hiddenNodes;
            type = NodeType.HIDDEN;
            hiddenNodes++;
        }

        Base base = new Base(nodeIndex, type);
        Gene in = oldGene.withOut(base).withWeight(1).withOffset(0).withInnovation(Gene.globalInnovation++);
        Gene out = oldGene.withIn(base).withInnovation(Gene.globalInnovation++);

        genesCopy.add(in);
        genesCopy.add(out);

        return withGenes(genesCopy).withHiddenNodes(hiddenNodes).withMemoryNodes(memoryNodes);
    }

    public Genome mutateAddGene() {
        ArrayList<Gene> genesCopy = new ArrayList<Gene>(genes);

        Base in;
        Base out;

        findNodes:
        do {
            ArrayList<NodeType> inTypeList = new ArrayList<>();
            ArrayList<NodeType> outTypeList = new ArrayList<>();
            inTypeList.add(NodeType.INPUT);
            outTypeList.add(NodeType.OUTPUT);

            if (hiddenNodes > 0) {
                inTypeList.add(NodeType.HIDDEN);
                outTypeList.add(NodeType.HIDDEN);
            }
            if (memoryNodes > 0) {
                inTypeList.add(NodeType.MEMORY);
                outTypeList.add(NodeType.MEMORY);
            }


            NodeType typeIn = inTypeList.get(((int) (Math.random() * inTypeList.size())));
            NodeType typeOut = outTypeList.get(((int) (Math.random() * outTypeList.size())));

            int indexIn = 0;
            int indexOut = 0;

            switch (typeIn) {
                case INPUT:
                    indexIn = (int) (Math.random() * Parameters.inputNodes);
                    break;
                case HIDDEN:
                    indexIn = (int) (Math.random() * hiddenNodes);
                    break;
                case MEMORY:
                    indexIn = (int) (Math.random() * memoryNodes);
                    break;
            }

            switch (typeOut) {
                case OUTPUT:
                    indexOut = (int) (Math.random() * Parameters.outputNodes);
                    break;
                case HIDDEN:
                    indexOut = (int) (Math.random() * hiddenNodes);
                    break;
                case MEMORY:
                    indexOut = (int) (Math.random() * memoryNodes);
                    break;
            }

            in = new Base(indexIn, typeIn);
            out = new Base(indexOut, typeOut);

            // check for duplicates
            if (in != out) {
                for (Gene g : genes) {
                    if (in.equals(g.getIn()) && out.equals(g.getOut())) {
                        continue findNodes;
                    }
                }
            }
        } while (in.equals(out));

        double weight = Math.random();
        Gene gene = Gene.builder()
                .in(in)
                .out(out)
                .weight(weight)
                .offset(Math.random() * (1 - weight))
                .innovation(Gene.globalInnovation++).build();

        genesCopy.add(gene);
        return withGenes(genesCopy);
    }

    private double ancestralDiff(ArrayList<Gene> a, ArrayList<Gene> b) {
        double totalDifference = 0;
        int ancestralGenes = 0;

        while (!a.isEmpty() && !b.isEmpty() && a.get(0).getInnovation() == b.get(0).getInnovation()) {
            Gene ag = a.remove(0);
            Gene bg = b.remove(0);
            totalDifference += bg.getWeight() - ag.getWeight();
            totalDifference += bg.getOffset() - ag.getOffset();
            ancestralGenes++;
        }

        if (ancestralGenes == 0) {
            return 0;
        }

        return totalDifference / (ancestralGenes * 2);
    }

    private ArrayList<Gene> ancestralGenes(ArrayList<Gene> a, ArrayList<Gene> b) {
        ArrayList<Gene> ancestralGenes = new ArrayList<Gene>();

        while (!a.isEmpty() && !b.isEmpty() && a.get(0).getInnovation() == b.get(0).getInnovation()) {
            Gene geneA = a.remove(0);
            Gene geneB = b.remove(0);

            if ((geneA.isDisabled() ^ geneB.isDisabled()) && Parameters.disabled > Math.random()) {
                ancestralGenes.add(geneA.withDisabled(true));
            } else {
                ancestralGenes.add(geneA);
            }
        }

        return ancestralGenes;
    }

    /**
     * Lists the disjoint genes between the this and the other genome.
     *
     * @param a
     * @param b
     * @return list of disjoint genes
     */
    private ArrayList<Gene> disjointGenes(ArrayList<Gene> a, ArrayList<Gene> b) {
        ArrayList<Gene> disjointGenes = new ArrayList<Gene>();

        while (!a.isEmpty() && !b.isEmpty()) {
            if (a.get(0).compareTo(b.get(0)) <= 0) {
                disjointGenes.add(a.remove(0));
            } else {
                disjointGenes.add(b.remove(0));
            }
        }

        return disjointGenes;
    }

    private ArrayList<Gene> excessGenes(ArrayList<Gene> a, ArrayList<Gene> b) {
        if (!a.isEmpty() && !b.isEmpty()) {
            throw new RuntimeException("Error finding excess genes");
        } else if (!a.isEmpty()) {
            return a;
        } else {
            return b;
        }
    }

    public boolean compatibilityDistance(Genome other) {
        ArrayList<Gene> a = new ArrayList<Gene>(genes);
        ArrayList<Gene> b = new ArrayList<Gene>(other.getGenes());

        double w = ancestralDiff(a, b);
        int d = disjointGenes(a, b).size();
        int e = excessGenes(a, b).size();

        int n = Math.max(genes.size(), other.getGenes().size());
        n = n < 20 ? 1 : n;

        return (Parameters.c1 * e) / n + (Parameters.c2 * d) / n + Parameters.c3 * w < Parameters.delta;
    }

    public void adjustedFitness(double simulationFitness, ArrayList<Genome> population) {
        int shared = 0;

        for (Genome g : population) {
            shared += compatibilityDistance(g) ? 1 : 0;
        }

        fitness = simulationFitness / shared;
    }

    @Override
    public int compareTo(Genome genome) {
        return (int) Math.round(fitness - genome.fitness);
    }

    @Override
    public String toString() {
        return "Genome{" +
               "hidden=" + hiddenNodes +
               ", memory=" + memoryNodes +
               ", genes=" + genes +
               '}';
    }
}
