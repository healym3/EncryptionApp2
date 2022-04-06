package healym3.encryptionapp2.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
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

        key = new SubstitutionKey();
        key.setKey("defghijklmnopqrstuvwxyzabc");
        displayKey();
        //substitution = new Substitution(key);
        binding.createKeyButton.setOnClickListener(view -> {
            key.generateKey();
            displayKey();
        });
        binding.saveKeyButton.setOnClickListener(view -> {
            if(!TextUtils.isEmpty(binding.customKeyEditText.getText())){
                if(key.setKey(binding.customKeyEditText.getText().toString())){
                    displayKey();
                }
                Utils.hideSoftKeyboard(requireContext(),view);
                binding.customKeyEditText.setText("");
            }



        });

        binding.encryptButton.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(binding.plainEditText.getText())) {
                substitution = new Substitution(key);
                String cipherText = substitution.encrypt(String.valueOf(binding.plainEditText.getText()));
                binding.cipherEditText.setText(cipherText);
                Utils.hideSoftKeyboard(requireContext(),view);
            }

        });

        binding.decryptButton.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(binding.cipherEditText.getText())) {
                substitution = new Substitution(key);
                String plainText = substitution.decrypt(String.valueOf(binding.cipherEditText.getText()));
                binding.plainEditText.setText(plainText);
                Utils.hideSoftKeyboard(requireContext(),view);
            }
        });

        return root;
    }

    private void displayKey(){
        binding.keyTextView.setText(key.toString());
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}