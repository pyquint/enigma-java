package src.fitness;

import java.util.Arrays;

public class IoC {
    public static float fitness(String str) {
        int[] histogram = new int[26];

        for (char c : str.toUpperCase().toCharArray()) if (c >= 'A' && c <= 'Z') histogram[c - 65]++;

        float frequencySum = Arrays.stream(histogram)
                .map(f -> f * (f - 1))
                .sum();

        int length = Arrays.stream(histogram).sum();

        return frequencySum / (length * (length - 1));
    }
}
