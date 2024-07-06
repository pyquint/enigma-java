package src.decryption.analysis;

public interface FitnessFunction {

    public double score(String ciphertext);
    public String name();
}
