package src.decryption.key;

import src.machine.Enigma;

import java.util.Arrays;

/**
 * <p>{@link EnigmaKey} is a class for representing the internal states of an {@link Enigma} machine.
 * <p>It is meant to store arrays of {@code wheels}, {@code rings}, {@code positions}, and {@code pairs}.
 */
public record EnigmaKey(String[] wheels, int[] rings, int[] positions, String[] pairs) implements BaseKey {

    @Override
    public String toString() {
        return "{" + "wheels=%s, rings=%s, positions=%s pairs=%s".formatted(
                Arrays.toString(wheels()),
                Arrays.toString(rings()),
                Arrays.toString(positions()),
                Arrays.toString(pairs())
        ) + "}";
    }
}
