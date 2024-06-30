package src.machine;

public class Rotor {
    String wheel;
    int ringSetting;
    int position;
    int initialPos;
    int turnover;
    int[] wiring;
    int[] inverseWiring;

    Rotor(String wheel, int ringSetting, int position) {
        this.inverseWiring = new int[26];
        this.wiring = new int[26];

        this.setWheel(wheel);
        this.setRingSetting(ringSetting);
        this.setPosition(position);
    }

    public void setWheel(String wheel) {
        verifyNonNull(wheel, "wheel");

        this.wheel = wheel;
        this.turnover = turnoverOf(wheel);
        String wiring = wiringOf(wheel);

        for (int i = 0; i < 26; i++) {
            int cIndex = wiring.charAt(i) - 65;
            this.wiring[i] = cIndex;
            this.inverseWiring[cIndex] = i;
        }
    }

    public String wiringOf(String wheel) {
        return switch (wheel) {
            case "I" -> "EKMFLGDQVZNTOWYHXUSPAIBRCJ";
            case "II" -> "AJDKSIRUXBLHWTMCQGZNPYFVOE";
            case "III" -> "BDFHJLCPRTXVZNYEIWGAKMUSQO";
            case "IV" -> "ESOVPZJAYQUIRHXLNFTGKDCMWB";
            case "V" -> "VZBRGITYUPSDNHLXAWMJQOFECK";
            default -> throw new IllegalArgumentException("unsupported wheel type");
        };
    }

    public int turnoverOf(String wheel) {
        return switch (wheel) {
            case "I" -> 16;
            case "II" -> 4;
            case "III" -> 21;
            case "IV" -> 9;
            case "V" -> 25;
            default -> throw new IllegalArgumentException("unsupported wheel type");
        };
    }

    public void setRingSetting(int ringSetting) {
        verifyInteger(ringSetting, "ringSetting");
        this.ringSetting = ringSetting;
    }

    public void setPosition(int position) {
        verifyInteger(position, "position");
        this.position = position;
        this.initialPos = position;
    }

    public void turn() {
        this.position = (this.position + 1) % 26;
    }

    public int wiringOf(int ordC, boolean inverse) {
        int offset = this.position - this.ringSetting;
        int[] wiring = (inverse) ? this.inverseWiring : this.wiring;
        return mod((wiring[mod((ordC + offset), 26) % 26] - offset), 26);
    }

    public boolean atTurnover() {
        return this.position == this.turnover;
    }

    public String toString() {
        return String.format("Rotor(wheel=%s, ringSetting=%d, position=%d)", this.wheel, this.ringSetting,
                this.position);
    }

    private int mod(int n, int mod) {
        return Math.abs(Math.floorMod(n, mod));
    }

    protected void verifyNonNull(Object o, String parameter) {
        if (o == null) throw new IllegalArgumentException("`" + parameter + "' must not be null");
    }

    protected void verifyInteger(int n, String parameter) {
        if (n < 0 || n > 26) throw new IllegalArgumentException("`" + parameter + "` must be within 0-26 inclusive (passed `" + n  + "`)");
    }
}
