package healym3.encryptionapp2.data;

import java.util.List;


public class Quadgram {
    String alphabet;
    int numberQuadgrams;
    String mostFrequentQuadgram;
    int maxFitness;
    double avgFitness;
    List<Integer> quadgrams;

    public String getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(String alphabet) {
        this.alphabet = alphabet;
    }

    public int getNumberQuadgrams() {
        return numberQuadgrams;
    }

    public void setNumberQuadgrams(int numberQuadgrams) {
        this.numberQuadgrams = numberQuadgrams;
    }

    public String getMostFrequentQuadgram() {
        return mostFrequentQuadgram;
    }

    public void setMostFrequentQuadgram(String mostFrequentQuadgram) {
        this.mostFrequentQuadgram = mostFrequentQuadgram;
    }

    public int getMaxFitness() {
        return maxFitness;
    }

    public void setMaxFitness(int maxFitness) {
        this.maxFitness = maxFitness;
    }

    public double getAvgFitness() {
        return avgFitness;
    }

    public void setAvgFitness(double avgFitness) {
        this.avgFitness = avgFitness;
    }

    public List<Integer> getQuadgrams() {

        return quadgrams;
    }

    public void setQuadgrams(List<Integer> quadgrams) {
        this.quadgrams = quadgrams;
    }

    @Override
    public String toString() {
        return "Quadgram{" +
                "alphabet='" + alphabet + '\'' +
                ", numberQuadgrams=" + numberQuadgrams +
                ", mostFrequentQuadgram='" + mostFrequentQuadgram + '\'' +
                ", maxFitness=" + maxFitness +
                ", avgFitness=" + avgFitness +
                ", quadgrams=" + quadgrams +
                '}';
    }
}
