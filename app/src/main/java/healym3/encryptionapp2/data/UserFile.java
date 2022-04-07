package healym3.encryptionapp2.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Locale;
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
    public static final int BITMAP_HEADER_SIZE = 54;
    public static final String TAG = "UserFile";
    private final Uri originalUri;
    private Path appStoragePath;
    private Path encryptedFilePath;
    private String encryptedFileName;
    private Path validEncryptedBitmapFilePath;
    private String fileName;
    private final Context context;
    private String extension;
    private byte[] header;
    private SecretKey key;
    private IvParameterSpec iv;

    public Path getEncryptedFilePath() {
        return encryptedFilePath;
    }

    public String getFileName() {
        return fileName;
    }

    public UserFile(Uri originalUri, Context context) {
        this.originalUri = originalUri;
        this.context = context;
        setFileName(originalUri);
        setExtension();
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(originalUri);
            appStoragePath = Paths.get(context.getFilesDir().getPath() + "/" + fileName);

            File originalFile = new File(String.valueOf(appStoragePath));

            try {
                copyInputStreamToFile(inputStream,originalFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if(extension.contains("bmp")){
            setHeader();
        }
    }


    private void setHeader(){

        try {
            InputStream inputStream = new FileInputStream(String.valueOf(appStoragePath));
            header = new byte[BITMAP_HEADER_SIZE];
            int bytesRead = inputStream.read(header);
            if(bytesRead != -1){
                StringBuilder stringBuilder = new StringBuilder();
                for (byte b: header
                     ) {
                    stringBuilder.append(b).append(", ");
                }
                Log.d(TAG, "setHeader: " + stringBuilder);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setExtension(){
        int cut = fileName.lastIndexOf('.');
        if(cut!=-1){
            this.extension = fileName.substring(cut+1).toLowerCase(Locale.ROOT);
        }
    }

    private void setFileName(Uri uri) {
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
        this.fileName = result;
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

    public File getFile(Path path){

        return new File(String.valueOf(path));
    }

    public Path getValidEncryptedBitmapFilePath() {
        if(this.extension.contains("bmp"))
            return validEncryptedBitmapFilePath;
        else
            return Paths.get("");
    }

    public void generateKey() throws NoSuchAlgorithmException {
        key = AES.generateKey(128);
    }

    public void encryptOriginalFile()
            throws NoSuchAlgorithmException, IOException, IllegalBlockSizeException,
            InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException,
            NoSuchPaddingException {
        File originalFile = getFile(appStoragePath);
        if(this.key == null){
            generateKey();
        }
        iv = AES.generateIv();
        encryptedFilePath = Paths.get(appStoragePath + ".encrypted");
        File encryptedFile = new File(String.valueOf(encryptedFilePath));
        AES.encryptFile(ALGORITHM, key, iv, originalFile, encryptedFile);
        if(this.extension.contains("bmp")) createValidBitmapFromEncrypted();

        this.encryptedFileName = encryptedFile.getName();
    }

    public IvParameterSpec getIv() {
        return iv;
    }

    private void createValidBitmapFromEncrypted(){
        Log.d(TAG, "createValidBitmapFromEncrypted: " + extension + " " + Arrays.toString(header));
        if(header!=null && extension.contains("bmp")){
            validEncryptedBitmapFilePath = Paths.get(encryptedFilePath + ".bmp");
            Log.d(TAG, "createValidBitmapFromEncrypted: " + validEncryptedBitmapFilePath.toString());
            File validBitmapFile = getFile(validEncryptedBitmapFilePath);
            File encryptedFile = getFile(encryptedFilePath);

            try {
                FileInputStream inputStream = new FileInputStream(encryptedFile);
                FileOutputStream outputStream = new FileOutputStream(validBitmapFile, false);
                outputStream.write(header);
                //noinspection ResultOfMethodCallIgnored
                inputStream.skip(BITMAP_HEADER_SIZE);
                int read;
                byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @NonNull
    @Override
    public String toString() {
        return "UserFile{" +
                "originalUri=" + originalUri +
                ", appStoragePath=" + appStoragePath +
                ", encryptedFilePath=" + encryptedFilePath +
                ", filename='" + fileName + '\'' +
                ", context=" + context +
                '}';
    }

    public SecretKey getKey() {
        return key;
    }

    public String getEncryptedFileName() {
        return encryptedFileName;
    }

    public void setKey(SecretKey key) {
        this.key = key;
    }
}
