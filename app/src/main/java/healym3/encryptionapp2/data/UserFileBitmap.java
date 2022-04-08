package healym3.encryptionapp2.data;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import healym3.encryptionapp2.algorithms.AES;

public class UserFileBitmap extends UserFile{
    public static final int BITMAP_HEADER_SIZE = 54;
    public static final String TAG = "UserFileBitmap";
    private File validCbcEncryptedBitmap;
    private File validEcbEncryptedBitmap;
    private File encryptedEcbFile;
    private String encryptedEcbFilename;
    private byte[] header;
    private String encryptedCBCFileName;

    private File encryptedCBCFile;

    public UserFileBitmap(Uri uri, Context context) {
        super(FILE_TYPE.ORIGINAL, uri, context);
        setHeader();
    }

    public File getValidCbcEncryptedBitmap() {
        return validCbcEncryptedBitmap;
    }

    public File getValidEcbEncryptedBitmap() {
        return validEcbEncryptedBitmap;
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

    private void setEncryptedFileNameFromOriginal(String original, MODE mode){
        switch(mode){
            case ECB:
                this.encryptedEcbFilename = original + ".ecb";

                break;

            case CBC:
                this.encryptedCBCFileName = original + ".cbc";
                break;

            default:

                break;
        }
        Log.d(TAG, "setEncryptedFileNameFromOriginal: " + mode + this.algorithm.getAlgorithm());
    }

    private void setAlgorithm(ALGORITHM_CHOICE algorithmChoice, MODE mode){
        algorithm.setAlgorithmChoice(algorithmChoice);
        setEncryptedFileNameFromOriginal(originalFileName, mode);
    }

    @Override
    public void encryptOriginalFile() throws NoSuchAlgorithmException, IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        if(this.key == null){
            generateKey();
        }
        iv = AES.generateIv();
        this.setAlgorithm(ALGORITHM_CHOICE.AES_CBC_PADDING, MODE.CBC);
        encryptedCBCFile = new File(this.appFilesDir + "/" + this.encryptedCBCFileName);
        AES.encryptFile(algorithm.getAlgorithm(), key, iv, originalFile, encryptedCBCFile);

        this.setAlgorithm(ALGORITHM_CHOICE.AES_ECB_PADDING, MODE.ECB);
        encryptedEcbFile = new File(this.appFilesDir + "/" + this.encryptedEcbFilename);
        AES.encryptFile(algorithm.getAlgorithm(), key, iv, originalFile, encryptedEcbFile);

        createValidBitmapFromEncrypted();
    }

    private void createValidBitmapFromEncrypted(){

        if(header!=null){
            this.validCbcEncryptedBitmap = new File(encryptedCBCFile.getPath() + ".bmp");
            this.validEcbEncryptedBitmap = new File(encryptedEcbFile.getPath() + ".bmp");

            try {
                FileInputStream inputStream = new FileInputStream(encryptedCBCFile);
                FileOutputStream outputStream = new FileOutputStream(validCbcEncryptedBitmap, false);
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
            try {
                FileInputStream inputStream = new FileInputStream(encryptedEcbFile);
                FileOutputStream outputStream = new FileOutputStream(validEcbEncryptedBitmap, false);
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

    private enum MODE {
        CBC,
        ECB
    }
}
