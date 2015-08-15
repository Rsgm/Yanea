package network;

import lombok.Builder;
import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.Wither;

@Value
@Builder
public class Gene implements Comparable<Gene> {
    @NonFinal
    public static int globalInnovation;

    @Wither
    Base in;
    @Wither
    Base out;

    @Wither
    double weight;
    @Wither
    double offset;
    @Wither
    boolean disabled;
    @Wither
    int innovation;

    @Override
    public String toString() {
        return String.valueOf(innovation);
    }

    @Override
    public int compareTo(Gene gene) {
        return innovation - gene.getInnovation();
    }
}
