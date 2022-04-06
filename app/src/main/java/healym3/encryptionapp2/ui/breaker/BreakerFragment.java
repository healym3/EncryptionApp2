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

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;



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
            if(!TextUtils.isEmpty(binding.cipherToBreakEditText.getText())){
                Breaker breaker = new Breaker(getContext());
                breaker.breakCipher(binding.cipherToBreakEditText.getText().toString());
                binding.cipherBreakerResultEditText.setText(breaker.getBreakerResult());
                Utils.hideSoftKeyboard(requireContext(),view);
            }

        });

        binding.openCipherTextButton.setOnClickListener(view -> chooseTextFile());

        return root;
    }

    private void chooseTextFile(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, CHOOSE_TXT_FROM_STORAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CHOOSE_TXT_FROM_STORAGE && resultCode == Activity.RESULT_OK){

            if(data != null){
                Uri uri = data.getData();
                getTextFromFile(uri);

            }
        }
    }

    private void getTextFromFile(Uri uri) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(requireContext().getContentResolver().openInputStream(uri));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null){
                stringBuilder.append(String.format("%s\n", line));
            }
            setCipherText(stringBuilder.toString());
            bufferedReader.close();
            inputStreamReader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setCipherText(String cipherText){
        if(cipherText != null){
            binding.cipherToBreakEditText.setText(cipherText);
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}