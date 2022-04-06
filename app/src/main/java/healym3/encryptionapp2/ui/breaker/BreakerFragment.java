package healym3.encryptionapp2.ui.breaker;

import android.app.Activity;
import android.content.ContentResolver;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import healym3.encryptionapp2.algorithms.Breaker;
import healym3.encryptionapp2.data.UserFile;
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
        binding.cipherToBreakEditText.setText("msr crkqimurem xz jxukdmri bjvrejr qec renverriven mrjsextxna vb xer " +
                "xz msirr qjqcruvj crkqimuremb pvmsve msr jxttrnr xz bjvrejrb qec " +
                "mrjsextxna qm msr devoribvma xz sxdbmxe cxpemxpe. pr xzzri bmdcremb q " +
                "devwdr xkkximdevma mx qjsvror qjqcruvj bdjjrbb ve jxukdmri bjvrejr qec " +
                "renverriven mrjsextxna msixdns ivnxixdb qec ryjvmven jxdibrb, " +
                "vejtdcven decriniqcdqmr irbrqijs xkkximdevmvrb. pr qir jxuuvmmrc mx " +
                "miqveven bmdcremb zxi jsqttrenrb ve jxukdmri bjvrejr qec renverriven " +
                "mrjsextxna, qec ve zvrtcb irwdviven msr gexptrcnr qec bgvttb, xdi " +
                "jdiivjdtdu kixovcrb zxi bmdcremb pvmsve mryqb qec qixdec msr pxitc. " +
                "msr crkqimurem xz jxukdmri bjvrejrb qec renverriven mrjsextxna vb msr " +
                "zqbmrbm nixpven crkqimurem pvmsve msr jxttrnr xz bjvrejrb qec " +
                "mrjsextxna. qccvmvxeqtta, qb q bmdcrem xz msr jbrm crkqimurem, axd " +
                "pvtt nqve sqecb xe rykrivrejr qec kiqjmvjqt miqveven. bmdcremb pvtt fr " +
                "irwdvirc mx kqimvjvkqmr ve q brurbmri kixhrjm, psvjs pvtt miqve " +
                "bmdcremb mx pxig xe irqt vecdbmia kixftrub ve q mrqu reovixeurem msqm " +
                "msra pvtt rejxdemri pxigven ve vecdbmia qzmri niqcdqmvxe. msr brevxi " +
                "kixhrjm qttxpb bmdcremb mx qkkta msrvi jxdibr uqmrivqtb qec trqieven " +
                "rykrivrejrb mx brurbmri txen irqt pxitc kixhrjmb.");

        binding.breakCipherButton.setOnClickListener(view -> {
            if(!TextUtils.isEmpty(binding.cipherToBreakEditText.getText())){
                Breaker breaker = new Breaker(getContext());
                breaker.breakCipher(binding.cipherToBreakEditText.getText().toString());
                binding.cipherBreakerResultEditText.setText(breaker.getBreakerResult());
                Utils.hideSoftKeyboard(getContext(),view);
            }

        });

        binding.openCipherTextButton.setOnClickListener(view -> {
            chooseTextFile();
        });
        //final TextView textView = binding.textDashboard;
        //breakerViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
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
                stringBuilder.append(line + "\n");
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