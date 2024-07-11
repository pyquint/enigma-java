package src.decryption.cracking;

import src.decryption.Decryptor;
import src.decryption.analysis.FitnessFunction;
import src.decryption.key.EnigmaKey;
import src.decryption.key.ScoredKey;
import src.machine.Enigma;

import java.util.List;
import java.util.stream.Stream;

/**
 * <p>The second phase only needs turning the 3rd and 2nd rotors.
 *
 * <p>We first go through all values of the 3rd leftmost rotor (index of 2), turning
 * the ring setting as well as the position, but keeping the other two rotors static.
 * We may get a new position combination in the process.
 *
 * <p>Use this new key for finding the optimal 2nd rotor (index of 1). We turn the
 * middle rotor's ring setting and position, again keeping the other two rotors static.
 *
 * <p>We have now optimized our rotors. The next step is to crack the plugboard.
 */
public class RingCracking {

    /**
     * <p>This phase expects to receive scored keys with wheels and positions,
     * such as those generated from phase one.
     *
     * @param keys      streamable collection of keys
     * @param analysis  fitness function
     * @return          {@link ScoredKey}
     * @see PositionCracking
     */
    public static List<ScoredKey> bestRingKeys(List<ScoredKey> keys, String ciphertext, FitnessFunction analysis) {
        return bestRingKeysStream(keys, ciphertext, analysis).toList();
    }

    public static Stream<ScoredKey> bestRingKeysStream(List<ScoredKey> keys, String ciphertext, FitnessFunction analysis) {
        return keys.stream().map(k -> bestRingKey(k, ciphertext, analysis));
    }

    /**
     * <p>The second phase cracking the key.
     *
     * <p>Finds the optimized position and ring setting for the wheel.
     * During this process, the positions is turned alongside the ring settings.
     * Returns a key with wheels, rings settings and positions.
     *
     * <p>This step expects to receive keys created from phase one.
     *
     * @param key       key with wheels and positions
     * @param analysis  fitness function
     * @return          {@link ScoredKey}
     * @see PositionCracking
     */
    public static ScoredKey bestRingKey(ScoredKey key, String ciphertext, FitnessFunction analysis) {
        var r1 = crackRingSetting(key, ciphertext, 2, analysis);      // crack the leftmost rotor
        var r2 = crackRingSetting(r1, ciphertext, 1, analysis);       // crack the middle rotor
        return r2;
    }

    /**
     * @param key           key with wheels and positions, with rings if {@code index} is {@code 1}
     * @param ciphertext    text to decrypt
     * @param index         index of rotor
     * @param analysis      fitness function
     * @return {            @link ScoredKey}
     * @see PlugboardCracking
     */
    protected static ScoredKey crackRingSetting(ScoredKey key, String ciphertext, int index, FitnessFunction analysis) {
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

            ringSetting[index]++;
            rotorPosition[index] = Math.abs(Math.floorMod(rotorPosition[index] + 1, 26));
        }

        return bestRingPositionKey;
    }
}
