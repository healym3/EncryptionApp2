package healym3.encryptionapp2.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import healym3.encryptionapp2.algorithms.AES;


public class UserFile {
    public static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    private Uri originalUri;
    private Path appStoragePath;
    private Path encryptedFilePath;
    private String filename;
    private Context context;

    public UserFile() {
    }

    public UserFile(Uri originalUri, Context context) {
        this.originalUri = originalUri;
        this.context = context;
        getFileName(originalUri);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(originalUri);
            appStoragePath = Paths.get(context.getFilesDir().getPath() + "/" + filename);

            File originalFile = new File(String.valueOf(appStoragePath));
            try {
                copyInputStreamToFile(inputStream,originalFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(index);
                }
            } finally {
                Objects.requireNonNull(cursor).close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        this.filename = result;
    }
    private static void copyInputStreamToFile(InputStream inputStream, File file)
            throws IOException {

        // append = false
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }


    }

    public Uri getOriginalUri() {
        return originalUri;
    }

    public File getOriginalFile(){

        return new File(String.valueOf(appStoragePath));
    }

    public void encryptOriginalFile()
            throws NoSuchAlgorithmException, IOException, IllegalBlockSizeException,
            InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException,
            NoSuchPaddingException {
        File originalFile = getOriginalFile();
        SecretKey key = AES.generateKey(128);
        IvParameterSpec ivParameterSpec = AES.generateIv();
        encryptedFilePath = Paths.get(appStoragePath + ".encrypted");
        File encryptedFile = new File(String.valueOf(encryptedFilePath));
        AES.encryptFile(ALGORITHM, key, ivParameterSpec, originalFile, encryptedFile);

    }

    @Override
    public String toString() {
        return "UserFile{" +
                "originalUri=" + originalUri +
                ", appStoragePath=" + appStoragePath +
                ", encryptedFilePath=" + encryptedFilePath +
                ", filename='" + filename + '\'' +
                ", context=" + context +
                '}';
    }
}
