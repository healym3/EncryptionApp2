package healym3.encryptionapp2.data;

import androidx.annotation.NonNull;

import java.util.HashMap;

public class Algorithm {
    private ALGORITHM_CHOICE algorithmChoice;
    private String algorithm;
    private String encryptedFileExtension;
    private final HashMap<ALGORITHM_CHOICE, String> algorithmChoiceToStringMap;
    private final HashMap<ALGORITHM_CHOICE, String> algorithmChoiceToFileExtension;

    public Algorithm(ALGORITHM_CHOICE algorithmChoice){
        this.algorithmChoice = algorithmChoice;

        algorithmChoiceToStringMap = new HashMap<>();
        algorithmChoiceToStringMap.put(ALGORITHM_CHOICE.AES_ECB_PADDING, "AES/ECB/PKCS5Padding");
        algorithmChoiceToStringMap.put(ALGORITHM_CHOICE.AES_ECB_NO_PADDING, "AES/ECB/NoPadding");
        algorithmChoiceToStringMap.put(ALGORITHM_CHOICE.AES_CBC_PADDING, "AES/CBC/PKCS5Padding");
        algorithmChoiceToStringMap.put(ALGORITHM_CHOICE.AES_CBC_NO_PADDING, "AES/CBC/NoPadding");
        algorithmChoiceToStringMap.put(ALGORITHM_CHOICE.DES_ECB_PADDING, "DES/ECB/PKCS5Padding");

        algorithmChoiceToFileExtension = new HashMap<>();
        algorithmChoiceToFileExtension.put(ALGORITHM_CHOICE.AES_ECB_PADDING, ".aes.ecbpad.encrypted");
        algorithmChoiceToFileExtension.put(ALGORITHM_CHOICE.AES_ECB_NO_PADDING,".aes.ecb.encrypted");
        algorithmChoiceToFileExtension.put(ALGORITHM_CHOICE.AES_CBC_PADDING,".aes.cbcpad.encrypted");
        algorithmChoiceToFileExtension.put(ALGORITHM_CHOICE.AES_CBC_NO_PADDING,".aes.cbc.encrypted");
        algorithmChoiceToFileExtension.put(ALGORITHM_CHOICE.DES_ECB_PADDING, ".des.ecb.encrypted");

        setAlgorithm();

    }

    public void setAlgorithmChoice(ALGORITHM_CHOICE algorithmChoice) {
        this.algorithmChoice = algorithmChoice;
        setAlgorithm();
    }

    private void setAlgorithm(){
        this.algorithm = algorithmChoiceToStringMap.get(this.algorithmChoice);
        this.encryptedFileExtension = algorithmChoiceToFileExtension.get(this.algorithmChoice);
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getEncryptedFileExtension() {
        return encryptedFileExtension;
    }

    @NonNull
    @Override
    public String toString() {
        return this.algorithm;
    }
}
