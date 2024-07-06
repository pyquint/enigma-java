package src.decryption.analysis;

import java.util.Arrays;

public class IndexOfCoincidence implements FitnessFunction {

    public double score(String str) {
        int[] histogram = new int[26];

        str.toUpperCase()
                .chars()
                .filter(c -> c >= 'A' && c <= 'Z')
                .forEach(c -> histogram[c - 65]++);

        float frequencySum = Arrays
                .stream(histogram)
                .map(f -> f * (f - 1))
                .sum();

        int length = Arrays.stream(histogram).sum();

        return frequencySum / (length * (length - 1));
    }

    @Override
    public String name() {
        return "IoC";
    }
}
