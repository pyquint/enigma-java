package src.machine;

import java.util.Arrays;

public class EnigmaKey {
    public String[] wheels;
    public int[] rings;
    public int[] positions;
    public String[] pairs;

    public EnigmaKey(String[] wheels, int[] rings, int[] positions, String[] pairs) {
        this.wheels = (wheels == null) ? new String[]{"I", "II", "III"} : wheels;
        this.rings = (rings == null) ? new int[]{0, 0, 0} : rings;
        this.positions = (positions == null) ? new int[]{0, 0, 0} : positions;
        this.pairs = pairs;
    }

    public EnigmaKey(EnigmaKey key) {
        wheels = key.wheels == null ? null : new String[]{key.wheels[0], key.wheels[1], key.wheels[2]};
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
