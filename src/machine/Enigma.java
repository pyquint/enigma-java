package src.machine;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;


public class Enigma {
    private final Rotor[] rotors;
    private String reflectorModel;
    private int[] reflector;
    private final HashMap<Character, Character> plugboard;
    private String[] pairs;

    /**
     * <p>
     * Java implementation of the 3-rotor Enigma (model M3) used during
     * WWII by the German Navy (Kriegsmarine).
     * <p>
     * (The M3 used letters rather than numbers for the rotor positions.
     * This implementation uses zero-indexing for speed and simplicity.)
     *
     * @param wheels         wheel order, array of three I - V exclusive
     * @param rings          ring settings, array of three 0 - 25
     * @param positions      initial rotor position, array of three 0 - 25
     * @param reflectorModel reflector model, 'B' or 'C'
     * @param pairs          plugboard settings, array of letter pairs
     */
    public Enigma(String[] wheels,
                  int[] rings,
                  int[] positions,
                  String reflectorModel,
                  String[] pairs) {

        this.rotors = new Rotor[3];

        for (int i = 0; i < 3; i++) {
            this.rotors[i] = new Rotor(wheels[i], rings[i], positions[i]);
        }

        setReflector(reflectorModel);

        this.plugboard = new HashMap<>();
        this.setPlugboard(pairs);
    }

    public Enigma(EnigmaKey key) {
        this(key.wheels, key.rings, key.positions, "B", key.pairs);
    }

    public static Enigma createDefault() {
        return new Enigma(
                new String[]{"I", "II", "III"},
                DefaultRotorConfig(),
                DefaultRotorConfig(),
                "B",
                new String[]{}
        );
    }

    public static String[] Wheels(int n) {
        return switch (n) {
            case 3 -> new String[]{"I", "II", "III"};
            case 5 -> new String[]{"I", "II", "III", "IV", "V"};
            case 8 -> new String[]{"I", "II", "III", "IV", "V", "VI", "VII", "VIII"};
            default -> throw new RuntimeException("Invalid n. Must be 3 5, 8.");
        };
    }

    public static int[] DefaultRotorConfig() {
        return new int[]{0, 0, 0};
    }

    public static Enigma copyOf(Enigma e) {
        return new Enigma(e.getRotorWheels(), e.getRingSettings(), e.getRotorPositions(), "B", e.getPluggedPairs());
    }

    public void setReflector(String model) {
        this.reflectorModel = model;
        String reflector = reflectorWiringOf(model);
        this.reflector = new int[26];

        for (int i = 0; i < 26; i++) {
            this.reflector[i] = reflector.charAt(i) - 65;
        }
    }

    public String encrypt(String plaintext) {
        return encrypt(plaintext, "");
    }

    public String encrypt(String plaintext, String sep) {
        return plaintext
                .toUpperCase()
                .chars()
                .filter(c -> (c >= 'A' && c <= 'Z') || c == ' ')
                .map(c -> cipher((char) c))
                .collect(
                        StringBuilder::new,
                        StringBuilder::appendCodePoint,
                        StringBuilder::append)
                .toString()
                .replaceAll(" ", sep);
    }

    public char cipher(char c) {
        turnRotors();
        int ec = getPlugboardPair(c) - 65;
        for (int i = 2; i >= 0; i--) {
            ec = this.rotors[i].wiringOf(ec, false);
        }
        ec = this.reflector[ec];
        for (Rotor rotor : this.rotors) {
            ec = rotor.wiringOf(ec, true);
        }
        return getPlugboardPair((char) (ec % 26 + 65));
    }

    public char getPlugboardPair(char c) {
        return plugboard.getOrDefault(c, c);
    }

    public void setPlugboard(Collection<String> pairs) {
        String[] toPass = (pairs != null) ? pairs.toArray(new String[0]) : null;
        setPlugboard(toPass);
    }

    public void setPlugboard(String[] pairs) {
        this.pairs = pairs;
        this.plugboard.clear();

        if (pairs != null && pairs.length > 0) {
            char[] p;
            for (String pair : pairs) {
                p = pair.toUpperCase().toCharArray();
                this.plugboard.put(p[0], p[1]);
                this.plugboard.put(p[1], p[0]);
            }
        }
    }

    public void resetPlugboard() {
        this.pairs = new String[]{};
        this.plugboard.clear();
    }

    public void setWheels(String[] wheels) {
        for (int i = 0; i < 3; i++) {
            this.rotors[i].setWheel(wheels[i]);
        }
    }

    public void setWheels(String w0, String w1, String w2) {
        this.rotors[0].setWheel(w0);
        this.rotors[1].setWheel(w1);
        this.rotors[2].setWheel(w2);
    }

    public void setRingSettings(int r0, int r1, int r2) {
        this.rotors[0].setRingSetting(r0);
        this.rotors[1].setRingSetting(r1);
        this.rotors[2].setRingSetting(r2);
    }

    public void setPositions(int[] positions) {
        for (int i = 0; i < 3; i++) {
            this.rotors[i].setPosition(positions[i]);
        }
    }

    public void setPositions(int p0, int p1, int p2) {
        this.rotors[0].setPosition(p0);
        this.rotors[1].setPosition(p1);
        this.rotors[2].setPosition(p2);
    }

    public void turnRotors() {
        if (this.rotors[1].atTurnover()) {
            this.rotors[0].turn();
            this.rotors[1].turn();
        }
        if (this.rotors[2].atTurnover()) {
            this.rotors[1].turn();
        }
        this.rotors[2].turn();
    }

    public void resetPositions() {
        for (Rotor rotor : this.rotors) {
            rotor.position = rotor.initialPos;
        }
    }

    public String reflectorWiringOf(String reflectorModel) {
        return switch (reflectorModel) {
            case "B" -> "YRUHQSLDPXNGOKMIEBFZCWVJAT";
            case "C" -> "RDOBJNTKVEHMLFCWZAXGYIPSUQ";
            default -> "ABDCEFGHIJKLMNOPQRSTUVWXYZ";
        };
    }

    public String[] getRotorWheels() {
        return new String[]{rotors[0].wheel, rotors[1].wheel, rotors[2].wheel};
    }

    public int[] getRingSettings() {
        return new int[]{rotors[0].ringSetting, rotors[1].ringSetting, rotors[2].ringSetting};
    }

    public void setRingSettings(int[] ringSettings) {
        for (int i = 0; i < 3; i++) {
            this.rotors[i].setRingSetting(ringSettings[i]);
        }
    }

    public int[] getRotorPositions() {
        return new int[]{rotors[0].position, rotors[1].position, rotors[2].position};
    }

    public String toString() {
        return String.format("Enigma(wheels=%s, ringSettings=%s, rotorPositions=%s, reflectorModel=%s, pairs=%s)",
                Arrays.toString(getRotorWheels()),
                Arrays.toString(getRingSettings()),
                Arrays.toString(getRotorPositions()),
                reflectorModel,
                Arrays.toString(pairs)
        );
    }

    public String[] getPluggedPairs() {
        return (pairs != null) ? pairs : new String[]{};
    }

    public EnigmaKey getEnigmaKeu() {
        return new EnigmaKey(getRotorWheels(), getRingSettings(), getRotorPositions(), getPluggedPairs());
    }

    public Rotor[] getRotors() {
        return rotors;
    }
}