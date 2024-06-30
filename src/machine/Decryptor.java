package src.machine;

import src.fitness.IoC;
import src.fitness.Ngram;
import src.fitness.ScoredEnigmaKey;

import java.util.*;
import java.util.stream.Collectors;

public class Decryptor {


    private final String ciphertext;
    private final Ngram ngram;
    private final int limit;


    /**
     * <p>
     * This class can attempt to decrypt ciphertext using William's method that extends Gillogly's.
     * <p>
     * Gillogly's method:
     *  <ul>
     *      <li>Phase 1: Get the best wheel & rotor position (Index of Coincidence)</li>
     *      <li>Phase 2: Get the best ring setting, using Phase 1 setting (Index of Coincidence)</li>
     *      <li>Phase 3: Get the best plug setting, using Phase 2 setting (n-grams)</li>
     * </ul>
     * <p>
     * William's method extends Gillogly's by utilizing 3,000 of the  best wheel & rotor position from Phase 1
     * instead of just the singular best, which might not be the correct key once processed to the next phases.
     * <p>
     * Gillogly's paper:<br>
     * CIPHERTEXT-ONLY CRYPTANALYSIS OF ENIGMA, James J. Gillogly, 1995, <a href="https://web.archive.org/web/20060720040135/http://members.fortunecity.com/jpeschel/gillog1.htm">web.archive.org</a>
     * <p>
     * William's paper:<br>
     * APPLYING STATISTICAL LANGUAGE RECOGNITION TECHNIQUES IN THE CIPHERTEXT-ONLY CRYPTANALYSIS OF ENIGMA, Heidi Williams, 2000, <a href="https://doi.org/10.1080/0161-110091888745">https://doi.org/10.1080/0161-110091888745</a>
     *
     * @param ciphertext    the encrypted text to decipher
     * @param ngram         n-gram length used to score keys during phase 3
     * @param limit         maximum no. of keys to test in phases 2 and 3, ideally at least 2</br>
     */
    public Decryptor(String ciphertext, int ngram, int limit) {
        this.ciphertext = clean(ciphertext);
        this.ngram = new Ngram(ngram);
        this.limit = limit;
    }

    /**
     * The canonical main method to run.
     * <p>
     * This implementation extends the William's method slightly still.
     * <p>
     * This method essentially follows the sequence:
     * <p>
     * {@code var lst1 = bestWheelOrderAndRotorPositionKeys();} <br>
     * {@code var lst2 = bestRingSettingKeys(lst1);} <br>
     * {@code var best = bestPlugboardKey(lst2.getFirst());}
     *
     * @see #bestWheelOrderAndRotorPositionKeys()
     * @see #bestRingSettingKeys(List)
     * @see #bestPlugboardKey(ScoredEnigmaKey)
     */
    public void decrypt() {
        var bestWheelAndPositions = bestWheelOrderAndRotorPositionKeys();
        System.out.println();
        var bestRingSettingKeys = bestRingSettingKeys(bestWheelAndPositions);
        System.out.println();
        var bestKey = bestPlugboardKey(bestRingSettingKeys);
        System.out.println();

        Enigma machine = new Enigma(bestKey);
        System.out.println("BEST KEY ATTEMPT = " + bestKey);
        System.out.println(machine.encrypt(ciphertext, "") + "\n");
    }

    /**
     * The first phase in cracking the key.
     * <p>
     * In {@link #decrypt()}, this method is called first and then passed to {@link #bestRingSettingKeys(List)}.
     * <p>
     * By far the most expensive step, averaging 10 keys per 60 wheel combination.
     *
     * @return {@link List} of {@link ScoredEnigmaKey} of cracked {@code wheel} and  {@code positions}.
     * 
     * @see #decrypt()
     */
    public List<ScoredEnigmaKey> bestWheelOrderAndRotorPositionKeys() {
        List<ScoredEnigmaKey> bestWheelAndPosKeys = new ArrayList<>();

        Enigma machine = Enigma.createDefault();
        String[] wheels = {"I", "II", "III", "IV", "V"};

        for (String w1 : wheels) {

            for (String w2 : wheels) {
                if (w2.equals(w1)) continue;

                for (String w3 : wheels) {
                    if (w3.equals(w2) || w3.equals(w1)) continue;

                    System.out.println(w1 + " " + w2 + " " + w3);

                    machine.setWheels(w1, w2, w3);
                    double perWheelBoundingScore = minScore();

                    for (int p1 = 0; p1 < 26; p1++) {
                        for (int p2 = 0; p2 < 26; p2++) {
                            for (int p3 = 0; p3 < 26; p3++) {

                                machine.setPositions(p1, p2, p3);

                                var snapshotKey = machine.getEnigmaKeu();
                                var attempt = machine.encrypt(ciphertext, "");
                                var score = IoC.fitness(attempt);

                                if (score > perWheelBoundingScore) {
                                    perWheelBoundingScore = score;
                                    var crackedWheelAndPos = new ScoredEnigmaKey(snapshotKey, score);
                                    bestWheelAndPosKeys.add(crackedWheelAndPos);

                                    System.out.println("CRACKED POS : " + crackedWheelAndPos);
                                }
                            }
                        }
                    }
                }
            }
        }

        bestWheelAndPosKeys.sort(Collections.reverseOrder());
        return bestWheelAndPosKeys;
    }

    /**
     * The second phase in cracking the key.
     * <p>
     * First cracks the ring settings of the leftmost rotor, then the middle rotor.
     * The rightmost rotor is turning so slow that it has no effect on the decryption whatsoever.
     * <p>
     * The positions turn together with the rings to find the optimal settings.
     * <p>
     * In {@link #decrypt()}, this method is called second and then passed to {@link #bestPlugboardKeys(List)}.
     *
     * @param keys  {@link List} of {@link ScoredEnigmaKey} with {@code wheels} and {@code positions} <strong>already cracked and sorted in descending order</strong> for best results.
     * @return      {@link List} of {@link ScoredEnigmaKey} of cracked {@code wheels}, {@code positions}, and {@code rings}, sorted in descending order, at most size {@link #limit}.
     *
     * @see #decrypt()
     * @see #bestRingSettingKey(ScoredEnigmaKey)
     */
    public List<ScoredEnigmaKey> bestRingSettingKeys(List<ScoredEnigmaKey> keys) {
        return keys.stream()
                .limit(limit)                                        // limit to highest-scoring keys
                .map(this::bestRingSettingKey)                       // crack the ring setting
                .sorted(Collections.reverseOrder())                  // sort by highest to lowest
                .collect(Collectors.toCollection(ArrayList::new));   // collect into an ArrayList
    }

    /**
     * The second phase in cracking the key.
     * <p>
     * First cracks the ring settings of the leftmost rotor, then the middle rotor.<br>
     * The rightmost rotor is turning so slow that it has no effect on the decryption whatsoever.
     *
     * @param key {@link ScoredEnigmaKey} with {@code wheels} and {@code positions} <strong>already cracked</strong> for best results.
     * @return {@link ScoredEnigmaKey} of cracked {@code wheels}, {@code positions}, and {@code rings}.
     *
     * @see #decrypt()
     * @see #bestRingSettingKeys(List)
     */
    public ScoredEnigmaKey bestRingSettingKey(ScoredEnigmaKey key) {
        var r1 = crackedRingSetting(key, 2);                // crack the leftmost rotor
        var r2 = crackedRingSetting(r1, 1);                   // crack the middle rotor
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
     *
     * @see #decrypt()
     * @see #bestPlugboardKey(List)
     * @see #bestPlugboardKeys(List)
     */
    public ScoredEnigmaKey bestPlugboardKey(ScoredEnigmaKey key) {
        return crackPlugboardPairs(key);
    }

    /**
     * The last phase in cracking the key.
     * <p>
     * Attempts to crack the plugboard pairs. If scores do not improve, keep the key as is.
     *
     * @param keys {@link List} of {@link ScoredEnigmaKey} with {@code wheels}, {@code positions}, and {@code rings} <strong>already cracked</strong> for best results.
     * @return {@link ScoredEnigmaKey}, fully-cracked for decrypting the ciphertext.
     *
     * @see #decrypt()
     * @see #bestPlugboardKey(ScoredEnigmaKey) 
     * @see #bestPlugboardKeys(List)
     */
    public ScoredEnigmaKey bestPlugboardKey(List<ScoredEnigmaKey> keys) {
        return bestPlugboardKeys(keys).getFirst();
    }

    /**
     * The last step in cracking the key.
     * <p>
     * Attempts to crack the plugboard pairs. If scores do not improve, keep the keys as is.
     *
     * @param keys {@link List} of {@link ScoredEnigmaKey} with {@code wheels}, {@code positions}, and {@code rings} <strong>already cracked and sorted in descending order</strong> for best results.
     * @return {@link List} of {@link ScoredEnigmaKey}, fully-cracked keys for decrypting the ciphertext, sorted in descending order.
     *
     * @see #decrypt()
     * @see #bestPlugboardKey(ScoredEnigmaKey)
     * @see #bestPlugboardKey(List)
     */
    public List<ScoredEnigmaKey> bestPlugboardKeys(List<ScoredEnigmaKey> keys) {
        return keys.stream()
                .limit(limit)
                .map(this::crackPlugboardPairs)
                .sorted(Collections.reverseOrder())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    protected ScoredEnigmaKey crackedRingSetting(ScoredEnigmaKey key, int rotorIndex) {
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
            double score = IoC.fitness(attempt);

            if (score > boundingScore) {
                boundingScore = score;
                bestRingPositionKey = new ScoredEnigmaKey(snapshotKey, score);
            }

            ringSetting[rotorIndex]++;
            rotorPosition[rotorIndex] = Math.abs(Math.floorMod(rotorPosition[rotorIndex] + 1, 26));
        }

        return bestRingPositionKey;
    }

    protected ScoredEnigmaKey crackPlugboardPairs(EnigmaKey key) {
        Enigma machine = new Enigma(key);

        String currentDecryption = machine.encrypt(ciphertext, "");
        machine.resetPositions();

        ArrayList<String> plugboardPairs = new ArrayList<>(Arrays.asList(key.pairs));
        double boundingPlugboardScore = ngram.score(currentDecryption);
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

                    double score = ngram.score(attempt);

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
