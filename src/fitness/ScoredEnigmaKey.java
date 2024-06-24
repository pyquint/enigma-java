package src.fitness;

import src.machine.EnigmaKey;

public class ScoredEnigmaKey extends EnigmaKey implements Comparable<ScoredEnigmaKey> {
    public final double score;


    public ScoredEnigmaKey(EnigmaKey key, double score) {
        super(key);
        this.score = score;
    }

    @Override
    public int compareTo(ScoredEnigmaKey o) {
        return Double.compare(this.score, o.score);
    }

    @Override
    public String toString() {
        return "[ %s, %f ]".formatted(super.toString(), score);
    }
}
