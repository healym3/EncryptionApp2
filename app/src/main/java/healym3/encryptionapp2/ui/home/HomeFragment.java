package healym3.encryptionapp2.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import healym3.encryptionapp2.databinding.FragmentHomeBinding;
import healym3.encryptionapp2.data.SubstitutionKey;
import healym3.encryptionapp2.algorithms.Substitution;
import healym3.encryptionapp2.util.Utils;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    private Substitution substitution;
    private SubstitutionKey key;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
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
                if (key.setKey(binding.customKeyEditText.getText().toString())) {
                    homeViewModel.getKey().setValue(key);
                    substitution.setKey(key);
                    homeViewModel.getSubstitution().setValue(substitution);
                    displayKey();
                }
                Utils.hideSoftKeyboard(requireContext(), view);
                binding.customKeyEditText.setText("");
            }
        });

        binding.encryptButton.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(binding.plainEditText.getText())) {
                String cipherText = substitution.encrypt(String.valueOf(binding.plainEditText.getText()));
                binding.cipherEditText.setText(cipherText);
                homeViewModel.getSubstitution().setValue(substitution);
                Utils.hideSoftKeyboard(requireContext(), view);
            }
        });

        binding.decryptButton.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(binding.cipherEditText.getText())) {
                String plainText = substitution.decrypt(String.valueOf(binding.cipherEditText.getText()));
                binding.plainEditText.setText(plainText);
                homeViewModel.getSubstitution().setValue(substitution);
                Utils.hideSoftKeyboard(requireContext(), view);
            }
        });

        return root;
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