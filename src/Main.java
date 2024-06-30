package src;

import src.machine.Decryptor;
import src.machine.Enigma;
import src.machine.Rotor;

// compile first then run

// Inspired by Mike Pound's demonstration on Computerphile
// his implementation of Enigma and ciphertext decryption:
// https://github.com/mikepound/enigma

// some implementation details borrowed from him
// but most is copied from my own implementation in Python a year ago

public class Main {


    public static void main(String[] args) {
        Enigma enigma = Enigma.createDefault();

        String ciphertext1 = enigma.encrypt("AAAA AAAA AAAA AAA", " ");
        System.out.println("ciphertext: " + ciphertext1);

        // enigma.encrypt("AAAA AAAA AAAA AAA", " ")
        // = BDZG OWCX LTKS BTM

        enigma.resetPositions();

        String foxInSocks = "Fox, Socks, Box, Knox. Knox in box. Fox in socks. Knox on fox in socks in box. Socks on Knox and Knox in box. Fox in socks on box on Knox.";
        String ciphertext2 = enigma.encrypt(foxInSocks, "");
        System.out.println("fox in socks: " + ciphertext2);

        // "Fox, Socks, Box, Knox. Knox in box. Fox in socks. Knox on fox in socks in box. Socks on Knox and Knox in box. Fox in socks on box on Knox."
        // = EIRNAMEFFSHCTCJIMRKCBLHFAVEVDIGPBHMPVGDANFOAKPIERXYMOIWGAJRGFQQXFKZYMQXEOFUYKELQMDWRNUXBNKDPLNCUMKD

        enigma.setWheels(new String[]{"III", "V", "IV"});
        enigma.setRingSettings(new int[]{25, 1, 9});
        enigma.setPositions(new int[]{11, 14, 11});

        System.out.println();

        for (Rotor rotor : enigma.getRotors()) {
            System.out.println(rotor.toString());
        }

        System.out.println();

        String a = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";
        String ciphertext3 = enigma.encrypt(a, "");
        System.out.println("a: " + ciphertext3);
        // - BTOZNTVXJRPEFOVFVGYZIGDQUJRONHFLQLILMCZZYLVHRPOEKQIGS

        System.out.println();


        String imitationGameNoPlugs = "VQSFHVLXCAWCYZZVJIFXFKFNXSEKWRHSFZNPYRNQUULSRFUKHJBKDOYXRTLKFLXJHOHDZKBQYXORRFQCSFGZXOVMXVQQMJEGVLSSOZWLMEPNPYBYPDIPADUIXXRGBNUGFVOAXZYLNROXJOMENEMBENOWMFGMLRXBMCBDOKHZVHGAQWNCMGAXCDWQNXYGLAQASXARZWVKGOPQXEBHVZQXQLLJKJUITMWKOLNHSOZIAJIYDFOHTOMARWJOYBQAJNMKHHPGFZXHPPFKIPSQMJIDNNZBTXTXYGSBLEREOAYYPEAGXSPNDPUJMZSLTDQYUAQILFFAWSWRJHSHNTJWIGUOHESQNRAYEGDWGLQUGAXHJZNCSVSGSRXNJSTUFKHPQKJHSRXEXXARTJCQCXLADYFFCDGKJRDJCGFFRQJFEGTRYJNMWKGTROOOCISKJDEUCQTABKLODFNGMRPXNDSEJODWCMTOIFZISTDMPUUUTDLTRJMQRIDADAGLPDFQHXVPVHGQJCGBFKJPOUEEIKLYKHWIUXQJDQUUWIRERXULEBFNLJJAFOPHMGOMKWXMYEUFRZYWYJDCBYWH";
        String imitationGameWithPlugs = "JCCPJTJRWZIFAGJOJFHRHSJFTWTQCJPSJRILGEKWRETAMIMVFCQZEHELDYXWDVDCSYPQCVBFICXLHYXHKHGSQLEMBITORRZYMEFPGFGNPVEVIBKTHXHYVCVCEFOVCHKPTKAZLHZYFMXYQPRPYPPMOGNSKSSFGYQIGKIJAEWDAZFIZHUMRQTFQZRWDWHOXGBVVZBTFJREGWBWKLSKFWLZKXBAWKCPEHIHRAJABGUKRQGJJDPKZJXBFAFMLJYOPFYZNQONYZGTWXRVURFKYOLIWIXBPIJHJWUZWXJICIBXTTKULVYTDEROVJJCJLAZBXDMTRDSHAHVMCKPOQFPKGWZWYEJOXHJDQQDBHVDUIBLWHLLYAYOKWSNAHVAFXOSWJUYPNZJRDOABZJPZVDXBGIMZZQCULNNOTTPRBNRTAHOXYJKYMDXCFELQBWPVYHHHPPVOPDDONBMQYHQKCRRLMTFUPMZSPCRSVDYNCBOWOAPGDHXQWDZIAYKOVSPMYDLMRVUEDABBSMRAGFIPNJWCGUANUIAMHDVEKMATYWNOGJXAKOHXBKIWXSDIMRSSIOSLQBOILFXTLBJ";

        // imitation game no plugs - ("V", "IV", "I"), (1, 15, 23), (22, 22, 1)
        // imitation game w/plugs  - ("V", "IV", "I"), (7, 20, 19), (1, 15, 23), ("sx", "bp", "eu", "nz")

        Decryptor decryptor = new Decryptor(imitationGameNoPlugs, 4, 2);
        decryptor.decrypt();
    }
}
