package healym3.encryptionapp2.ui.aes;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import healym3.encryptionapp2.algorithms.AES;
import healym3.encryptionapp2.databinding.FragmentAesBinding;
import healym3.encryptionapp2.util.Utils;

public class AesFragment extends Fragment {

    public static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private final int CHOOSE_BMP_FROM_DEVICE = 1000;
    private final String algorithm = "AES/ECB/PKCS5Padding";
    private Uri imageUriOriginal;
    private FragmentAesBinding binding;
    private SecretKey key;
    private Bitmap originalBmp;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AesViewModel AESViewModel =
                new ViewModelProvider(this).get(AesViewModel.class);

        binding = FragmentAesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.buttonLoadBitmap.setOnClickListener(view -> {
            chooseBmpFromDevice();
            if (imageUriOriginal!=null){

            }



        });
        try {
            getKey();
            StringBuilder sb = new StringBuilder();
            for (byte b: key.getEncoded()
                 ) {
                sb.append(b + ", ");
            }
            binding.keyTextView2.setText(sb);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        //final TextView textView = binding.textNotifications;
        //AESViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    private void encryptBitmap() throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        IvParameterSpec ivParameterSpec = AES.generateIv();
        File inputFile = new File(imageUriOriginal.getPath());
        File encryptedFile = new File(inputFile.getPath() + ".encrypted");
        //File decryptedFile = new File("document.decrypted");
        AES.encryptFile(algorithm, key, ivParameterSpec, inputFile, encryptedFile);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void getKey() throws NoSuchAlgorithmException {
        key = AES.generateKey(128);
    }

    private void chooseBmpFromDevice(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, CHOOSE_BMP_FROM_DEVICE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CHOOSE_BMP_FROM_DEVICE && resultCode == Activity.RESULT_OK){

            if(data != null){
                imageUriOriginal = data.getData();
//                Log.d("TAG", "onActivityResult: " + imageUriOriginal.getPath());

                Glide.with(getContext())
                        .load(imageUriOriginal)
                        .fitCenter()
                        .into(binding.imageViewOriginal);

                try {
                    InputStream inputStream = getContext().getContentResolver().openInputStream(data.getData());
                    File originalBmpFile = new File(getContext().getFilesDir().getPath()+"/originalBmp.bmp");
                    try {
                        Utils.copyInputStreamToFile(inputStream,originalBmpFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                File originalBmpFile = new File(getContext().getFilesDir().getPath()+"/originalBmp.bmp");
                try {
                    encryptOriginalFile(originalBmpFile);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    void encryptOriginalFile(File inputFile)
            throws NoSuchAlgorithmException, IOException, IllegalBlockSizeException,
            InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException,
            NoSuchPaddingException {

        SecretKey key = AES.generateKey(128);
        IvParameterSpec ivParameterSpec = AES.generateIv();

        File encryptedFile = new File(inputFile.getPath() + ".encrypted");
        AES.encryptFile(ALGORITHM, key, ivParameterSpec, inputFile, encryptedFile);


    }

}