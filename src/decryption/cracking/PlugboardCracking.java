package src.decryption.cracking;

import src.decryption.analysis.FitnessFunction;
import src.decryption.key.ScoredKey;
import src.machine.Enigma;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PlugboardCracking {
    /**
     * <p>The last phase in cracking the key.
     * <p>Attempts to crack the plugboard pairs. If scores do not improve, keep the key as is.
     *
     * @param key      {@link ScoredKey} with {@code wheels}, {@code positions}, and {@code rings} already cracked
     * @param analysis statistical method to use in calculating fitness score
     * @return {@link ScoredKey}, fully-cracked for decrypting the ciphertext
     */
    public static ScoredKey bestPlugboardKey(ScoredKey key, String ciphertext, FitnessFunction analysis) {
        return crackPlugboardPairs(key, ciphertext, analysis);
    }

    /**
     * <p>The last phase in cracking the key.
     * <p>Attempts to crack the plugboard pairs. If scores do not improve, keep the key as is.
     *
     * @param keys     {@link List} of {@link ScoredKey} with {@code wheels}, {@code positions}, and {@code rings} <strong>already cracked</strong> for best results.
     * @param analysis statistical method to use in calculating fitness score
     * @return {@link ScoredKey}, fully-cracked for decrypting the ciphertext
     */
    public static ScoredKey bestPlugboardKey(List<ScoredKey> keys, String ciphertext, FitnessFunction analysis) {
        return bestPlugboardKeys(keys, ciphertext, analysis).getFirst();
    }

    /**
     * <p>The last step in cracking the key.
     * <p>Attempts to crack the plugboard pairs. If scores do not improve, keep the keys as is.
     *
     * @param keys     {@link List} of {@link ScoredKey} with {@code wheels}, {@code positions}, and {@code rings} <strong>already cracked and sorted in descending order</strong> for best results.
     * @param analysis statistical method to use in calculating fitness score
     * @return {@link List} of {@link ScoredKey} of the best attempted cracks, sorted in ascending order
     */
    public static List<ScoredKey> bestPlugboardKeys(List<ScoredKey> keys, String ciphertext, FitnessFunction analysis) {
        return keys.stream()
                .map(k -> crackPlugboardPairs(k, ciphertext, analysis))
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected static ScoredKey crackPlugboardPairs(ScoredKey key, String ciphertext, FitnessFunction analysis) {
        Enigma machine = new Enigma(key);

        String currentDecryption = machine.encrypt(ciphertext);
        machine.resetPositions();
        double boundingPlugboardScore = analysis.score(currentDecryption);

        ScoredKey bestPlugboardKey = new ScoredKey(key, boundingPlugboardScore);

        List<String> bestPairs = new ArrayList<>(Arrays.asList(key.pairs()));
        String checked = String.join("", bestPairs);

        for (int i = 0; i < 10; i++) {
            String bestPair = null;
            double boundingPairsScore = boundingPlugboardScore;

            for (char p1 = 'A'; p1 <= 'Z'; p1++) {
                if (checked.indexOf(p1) != -1) continue;

                for (char p2 = 'A'; p2 <= 'Z'; p2++) {
                    if (p1 == p2 || checked.indexOf(p2) != -1) continue;

                    String pair = p1 + "" + p2;

                    machine.setPlugboard(bestPairs);
                    machine.addPlugboardPair(pair);

                    var snapshotKey = machine.getEnigmaKeu();
                    String attempt = machine.encrypt(ciphertext);
                    machine.resetPositions();
                    double score = analysis.score(attempt);

                    if (score > boundingPairsScore) {
                        bestPair = pair;
                        boundingPairsScore = score;
                        bestPlugboardKey = new ScoredKey(snapshotKey, score);
                    }
                }
            }

            if (bestPair != null && boundingPairsScore > boundingPlugboardScore) {
                boundingPlugboardScore = boundingPairsScore;
                bestPairs.add(bestPair);
                checked += bestPair;
            } else {
                break;
            }
        }

        System.out.println("CRACKING PLUGBOARD : " + bestPlugboardKey);
        return bestPlugboardKey;
    }
}
