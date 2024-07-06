package src.decryption.key;

public sealed interface BaseKey permits EnigmaKey, ScoredKey {
    String[] wheels();
    int[] rings();
    int[] positions();
    String[] pairs();
}
