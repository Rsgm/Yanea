package network;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;

import java.util.ArrayList;

@Value
@Builder
public class Genome implements Comparable<Genome> {
    @Wither
    ArrayList<Gene> genes;
    @Wither
    int hiddenNodes;

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
                .hiddenNodes(Math.max(hiddenNodes, genome.getHiddenNodes())).build();
    }

    public Genome mutate() {
        Genome mutated = this;

        // chance of mutating weights
        if (Parameters.weightMutation > Math.random()) {
            if (Parameters.randomMutate > Math.random()) { // random weights
                ArrayList<Gene> newGenes = new ArrayList<Gene>();

                for (Gene g : genes) {
                    newGenes.add(g.withWeight(Math.random()));
                }

                mutated.withGenes(newGenes);
            } else { // uniformly mutate weights
                ArrayList<Gene> newGenes = new ArrayList<Gene>();
                double uniformModifier = (Math.random() * 2 - 1) * Parameters.uniformWeightModifier;

                for (Gene g : mutated.getGenes()) {
                    double newWeight = g.getWeight() * uniformModifier;
                    newWeight = Math.min(0, newWeight);
                    newWeight = Math.max(1, newWeight);

                    newGenes.add(g.withWeight(newWeight));
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

        int newNode = hiddenNodes + Parameters.inputNodes + Parameters.outputNodes + 1;

        Gene in = oldGene.withOut(newNode).withWeight(1).withInnovation(Gene.globalInnovation++);
        Gene out = oldGene.withIn(newNode).withInnovation(Gene.globalInnovation++);

        genesCopy.add(in);
        genesCopy.add(out);

        return withGenes(genesCopy).withHiddenNodes(hiddenNodes + 1);
    }

    public Genome mutateAddGene() {
        ArrayList<Gene> genesCopy = new ArrayList<Gene>(genes);

        int in;
        int out;

        findNodes:
        do {
            in = (int) (Math.random() * (hiddenNodes + Parameters.inputNodes));
            out = (int) (Math.random() * (hiddenNodes + Parameters.outputNodes) + Parameters.inputNodes);

            if (in >= Parameters.inputNodes) {
                in += Parameters.outputNodes;
            }

            // check for duplicates
            if (in != out) {
                for (Gene g : genes) {
                    if (in == g.getIn() && out == g.getOut()) {
                        continue findNodes;
                    }
                }
            }
        } while (in == out);

        Gene gene = Gene.builder()
                .in(in)
                .out(out)
                .weight(Math.random())
                .innovation(Gene.globalInnovation++).build();

        genesCopy.add(gene);
        return withGenes(genesCopy);
    }

    private double ancestralWeightDiff(ArrayList<Gene> a, ArrayList<Gene> b) {
        double totalDifference = 0;
        int ancestralGenes = 0;

        while (!a.isEmpty() && !b.isEmpty() && a.get(0).getInnovation() == b.get(0).getInnovation()) {
            totalDifference += b.remove(0).getWeight() - a.remove(0).getWeight();
            ancestralGenes++;
        }

        if (ancestralGenes == 0) {
            return 0;
        }

        return totalDifference / ancestralGenes;
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

        double w = ancestralWeightDiff(a, b);
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
               "hiddenNodes=" + hiddenNodes +
               ", genes=" + genes +
               '}';
    }
}
