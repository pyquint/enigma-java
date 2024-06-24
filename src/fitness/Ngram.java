package src.fitness;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.stream.IntStream;

public class Ngram {
    private final int n;
    private HashMap<String, Double> ngramMap;

    public Ngram(int ngram) throws Exception {
        String n = switch (ngram) {
            case 2 -> "bi";
            case 3 -> "tri";
            case 4 -> "quad";
            default -> throw new Exception("Unsupported ngram. Currently 1, 2, or 3 only.");
        };

        this.n = ngram;
        this.ngramMap = new HashMap<>();

        Files.lines(new File("data/" + n + "grams.txt").toPath())
                .map(l -> l.split(","))
                .forEach(l -> ngramMap.put(l[0], Double.valueOf(l[1])));
    }

    public Double score(String text) {
        return IntStream
                .range(0, text.length() - (n - 1))
                .mapToObj(i -> text.substring(i, n + i))
                .mapToDouble(nmap -> {
                    Double val = ngramMap.get(nmap);
                    return (val != null) ? val : -12.0;
                })
                .sum();
    }

}
