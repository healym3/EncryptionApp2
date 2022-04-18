package healym3.encryptionapp2.ui.breaker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import healym3.encryptionapp2.algorithms.Breaker;
import healym3.encryptionapp2.databinding.FragmentBreakerBinding;
import healym3.encryptionapp2.util.Utils;

public class BreakerFragment extends Fragment {

    public static final int CHOOSE_TXT_FROM_STORAGE = 1000;

    private FragmentBreakerBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        BreakerViewModel breakerViewModel =
                new ViewModelProvider(this).get(BreakerViewModel.class);

        binding = FragmentBreakerBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        binding.breakCipherButton.setOnClickListener(view -> {
            if (!TextUtils.isEmpty(binding.cipherToBreakEditText.getText())) {
                Breaker breaker = new Breaker(getContext());
                breaker.breakCipher(binding.cipherToBreakEditText.getText().toString());
                binding.cipherBreakerResultEditText.setText(breaker.getBreakerResult());
                Utils.hideSoftKeyboard(requireContext(), view);
            }
            else{
                Utils.displaySnackbar(binding.cipherToBreakEditText, binding.cipherToBreakEditText, "No cipher text has been entered or loaded from file.");
            }

        });

        binding.openCipherTextButton.setOnClickListener(view -> chooseTextFile());

        return root;
    }

    @SuppressWarnings("deprecation")
    private void chooseTextFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, CHOOSE_TXT_FROM_STORAGE);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_TXT_FROM_STORAGE && resultCode == Activity.RESULT_OK) {

            if (data != null) {
                Uri uri = data.getData();
                setCipherText(Utils.getTextFromFile(requireContext(), uri));
            }
        }
        else{
            Utils.displaySnackbar(binding.cipherToBreakEditText, binding.cipherToBreakEditText, "Opening file canceled.");
        }
    }

    private void setCipherText(String cipherText) {
        if (cipherText != null) {
            binding.cipherToBreakEditText.setText(cipherText);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}