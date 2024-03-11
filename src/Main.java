package src;

import src.machine.*;

// compile first then run

public class Main {
    public static void main(String[] args) {
        String[] wheels = { "I", "II", "III" };
        int[] ringSettings = { 0, 0, 0 };
        int[] rotorPositions = { 0, 0, 0 };
        Enigma enigma = new Enigma(wheels, ringSettings, rotorPositions, 'B', new String[] {});

        String ciphertext1 = enigma.encrypt("AAAA AAAA AAAA AAA", " ");
        System.out.println("ciphertext: " + ciphertext1); // BDZG OWCX LTKS BTM

        enigma.resetPositions();

        String foxInSocks = "Fox, Socks, Box, Knox. Knox in box. Fox in socks. Knox on fox in socks in box. Socks on Knox and Knox in box. Fox in socks on box on Knox.";
        String ciphertext2 = enigma.encrypt(foxInSocks, "");
        System.out.println("fox in socks: " + ciphertext2);
        // expected -
        // EIRNAMEFFSHCTCJIMRKCBLHFAVEVDIGPBHMPVGDANFOAKPIERXYMOIWGAJRGFQQXFKZYMQXEOFUYKELQMDWRNUXBNKDPLNCUMKD

        enigma.setWheels(new String[] { "III", "V", "IV" });
        enigma.setRingSettings(new int[] { 25, 1, 9 });
        enigma.setPositions(new int[] { 11, 14, 11 });

        System.out.println();
        for (Rotor rotor : enigma.rotors) {
            System.out.println(rotor.toString());
        }
        System.out.println();

        String a = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String ciphertext3 = enigma.encrypt(a, "");
        System.out.println("a: " + ciphertext3);
        // expected - BTOZNTVXJRPEFOVFVGYZIGDQUJRONHFLQLILMCZZYLVHRPOEKQIGS
    }
}
