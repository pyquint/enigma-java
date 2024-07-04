package src.fitness;

import src.machine.Enigma;
import src.machine.EnigmaKey;


/**
 * {@link ScoredEnigmaKey} is a class for representing the internal states of an {@link Enigma} machine
 * including a score of attempted decryption using those as starting settings.
 *
 * @see #ScoredEnigmaKey(EnigmaKey, double) (EnigmaKey)
 */
public class ScoredEnigmaKey extends EnigmaKey implements Comparable<ScoredEnigmaKey> {
    public final double score;


    /**
     * @param key       {@link EnigmaKey} object
     * @param score     text fitness/analysis score
     */
    public ScoredEnigmaKey(EnigmaKey key, double score) {
        super(key);
        this.score = score;
    }

    public double score() {
        return score;
    }

    @Override
    public int compareTo(ScoredEnigmaKey o) {
        return Double.compare(this.score, o.score);
    }

    @Override
    public String toString() {
        return "ScoredEnigmaKey[%s, score=%f]".formatted(prettyKey(), score);
    }
}
