package healym3.encryptionapp2.ui.substitution;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import healym3.encryptionapp2.data.SubstitutionKeyError;
import healym3.encryptionapp2.databinding.FragmentSubstitutionBinding;
import healym3.encryptionapp2.data.SubstitutionKey;
import healym3.encryptionapp2.algorithms.Substitution;
import healym3.encryptionapp2.util.Utils;

public class SubstitutionFragment extends Fragment {

    private FragmentSubstitutionBinding binding;

    private Substitution substitution;
    private SubstitutionKey key;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SubstitutionViewModel homeViewModel =
                new ViewModelProvider(this).get(SubstitutionViewModel.class);

        binding = FragmentSubstitutionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final Observer<SubstitutionKey> substitutionKeyObserver = newSubstitutionKey -> {
            key = newSubstitutionKey;
            displayKey();
        };

        homeViewModel.getKey().observe(getViewLifecycleOwner(), substitutionKeyObserver);

        final Observer<Substitution> substitutionObserver = newSubstitution -> substitution = newSubstitution;

        homeViewModel.getSubstitution().observe(getViewLifecycleOwner(), substitutionObserver);

        binding.createKeyButton.setOnClickListener(view -> {
            key.generateKey();
            homeViewModel.getKey().setValue(key);
            displayKey();
        });

        binding.saveKeyButton.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(binding.customKeyEditText.getText())) {
                SubstitutionKeyError substitutionKeyError = key.setKey(binding.customKeyEditText.getText().toString());
                switch (substitutionKeyError){
                    case OK:
                        homeViewModel.getKey().setValue(key);
                        substitution.setKey(key);
                        homeViewModel.getSubstitution().setValue(substitution);
                        displayKey();
                        Utils.hideSoftKeyboard(requireContext(), view);
                        binding.customKeyEditText.setText("");
                        break;
                    case INVALID_LENGTH:
                        displaySnackbar(binding.customKeyEditText, binding.plainEditText, "Key must be as long as alphabet.");
                        break;
                    case NOT_UNIQUE:
                        displaySnackbar(binding.customKeyEditText, binding.plainEditText,"Key letters must be unique.");
                        break;
                    case INVALID_CHARACTER:
                        displaySnackbar(binding.customKeyEditText, binding.plainEditText,"Key letters must be from alphabet.");
                        break;
                    default:

                }

            }
            else{
                displaySnackbar(binding.customKeyEditText, binding.plainEditText, "No custom key has been entered.");
            }
        });

        binding.encryptButton.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(binding.plainEditText.getText())) {
                String cipherText = substitution.encrypt(String.valueOf(binding.plainEditText.getText()));
                binding.cipherEditText.setText(cipherText);
                homeViewModel.getSubstitution().setValue(substitution);
                Utils.hideSoftKeyboard(requireContext(), view);
                Log.d("Substitution", "Encrypt: " + key.toString() + "Plain: " + binding.plainEditText.getText() +
                        " Cipher: " + cipherText);
            }
            else{
                displaySnackbar(binding.plainEditText, binding.plainEditText, "No plain text has been entered.");
            }
        });

        binding.decryptButton.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(binding.cipherEditText.getText())) {
                String plainText = substitution.decrypt(String.valueOf(binding.cipherEditText.getText()));
                binding.plainEditText.setText(plainText);
                homeViewModel.getSubstitution().setValue(substitution);
                Utils.hideSoftKeyboard(requireContext(), view);
                Log.d("Substitution", "Decrypt: " + key.toString() + "Cipher: " + binding.cipherEditText.getText() +
                        " Cipher: " + plainText);
            }
            else{
                displaySnackbar(binding.cipherEditText, binding.plainEditText, "No cipher text has been entered.");
            }
        });

        return root;
    }

    private void displaySnackbar(View contextView, String message){
        Snackbar.make(contextView, message, Snackbar.LENGTH_SHORT)
                .show();
    }

    private void displaySnackbar(View contextView, View anchorView, String message){
        Snackbar.make(contextView, message, Snackbar.LENGTH_SHORT)
                .setAnchorView(anchorView)
                .show();
    }

    private void displayKey() {
        binding.keyTextView.setText(key.toString());
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