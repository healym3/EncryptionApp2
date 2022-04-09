package healym3.encryptionapp2.ui.files;

import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import healym3.encryptionapp2.data.FILE_TYPE;
import healym3.encryptionapp2.data.UserFile;
import healym3.encryptionapp2.databinding.FileEncryptionFragmentBinding;

public class FileEncryptionFragment extends Fragment {
    private final int CHOOSE_FILE_FROM_DEVICE = 1020;
    private FileEncryptionViewModel fileEncryptionViewModel;
    private FileEncryptionFragmentBinding binding;
    private UserFile originalFile;
    private UserFile encryptedFile;

//    public static FileEncryptionFragment newInstance() {
//        return new FileEncryptionFragment();
//    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        fileEncryptionViewModel = new ViewModelProvider(this).get(FileEncryptionViewModel.class);
        binding = FileEncryptionFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final Observer<UserFile> originalFileObserver = new Observer<UserFile>() {
            @Override
            public void onChanged(@Nullable final UserFile newOriginalFile) {
                originalFile = newOriginalFile;
                displayFileName();
                displayKeyAndIv();
            }
        };

        fileEncryptionViewModel.getOriginalFile().observe(getViewLifecycleOwner(), originalFileObserver);

        final Observer<UserFile> encryptedFileObserver = new Observer<UserFile>() {
            @Override
            public void onChanged(@Nullable final UserFile newEncryptedFile) {
                encryptedFile = newEncryptedFile;
                displayFileName();
                displayKeyAndIv();
            }
        };

        fileEncryptionViewModel.getOriginalFile().observe(getViewLifecycleOwner(), encryptedFileObserver);

        binding.openFileButtonOriginalFile.setOnClickListener(view -> {
            chooseFileFromDevice();
        });

        binding.generateKeyButton.setOnClickListener(view -> {
            if(originalFile !=null){
                try {
                    originalFile.generateKey();
                    originalFile.generateIv();
                    updateUserKeyViewModel();
                    displayKeyAndIv();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

            }
        });

        binding.saveKeyAESButton.setOnClickListener(view -> {
            saveKeyToFile();
        });

        binding.encryptFileButton.setOnClickListener(view -> encryptFile());

        binding.emailFileButton.setOnClickListener(view -> composeEmail());




        return root;

    }


    public void composeEmail() {
        Uri encryptedFileUri = FileProvider.getUriForFile(requireContext(), "healym3.fileprovider", originalFile.getEncryptedFile());
        Uri ivFileUri = FileProvider.getUriForFile(requireContext(), "healym3.fileprovider", originalFile.getIvFile());
        ArrayList<Uri> attachmentUris = new ArrayList<>(2);
        attachmentUris.add(encryptedFileUri);
        attachmentUris.add(ivFileUri);
        //Intent intent = createEmailIntent(attachment);
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("vnd.android.cursor.dir/email");

        intent.putExtra(Intent.EXTRA_SUBJECT, "EncryptedFile Attached");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM,attachmentUris);
        startActivity(intent);

    }



    private void encryptFile() {
        if (originalFile != null){
            if(originalFile.getKey() != null){
                try {
                    originalFile.encryptOriginalFile();
                } catch (NoSuchAlgorithmException | IOException | IllegalBlockSizeException | InvalidKeyException | BadPaddingException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void displayFileName(){
        if(originalFile !=null){
            binding.fileNameTextViewOriginalFile.setText(originalFile.getOriginalFileName());
        }
        if(encryptedFile!=null){
            binding.fileNameTextViewEncryptedFile.setText(encryptedFile.getEncryptedFileName());
        }
    }

    private void displayKeyAndIv() {
        if(originalFile !=null){
            SecretKey key = originalFile.getKey();
            IvParameterSpec iv = originalFile.getIv();
            if(key != null){
                binding.keyTextViewOriginalFile.setText(Arrays.toString(key.getEncoded()));
            }
            if(iv!=null){
                binding.ivTextViewOriginalFile.setText(Arrays.toString(iv.getIV()));
            }
        }
        if(encryptedFile!=null){
            SecretKey key = encryptedFile.getKey();
            IvParameterSpec iv = encryptedFile.getIv();
            if(key != null){
                binding.keyTextViewEncryptedFile.setText(Arrays.toString(key.getEncoded()));
            }
            if(iv!=null){
                binding.ivTextViewEncryptedFile.setText(Arrays.toString(iv.getIV()));
            }
        }


    }

    private void saveKeyToFile(){

        if(originalFile !=null){
            SecretKey key = originalFile.getKey();
            if(key!=null){
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(requireContext().getFilesDir() + "/AES.key");
                    fileOutputStream.write(key.getEncoded());
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

    private void updateUserKeyViewModel(){
        fileEncryptionViewModel.getOriginalFile().setValue(originalFile);
    }

    private void chooseFileFromDevice(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, CHOOSE_FILE_FROM_DEVICE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CHOOSE_FILE_FROM_DEVICE && resultCode == Activity.RESULT_OK){

            if(data != null){
                //userFile = ;
                fileEncryptionViewModel.getOriginalFile().setValue(new UserFile(FILE_TYPE.ORIGINAL, data.getData(),requireContext()));
                Log.d("TAG", "onActivityResult: " + originalFile.toString());
//                try {
//                    userFile.encryptOriginalFile();
//                } catch (NoSuchAlgorithmException | IOException | IllegalBlockSizeException | InvalidKeyException | BadPaddingException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
//                    e.printStackTrace();
//                }

            }
        }
    }



    @Override
    public void onResume() {
        super.onResume();
        if(originalFile !=null){
            displayFileName();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}