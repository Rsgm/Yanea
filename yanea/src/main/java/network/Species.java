package network;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;

import java.util.ArrayList;

@Value
@RequiredArgsConstructor
@AllArgsConstructor
public class Species {
    @Wither
    ArrayList<Genome> genomes;

    int species;

    @Setter
    @NonFinal
    int averageFitness;
}
