package healym3.encryptionapp2.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

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

public class UserFile {
    public static final int DEFAULT_BUFFER_SIZE = 8192;

    public static ALGORITHM_CHOICE DEFAULT_ALGORITHM = ALGORITHM_CHOICE.AES_CBC_PADDING;
    protected Algorithm algorithm;
    protected Context context;
    protected String originalFileName;
    protected File filesDir;
    protected File originalFile;
    protected SecretKey key;
    protected IvParameterSpec iv;
    private String encryptedFileName;
    private String ivFileName;
    private File encryptedFile;
    private File ivFile;

//    public void setFilesDir(File filesDir) {
//        this.filesDir = filesDir;
//    }

    public UserFile(FILE_TYPE file_type, Uri uri, Context context) {
        this.context = context;
        this.filesDir = context.getFilesDir();
        this.algorithm = new Algorithm(DEFAULT_ALGORITHM);

        init(file_type, uri, context);
    }

    public UserFile(FILE_TYPE file_type, Uri uri, Context context, Path filesDir) {
        this.context = context;
        this.filesDir = filesDir.toFile();
        Log.d("TAG", "UserFile: filesDir" + filesDir);
        this.algorithm = new Algorithm(DEFAULT_ALGORITHM);

        init(file_type, uri, context);
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
        inputStream.close();


    }

    private void init(FILE_TYPE file_type, Uri uri, Context context) {
        InputStream inputStream;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            switch (file_type) {
                case ORIGINAL:
                    this.originalFileName = getFileNameFromUri(uri);
                    setEncryptedFileNameFromOriginal(this.originalFileName);
                    originalFile = new File(filesDir.getPath() + "/" + originalFileName);
                    try {
                        copyInputStreamToFile(inputStream, originalFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    break;

                case ENCRYPTED:
                    this.encryptedFileName = getFileNameFromUri(uri);
                    setOriginalFileNameFromEncrypted(this.encryptedFileName);
                    encryptedFile = new File(filesDir.getPath() + "/" + encryptedFileName);
                    try {
                        copyInputStreamToFile(inputStream, encryptedFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;

                default:

                    break;

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void importKey(Uri uri) {
        InputStream inputStream;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            byte[] keyBytes = new byte[inputStream.available()];
            if (inputStream.read(keyBytes) != -1) {
                this.key = AES.importKey(keyBytes);
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void importIv(Uri uri) {
        InputStream inputStream;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            byte[] ivBytes = new byte[inputStream.available()];
            if (inputStream.read(ivBytes) != -1) {
                this.iv = AES.importIv(ivBytes);
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    //TODO import key from file

    public File getIvFile() {
        return ivFile;
    }

    public void setIvFile(File ivFile) {
        this.ivFile = ivFile;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getEncryptedFileName() {
        return encryptedFileName;
    }

    public IvParameterSpec getIv() {
        return iv;
    }

    public void setIv(IvParameterSpec iv) {
        this.iv = iv;
    }

    public void generateIv() {
        this.iv = AES.generateIv();
    }

    public SecretKey getKey() {
        return key;
    }

    public void setKey(SecretKey key) {
        this.key = key;
    }

    public File getOriginalFile() {
        return originalFile;
    }

    public File getEncryptedFile() {
        return encryptedFile;
    }

    public String getIvFileName() {
        return ivFileName;
    }

    public void setAlgorithm(ALGORITHM_CHOICE algorithmChoice) {
        algorithm.setAlgorithmChoice(algorithmChoice);
        setEncryptedFileNameFromOriginal(originalFileName);
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
        this.encryptedFileName = original + algorithm.getEncryptedFileExtension();
        setIvFileName();
    }

    private void setOriginalFileNameFromEncrypted(String encrypted) {
        int cut = encrypted.lastIndexOf(".aes");
        this.originalFileName = encrypted.substring(0, cut);
        setIvFileName();
    }

    private void setIvFileName() {
        if (this.encryptedFileName != null) {
            this.ivFileName = encryptedFileName + ".iv";
        }
    }

    public void generateKey() throws NoSuchAlgorithmException {
        key = AES.generateKey(128);

    }

    public void encryptOriginalFile()
            throws NoSuchAlgorithmException, IOException, IllegalBlockSizeException,
            InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException,
            NoSuchPaddingException {

        encryptedFile = new File(this.filesDir + "/" + this.encryptedFileName);
        if (this.key == null) {
            generateKey();
        }
        if (this.iv == null) {
            this.iv = AES.generateIv();
        }


        AES.encryptFile(algorithm.getAlgorithm(), key, iv, originalFile, encryptedFile);
        saveIvToFile();

    }

    public void decryptEncryptedFile() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException {
        originalFile = new File(this.filesDir + "/" + this.originalFileName);
        AES.decryptFile(algorithm.getAlgorithm(), key, iv, encryptedFile, originalFile);
    }

    private void saveIvToFile() {
        if (iv != null) {
            ivFile = new File(this.filesDir + "/" + ivFileName);

            FileOutputStream fileOutputStream;
            try {
                fileOutputStream = new FileOutputStream(ivFile);
                fileOutputStream.write(iv.getIV());
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
