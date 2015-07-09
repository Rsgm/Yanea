package network;

public class Parameters {
    /**
     * Number of input nodes supplied by the simulation
     */
    public static int inputNodes;
    /**
     * Number of output nodes needed by the simulation
     */
    public static int outputNodes;

    /**
     * Number of initial genomes
     */
    public static int initialPopulation;

    public static double c1;
    public static double c2;
    public static double c3;

    public static double delta;
    public static double epsilon;

    public static double fireNeuron;

    /**
     * Percent(kind of) of unfit genomes to survive.
     * It is really the percent of the average fitness a genome has to beat to survive.
     */
    public static double survivalRate;

    /**
     * Chance of a mutation that modifies weights only
     */
    public static double weightMutation;

    /**
     * Modifier of uniform weight mutation
     */
    public static double uniformWeightModifier;

    /**
     * Chance of a weight mutation being completely random
     */
    public static double randomMutate;

    /**
     * Chance of new node mutation
     */
    public static double newNode;

    /**
     * Chance of new gene mutation
     */
    public static double newGene;

    /**
     * Number of nodes to try creating when mutating
     */
    public static int newNodeCount;

    /**
     * Number of genes to try creating when mutating
     */
    public static int newGeneCount;

    /**
     * Chance of a gene being disabled if exactly one parent's gene is disabled
     */
    public static double disabled;

    /**
     * Chance of a gene being disabled if exactly one parent's gene is disabled
     */
    public static double noCrossing;

    /**
     * Chance of inter-species breeding
     */
    public static double interSpecies;

    /**
     * Times to mutate each of the initial population
     */
    public static int initialMutation;
}
