package src.decryption.key;

import src.machine.Enigma;

import java.util.Arrays;


/**
 * <p>{@link ScoredKey} is a record for representing the internal states of an {@link Enigma} machine
 * including a score of attempted decryption using those as starting settings.
 */
public record ScoredKey(String[] wheels, int[] rings, int[] positions, String[] pairs, double score)
        implements BaseKey, Comparable<ScoredKey> {

    public ScoredKey(BaseKey key, double score) {
        this(key.wheels(), key.rings(), key.positions(), key.pairs(), score);
    }

    @Override
    public int compareTo(ScoredKey o) {
        return Double.compare(this.score, o.score);
    }

    @Override
    public String toString() {
        return "wheels=%s, rings=%s, positions=%s pairs=%s, score=%f".formatted(
                Arrays.toString(wheels()),
                Arrays.toString(rings()),
                Arrays.toString(positions()),
                Arrays.toString(pairs()),
                score()
        );
    }
}
