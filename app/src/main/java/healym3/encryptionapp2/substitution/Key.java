package healym3.encryptionapp2.substitution;

import android.util.Log;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Key {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private Map<Character, Character> key;
    private Map<Character, Character> keyDecrypt;
    private String keyString;

    public Key() {
        key = new HashMap<Character, Character>();
        keyDecrypt = new HashMap<Character, Character>();
        keyString = "";
    }

    private boolean checkKey(String keyString){
        //Log.d("Key", "checkKey: keyString" + keyString);
        // Key must be as long as alphabet
        if(keyString.length() != ALPHABET.length()) {
            Log.d("Key", "checkKey: key must be as long as alphabet" + keyString.length() + ALPHABET.length());
            return false;
        }
        keyString = keyString.toUpperCase(Locale.ROOT);

        //Key characters must be unique
        Set<Character> keySet = new HashSet<>();
        for(char ch: keyString.toCharArray()){
            keySet.add(ch);
        }
        if (keySet.size() != ALPHABET.length()) {
            Log.d("Key", "checkKey: Key characters must be unique");
            return false;
        }

        Set<Character> alphabetSet = new HashSet<>();
        for(char ch: ALPHABET.toCharArray()){
            alphabetSet.add(ch);
        }
        if (!keySet.equals(alphabetSet)) {
            Log.d("Key", "checkKey: Key must contain characters of alphabet");
            return false;
        }
        //Log.d("Key", "checkKey: keySet" + keySet.toString());


        for (int i = 0; i < ALPHABET.length(); i++) {
            char alphabetChar = ALPHABET.charAt(i);
            char keyChar = keyString.charAt(i);
            //Log.d("Key", "checkKey: " + i + ":" + alphabetChar + ":" + keyChar);
            this.key.put(alphabetChar, keyChar);
            keyDecrypt.put(keyChar, alphabetChar);
        }

        this.keyString = keyString;

        return true;
    }



    public boolean setKey(String key) {
        return checkKey(key);
    }

    public Map<Character, Character> getKey() {
        return key;
    }

    public Map<Character, Character> getKeyDecrypt() {
        return keyDecrypt;
    }

    public void generateKey(){
        ArrayList<Character> alphabetList = new ArrayList<Character>();
        for(char ch: ALPHABET.toCharArray()){
            alphabetList.add(ch);
        }
        //Log.d("Key", "generateKey: alphabetList" + alphabetList.toString());
        Collections.shuffle(alphabetList, new SecureRandom());
        //Log.d("Key", "generateKey: alphabetList after shuffle" + alphabetList.toString());
        StringBuilder stringBuilder = new StringBuilder();
        for (Character ch: alphabetList
             ) {
            stringBuilder.append(ch);
        }
        if(!setKey(stringBuilder.toString())) Log.d("Key", "generateKey: ERROR GENERATING KEY");
    }

    public boolean keyHas(char ch){
        return key.containsKey(ch);
    }

    public char getCipherChar(char ch){
        if(!keyHas(ch)) return ' ';
        else return key.get(ch);
    }

    public boolean decryptKeyHas(char ch){
        return keyDecrypt.containsKey(ch);
    }

    public char getPlainChar(char ch){
        if(!decryptKeyHas(ch)) return ' ';
        else return keyDecrypt.get(ch);
    }

    @Override
    public String toString() {
        return this.keyString;
    }
}
