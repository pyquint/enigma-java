package src.machine;

import java.util.HashMap;

public class Enigma {
    public Rotor[] rotors;
    String plaintext;
    String ciphertext;

    String[] wheels;
    int[] ringSettings;
    int[] rotorPosiions;
    char reflectorModel;
    int[] reflector;
    String[] plugboardPairs;
    HashMap<Character, Character> plugboard;

    public Enigma(String[] wheels, int[] ringSettings, int[] rotorPositions, char reflectorModel,
            String[] plugboardPairs) {
        this.wheels = wheels;
        this.ringSettings = ringSettings;
        this.rotorPosiions = rotorPositions;
        this.rotors = new Rotor[3];
        this.rotors[0] = new Rotor(wheels[0], ringSettings[0], rotorPositions[0]);
        this.rotors[1] = new Rotor(wheels[1], ringSettings[1], rotorPositions[1]);
        this.rotors[2] = new Rotor(wheels[2], ringSettings[2], rotorPositions[2]);

        this.reflectorModel = reflectorModel;

        String reflector;
        switch (reflectorModel) {
            case 'B' -> reflector = "YRUHQSLDPXNGOKMIEBFZCWVJAT";
            case 'C' -> reflector = "RDOBJNTKVEHMLFCWZAXGYIPSUQ";
            default -> reflector = "ABDCEFGHIJKLMNOPQRSTUVWXYZ";
        }
        this.reflector = new int[26];
        for (int i = 0; i < 26; i++) {
            this.reflector[i] = reflector.charAt(i) - 65;
        }

        this.plugboard = new HashMap<Character, Character>();
        this.setPlugboard(plugboardPairs);
    }

    public String encrypt(String plaintext, String sep) {
        StringBuilder ciphertext = new StringBuilder();
        for (char c : plaintext.toUpperCase().toCharArray()) {
            ciphertext.append((c >= 'A' && c <= 'Z') ? cipher(c) : sep);
        }
        return ciphertext.toString();
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
        return (plugboard.containsKey(c)) ? plugboard.get(c) : c;
    }

    public void setPlugboard(String[] pairs) {
        if (pairs.length > 0) {
            char[] p;
            for (String pair : pairs) {
                p = pair.toUpperCase().toCharArray();
                this.plugboard.put(p[0], p[1]);
                this.plugboard.put(p[1], p[0]);
            }
        }
    }

    public void setWheels(String[] wheels) {
        for (int i = 0; i < 3; i++) {
            this.rotors[i].setWheel(wheels[i]);
        }
    }

    public void setRingSettings(int[] ringSettings) {
        for (int i = 0; i < 3; i++) {
            this.rotors[i].setRingSetting(ringSettings[i]);
        }
    }

    public void setPositions(int[] positions) {
        for (int i = 0; i < 3; i++) {
            this.rotors[i].setPosition(positions[i]);
        }
    }

    public void turnRotors() {
        if (this.rotors[1].position == this.rotors[1].turnover) {
            this.rotors[0].turn();
            this.rotors[1].turn();
        }
        if (this.rotors[2].position == this.rotors[2].turnover) {
            this.rotors[1].turn();
        }
        this.rotors[2].turn();
    }

    public void resetPositions() {
        for (Rotor rotor : this.rotors) {
            rotor.position = rotor.initialPos;
        }
    }
}
