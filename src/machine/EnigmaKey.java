package src.machine;

import java.util.Arrays;

/**
 * {@link EnigmaKey} is a class for representing the internal states of an {@link Enigma} machine.
 * <p>
 * It is meant to store Arrays of {@code wheels}, {@code rings}, {@code positions}, and {@code pairs}.
 * 
 * @see #EnigmaKey(String[], int[], int[], String[]) 
 * @see #EnigmaKey(EnigmaKey) 
 */
public class EnigmaKey {
    public final String[] wheels;
    public final int[] rings;
    public final int[] positions;
    public final String[] pairs;

    /**
     * @param wheels        Array of {@link String} wheels types
     * @param rings         Array of {@code int} ring settings
     * @param positions     Array of {@code int} rotor positions
     * @param pairs         Array of {@link String} letter pairs
     * @see #EnigmaKey(EnigmaKey) 
     */
    public EnigmaKey(String[] wheels, int[] rings, int[] positions, String[] pairs) {
        this.wheels = (wheels == null) ? new String[]{"I", "II", "III"} : wheels;
        this.rings = (rings == null) ? new int[]{0, 0, 0} : rings;
        this.positions = (positions == null) ? new int[]{0, 0, 0} : positions;
        this.pairs = (pairs == null) ? new String[]{} : pairs;
    }

    /**
     * For 'copying' an {@link EnigmaKey} object.
     *
     * @param key   {@link EnigmaKey} object
     * @see #EnigmaKey(String[], int[], int[], String[]) 
     */
    public EnigmaKey(EnigmaKey key) {
        wheels = key.wheels == null ? null : Arrays.copyOf(key.wheels, 3);;
        rings = key.rings == null ? null : Arrays.copyOf(key.rings, 3);
        positions = key.positions == null ? null : Arrays.copyOf(key.positions, 3);
        pairs = key.pairs;
    }

    public String toString() {
        return "[wheels=%s, rings=%s, positions=%s, pairs=%s]".formatted(
                Arrays.toString(wheels),
                Arrays.toString(rings),
                Arrays.toString(positions),
                Arrays.toString(pairs)
        );
    }
}
