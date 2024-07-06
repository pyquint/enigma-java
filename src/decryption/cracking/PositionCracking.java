package src.decryption.cracking;

import src.decryption.Decryptor;
import src.decryption.analysis.FitnessFunction;
import src.decryption.key.EnigmaKey;
import src.decryption.key.ScoredKey;
import src.machine.Enigma;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>The first phase in cracking the key.
 * ... (detailed description) ...
 */
 public class PositionCracking {
    /**
     * @param types    number of wheel types to attempt decrypting through
     * @param analysis statistical method to use in calculating fitness score
     * @return {@code Stream<ScoredKey>}
     */
    public static Stream<ScoredKey> bestRotorPositionKeysStream(int types, String ciphertext, FitnessFunction analysis) {
        return wheelCombinations(types).stream()
                .flatMap(c -> crackRotorPositions(c, ciphertext, analysis).stream());
    }

    /**
     * @param wheels array of three wheel types for each of the three rotor
     * @param analysis         statistical method to use in calculating fitness score
     * @return {@link List} of {@link ScoredKey} with cracked {@code wheel} and {@code positions}
     */
    public static ScoredKey bestRotorPositionKey(String[] wheels, String ciphertext, FitnessFunction analysis) {
        return crackRotorPosition(wheels, ciphertext, analysis);
    }

    /**
     * <p>The first phase in cracking the key.
     * @param types    number of wheel types to attempt decrypting through
     * @param analysis statistical method to use in calculating fitness score
     * @return {@link List} of {@link ScoredKey} with cracked {@code wheel} and {@code positions}
     */
    public static List<ScoredKey> bestRotorPositionKeys(int types, String ciphertext, FitnessFunction analysis) {
        return wheelCombinations(types).stream()
                .flatMap(c -> crackRotorPositions(c, ciphertext, analysis).stream())
                .collect(Collectors.toList());
    }

    protected static ScoredKey crackRotorPosition(String[] wheels, String ciphertext, FitnessFunction analysis) {
        Enigma machine = Enigma.createDefault();
        machine.setWheels(wheels);

        double perWheelBoundingScore = Decryptor.minScore();
        ScoredKey crackedPositionKey = null;

        for (int[] positions : intPermutations()) {
            machine.setPositions(positions);

            var snapshotKey = machine.getEnigmaKeu();
            String attempt = machine.encrypt(ciphertext);
            double score = analysis.score(attempt);

            if (score > perWheelBoundingScore) {
                perWheelBoundingScore = score;
                crackedPositionKey = new ScoredKey(snapshotKey, score);
            }
        }

        System.out.println("CRACKING POS: " + crackedPositionKey);
        return crackedPositionKey;
    }

    protected static List<ScoredKey> crackRotorPositions(String[] wheels, String ciphertext, FitnessFunction analysis) {
        List<ScoredKey> crackedPositionKeys = new ArrayList<>();
        Enigma machine = Enigma.createDefault();
        machine.setWheels(wheels);

        double perWheelBoundingScore = Decryptor.minScore();
        ScoredKey crackedPositionKey = null;

        for (int p1 = 0; p1 < 26; p1++) {
            for (int p2 = 0; p2 < 26; p2++) {
                for (int p3 = 0; p3 < 26; p3++) {

                    machine.setPositions(p1, p2, p3);

                    var snapshotKey = machine.getEnigmaKeu();
                    String attempt = machine.encrypt(ciphertext);
                    double score = analysis.score(attempt);

                    if (score > perWheelBoundingScore) {
                        perWheelBoundingScore = score;
                        crackedPositionKey = new ScoredKey(snapshotKey, score);
                        crackedPositionKeys.add(crackedPositionKey);
                        System.out.println("CRACKING POS: " + crackedPositionKey);
                    }
                }
            }
        }
        return crackedPositionKeys;
    }

    public static ScoredKey getScoredKey(String ciphertext, Enigma machine, String[] wheels, int[] positions, FitnessFunction analysis) {
        machine.setWheels(wheels);
        machine.setPositions(positions);
        EnigmaKey snapshot = machine.getEnigmaKeu();
        double score = analysis.score(machine.encrypt(ciphertext));
        System.out.printf("%s -> %s = %f (%s)\r", Arrays.toString(wheels), Arrays.toString(positions), score, analysis.name());
        return new ScoredKey(snapshot, score);
    }

    public static List<ScoredKey> generateAllPositionKeys(int wheelTypes, String ciphertext, FitnessFunction analysis) {
        return generateAllPositionKeysStream(wheelTypes, ciphertext, analysis).toList();
    }

    public static Stream<ScoredKey> generateAllPositionKeysStream(int wheelTypes, String ciphertext, FitnessFunction analysis) {
        Enigma e = Enigma.createDefault();
        System.out.println("Scoring all 1,054,560 wheel combinations and position permutations (60 x 26^3)");
        return wheelCombinations(wheelTypes).stream()
                .flatMap(w -> intPermutations().stream()
                        .map(p -> getScoredKey(ciphertext, e, w, p, analysis)));
    }

    public static List<int[]> intPermutations() {
        List<int[]> permutations = new ArrayList<>();

        for (int i = 0; i < 26; i++) {
            for (int j = 0; j < 26; j++) {
                for (int k = 0; k < 26; k++) {
                    permutations.add(new int[]{i, j, k});
                }
            }
        }

        return permutations;
    }

    public static List<String[]> wheelCombinations(int types) {
        List<String[]> combinations = new ArrayList<>();

        String[] wheels = switch (types) {
            case 5 -> new String[]{"I", "II", "III", "IV", "V"};
            case 8 -> new String[]{"I", "II", "III", "IV", "V", "VI", "VII", "VIII"};
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
}
