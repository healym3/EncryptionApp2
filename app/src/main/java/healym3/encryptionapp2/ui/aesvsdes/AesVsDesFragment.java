package healym3.encryptionapp2.ui.aesvsdes;

import androidx.lifecycle.ViewModelProvider;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import healym3.encryptionapp2.R;
import healym3.encryptionapp2.data.FILE_TYPE;
import healym3.encryptionapp2.data.UserFileBitmap;
import healym3.encryptionapp2.data.UserFileCompare;
import healym3.encryptionapp2.databinding.AesVsDesFragmentBinding;

public class AesVsDesFragment extends Fragment {

    private AesVsDesViewModel aesVsDesViewModel;

    private AesVsDesFragmentBinding binding;
    private final int CHOOSE_FILE_FROM_DEVICE = 1090;
    private UserFileCompare userFileCompare;
    private Path encryptionPath;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        aesVsDesViewModel = new ViewModelProvider(this).get(AesVsDesViewModel.class);
        binding = AesVsDesFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        try {
            encryptionPath = Paths.get(requireContext().getFilesDir() + "/AesVsDes");
            Files.createDirectory(encryptionPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        binding.openUserFile.setOnClickListener(view -> chooseFileFromDevice());
        return root;
    }
    @SuppressWarnings("deprecation")
    private void chooseFileFromDevice() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, CHOOSE_FILE_FROM_DEVICE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_FILE_FROM_DEVICE && resultCode == Activity.RESULT_OK) {

            if (data != null) {
                userFileCompare = new UserFileCompare(FILE_TYPE.ORIGINAL, data.getData(), requireContext(), encryptionPath);

                try {
                    userFileCompare.encryptOriginalFile();
                    displayResult();
                } catch (NoSuchAlgorithmException | IOException | IllegalBlockSizeException | InvalidKeyException | BadPaddingException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void displayResult(){
        if(userFileCompare != null){
            binding.aesDurationTextView.setText((int) userFileCompare.getDurationAES() + " ms");
            binding.fileSizeTextView.setText((int) (userFileCompare.getEncryptedAESFileSize()/1024) + "kb");
            binding.desDurationTextView.setText((int) userFileCompare.getDurationDES() + " ms");


        }
    }


}