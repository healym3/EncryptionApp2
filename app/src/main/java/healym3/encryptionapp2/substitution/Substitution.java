package healym3.encryptionapp2.substitution;

import java.util.Locale;

public class Substitution {

    private Key key;

    public Substitution(Key key) {
        this.key = key;
    }

    public Substitution() {
    }

    public void setKey(Key key) {
        this.key = key;
    }

    public String encrypt(String text){
        StringBuilder stringBuilder = new StringBuilder();
        text = text.toUpperCase(Locale.ROOT);
        for (char ch: text.toCharArray()
             ) {
            if(key.keyHas(ch)){
                stringBuilder.append(key.getCipherChar(ch));
            }

        }

        return stringBuilder.toString();

    }

    public String decrypt(String text){
        StringBuilder stringBuilder = new StringBuilder();
        text = text.toUpperCase(Locale.ROOT);

        for (char ch: text.toCharArray()
             ) {
            stringBuilder.append(key.getPlainChar(ch));
        }

        return stringBuilder.toString();
    }
}
