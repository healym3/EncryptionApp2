package healym3.encryptionapp2.util;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Utils {
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    public static String getJsonFromAssets(Context context, String fileName) {
        String jsonString;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return jsonString;
    }

    public static void hideSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static String getTextFromFile(Context context, Uri uri) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(context.getContentResolver().openInputStream(uri));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(String.format("%s\n", line));
            }
            bufferedReader.close();
            inputStreamReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return stringBuilder.toString();
    }
}