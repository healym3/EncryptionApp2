package healym3.encryptionapp2.ui.substitution;

import android.content.Context;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKeys;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import healym3.encryptionapp2.data.SubstitutionKeyImportResult;
import healym3.encryptionapp2.databinding.FragmentSubstitutionBinding;
import healym3.encryptionapp2.data.SubstitutionKey;
import healym3.encryptionapp2.algorithms.Substitution;
import healym3.encryptionapp2.util.Utils;

public class SubstitutionFragment extends Fragment {

    private FragmentSubstitutionBinding binding;
    private SubstitutionViewModel homeViewModel;

    private Substitution substitution;
    private SubstitutionKey key;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(SubstitutionViewModel.class);

        binding = FragmentSubstitutionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final Observer<SubstitutionKey> substitutionKeyObserver = newSubstitutionKey -> {
            key = newSubstitutionKey;
            if(key!=null && substitution!=null){
                substitution.setKey(key);
                updateViewModelSubstitution();
            }
            displayKey();
        };

        homeViewModel.getKey().observe(getViewLifecycleOwner(), substitutionKeyObserver);

        final Observer<Substitution> substitutionObserver = newSubstitution -> substitution = newSubstitution;

        homeViewModel.getSubstitution().observe(getViewLifecycleOwner(), substitutionObserver);

        binding.createKeyButton.setOnClickListener(view -> {
            key.generateKey();
            updateViewModelKey();
            displayKey();
        });

        binding.saveKeyButton.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(binding.customKeyEditText.getText())) {
                SubstitutionKeyImportResult substitutionKeyImportResult = key.setKey(binding.customKeyEditText.getText().toString());
                switch (substitutionKeyImportResult){
                    case OK:
                        updateViewModelKey();
                        displayKey();
                        Utils.hideSoftKeyboard(requireContext(), view);
                        binding.customKeyEditText.setText("");
                        break;
                    case INVALID_LENGTH:
                        Utils.displaySnackbar(binding.customKeyEditText, binding.plainEditText, "Key must be as long as alphabet.");
                        break;
                    case NOT_UNIQUE:
                        Utils.displaySnackbar(binding.customKeyEditText, binding.plainEditText,"Key letters must be unique.");
                        break;
                    case INVALID_CHARACTER:
                        Utils.displaySnackbar(binding.customKeyEditText, binding.plainEditText,"Key letters must be from alphabet.");
                        break;
                    default:

                }

            }
            else{
                Utils.displaySnackbar(binding.customKeyEditText, binding.plainEditText, "No custom key has been entered.");
            }
        });

        binding.encryptButton.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(binding.plainEditText.getText())) {
                String cipherText = substitution.encrypt(String.valueOf(binding.plainEditText.getText()));
                binding.cipherEditText.setText(cipherText);
                updateViewModelSubstitution();
                Utils.hideSoftKeyboard(requireContext(), view);
                Log.d("Substitution", "Encrypt: " + key.toString() + "Plain: " + binding.plainEditText.getText() +
                        " Cipher: " + cipherText);
                Utils.displaySnackbar(binding.plainEditText, binding.plainEditText, "Encryption completed successfully.");
            }
            else{
                Utils.displaySnackbar(binding.plainEditText, binding.plainEditText, "No plain text has been entered.");
            }
        });

        binding.decryptButton.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(binding.cipherEditText.getText())) {
                String plainText = substitution.decrypt(String.valueOf(binding.cipherEditText.getText()));
                binding.plainEditText.setText(plainText);
                updateViewModelSubstitution();
                Utils.hideSoftKeyboard(requireContext(), view);
                Log.d("Substitution", "Decrypt: " + key.toString() + "Cipher: " + binding.cipherEditText.getText() +
                        " Cipher: " + plainText);
                Utils.displaySnackbar(binding.plainEditText, binding.cipherEditText, "Decryption completed successfully.");
            }
            else{
                Utils.displaySnackbar(binding.cipherEditText, binding.cipherEditText, "No cipher text has been entered.");
            }
        });

        return root;
    }


    private void displayKey() {
        binding.keyTextView.setText(key.toString());
    }

    private void updateViewModelSubstitution(){

        if(key!=null) substitution.setKey(key);
        homeViewModel.getSubstitution().setValue(substitution);
    }
    private void updateViewModelKey(){
        homeViewModel.getKey().setValue(key);
        if(key!=null) substitution.setKey(key);
        homeViewModel.getSubstitution().setValue(substitution);
    }

    @Override
    public void onResume() {
        super.onResume();
        displayKey();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}