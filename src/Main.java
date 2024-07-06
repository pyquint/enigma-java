package src;

import src.decryption.key.EnigmaKey;
import src.decryption.key.ScoredKey;
import src.decryption.Decryptor;
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

        String ciphertext1 = enigma.encrypt("AAAA AAAA AAAA AAA");
        System.out.println("ciphertext: " + ciphertext1);

        // enigma.encrypt("AAAA AAAA AAAA AAA", " ")
        // = BDZG OWCX LTKS BTM

        enigma.resetPositions();

        String foxInSocks = "Fox, Socks, Box, Knox. Knox in box. Fox in socks. Knox on fox in socks in box. Socks on Knox and Knox in box. Fox in socks on box on Knox.";
        String ciphertext2 = enigma.encrypt(foxInSocks);
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
        String ciphertext3 = enigma.encrypt(a);
        System.out.println("a: " + ciphertext3);
        // - BTOZNTVXJRPEFOVFVGYZIGDQUJRONHFLQLILMCZZYLVHRPOEKQIGS

        System.out.println();


        String imitationGameNoPlugs = "VQSFHVLXCAWCYZZVJIFXFKFNXSEKWRHSFZNPYRNQUULSRFUKHJBKDOYXRTLKFLXJHOHDZKBQYXORRFQCSFGZXOVMXVQQMJEGVLSSOZWLMEPNPYBYPDIPADUIXXRGBNUGFVOAXZYLNROXJOMENEMBENOWMFGMLRXBMCBDOKHZVHGAQWNCMGAXCDWQNXYGLAQASXARZWVKGOPQXEBHVZQXQLLJKJUITMWKOLNHSOZIAJIYDFOHTOMARWJOYBQAJNMKHHPGFZXHPPFKIPSQMJIDNNZBTXTXYGSBLEREOAYYPEAGXSPNDPUJMZSLTDQYUAQILFFAWSWRJHSHNTJWIGUOHESQNRAYEGDWGLQUGAXHJZNCSVSGSRXNJSTUFKHPQKJHSRXEXXARTJCQCXLADYFFCDGKJRDJCGFFRQJFEGTRYJNMWKGTROOOCISKJDEUCQTABKLODFNGMRPXNDSEJODWCMTOIFZISTDMPUUUTDLTRJMQRIDADAGLPDFQHXVPVHGQJCGBFKJPOUEEIKLYKHWIUXQJDQUUWIRERXULEBFNLJJAFOPHMGOMKWXMYEUFRZYWYJDCBYWH";
        String imitationGameWithPlugs = "JCCPJTJRWZIFAGJOJFHRHSJFTWTQCJPSJRILGEKWRETAMIMVFCQZEHELDYXWDVDCSYPQCVBFICXLHYXHKHGSQLEMBITORRZYMEFPGFGNPVEVIBKTHXHYVCVCEFOVCHKPTKAZLHZYFMXYQPRPYPPMOGNSKSSFGYQIGKIJAEWDAZFIZHUMRQTFQZRWDWHOXGBVVZBTFJREGWBWKLSKFWLZKXBAWKCPEHIHRAJABGUKRQGJJDPKZJXBFAFMLJYOPFYZNQONYZGTWXRVURFKYOLIWIXBPIJHJWUZWXJICIBXTTKULVYTDEROVJJCJLAZBXDMTRDSHAHVMCKPOQFPKGWZWYEJOXHJDQQDBHVDUIBLWHLLYAYOKWSNAHVAFXOSWJUYPNZJRDOABZJPZVDXBGIMZZQCULNNOTTPRBNRTAHOXYJKYMDXCFELQBWPVYHHHPPVOPDDONBMQYHQKCRRLMTFUPMZSPCRSVDYNCBOWOAPGDHXQWDZIAYKOVSPMYDLMRVUEDABBSMRAGFIPNJWCGUANUIAMHDVEKMATYWNOGJXAKOHXBKIWXSDIMRSSIOSLQBOILFXTLBJ";

        // imitation game no plugs - ("V", "IV", "I"), (1, 15, 23), (22, 22, 1)
        // imitation game w/plugs  - ("V", "IV", "I"), (7, 20, 19), (1, 15, 23), ("sx", "bp", "eu", "nz")

        var test = imitationGameWithPlugs;

        Decryptor decryptor = new Decryptor(test);
        ScoredKey bestKey = decryptor.decrypt();

// region debug
//        e1.setWheels("V", "IV", "I");
//        e1.setRingSettings(7, 20, 19);
//        e1.setPositions(1, 15, 23);
//        e1.setPlugboard(List.of("sx", "bp", "eu", "nz"));
//
//        var k = e1.getEnigmaKeu();
//        var s = e1.encrypt(test);
//        var f = Decryptor.BIGRAM.score(s);
//
//        System.out.println(decryptor.bestPlugboardKey(new ScoredEnigmaKey(k, f, "bigram"), Decryptor.BIGRAM));
// endregion

        Enigma e = new Enigma(bestKey);
        System.out.println("Best key: " + bestKey);
        System.out.println(e.encrypt(test));
    }
}
