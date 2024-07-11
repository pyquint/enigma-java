package src.decryption.cracking;

import src.decryption.analysis.FitnessFunction;
import src.decryption.key.EnigmaKey;
import src.decryption.key.ScoredKey;
import src.machine.Enigma;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * <p>The first phase in ciphertext-only cryptanalysis of Enigma is exhaustive.
 * This involves generating all wheel types and rotor position combinations.
 *
 * <p>For each of the combination, decrypt the given ciphertext through an Enigma machine
 * using those as starting settings, while keeping the ring settings of each rotor to 0.
 *
 * <p>The resulting decryption is put through fitness functions, such as
 * {@link src.decryption.analysis.IndexOfCoincidence Index of Coincidence} and
 * {@link src.decryption.analysis.Ngram Ngram}, which are statistical methods
 * that take the frequency of letters in a given text and produce a score
 * we can use as a quantitative measure of how close it is to plaintext.
 *
 * <p>For Index of Coincidence (IC), the expected value for English is 1.73.
 * We will not focus on that particular value however, as
 * rather we shall simply measure the scores and then take the highest values.
 */
public class PositionCracking {

    /**
     * <p>Returns a Stream of ScoredKeys of all possible wheel and position combinations.
     * <p>For a 3-rotor Enigma with 5 available wheel types {@code (5*4*3)} and 26 letters {@code (26^3)},
     *  the total number of combinations is {@code 1,054,560}.
     *
     * <pre>{@code
     * I, II, III   x   0, 0, 0
     * I, II, III   x   0, 0, 1
     * I, II, III   x   0, 0, 2
     * ...
     * III, I, II   x   22, 25, 25
     * III, I, II   x   23, 0, 0
     * ...
     * V, VI, III   x   25, 25, 23
     * V, VI, III   x   25, 25, 24
     * V, VI, III   x   25, 25, 25
     * }</pre>
     *
     * <p>These keys must be process further in {@link RingCracking}.
     * @param types         number of wheel types supported by the machine
     * @param ciphertext    text to decrypt
     * @param analysis      statistical method to use in calculating fitness score
     * @return              {@link List} of {@link ScoredKey} with cracked {@code wheel} and {@code positions}
     */
    public static List<ScoredKey> scoredWheelPosKeys(int types, String ciphertext, FitnessFunction analysis) {
        return scoredWheelPosKeysStream(types, ciphertext, analysis).toList();
    }

    /**
     * <p>Returns a Stream of ScoredKeys of all the wheel and position combinations.
     * <p>For a 3-rotor Enigma with 5 available wheel types and 26 letters,
     * the total number of combinations is {@code (5*4*3) x (26^3)}, or {@code 1,054,560}.
     *
     * <pre>{@code
     * I, II, III   x   0, 0, 0
     * I, II, III   x   0, 0, 1
     * I, II, III   x   0, 0, 2
     * ...
     * III, I, II   x   22, 25, 25
     * III, I, II   x   23, 0, 0
     * ...
     * V, VI, III   x   25, 25, 23
     * V, VI, III   x   25, 25, 24
     * V, VI, III   x   25, 25, 25
     * }</pre>
     *
     * <p>These keys must be process further in {@link RingCracking}.
     *
     * @param wheelTypes    number of wheel types
     * @param ciphertext    text to decrypt
     * @param analysis      statistical method to use in calculating fitness score
     * @return              {@code Stream<ScoredKey>}
     */
    public static Stream<ScoredKey> scoredWheelPosKeysStream(int wheelTypes, String ciphertext, FitnessFunction analysis) {
        return wheelCombinations(wheelTypes).stream()
                .flatMap(w -> {
                    return positionPermutations()
                            .stream()
                            .map(p -> scoredKey(w, p, ciphertext, analysis));
                });
    }

    /**
     * <p>Returns a List of {@link ScoredKey} of all the wheel and position combinations.
     *
     * @param wheelTypes    number of wheel types
     * @param ciphertext    target ciphertext
     * @param analysis      fitness function
     * @return              {@code List<ScoredKey>}
     */
    public static List<ScoredKey> scoredKeys(int wheelTypes, String ciphertext, FitnessFunction analysis) {
        return scoredKeysStream(wheelTypes, ciphertext, analysis).toList();
    }

    /**
     *<p>Returns a Stream of {@link ScoredKey} of every wheel and position combinations.
     *
     * @param wheelTypes    number of wheel types
     * @param ciphertext    target ciphertext
     * @param analysis      fitness function
     * @return              stream of scored keys
     */
    public static Stream<ScoredKey> scoredKeysStream(int wheelTypes, String ciphertext, FitnessFunction analysis) {
        Enigma e = Enigma.createDefault();
        return wheelCombinations(wheelTypes).stream()
                .flatMap(w -> positionPermutations().stream()
                            .map(p -> scoredKey(w, p, ciphertext, analysis, e)));
    }

    /**
     * Returns a scored key of the given wheel and position combination.
     *
     * @param wheels        wheel combination
     * @param positions     position combination
     * @param ciphertext    text to decrypt
     * @param analysis      fitness function
     * @return              ScoredKey
     */
    public static ScoredKey scoredKey(String[] wheels, int[] positions, String ciphertext, FitnessFunction analysis) {
        var e = Enigma.createDefault();
        return scoredKey(wheels, positions, ciphertext, analysis, e);
    }

    /**
     * Returns a scored key of the given wheel and position combination.
     * For internal use only. Must have instantiated an Enigma object with default ring and plugboard settings
     * before calling this method to avoid overhead of creating a new object in every iteration.
     *
     * @param wheels        wheel combination
     * @param positions     position combination
     * @param ciphertext    text to decrypt
     * @param analysis      fitness function
     * @return              ScoredKey
     */
    protected static ScoredKey scoredKey(String[] wheels, int[] positions, String ciphertext, FitnessFunction analysis, Enigma e) {
        e.setWheels(wheels);
        e.setPositions(positions);
        var snapshot = e.getEnigmaKeu();
        double score = analysis.score(e.encrypt(ciphertext));
        return new ScoredKey(snapshot, score);
    }

    public static List<String[]> wheelCombinations(int types) {
        String[] wheels = switch (types) {
            case 5 -> new String[]{"I", "II", "III", "IV", "V"};
            case 8 -> new String[]{"I", "II", "III", "IV", "V", "VI", "VII", "VIII"};
            default -> throw new UnsupportedOperationException();
        };

        List<String[]> combinations = new ArrayList<>(60);

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

    public static List<int[]> positionPermutations() {
        List<int[]> positions = new ArrayList<>(17_576);
        for (int i = 0; i < 26; i++) {
            for (int j = 0; j < 26; j++) {
                for (int k = 0; k < 26; k++) {
                    positions.add(new int[]{i, j, k});
                }
            }
        }
        return positions;
    }
}