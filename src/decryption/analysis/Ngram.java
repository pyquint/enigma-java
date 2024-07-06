package src.decryption.analysis;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Ngram implements FitnessFunction {
    private final String name;
    private final int n;
    private final HashMap<String, Double> ngramMap;

    public Ngram(int ngram) {
        name = switch (ngram) {
            case 2 -> "bigram";
            case 3 -> "trigram";
            case 4 -> "quadgram";
            default -> throw new IllegalArgumentException("Unsupported ngram. Currently 1, 2, or 3 only.");
        };

        n = ngram;
        ngramMap = new HashMap<>();

        try (Stream<String> stream = Files.lines(new File("data/" + name + "s.txt").toPath())) {
            stream.map(l -> l.split(","))
                    .forEach(l -> ngramMap.put(l[0], Double.valueOf(l[1])));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public double score(String text) {
        return IntStream
                .range(0, text.length() - (n - 1))
                .mapToObj(i -> text.substring(i, n + i))
                .mapToDouble(nmap -> {
                    Double val = ngramMap.get(nmap);
                    return (val != null) ? val : -12.0;
                })
                .sum();
    }

    @Override
    public String name() {
        return name;
    }

}
