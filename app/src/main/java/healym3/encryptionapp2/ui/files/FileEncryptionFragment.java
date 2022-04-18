package healym3.encryptionapp2.ui.files;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import healym3.encryptionapp2.data.IVorKeyImportResult;
import healym3.encryptionapp2.data.UserFile;
import healym3.encryptionapp2.databinding.FileEncryptionFragmentBinding;
import healym3.encryptionapp2.util.Utils;

@SuppressWarnings("deprecation")
public class FileEncryptionFragment extends Fragment {
    public static final String TAG = "FileEncryptionFragment";
    private final int CHOOSE_ORIGINAL_FILE_FROM_DEVICE = 1020;
    private final int CHOOSE_ORIGINAL_KEY_FROM_DEVICE = 1021;
    private final int CHOOSE_ENCRYPTED_KEY_FROM_DEVICE = 1022;
    private final int CHOOSE_IV_FROM_DEVICE = 1023;
    private final int CHOOSE_ENCRYPTED_FILE_FROM_DEVICE = 1024;
    private FileEncryptionViewModel fileEncryptionViewModel;
    private FileEncryptionFragmentBinding binding;
    private UserFile originalFile;
    private UserFile encryptedFile;
    private Path originalPath, encryptedPath;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        fileEncryptionViewModel = new ViewModelProvider(this).get(FileEncryptionViewModel.class);
        binding = FileEncryptionFragmentBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        try {
            originalPath = Paths.get(requireContext().getFilesDir() + "/original");
            encryptedPath = Paths.get(requireContext().getFilesDir() + "/encrypted");
            Files.createDirectory(originalPath);
            Files.createDirectory(encryptedPath);
        } catch (IOException e) {
            e.printStackTrace();
        }


        final Observer<UserFile> originalFileObserver = newOriginalFile -> {
            originalFile = newOriginalFile;
            displayFileName();
            displayKeyAndIv();
        };

        fileEncryptionViewModel.getOriginalFile().observe(getViewLifecycleOwner(), originalFileObserver);

        final Observer<UserFile> encryptedFileObserver = newEncryptedFile -> {
            encryptedFile = newEncryptedFile;
            displayFileName();
            displayKeyAndIv();
        };

        fileEncryptionViewModel.getEncryptedFile().observe(getViewLifecycleOwner(), encryptedFileObserver);

        binding.openFileButtonOriginalFile.setOnClickListener(view -> chooseOriginalFileFromDevice());

        binding.generateKeyButton.setOnClickListener(view -> {
            if (originalFile != null) {
                try {
                    originalFile.generateKey();
                    originalFile.generateIv();
                    updateUserKeyViewModel();
                    displayKeyAndIv();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

            }
            else{
                Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonOriginalFile, "No file has been opened yet.");
            }
        });

        binding.saveKeyAESButton.setOnClickListener(view -> {
            if (originalFile != null) {
                saveKeyToFile();
            }
            else{
                Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonOriginalFile, "No file open or key loaded.");
            }
        });

        binding.encryptFileButton.setOnClickListener(view -> {
            if (originalFile != null) {
                if (originalFile.getKey() != null) {
                    encryptFile();
                }
            }
            else{
                Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonOriginalFile, "No file has been opened yet.");
            }
        });

        binding.emailFileButton.setOnClickListener(view -> {
            if (originalFile != null) {
                composeEmail();
            }
            else{
                Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonOriginalFile, "No file has been opened yet.");
            }
        });

        binding.importKeyButtonOriginalFile.setOnClickListener(view -> {
            if (originalFile != null) {
                chooseOriginalKeyFromDevice();
            }
            else{
                Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonOriginalFile, "No file has been opened yet.");
            }
        });

        binding.openFileButtonEncryptedFile.setOnClickListener(view -> chooseEncryptedFileFromDevice());

        binding.importKeyButtonEncryptedFile.setOnClickListener(view -> {
            if (encryptedFile != null){
                chooseEncryptedKeyFromDevice();
            }
            else{
                Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonEncryptedFile, "No file has been opened yet.");
            }
        });

        binding.importIvButtonEncryptedFile.setOnClickListener(view -> {
            if (encryptedFile != null) {
                chooseIvFromDevice();
            }
            else{
                Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonEncryptedFile, "No file has been opened yet.");
            }
        });
        binding.decryptFileButton.setOnClickListener(view -> {
            if (encryptedFile != null) {
                if ((encryptedFile.getKey() != null) && (encryptedFile.getIv() != null)) {
                    decryptFile();
                }
                else{
                    Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonEncryptedFile, "Key and IV must be imported first.");
                }
            }
            else{
                Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonEncryptedFile, "No file has been opened yet.");
            }
        });

        return root;

    }

    private void encryptFile() {
        try {
            originalFile.encryptOriginalFile();
            displayKeyAndIv();
            Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonOriginalFile, "Original file encrypted successfully.");
        } catch (NoSuchAlgorithmException | IOException | IllegalBlockSizeException | InvalidKeyException | BadPaddingException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
    }

    private void decryptFile() {
        try {
            encryptedFile.decryptEncryptedFile();
            Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonEncryptedFile, "File has been decrypted successfully.");
        } catch (NoSuchAlgorithmException | IOException | IllegalBlockSizeException | InvalidKeyException | BadPaddingException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
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
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, attachmentUris);
        startActivity(intent);

    }

    private void displayFileName() {
        if (originalFile != null) {
            binding.fileNameTextViewOriginalFile.setText(originalFile.getOriginalFileName());
        }
        if (encryptedFile != null) {
            binding.fileNameTextViewEncryptedFile.setText(encryptedFile.getEncryptedFileName());
        }
    }

    private void displayKeyAndIv() {
        if (originalFile != null) {
            SecretKey key = originalFile.getKey();
            IvParameterSpec iv = originalFile.getIv();
            if (key != null) {
                binding.keyTextViewOriginalFile.setText(Arrays.toString(key.getEncoded()));
            }
            if (iv != null) {
                binding.ivTextViewOriginalFile.setText(Arrays.toString(iv.getIV()));
            }
        }
        if (encryptedFile != null) {
            SecretKey key = encryptedFile.getKey();
            IvParameterSpec iv = encryptedFile.getIv();
            if (key != null) {
                binding.keyTextViewEncryptedFile.setText(Arrays.toString(key.getEncoded()));
            }
            if (iv != null) {
                binding.ivTextViewEncryptedFile.setText(Arrays.toString(iv.getIV()));
            }
        }


    }

    private void saveKeyToFile() {

        if (originalFile != null) {
            SecretKey key = originalFile.getKey();
            if (key != null) {
                FileOutputStream fileOutputStream;
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

    private void updateUserKeyViewModel() {
        fileEncryptionViewModel.getOriginalFile().setValue(originalFile);
        fileEncryptionViewModel.getEncryptedFile().setValue(encryptedFile);
    }

    private void chooseOriginalFileFromDevice() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, CHOOSE_ORIGINAL_FILE_FROM_DEVICE);
    }

    private void chooseEncryptedFileFromDevice() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, CHOOSE_ENCRYPTED_FILE_FROM_DEVICE);
    }

    private void chooseOriginalKeyFromDevice() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, CHOOSE_ORIGINAL_KEY_FROM_DEVICE);
    }

    private void chooseEncryptedKeyFromDevice() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, CHOOSE_ENCRYPTED_KEY_FROM_DEVICE);
    }

    private void chooseIvFromDevice() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, CHOOSE_IV_FROM_DEVICE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null) {
            switch (requestCode) {
                case CHOOSE_ORIGINAL_FILE_FROM_DEVICE:
                    fileEncryptionViewModel.getOriginalFile().setValue(new UserFile(FILE_TYPE.ORIGINAL, data.getData(), requireContext(), originalPath));
                    Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonOriginalFile, "Original file imported.");
                    break;

                case CHOOSE_ENCRYPTED_FILE_FROM_DEVICE:
                    fileEncryptionViewModel.getEncryptedFile().setValue(new UserFile(FILE_TYPE.ENCRYPTED, data.getData(), requireContext(), encryptedPath));
                    Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonEncryptedFile, "Encrypted file imported.");
                    break;

                case CHOOSE_ORIGINAL_KEY_FROM_DEVICE:
                    IVorKeyImportResult importResultOriginal = originalFile.importKey(data.getData());
                    switch (importResultOriginal){
                        case OK:
                            updateUserKeyViewModel();
                            Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonOriginalFile, "Key imported successfully.");
                            break;
                        case INVALID_SIZE:
                            Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonOriginalFile, "Import file is the wrong size.");
                            break;
                    }
                    break;

                case CHOOSE_ENCRYPTED_KEY_FROM_DEVICE:
                    IVorKeyImportResult importResultEncrypted = encryptedFile.importKey(data.getData());
                    switch(importResultEncrypted){
                        case OK:
                            updateUserKeyViewModel();
                            Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonEncryptedFile, "Key imported successfully.");
                            break;
                        case INVALID_SIZE:
                            Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonEncryptedFile, "Import file is the wrong size.");
                            break;

                    }
                    break;

                case CHOOSE_IV_FROM_DEVICE:
                    IVorKeyImportResult importResultIV = encryptedFile.importIv(data.getData());
                    switch(importResultIV){
                        case OK:
                            updateUserKeyViewModel();
                            Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonEncryptedFile, "IV imported successfully.");
                            break;
                        case INVALID_SIZE:
                            Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonEncryptedFile, "Import file is the wrong size.");
                            break;
                    }
                    break;

                default:
                    Log.d(TAG, "onActivityResult: Error, request code unexpected: " + requestCode);

            }
        }
        else{
            Utils.displaySnackbar(binding.importKeyButtonOriginalFile, binding.importKeyButtonOriginalFile, "Opening a file process cancelled.");
        }


    }


    @Override
    public void onResume() {
        super.onResume();
        displayFileName();
        displayKeyAndIv();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}