package src.decryption;

import src.decryption.cracking.PlugboardCracking;
import src.decryption.cracking.PositionCracking;
import src.decryption.cracking.RingCracking;
import src.decryption.key.*;
import src.decryption.analysis.*;

import java.util.*;

public class Decryptor {

    private final String CIPHERTEXT;
    /**
     * <p>Uses Index of Coincidence for cryptanalysis
     * <p>See {@link IndexOfCoincidence} class
     */
    public final static FitnessFunction IOC = new IndexOfCoincidence();

    /**
     * <p>Uses ngram of 2 for cryptanalysis
     * <p>See {@link Ngram} class
     */
    public final static FitnessFunction BIGRAM = new Ngram(2);

    /**
     * <p>Uses ngram of 3 for cryptanalysis
     * <p>See {@link Ngram} class
     */
    public final static FitnessFunction TRIGRAM = new Ngram(3);

    /**
     * <p>Uses ngram of 4 for cryptanalysis
     * <p>See {@link Ngram} class
     */
    public final static FitnessFunction QUADGRAM = new Ngram(4);

    // @param ngram         n-gram length used to score keys during phase 3

    /**
     * <p>The Decryptor class provide instance methods for decrypting Enigma-encoded text using ciphertext-only cryptanalysis.
     * This implements the techniques of James Gillogly [1] and Heidi Williams' improvements [2].
     * <p>[1] CIPHERTEXT-ONLY CRYPTANALYSIS OF ENIGMA, James J. Gillogly, 1995, <a href="https://web.archive.org/web/20060720040135/http://members.fortunecity.com/jpeschel/gillog1.htm">web.archive.org</a>
     * <p>[2] APPLYING STATISTICAL LANGUAGE RECOGNITION TECHNIQUES IN THE CIPHERTEXT-ONLY CRYPTANALYSIS OF ENIGMA, Heidi Williams, 2000, <a href="https://doi.org/10.1080/0161-110091888745">https://doi.org/10.1080/0161-110091888745</a>
     *
     * @param ciphertext the encrypted text to decipher
     */
    public Decryptor(String ciphertext) {
        this.CIPHERTEXT = clean(ciphertext);
    }

    /**
     * <p>This method automatically performs the three steps described in Gillogly's paper:</p>
     *  <ul>
     *          1. Determine the best wheel & rotor position setting, by {@code IoC} (Index of Coincidence).
     *      <br>2. Determine the best ring setting, using settings from step 1, again by {@code IoC}.
     *      <br>3. Determine the best plugboard setting, using settings from step 2, by {@code ngrams}.
     * </ul>
     * <p>Heidi William extends Gillogly's by recording 3,000 of the best-scoring combinations from the first step
     * instead of just the singular best, and choosing a better statistical technique.
     * <p>{@link src.decryption.cracking.PositionCracking#scoredWheelPosKeys}'s impl
     *
     * @return {@link ScoredKey} of the best-scoring Enigma settings
     * @see #decryptParallel()
     */
    public ScoredKey decrypt() {
        // phase 1
        var hold = PositionCracking.scoredKeysStream(5, CIPHERTEXT, IOC)
                .peek(k -> System.out.printf("SCORING POSITIONS :: wheels=%s, positions=%s, score=%f\r", Arrays.toString(k.wheels()), Arrays.toString(k.positions()), k.score()))
                .sorted(Comparator.reverseOrder());

        // phase 2
        hold = hold
                .peek(k -> System.out.printf("CRACKING RINGS :: wheels=%s, rings=%s, positions=%s score=%f\r", Arrays.toString(k.wheels()), Arrays.toString(k.positions()), Arrays.toString(k.rings()), k.score()))
                .limit(3000)
                .map(k -> RingCracking.bestRingKey(k, CIPHERTEXT, IOC))
                .sorted(Comparator.reverseOrder());

        //phase 3
        return hold
                .limit(5)
                .map(k -> PlugboardCracking.bestPlugboardKey(k, CIPHERTEXT, BIGRAM))
                .peek(k -> System.out.printf("CRACKING PLUGBOARD :: %s\r", k))
                .max(Comparator.comparing(ScoredKey::score))
                .get();
    }

    /**
     * <p>Parallel-computed cracking of Enigma key.
     * <p>This method automatically performs the three steps described in Gillogly's paper:</p>
     *  <ul>
     *          1. Determine the best wheel & rotor position setting, by {@code IoC} (Index of Coincidence).
     *      <br>2. Determine the best ring setting, using settings from step 1, again by {@code IoC}.
     *      <br>3. Determine the best plugboard setting, using settings from step 2, by {@code ngrams}.
     * </ul>
     * <p>Heidi William extends Gillogly's by recording 3,000 of the best-scoring combinations from the first step
     * instead of just the singular best, and choosing a better statistical technique.</p>
     *
     * @return {@link ScoredKey} of the best-scoring Enigma settings
     * @see #decrypt()
     */
    public ScoredKey decryptParallel() {
        // we will assume the ciphertext is encrypted using an Enigma whose rotors support 5 types
        return
            PositionCracking.scoredKeysStream(5, CIPHERTEXT, IOC)
                .parallel()
                .sorted(Comparator.reverseOrder())
                .limit(100)

                .map(k -> RingCracking.bestRingKey(k, CIPHERTEXT, IOC))
                .map(k -> PlugboardCracking.bestPlugboardKey(k, CIPHERTEXT, BIGRAM))

                .max(Comparator.comparing(ScoredKey::score))
                .get();
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

    public static double minScore() {
        return Double.NEGATIVE_INFINITY;
    }
}
