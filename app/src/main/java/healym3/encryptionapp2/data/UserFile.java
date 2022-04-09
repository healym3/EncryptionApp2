package healym3.encryptionapp2.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public static ALGORITHM_CHOICE DEFAULT_ALGORITHM = ALGORITHM_CHOICE.AES_CBC_PADDING;
    protected Algorithm algorithm;
    protected Context context;
    private FILE_TYPE file_type;
    protected String originalFileName;
    private String encryptedFileName;
    private String ivFileName;

    private File encryptedFile;
    protected File appFilesDir;
    protected File originalFile;
    private File ivFile;

    protected SecretKey key;
    protected IvParameterSpec iv;

    public File getIvFile() {
        return ivFile;
    }

    public void setIvFile(File ivFile) {
        this.ivFile = ivFile;
    }
    //TODO import key from file

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

    public void generateIv(){
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

    public void setAlgorithm(ALGORITHM_CHOICE algorithmChoice){
        algorithm.setAlgorithmChoice(algorithmChoice);
        setEncryptedFileNameFromOriginal(originalFileName);
    }

    public UserFile(FILE_TYPE file_type, Uri uri, Context context){
        this.context = context;
        this.file_type = file_type;
        this.appFilesDir = context.getFilesDir();
        this.algorithm = new Algorithm(DEFAULT_ALGORITHM);
        this.iv = AES.generateIv();

        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            switch(file_type){
                case ORIGINAL:
                    this.originalFileName = getFileNameFromUri(uri);
                    setEncryptedFileNameFromOriginal(this.originalFileName);
                    originalFile = new File(appFilesDir.getPath() + "/" + originalFileName );
                    try {
                        copyInputStreamToFile(inputStream,originalFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    break;

                case ENCRYPTED:
                    this.encryptedFileName = getFileNameFromUri(uri);
                    setOriginalFileNameFromEncrypted(this.encryptedFileName);
                    encryptedFile = new File(appFilesDir.getPath() + "/" + encryptedFileName);
                    try {
                        copyInputStreamToFile(inputStream,encryptedFile);
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

    private void setEncryptedFileNameFromOriginal(String original){
        this.encryptedFileName = original + algorithm.getEncryptedFileExtension();
        setIvFileName();
    }
    private void setOriginalFileNameFromEncrypted(String encrypted){
        int cut = encrypted.lastIndexOf(".aes");
        this.originalFileName = encrypted.substring(0, cut);
        setIvFileName();
    }

    private void setIvFileName(){
        if(this.encryptedFileName!=null){
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

        encryptedFile = new File(this.appFilesDir + "/" + this.encryptedFileName);
        if(this.key == null){
            generateKey();
        }


        AES.encryptFile(algorithm.getAlgorithm(), key, iv, originalFile, encryptedFile);
        saveIvToFile();

    }

    private void saveIvToFile(){
        if(iv!=null){
            ivFile = new File(this.appFilesDir + "/" + ivFileName);

            FileOutputStream fileOutputStream = null;
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
