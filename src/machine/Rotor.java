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
        this.setWheel(wheel);
        this.setRingSetting(ringSetting);
        this.setPosition(position);
    }

    public void setWheel(String wheel) {
        this.wiring = new int[26];
        this.inverseWiring = new int[26];
        this.wheel = wheel;
        String wiring;
        switch (wheel) {
            case "I" -> {
                wiring = "EKMFLGDQVZNTOWYHXUSPAIBRCJ";
                this.turnover = 16;
            }
            case "II" -> {
                wiring = "AJDKSIRUXBLHWTMCQGZNPYFVOE";
                this.turnover = 4;
            }
            case "III" -> {
                wiring = "BDFHJLCPRTXVZNYEIWGAKMUSQO";
                this.turnover = 21;
            }
            case "IV" -> {
                wiring = "ESOVPZJAYQUIRHXLNFTGKDCMWB";
                this.turnover = 9;
            }
            case "V" -> {
                wiring = "VZBRGITYUPSDNHLXAWMJQOFECK";
                this.turnover = 25;
            }
            default -> {
                wiring = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
                this.turnover = 25;
            }
        }
        int cIndex;
        for (int i = 0; i < 26; i++) {
            cIndex = wiring.charAt(i) - 65;
            this.wiring[i] = cIndex;
            this.inverseWiring[cIndex] = i;
        }
    }

    public void setRingSetting(int ringSetting) {
        this.ringSetting = ringSetting;
    }

    public void setPosition(int position) {
        this.position = position;
        this.initialPos = position;
    }

    public void turn() {
        this.position = (this.position + 1) % 26;
    }

    public int wiringOf(int ordC, boolean inverse) {
        int offset = this.position - this.ringSetting;
        int[] wiring = (inverse) ? this.inverseWiring : this.wiring;
        // `%` in Java is 'remainder' which can return a negative value
        return (26 + (wiring[(ordC + offset) % 26] - offset)) % 26;
    }

    public String toString() {
        return String.format("Rotor(wheel=%s, ringSetting=%d, position=%d)", this.wheel, this.ringSetting,
                this.position);
    }

}
