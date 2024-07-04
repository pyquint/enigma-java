package src.machine;

import src.fitness.Analysis;
import src.fitness.IoC;
import src.fitness.Ngram;
import src.fitness.ScoredEnigmaKey;

import java.util.*;
import java.util.stream.Collectors;

public class Decryptor {

    private final String ciphertext;

    /**
     * Uses Index of Coincidence for cryptanalysis.
     * @see IoC IoC class
     */
    public final static Analysis IOC = new IoC();

    /**
     * Uses ngram of 2 for cryptanalysis.
     * @see Ngram Ngram class
     */
    public final static Analysis BIGRAM = new Ngram(2);

    /**
     * Uses ngram of 3 for cryptanalysis.
     * @see Ngram Ngram class
     */
    public final static Analysis TRIGRAM = new Ngram(3);

    /**
     * Uses ngram of 4 for cryptanalysis.
     * @see Ngram Ngram class
     */
    public final static Analysis QUADGRAM = new Ngram(4);


    // @param ngram         n-gram length used to score keys during phase 3

    /**
     * <p>The Decryptor class provide instance methods for decrypting Enigma-encoded text using ciphertext-only cryptanalysis.
     * This implements the techniques of James Gillogly [1] and Heidi Williams' improvements [2].
     * <p>[1] CIPHERTEXT-ONLY CRYPTANALYSIS OF ENIGMA, James J. Gillogly, 1995, <a href="https://web.archive.org/web/20060720040135/http://members.fortunecity.com/jpeschel/gillog1.htm">web.archive.org</a>
     * <p>[2] APPLYING STATISTICAL LANGUAGE RECOGNITION TECHNIQUES IN THE CIPHERTEXT-ONLY CRYPTANALYSIS OF ENIGMA, Heidi Williams, 2000, <a href="https://doi.org/10.1080/0161-110091888745">https://doi.org/10.1080/0161-110091888745</a>
     *
     * @param ciphertext    the encrypted text to decipher
     */
    public Decryptor(String ciphertext) {
        this.ciphertext = clean(ciphertext);
    }

    /**
     * <p>This method automatically performs the three steps described in Gillogly's paper:</p>
     *  <ul>
     *          1. Determine the best wheel & rotor position setting, by {@code IoC} (Index of Coincidence).
     *      <br>2. Determine the best ring setting, using settings from step 1, again by {@code IoC}.
     *      <br>3. Determine the best plugboard setting, using settings from step 2, by {@code ngrams}.
     * </ul>
     * <p>Heidi William extends Gillogly's by recording 3,000 of the best-scoring combinations from the first step
     * instead of just the singular best, and choosing a better statistical technique.</p>
     * <pre>{@code
     *      String[] wheels = {"I", "II", "III", "IV", "V"};
     *
     *      List<ScoredEnigmaKey> wheelPosKeys = bestWheelAndPositionKeys(wheels, Decryptor.IOC);
     *      List<ScoredEnigmaKey> ringSettKeys = bestRingSettingKeys(wheelPosKeys, Decryptor.IOC);
     *      ScoredEnigmaKey bestKey = bestPlugboardKey(ringSettKeys, Decryptor.BIGRAM);
     *
     *      return bestKey;
     * }</pre>
     */
    public ScoredEnigmaKey decrypt() {
        // we will assume the ciphertext is encrypted using an Enigma whose rotors support 5 types
        return wheelCombinations(5).stream().parallel()
                .map(w -> bestRotorPositionKey(w, IOC))
                .map(k -> bestRingSettingKey(k, IOC))
                .map(k -> bestPlugboardKey(k, BIGRAM))
                .max(Comparator.comparing(ScoredEnigmaKey::score))
                .get();
    }

    /**
     * The first phase in cracking the key.
     * <p>
     * In {@link #decrypt()}, this method is called first and then passed to {@link #bestRingSettingKeys(List, Analysis)}.
     * <p>
     * By far the most expensive step, averaging 10 keys per 60 wheel combination.
     *
     * @return {@link List} of {@link ScoredEnigmaKey} of cracked {@code wheel} and  {@code positions}.
     *
     * @see #decrypt()
     */
    public ScoredEnigmaKey bestRotorPositionKey(String[] wheelCombination, Analysis analysis) {
        return crackRotorPosition(wheelCombination, analysis);
    }

    /**
     * The first phase in cracking the key.
     * <p>
     * In {@link #decrypt()}, this method is called first and then passed to {@link #bestRingSettingKeys(List, Analysis)}.
     * <p>
     * By far the most expensive step, averaging 10 keys per 60 wheel combination.
     *
     * @return {@link List} of {@link ScoredEnigmaKey} of cracked {@code wheel} and  {@code positions}.
     * 
     * @see #decrypt()
     */
    public List<ScoredEnigmaKey> bestRotorPositionKeys(int types, Analysis analysis) {
        return wheelCombinations(types).stream()
                .map(c -> crackRotorPosition(c, analysis))
                .collect(Collectors.toList());
    }

    /**
     * The second phase in cracking the key.
     * <p>
     * First cracks the ring settings of the leftmost rotor, then the middle rotor.
     * The rightmost rotor is turning so slow that it has no effect on the decryption whatsoever.
     * <p>
     * The positions turn together with the rings to find the optimal settings.
     *
     * @param keys         {@link List} of {@link ScoredEnigmaKey} with {@code wheels} and {@code positions} <strong>already cracked and sorted in descending order</strong> for best results.
     * @param analysis     {@link Analysis} to calculate fitness score. Use fields {@link #IOC}, {@link #BIGRAM}, {@link #TRIGRAM}, or {@link #QUADGRAM}.
     * @return {@link List} of {@link ScoredEnigmaKey} of cracked {@code wheels}, {@code positions}, and {@code rings}, sorted in descending order
     * @see #decrypt()
     * @see #bestRingSettingKey(ScoredEnigmaKey, Analysis)
     */
    public List<ScoredEnigmaKey> bestRingSettingKeys(List<ScoredEnigmaKey> keys, Analysis analysis) {
        return keys.stream()
                .map(k -> bestRingSettingKey(k, analysis))           // crack the ring setting
                .sorted(Collections.reverseOrder())                  // sort by highest to lowest
                .collect(Collectors.toCollection(ArrayList::new));   // collect into an ArrayList
    }

    /**
     * The second phase in cracking the key.
     * <p>
     * First cracks the ring settings of the leftmost rotor, then the middle rotor.<br>
     * The rightmost rotor is turning so slow that it has no effect on the decryption whatsoever.
     *
     * @param key       {@link ScoredEnigmaKey} with {@code wheels} and {@code positions} <strong>already cracked</strong> for best results.
     * @param analysis  {@link Analysis} to calculate fitness score. Use fields {@link #IOC}, {@link #BIGRAM}, {@link #TRIGRAM}, or {@link #QUADGRAM}.
     * @return {@link ScoredEnigmaKey} of cracked {@code wheels}, {@code positions}, and {@code rings}.
     * @see #decrypt()
     * @see #bestRingSettingKeys(List, Analysis)
     */
    public ScoredEnigmaKey bestRingSettingKey(ScoredEnigmaKey key, Analysis analysis) {
        var r1 = crackRingSetting(key, 2, analysis);      // crack the leftmost rotor
        var r2 = crackRingSetting(r1, 1, analysis);       // crack the middle rotor
        System.out.println("CRACKED RINGS : " + r2);
        return r2;

    }

    /**
     * The last phase in cracking the key.
     * <p>
     * Attempts to crack the plugboard pairs. If scores do not improve, keep the key as is.
     *
     * @param key {@link ScoredEnigmaKey} with {@code wheels}, {@code positions}, and {@code rings} already cracked for best results.
     * @return {@link ScoredEnigmaKey}, fully-cracked for decrypting the ciphertext.
     */
    public ScoredEnigmaKey bestPlugboardKey(ScoredEnigmaKey key, Analysis analysis) {
        return crackPlugboardPairs(key, analysis);
    }

    /**
     * The last phase in cracking the key.
     * <p>
     * Attempts to crack the plugboard pairs. If scores do not improve, keep the key as is.
     *
     * @param keys          {@link List} of {@link ScoredEnigmaKey} with {@code wheels}, {@code positions}, and {@code rings} <strong>already cracked</strong> for best results.
     * @param analysis
     * @return {@link ScoredEnigmaKey}, fully-cracked for decrypting the ciphertext.
     */
    public ScoredEnigmaKey bestPlugboardKey(List<ScoredEnigmaKey> keys, Analysis analysis) {
        return bestPlugboardKeys(keys, analysis).getFirst();
    }

    /**
     * The last step in cracking the key.
     * <p>
     * Attempts to crack the plugboard pairs. If scores do not improve, keep the keys as is.
     *
     * @param keys {@link List} of {@link ScoredEnigmaKey} with {@code wheels}, {@code positions}, and {@code rings} <strong>already cracked and sorted in descending order</strong> for best results.
     * @return {@link List} of {@link ScoredEnigmaKey}, fully-cracked keys for decrypting the ciphertext, sorted in descending order.
     */
    public List<ScoredEnigmaKey> bestPlugboardKeys(List<ScoredEnigmaKey> keys, Analysis analysis) {
        return keys.stream()
                .map(k -> crackPlugboardPairs(k, analysis))
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected ScoredEnigmaKey crackRotorPosition(String[] wheels, Analysis analysis) {
        Enigma machine = Enigma.createDefault();
        machine.setWheels(wheels);

        double perWheelBoundingScore = minScore();
        ScoredEnigmaKey crackedPositionKey = null;

        for (int p1 = 0; p1 < 26; p1++) {
            for (int p2 = 0; p2 < 26; p2++) {
                for (int p3 = 0; p3 < 26; p3++) {

                    machine.setPositions(p1, p2, p3);

                    var snapshotKey = machine.getEnigmaKeu();
                    String attempt = machine.encrypt(ciphertext, "");
                    double score = analysis.score(attempt);

                    if (score > perWheelBoundingScore) {
                        perWheelBoundingScore = score;
                        crackedPositionKey = new ScoredEnigmaKey(snapshotKey, score);
                    }
                }
            }
        }

        var rev = crackedPositionKey != null
                    ? crackedPositionKey
                    : new ScoredEnigmaKey(machine.getEnigmaKeu(), perWheelBoundingScore);

        System.out.println("CRACKED POS : " + rev);
        return rev;
    }

    protected ScoredEnigmaKey crackRingSetting(ScoredEnigmaKey key, int rotorIndex, Analysis analysis) {
        Enigma machine = new Enigma(key);

        int[] ringSetting = key.rings;
        int[] rotorPosition = key.positions;

        ScoredEnigmaKey bestRingPositionKey = key;

        double boundingScore = minScore();

        for (int i = 0; i < 26; i++) {
            machine.setRingSettings(ringSetting);
            machine.setPositions(rotorPosition);

            EnigmaKey snapshotKey = machine.getEnigmaKeu();
            String attempt = machine.encrypt(ciphertext, "");
            double score = analysis.score(attempt);

            if (score > boundingScore) {
                boundingScore = score;
                bestRingPositionKey = new ScoredEnigmaKey(snapshotKey, score);
            }

            ringSetting[rotorIndex]++;
            rotorPosition[rotorIndex] = Math.abs(Math.floorMod(rotorPosition[rotorIndex] + 1, 26));
        }

        return bestRingPositionKey;
    }

    protected ScoredEnigmaKey crackPlugboardPairs(EnigmaKey key, Analysis analysis) {
        Enigma machine = new Enigma(key);

        String currentDecryption = machine.encrypt(ciphertext, "");
        machine.resetPositions();

        ArrayList<String> plugboardPairs = new ArrayList<>(Arrays.asList(key.pairs));
        double boundingPlugboardScore = analysis.score(currentDecryption);
        ScoredEnigmaKey bestPlugboardKey = new ScoredEnigmaKey(key, boundingPlugboardScore);

        for (int i = 0; i < 7; i++) {

            String bestPair = null;
            String checked = String.join("", plugboardPairs);
            double boundingPairsScore = boundingPlugboardScore;

            for (char p1 = 'a'; p1 <= 'z'; p1++) {
                if (checked.indexOf(p1) != -1) continue;

                for (char p2 = 'a'; p2 <= 'z'; p2++) {
                    if (p1 == p2 || checked.indexOf(p2) != -1) continue;

                    String pair = p1 + "" + p2;

                    plugboardPairs.add(pair);
                    machine.setPlugboard(plugboardPairs);

                    String attempt = machine.encrypt(ciphertext, "");
                    machine.resetPositions();

                    double score = analysis.score(attempt);

                    if (score > boundingPairsScore) {
                        bestPair = pair;
                        boundingPairsScore = score;
                        bestPlugboardKey = new ScoredEnigmaKey(machine.getEnigmaKeu(), score);
                        machine.resetPlugboard();
                    }

                    plugboardPairs.remove(pair);
                }
            }

            if (bestPair != null && boundingPairsScore > boundingPlugboardScore) {
                boundingPlugboardScore = boundingPairsScore;
                plugboardPairs.add(bestPair);
            } else {
                break;
            }
        }

        System.out.println("CRACKED PLUGBOARD : " + bestPlugboardKey);
        return bestPlugboardKey;
    }

    private static List<String[]> wheelCombinations(int types) {
        List<String[]> combinations = new ArrayList<>();

        String[] wheels = switch (types) {
            case 5 -> new String[] {"I", "II", "III", "IV", "V"};
            case 8 -> new String[] {"I", "II", "III", "IV", "V", "VI", "VII", "VIII"};
            default -> throw new UnsupportedOperationException();
        };

        for (String w1 : wheels) {
            for (String w2 : wheels) {
                if (w2.equals(w1)) continue;
                for (String w3 : wheels) {
                    if (w3.equals(w2) || w3.equals(w1)) continue;
                    combinations.add(new String[]{w1, w2, w3});
                }
            }
        }
        return combinations;
    }

    private static String clean(String text) {
        return text.toUpperCase()
                .chars()
                .filter(c -> c >= 'A' && c <= 'Z')
                .collect(StringBuilder::new,
                        StringBuilder::appendCodePoint,
                        StringBuilder::append)
                .toString();
    }

    protected static double minScore() {
        return Double.NEGATIVE_INFINITY;
    }
}
