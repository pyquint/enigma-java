package src.machine;

import src.fitness.IoC;
import src.fitness.Ngram;
import src.fitness.ScoredEnigmaKey;

import java.util.*;

public class Decryptor {
    // Gillogly's method
    // Phase 1: Get the best wheel & rotor position (IoC)
    // Phase 2: Get the best ring setting, using Phase 1 setting (IoC)
    // Phase 3: Get the best plug setting, using Phase 2 setting (trigrams)
    // NOTE: Position and ring setting in his paper is one-indexed (+1).

    // William's method, based on Gillogly
    // Phase 1: Get the 3,000 best wheel & rotor position
    // Phase 2: Get the best ring setting using each Phase 1 settings
    // Phase 3: Get the best plug setting using Phase 2 settings

    private final String ciphertext;
    private final Ngram ngram;

    public Decryptor(String ciphertext, int ngram) {
        this.ciphertext = clean(ciphertext);
        try {
            this.ngram = new Ngram(ngram);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static String clean(String text) {
        return text
                .toUpperCase()
                .chars()
                .filter(c -> c >= 'A' && c <= 'Z')
                .collect(
                        StringBuilder::new,
                        StringBuilder::appendCodePoint,
                        StringBuilder::append)
                .toString();
    }

    /**
     * The canonical main method to run.
     * <p>
     * Calling this is similar to:
     * <p>
     * {@code bestKeysWithPlugboard( bestRingSettingKeys( bestWheelOrderAndRotorPositionKeys() ))}
     * <p>
     * but immediately prints out all cracked keys, the last one being the best attempt.
     * </p>
     */
    public void decrpyt() {
        var bestWheelAndPositions = bestWheelOrderAndRotorPositionKeys();
        System.out.println();
        var bestRingSettings = bestRingSettingKeys(bestWheelAndPositions);
        System.out.println();
        var bestKeys = bestKeysWithPlugboard(bestRingSettings);
        System.out.println();

        int i = 0;
        for (ScoredEnigmaKey key : bestKeys) {
            Enigma machine = new Enigma(key);
            System.out.println("CRACK ATTEMPT #" + ++i + " // KEY = " + key);
            System.out.println(machine.encrypt(ciphertext) + "\n");
        }
    }

    public Collection<ScoredEnigmaKey> bestWheelOrderAndRotorPositionKeys() {
        List<ScoredEnigmaKey> bestWheelAndPosKeys = new ArrayList<>();

        Enigma machine = Enigma.createDefault();
        String[] wheels = Enigma.Wheels(5);

        for (String w1 : wheels) {

            for (String w2 : wheels) {
                if (w2.equals(w1)) continue;

                for (String w3 : wheels) {
                    if (w3.equals(w2) || w3.equals(w1)) continue;

                    System.out.println(w1 + " " + w2 + " " + w3);
                    machine.setWheels(w1, w2, w3);
                    double perWheelBoundingScore = minScore();

                    for (int p1 = 0; p1 < 26; p1++) {
                        for (int p2 = 0; p2 < 26; p2++) {
                            for (int p3 = 0; p3 < 26; p3++) {

                                machine.setPositions(p1, p2, p3);

                                EnigmaKey snapshotKey = machine.getEnigmaKeu();
                                String attempt = machine.encrypt(ciphertext, "");
                                double score = IoC.fitness(attempt);

                                if (score > perWheelBoundingScore) {
                                    perWheelBoundingScore = score;
                                    ScoredEnigmaKey crackedWheelAndPos = new ScoredEnigmaKey(snapshotKey, score);
                                    System.out.println("WHEEL_AND_POS: " + crackedWheelAndPos);
                                    bestWheelAndPosKeys.add(crackedWheelAndPos);
                                }
                            }
                        }
                    }
                }
            }
        }

        Collections.sort(bestWheelAndPosKeys);
        return bestWheelAndPosKeys;
    }

    public Collection<ScoredEnigmaKey> bestRingSettingKeys(Collection<ScoredEnigmaKey> keys) {
        List<ScoredEnigmaKey> bestRingKeys = new ArrayList<>();

        for (ScoredEnigmaKey key : keys) {
            ScoredEnigmaKey crackedRingKey = crackedRingSetting(crackedRingSetting(key, 2), 1);
            System.out.println("RING: " + crackedRingKey);
            bestRingKeys.add(crackedRingKey);
        }

        Collections.sort(bestRingKeys);
        return bestRingKeys;
    }

    private ScoredEnigmaKey crackedRingSetting(ScoredEnigmaKey key, int rotorIndex) {
        Enigma machine = new Enigma(key);

        int[] ringSetting = key.rings;
        int[] rotorPosition = key.positions;

        ScoredEnigmaKey bestRingPositionKey = key;

        double boundingScore = minScore();

        for (int i = 0; i < 26; i++) {
            machine.setRingSettings(ringSetting);
            machine.setPositions(rotorPosition);

            EnigmaKey snapshotKey = machine.getEnigmaKeu();
            String attempt = machine.encrypt(ciphertext, "");
            double score = IoC.fitness(attempt);

            if (score > boundingScore) {
                boundingScore = score;
                bestRingPositionKey = new ScoredEnigmaKey(snapshotKey, score);
            }

            ringSetting[rotorIndex]++;
            rotorPosition[rotorIndex] = Math.abs(Math.floorMod(rotorPosition[rotorIndex] + 1, 26));
        }

        return bestRingPositionKey;
    }

    public Collection<ScoredEnigmaKey> bestKeysWithPlugboard(Collection<ScoredEnigmaKey> keys) {
        List<ScoredEnigmaKey> bestPlugboardKeys = new ArrayList<>();

        for (ScoredEnigmaKey key : keys) {
            ScoredEnigmaKey crackedKey = bestKeysWithPlugboard(key);
            bestPlugboardKeys.add(crackedKey);
            System.out.println("CRACKED_KEY_W_PLUGS: " + crackedKey);
        }

        Collections.sort(bestPlugboardKeys);
        return bestPlugboardKeys;
    }

    public ScoredEnigmaKey bestKeysWithPlugboard(EnigmaKey key) {
        Enigma machine = new Enigma(key);

        String currentDecryption = machine.encrypt(ciphertext, "");
        machine.resetPositions();

        ArrayList<String> plugboardPairs = new ArrayList<>(Arrays.asList(key.pairs));
        double boundingPlugboardScore = ngram.score(currentDecryption);
        ScoredEnigmaKey bestPlugboardKey = new ScoredEnigmaKey(key, boundingPlugboardScore);

        for (int i = 0; i < 7; i++) {

            String bestPair = null;
            String checked = String.join("", plugboardPairs);
            double boundingPairsScore = boundingPlugboardScore;

            for (char p1 = 'a'; p1 <= 'z'; p1++) {
                if (checked.indexOf(p1) != -1) continue;

                for (char p2 = 'a'; p2 <= 'z'; p2++) {
                    if (p1 == p2 || checked.indexOf(p2) != -1) continue;

                    String pair = p1 + "" + p2;


                    plugboardPairs.add(pair);
                    machine.setPlugboard(plugboardPairs);

                    String attempt = machine.encrypt(ciphertext, "");
                    machine.resetPositions();

                    double score = ngram.score(attempt);

                    if (score > boundingPairsScore) {
                        bestPair = pair;
                        boundingPairsScore = score;
                        bestPlugboardKey = new ScoredEnigmaKey(machine.getEnigmaKeu(), score);
                        machine.resetPlugboard();
                    }

                    plugboardPairs.remove(pair);
                }
            }

            if (bestPair != null && boundingPairsScore > boundingPlugboardScore) {
                boundingPlugboardScore = boundingPairsScore;
                plugboardPairs.add(bestPair);
            } else {
                break;
            }
        }

        return bestPlugboardKey;
    }


    protected static double minScore() {
        return Double.NEGATIVE_INFINITY;
    }
}
