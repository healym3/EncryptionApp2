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

public class UserFileBitmap {
    public static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    public static final int DEFAULT_BUFFER_SIZE = 8192;
    public static final int BITMAP_HEADER_SIZE = 54;
    private Context context;
    private FILE_TYPE file_type;
    private String originalFileName;
    private String encryptedFileName;
    private File originalFile;
    private File encryptedFile;
    private File appFilesDir;
    private File validEncryptedBitmapFile;
    private byte[] header;
    private SecretKey key;
    private IvParameterSpec iv;

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

    public File getValidEncryptedBitmapFile() {
        return validEncryptedBitmapFile;
    }

    public UserFileBitmap(FILE_TYPE file_type, Uri uri, Context context){
        this.context = context;
        this.file_type = file_type;
        this.appFilesDir = context.getFilesDir();
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
                    if(this.originalFileName.contains(".bmp")){
                        setHeader();
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
        this.encryptedFileName = original + ".encrypted";
    }
    private void setOriginalFileNameFromEncrypted(String encrypted){
        int cut = encrypted.lastIndexOf(".encrypted");
        this.originalFileName = encrypted.substring(0, cut);
    }

    private void setHeader(){

        try {
            InputStream inputStream = new FileInputStream(originalFile);
            header = new byte[BITMAP_HEADER_SIZE];
            int bytesRead = inputStream.read(header);
        } catch (IOException e) {
            e.printStackTrace();
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
        iv = AES.generateIv();

        AES.encryptFile(ALGORITHM, key, iv, originalFile, encryptedFile);
        if(this.originalFileName.contains(".bmp")) createValidBitmapFromEncrypted();

    }

    private void createValidBitmapFromEncrypted(){

        if(header!=null && originalFileName.contains(".bmp")){
            this.validEncryptedBitmapFile = new File(encryptedFile.getPath() + ".bmp");

            try {
                FileInputStream inputStream = new FileInputStream(encryptedFile);
                FileOutputStream outputStream = new FileOutputStream(validEncryptedBitmapFile, false);
                outputStream.write(header);
                //noinspection ResultOfMethodCallIgnored
                inputStream.skip(BITMAP_HEADER_SIZE);
                int read;
                byte[] bytes = new byte[DEFAULT_BUFFER_SIZE];
                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }
                inputStream.close();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
