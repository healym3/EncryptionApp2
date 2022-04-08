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

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

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
                displayKey();
            }
        };

        fileEncryptionViewModel.getOriginalFile().observe(getViewLifecycleOwner(), originalFileObserver);

        final Observer<UserFile> encryptedFileObserver = new Observer<UserFile>() {
            @Override
            public void onChanged(@Nullable final UserFile newEncryptedFile) {
                encryptedFile = newEncryptedFile;
                displayFileName();
                displayKey();
            }
        };

        fileEncryptionViewModel.getOriginalFile().observe(getViewLifecycleOwner(), encryptedFileObserver);

        binding.openFileButton.setOnClickListener(view -> {
            chooseFileFromDevice();
        });

        binding.generateKeyButton.setOnClickListener(view -> {
            if(originalFile !=null){
                try {
                    originalFile.generateKey();
                    updateUserKeyViewModel();
                    displayKey();
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
        Uri attachment = FileProvider.getUriForFile(requireContext(), "healym3.fileprovider", originalFile.getEncryptedFile());
        Intent intent = createEmailIntent(attachment);
        startActivity(intent);

    }

    private Intent createEmailIntent(Uri uri){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("vnd.android.cursor.dir/email");

        intent.putExtra(Intent.EXTRA_SUBJECT, "EncryptedFile Attached");
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        return intent;
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
            binding.fileNameTextView.setText(originalFile.getOriginalFileName());
        }
    }

    private void displayKey() {
        if(originalFile !=null){
            SecretKey key = originalFile.getKey();
            if(key != null){
                StringBuilder sb = new StringBuilder();
                for (byte b: key.getEncoded()
                ) {
                    sb.append(b).append(", ");
                }
                binding.keyTextViewFileEncryption.setText(sb);
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
                    byte[] keyBytes = key.getEncoded();
                    fileOutputStream.write(keyBytes);
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