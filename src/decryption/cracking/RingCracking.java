package src.decryption.cracking;

import src.decryption.Decryptor;
import src.decryption.analysis.FitnessFunction;
import src.decryption.key.EnigmaKey;
import src.decryption.key.ScoredKey;
import src.machine.Enigma;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RingCracking {
    /**
     * <p>The second phase in cracking the key.
     * <p>First cracks the ring settings of the leftmost rotor, then the middle rotor.
     * <br>The rightmost rotor is turning so slow that it has no effect on the decryption whatsoever.
     * <p>The positions turn together with the rings to find the optimal settings.
     *
     * @param keys       {@link List} of {@link ScoredKey} with {@code wheels} and {@code positions} already cracked
     * @param ciphertext
     * @param analysis   statistical method to use in calculating fitness score
     * @return {@link List} of {@link ScoredKey} of cracked {@code wheels}, {@code positions}, and {@code rings}, sorted in descending order
     */
    public static List<ScoredKey> bestRingSettingKeys(List<ScoredKey> keys, String ciphertext, FitnessFunction analysis) {
        return keys.stream()
                .map(k -> bestRingSettingKey(k, ciphertext, analysis))           // crack the ring setting
                .sorted(Collections.reverseOrder())                  // sort by highest to lowest
                .collect(Collectors.toCollection(ArrayList::new));   // collect into an ArrayList
    }

    /**
     * <p>The second phase in cracking the key.
     * <p>First cracks the ring settings of the leftmost rotor, then the middle rotor.
     * <br>The rightmost rotor is turning so slow that it has no effect on the decryption whatsoever.
     *
     * @param key      {@link ScoredKey} with {@code wheels} and {@code positions} already cracked
     * @param analysis statistical method to use in calculating fitness score
     * @return {@link ScoredKey} with cracked {@code wheels}, {@code positions}, and {@code rings}
     */
    public static ScoredKey bestRingSettingKey(ScoredKey key, String ciphertext, FitnessFunction analysis) {
        var r1 = crackRingSetting(key, ciphertext, 2, analysis);      // crack the leftmost rotor
        var r2 = crackRingSetting(r1, ciphertext, 1, analysis);       // crack the middle rotor
        System.out.println("CRACKING RINGS : " + r2);
        return r2;

    }

    protected static ScoredKey crackRingSetting(ScoredKey key, String ciphertext, int rotorIndex, FitnessFunction analysis) {
        Enigma machine = new Enigma(key);

        int[] ringSetting = key.rings();
        int[] rotorPosition = key.positions();

        ScoredKey bestRingPositionKey = key;

        double boundingScore = Decryptor.minScore();

        for (int i = 0; i < 26; i++) {
            machine.setRingSettings(ringSetting);
            machine.setPositions(rotorPosition);

            EnigmaKey snapshotKey = machine.getEnigmaKeu();
            String attempt = machine.encrypt(ciphertext);
            double score = analysis.score(attempt);

            if (score > boundingScore) {
                boundingScore = score;
                bestRingPositionKey = new ScoredKey(snapshotKey, score);
            }

            ringSetting[rotorIndex]++;
            rotorPosition[rotorIndex] = Math.abs(Math.floorMod(rotorPosition[rotorIndex] + 1, 26));
        }

        return bestRingPositionKey;
    }
}
