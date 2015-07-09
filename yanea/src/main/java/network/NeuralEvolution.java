package network;

import lombok.Data;
import network.nodes.Node;

import java.util.ArrayList;
import java.util.Collections;

@Data
public class NeuralEvolution {
    ArrayList<Species> species = new ArrayList<Species>();
    ArrayList<Genome> population = new ArrayList<Genome>();

    int currentGeneration = 0;
    int currentSpecies = 0;
    int currentGenome = 0;

    public NeuralEvolution() {
        initialPopulation();
    }

    private void initialPopulation() {
        species.clear();
        population.clear();

        genomes:
        for (int i = 0; i < Parameters.initialPopulation; i++) {
            Genome g = Genome.builder().genes(new ArrayList<Gene>()).hiddenNodes(0).build();
            g = g.mutateAddGene().mutate();
            population.add(g);

            // assign species
            for (Species s : species) {
                if (!s.getGenomes().isEmpty() && g.compatibilityDistance(s.getGenomes().get(0))) {
                    s.getGenomes().add(g);
                    continue genomes;
                }
            }

            ArrayList<Genome> genomes = new ArrayList<Genome>();
            genomes.add(g);
            species.add(new Species(genomes, species.size()));
        }
    }

    public void nextRound(ArrayList<Node> inputs, ArrayList<Node> outputs) {
        inputs.addAll(outputs);

        while (species.get(currentSpecies).getGenomes().isEmpty()) {
            advanceCurrentGenome();
        }

        Genome genome = species.get(currentSpecies).getGenomes().get(currentGenome);

        System.out.println();
        System.out.println();
        System.out.printf("Generation %d, Species %d, Genome %d\n", currentGeneration, currentSpecies, currentGenome);
        System.out.println(genome.toString());

        Network network = new Network(genome, inputs); // not needed since it connects everything to the output nodes
    }

    public void results(int fitness) {
        System.out.printf("RawFitness %d\n", fitness);

        Genome genome = species.get(currentSpecies).getGenomes().get(currentGenome);
        genome.adjustedFitness(fitness, population);

        System.out.printf("Adjusted Fitness %f\n", genome.getFitness());

        advanceCurrentGenome();
    }

    private void advanceCurrentGenome() {
        if (currentGenome >= species.get(currentSpecies).getGenomes().size() - 1) {
            currentGenome = 0;

            if (currentSpecies >= species.size() - 1) {
                currentSpecies = 0;

                currentGeneration++;
                nextGeneration();
            } else {
                currentSpecies++;
            }
        } else {
            currentGenome++;
        }
    }

    private void nextGeneration() {
        ArrayList<Species> parentSpecies = new ArrayList<Species>();
        ArrayList<Species> newSpecies = new ArrayList<Species>();
        ArrayList<Genome> newGenomes = new ArrayList<Genome>();

        boolean keepPopulation = false;

        // calculate species fitness
        int totalSpeciesFitness = 0;
        for (Species s : species) {
            int totalFitness = 0;
            for (Genome g : s.getGenomes()) {
                if (g.getFitness() > 0) {
                    keepPopulation = true;
                }

                totalFitness += g.getFitness();
            }

            if (s.getGenomes().isEmpty()) {
                s.setAverageFitness(0);
            } else {
                s.setAverageFitness((int) Math.ceil(totalFitness / (double) s.getGenomes().size()));
            }
            totalSpeciesFitness += s.getAverageFitness();
        }

        // if all genomes had exactly zero fitness, reinitialize the population
        if (!keepPopulation) {
            currentGeneration = 0;
            initialPopulation();
            return;
        }

        // remove weak in current generation
        for (Species s : species) {
            Species parents = s.withGenomes(new ArrayList<Genome>());

            ArrayList<Genome> sortedGenomes = new ArrayList<Genome>(s.getGenomes());
            Collections.sort(sortedGenomes);

            // remove weak genomes
            for (int i = 0; i < sortedGenomes.size(); i++) {
                if (sortedGenomes.get(i).getFitness() == 0) {
                    sortedGenomes.remove(i);
                }
            }

            double surviving = sortedGenomes.size() > 2 ? Math.ceil(sortedGenomes.size() * Parameters.survivalRate) : sortedGenomes.size();
            for (int i = 0; i < surviving; i++) {
                Genome g = sortedGenomes.get(sortedGenomes.size() - i - 1);
                parents.getGenomes().add(g);
            }

            // remove weak species
            if (parents.getAverageFitness() > 0 && !parents.getGenomes().isEmpty()) {
                parentSpecies.add(parents);
            }
        }

        // assign each species a number of offspring and breed
        for (Species s : parentSpecies) {
            int offspring = (int) Math.floor(s.getAverageFitness() / (double) totalSpeciesFitness * Parameters.initialPopulation);
            for (int i = 0; i < offspring; i++) {
                if (i == 0 && s.getGenomes().size() > 5) { // keep the fittest if there are more than 5 in a species
                    Genome fittest = s.getGenomes().get(0);

                    for (Genome g : s.getGenomes()) {
                        if (g.getFitness() > fittest.getFitness()) {
                            fittest = g;
                        }
                    }

                    newGenomes.add(fittest.breed(fittest));
                } else if (Parameters.interSpecies > Math.random()) { // inter-species breed
                    Genome a = s.getGenomes().get((int) (Math.random() * s.getGenomes().size()));
                    Genome b;

                    Species otherSpecies = null;
                    for (Species o : parentSpecies) {
                        if (s != o && !o.getGenomes().isEmpty()) {
                            otherSpecies = o;
                        }
                    }

                    if (otherSpecies == null) {
                        b = a; // if there are no genomes of other species, breed alone
                    } else {
                        b = otherSpecies.getGenomes().get((int) (Math.random() * otherSpecies.getGenomes().size()));
                    }

                    newGenomes.add(a.breed(b));
                } else { // normal breed
                    int indexA = (int) (Math.random() * s.getGenomes().size());
                    int indexB = (int) (Math.random() * s.getGenomes().size());

                    Genome a = s.getGenomes().get(indexA);
                    Genome b;
                    if (s.getGenomes().size() > 1) {
                        if (indexB == indexA && indexB < s.getGenomes().size() - 1) {
                            indexB++;
                        } else if (indexB == indexA && indexB > 0) {
                            indexB--;
                        }
                    } // if size == 1, then breed the genome alone

                    b = s.getGenomes().get(indexB);

                    newGenomes.add(a.breed(b));
                }
            }
        }

        // fill the new species list
        for (Species s : species) {
            newSpecies.add(s.withGenomes(new ArrayList<Genome>()));
        }

        // assign new generation species
        genomes:
        for (Genome g : newGenomes) {
            for (int i = 0; i < newSpecies.size(); i++) {
                if (i < species.size()) {
                    Species s = species.get(i);

                    if (s.getGenomes().isEmpty()) {
                        continue; // this species died out
                    }

                    boolean isCompatible = g.compatibilityDistance(s.getGenomes().get(0));

                    if (!s.getGenomes().isEmpty() && isCompatible) {
                        newSpecies.get(i).getGenomes().add(g);
                        continue genomes;
                    }
                } else {
                    Species s = newSpecies.get(i);

                    if (s.getGenomes().isEmpty()) {
                        continue; // this species died out
                    }

                    boolean isCompatible = g.compatibilityDistance(s.getGenomes().get(0));

                    if (!s.getGenomes().isEmpty() && isCompatible) {
                        newSpecies.get(i).getGenomes().add(g);
                        continue genomes;
                    }
                }
            }

            ArrayList<Genome> genomes = new ArrayList<Genome>();
            genomes.add(g);
            newSpecies.add(new Species(genomes, newSpecies.size()));
        }

        species.clear();
        for (Species s : newSpecies) {
            if (!s.getGenomes().isEmpty()) {
                species.add(s);
            }
        }

        population = newGenomes;

        System.out.println("\n\n");
        System.out.printf("New Generation - %d\n", currentGeneration);
        System.out.printf("Population %d, Species %d\n", population.size(), species.size());
    }
}
