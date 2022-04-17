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
import healym3.encryptionapp2.algorithms.DES;

public class UserFileCompare {
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    //public static ALGORITHM_CHOICE DEFAULT_ALGORITHM = ALGORITHM_CHOICE.;
    private Algorithm algorithmAES;
    private Algorithm algorithmDES;
    private Context context;
    private String originalFileName;
    private File filesDir;
    private File originalFile;
    private SecretKey keyAES;
    private SecretKey keyDES;
    private IvParameterSpec iv;
    private String encryptedFileNameAES;
    private String encryptedFileNameDES;

    private File encryptedFileAES;
    private File encryptedFileDES;

    private long durationAES;
    private long durationDES;
    private long originalFileSize;
    private long encryptedAESFileSize;
    private long encryptedDESFileSize;

    public UserFileCompare(FILE_TYPE file_type, Uri uri, Context context) {
        this.context = context;
        this.filesDir = context.getFilesDir();
        this.algorithmAES = new Algorithm(ALGORITHM_CHOICE.AES_ECB_PADDING);
        this.algorithmDES = new Algorithm(ALGORITHM_CHOICE.DES_ECB_PADDING);

        init(file_type, uri, context);
    }

    public UserFileCompare(FILE_TYPE file_type, Uri uri, Context context, Path filesDir) {
        this.context = context;
        this.filesDir = filesDir.toFile();

        this.algorithmAES = new Algorithm(ALGORITHM_CHOICE.AES_ECB_PADDING);
        this.algorithmDES = new Algorithm(ALGORITHM_CHOICE.DES_ECB_PADDING);

        init(file_type, uri, context);
    }

    private static void copyInputStreamToFile(InputStream inputStream, File file)
            throws IOException {


        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
        inputStream.close();


    }

    private void init(FILE_TYPE file_type, Uri uri, Context context) {
        InputStream inputStream;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            this.originalFileName = getFileNameFromUri(uri);
            setEncryptedFileNameFromOriginal(this.originalFileName);
            originalFile = new File(filesDir.getPath() + "/" + originalFileName);
            try {
                copyInputStreamToFile(inputStream, originalFile);
                this.originalFileSize = originalFile.length();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    private String getFileNameFromUri(Uri uri) {
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
        return result;
    }

    private void setEncryptedFileNameFromOriginal(String original) {
        this.encryptedFileNameAES = original + algorithmAES.getEncryptedFileExtension();
        this.encryptedFileNameDES = original + algorithmDES.getEncryptedFileExtension();
    }



    public void generateKey() throws NoSuchAlgorithmException {
        keyAES = AES.generateKey(128);
        keyDES = DES.generateKey(56);
    }

    public void encryptOriginalFile()
            throws NoSuchAlgorithmException, IOException, IllegalBlockSizeException,
            InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException,
            NoSuchPaddingException {
        generateKey();
        encryptedFileAES = new File(this.filesDir + "/" + this.encryptedFileNameAES);
        encryptedFileDES = new File(this.filesDir + "/" + this.encryptedFileNameDES);

        if (this.iv == null) {
            this.iv = AES.generateIv();
        }
        long startTime, endTime;

        startTime = System.currentTimeMillis();
        AES.encryptFile(algorithmAES.getAlgorithm(), keyAES, iv, originalFile, encryptedFileAES);
        endTime = System.currentTimeMillis();
        this.durationDES = endTime - startTime;
        this.encryptedAESFileSize = encryptedFileAES.length();
        startTime = System.currentTimeMillis();
        DES.encryptFile(algorithmDES.getAlgorithm(), keyDES, iv, originalFile, encryptedFileDES);
        endTime = System.currentTimeMillis();
        this.durationAES = endTime - startTime;
        this.encryptedDESFileSize = encryptedFileDES.length();





    }

    public long getDurationAES() {
        return durationAES;
    }

    public long getDurationDES() {
        return durationDES;
    }

    public long getOriginalFileSize() {
        return originalFileSize;
    }

    public long getEncryptedAESFileSize() {
        return encryptedAESFileSize;
    }

    public long getEncryptedDESFileSize() {
        return encryptedDESFileSize;
    }
}
