package src;

import src.machine.Enigma;

// compile first then run

public class Main {
    public static void main(String[] args) {
        String[] wheels = { "I", "II", "III" };
        int[] ringSettings = { 0, 0, 0 };
        int[] rotorPositions = { 0, 0, 0 };
        Enigma enigma = new Enigma(wheels, ringSettings, rotorPositions, 'B', new String[] {});

        String ciphertext = enigma.encrypt("AAAA AAAA AAAA AAA", ' ');
        System.out.println("ciphertext: " + ciphertext); // BDZG OWCX LTKS BTM
    }
}
