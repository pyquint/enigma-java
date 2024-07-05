package src.machine;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

/**
 * Java implementation of the 3-rotor Enigma (model M3) used during
 * WWII by the German Navy (Kriegsmarine).
 * <p>
 * (The M3 used letters rather than numbers for the rotor positions.
 * This implementation uses zero-indexing for speed and simplicity.)
 */
public class Enigma {
    public final int ROTOR_COUNT = 3;
    public final int LETTER_COUNT = 26;

    private String reflectorModel;
    private final int[] reflector = new int[LETTER_COUNT];
    private final Rotor[] rotors = new Rotor[ROTOR_COUNT];
    private final List<String> pairs = new ArrayList<>();
    private final HashMap<Character, Character> plugboardMap = new HashMap<>();

    /**
     * Java implementation of the 3-rotor Enigma (model M3) used during
     * WWII by the German Navy (Kriegsmarine).
     * <p>
     * (The M3 used letters rather than numbers for the rotor positions.
     * This implementation uses zero-indexing for speed and simplicity.)
     *
     * @param wheels         wheel order, array of three I - V exclusive
     * @param rings          ring settings, array of three 0 - 25
     * @param positions      initial rotor position, array of three 0 - 25
     * @param reflectorModel reflector model, "B" or "C"
     * @param pairs          plugboard settings, array of letter pairs
     *
     * @see #Enigma(EnigmaKey)
     */
    public Enigma(String[] wheels, int[] rings, int[] positions, String reflectorModel, String[] pairs) {
        for (int i = 0; i < ROTOR_COUNT; i++) {
            this.rotors[i] = new Rotor(wheels[i], rings[i], positions[i]);
        }
        setReflector(reflectorModel);
        setPlugboard(pairs);
    }

    /**
     * Java implementation of the 3-rotor Enigma (model M3) used during
     * WWII by the German Navy (Kriegsmarine).
     * <p>
     * (The M3 used letters rather than numbers for the rotor positions.
     * This implementation uses zero-indexing for speed and simplicity.)
     *
     * @param key    {@link EnigmaKey} populated with settings
     *
     * @see #Enigma(String[], int[], int[], String, String[])
     */
    public Enigma(EnigmaKey key) {
        this(key.wheels, key.rings, key.positions, "B", key.pairs);
    }

    /**
     * Factory method that creates an {@link Enigma} object with these predefined parameters:
     * <ul>
     *     <li>{@code wheels} = {@code ["I", "II", "III"]}</li>
     *     <li>{@code rings} = {@code [0, 0, 0]}</li>
     *     <li>{@code positions} = {@code [0, 0, 0]}</li>
     *     <li>{@code reflectorModel} = {@code "B"}</li>
     *     <li>{@code pairs} = {@code []}</li>
     * </ul>
     *
     * @return {@link Enigma} object
     */
    public static Enigma createDefault() {
        return new Enigma(
                new String[]{"I", "II", "III"},
                new int[]{0, 0, 0},
                new int[]{0, 0, 0},
                "B",
                new String[]{}
        );
    }

    public String encrypt(String plaintext) {
        return plaintext.toUpperCase()
                .chars()
                .filter(c -> (c >= 'A' && c <= 'Z'))
                .map(c -> cipher((char) c))
                .collect(
                        StringBuilder::new,
                        StringBuilder::appendCodePoint,
                        StringBuilder::append)
                .toString();
    }

    protected char cipher(char c) {
        turnRotors();
        int ec = getPairOf(c) - 65;
        for (int i = 2; i >= 0; i--) {
            ec = this.rotors[i].wiringOf(ec, false);
        }
        ec = this.reflector[ec];
        for (Rotor rotor : this.rotors) {
            ec = rotor.wiringOf(ec, true);
        }
        return getPairOf((char) (ec % LETTER_COUNT + 65));
    }

    public char getPairOf(char c) {
        return plugboardMap.getOrDefault(c, c);
    }

    public void setReflector(String model) {
        this.reflectorModel = model;
        String reflector = reflectorWiringOf(model);

        for (int i = 0; i < LETTER_COUNT; i++) {
            this.reflector[i] = reflector.charAt(i) - 65;
        }
    }

    public static String reflectorWiringOf(String reflectorModel) {
        return switch (reflectorModel) {
            case "B" -> "YRUHQSLDPXNGOKMIEBFZCWVJAT";
            case "C" -> "RDOBJNTKVEHMLFCWZAXGYIPSUQ";
            default -> throw new IllegalArgumentException("reflector must be \"B\" or \"C\".");
        };
    }

    /**
     * @param pairs     {@link Collection} of {@link String} letter pairs, e.g. {@code List.of("ab", "cd")}
     * @throws IllegalArgumentException <br>
     * if {@code pairs} is {@code null},<br>
     * if an element is {@code null},<br>
     * if the String element is not of length two,<br>
     * if the characters are not letters,<br>
     * if one of the letters is already paired
     *
     * @see #getPluggedPairs()
     */
    public void setPlugboard(Collection<String> pairs) {
        plugboardMap.clear();
        this.pairs.clear();
        pairs.forEach(this::addPlugboardPair);
    }

    /**
     * @param pairs     Array of {@link String} letter pairs, e.g. {@code ["ab", "cd"]}
     * @throws IllegalArgumentException <br>
     * if {@code pairs} is {@code null},<br>
     * if an element is {@code null},<br>
     * if the String element is not of length two,<br>
     * if the characters are not letters,<br>
     * if one of the letters is already paired
     *
     * @see #getPluggedPairs()
     */
    public void setPlugboard(String[] pairs) {
        setPlugboard(List.of(pairs));
    }

    public void addPlugboardPair(String pair) {
        pair = pair.toUpperCase();
        char p1 = pair.charAt(0);
        char p2 = pair.charAt(1);

        if (pair.length() != 2 || !(isValidLetter(p1) && isValidLetter(p2))) throw new IllegalArgumentException("pair must be a string of two letters");
        if (plugboardMap.containsKey(p1) || plugboardMap.containsKey(p2)) throw new IllegalArgumentException("one of the letters is already paired: `" + p1 +  p2 + "`");

        plugboardMap.put(p1, p2);
        plugboardMap.put(p2, p1);
        pairs.add(pair);
    }

    /**
     * @param wheels     String array of length three to set three rotors.
     *
     * @see #setWheels(String, String, String) 
     * @see #getWheels()
     */
    public void setWheels(String[] wheels) {
        verifyNonNull(wheels, "wheels");
        verifyArrayLength(wheels, "wheels");

        for (int i = 0; i < ROTOR_COUNT; i++) {
            this.rotors[i].setWheel(wheels[i]);
        }
    }

    /**
     * @param w0    wheel at leftmost rotor
     * @param w1    wheel at middle rotor
     * @param w2    wheel at rightmost rotor
     *
     * @see #setWheels(String[]) 
     * @see #getWheels()
     */
    public void setWheels(String w0, String w1, String w2) {
        this.rotors[0].setWheel(w0);
        this.rotors[1].setWheel(w1);
        this.rotors[2].setWheel(w2);
    }

    /**
     * @param ringSettings     integer array of length three to set three rotors.
     *
     * @see #setRingSettings(int, int, int) 
     * @see #getRingSettings()
     */
    public void setRingSettings(int[] ringSettings) {
        verifyNonNull(ringSettings, "ringSettings");
        verifyArrayLength(ringSettings, "ringSettings");

        for (int i = 0; i < ROTOR_COUNT; i++) {
            this.rotors[i].setRingSetting(ringSettings[i]);
        }
    }

    /**
     * @param r0    ring setting at leftmost rotor
     * @param r1    ring setting at middle rotor
     * @param r2    ring setting at rightmost rotor
     *
     * @see #setRingSettings(int[]) 
     * @see #getRingSettings()
     */
    public void setRingSettings(int r0, int r1, int r2) {
        this.rotors[0].setRingSetting(r0);
        this.rotors[1].setRingSetting(r1);
        this.rotors[2].setRingSetting(r2);
    }

    /**
     * @param positions     integer array of length three to set three rotors.
     *
     * @see #setPositions(int, int, int) 
     * @see #getPositions()
     */
    public void setPositions(int[] positions) {
        verifyNonNull(positions, "positions");
        verifyArrayLength(positions, "positions");

        for (int i = 0; i < ROTOR_COUNT; i++) {
            this.rotors[i].setPosition(positions[i]);
        }
    }

    /**
     * @param p0    position at leftmost rotor
     * @param p1    position at middle rotor
     * @param p2    position at rightmost rotor
     *
     * @see #setPositions(int[]) 
     * @see #getPositions()
     */
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

    /**
     * Resets the {@code position} of each {@link Rotor} to the ones initialized during object creation or through the setter methods.
     *
     * @see #getPositions()
     * @see #setPositions(int[])
     * @see #setPositions(int, int, int)
     */
    public void resetPositions() {
        for (Rotor rotor : this.rotors) {
            rotor.position = rotor.initialPos;
        }
    }

    public void resetPlugboard() {
        pairs.clear();
        plugboardMap.clear();
    }

    /**
     * This returns a new copy to get a view the current wheel types per rotor.
     * <p>
     * Any changes to that will not reflect to the internal states.
     *
     * @return {@link String} array object of length three.
     *
     * @see #setWheels(String[])
     * @see #setWheels(String, String, String)
     */
    public String[] getWheels() {
        return new String[]{rotors[0].wheel, rotors[1].wheel, rotors[2].wheel};
    }

    /**
     * This returns a new copy to get a view of the current {@code rings} per {@link Rotor}.
     * <p>
     * Any changes to that will not reflect to the internal states.
     *
     * @return {@code int} array object of length three.
     *
     * @see #setPlugboard(String[])
     * @see #setPlugboard(Collection)
     */
    public int[] getRingSettings() {
        return new int[]{rotors[0].ringSetting, rotors[1].ringSetting, rotors[2].ringSetting};
    }

    /**
     * This returns a new copy to get a view of the current rotor positions per rotor.
     * <p>
     * Any changes to that will not reflect to the internal states.
     *
     * @return {@code int} array object of length three.
     *
     * @see #setPositions(int[])
     * @see #setPositions(int, int, int)
     */
    public int[] getPositions() {
        return new int[]{rotors[0].position, rotors[1].position, rotors[2].position};
    }

    /**
     * The plugboard pairs initialized during object creation must not be directly modified.
     * <p>
     * This returns a new copy. Any changes to that will not reflect to the internal states.
     *
     * @return new {@link Rotor} array object
     *
     * @see #setPlugboard(String[])
     * @see #setPlugboard(Collection)
     */
    public String[] getPluggedPairs() {
        return pairs.toArray(new String[0]);
    }

    /**
     * The three {@link Rotor} initialized during object creation must not be directly modified.
     * <p>
     * This returns a new copy. Any changes to that will not reflect to the internal states.
     *
     * @return new {@link Rotor} array object
     *
     * @see #setWheels(String[])
     * @see #setWheels(String, String, String)
     * @see #setRingSettings(int[])
     * @see #setRingSettings(int, int, int)
     * @see #setPositions(int[])
     * @see #setPositions(int, int, int)
     */
    public Rotor[] getRotors() {
        return Arrays.copyOf(rotors, rotors.length);
    }

    /**
     * Changes done to the returned {@link EnigmaKey} will not reflect to the internal states.
     *
     * @return {@link EnigmaKey} snapshot key of the machine's current rotor states and plugboard pairs.
     */
    public EnigmaKey getEnigmaKeu() {
        return new EnigmaKey(getWheels(), getRingSettings(), getPositions(), pairs.toArray(new String[0]));
    }

    public String toString() {
        return String.format("Enigma(wheels=%s, ringSettings=%s, rotorPositions=%s, reflectorModel=%s, pairs=%s)",
                Arrays.toString(getWheels()),
                Arrays.toString(getRingSettings()),
                Arrays.toString(getPositions()),
                reflectorModel,
                pairs
        );
    }

    protected boolean isValidLetter(char c) {
        return c >= 'A' && c <= 'Z';
    }

    protected void verifyArrayLength(Object[] o, String parameter) {
        if (o.length != ROTOR_COUNT) throw new IllegalArgumentException("`" + parameter + "` must be of length three.");
    }

    protected void verifyArrayLength(int[] o, String parameter) {
        if (o.length != ROTOR_COUNT) throw new IllegalArgumentException("`" + parameter + "` must be of length three.");
    }

    private void verifyNonNull(Object o, String parameter) {
        if (o == null) throw new IllegalArgumentException("`" + parameter + "' must not be null");
    }
}
