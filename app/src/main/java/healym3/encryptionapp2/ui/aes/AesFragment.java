package healym3.encryptionapp2.ui.aes;

import android.app.Activity;
import android.content.Intent;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;


import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;


import healym3.encryptionapp2.data.UserFileBitmap;
import healym3.encryptionapp2.databinding.FragmentAesBinding;

public class AesFragment extends Fragment {

    private final int CHOOSE_BMP_FROM_DEVICE = 1010;

    private FragmentAesBinding binding;

    private UserFileBitmap userFile;
    private AesViewModel AESViewModel;

    @Override
    public void onResume() {
        super.onResume();
        if(userFile!=null){
            displayImage();
            displayKey();
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AESViewModel =
                new ViewModelProvider(this).get(AesViewModel.class);

        binding = FragmentAesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.buttonLoadBitmap.setOnClickListener(view -> chooseBmpFromDevice());

        final Observer<UserFileBitmap> userFileObserver = new Observer<UserFileBitmap>() {
            @Override
            public void onChanged(@Nullable final UserFileBitmap newUserFile) {
                userFile = newUserFile;
                displayImage();
                displayKey();
            }
        };

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        AESViewModel.getUserFile().observe(getViewLifecycleOwner(), userFileObserver);

        return root;
    }

    private void displayKey() {
        SecretKey key = userFile.getKey();
        if(key != null){
            StringBuilder sb = new StringBuilder();
            for (byte b: key.getEncoded()
            ) {
                sb.append(b).append(", ");
            }
            binding.keyTextView2.setText(sb);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private void chooseBmpFromDevice(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, CHOOSE_BMP_FROM_DEVICE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CHOOSE_BMP_FROM_DEVICE && resultCode == Activity.RESULT_OK){

            if(data != null){
                AESViewModel.getUserFile().setValue(new UserFileBitmap(data.getData(), requireContext()));

                try {
                    userFile.encryptOriginalFile();
                } catch (NoSuchAlgorithmException | IOException | IllegalBlockSizeException | InvalidKeyException | BadPaddingException | InvalidAlgorithmParameterException | NoSuchPaddingException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void displayImage() {
        Glide.with(requireContext())
                .load(userFile.getOriginalFile())
                .fitCenter()
                .into(binding.imageViewOriginal);
        Glide.with(requireContext())
                .load(userFile.getValidEcbEncryptedBitmap())
                .fitCenter()
                .into(binding.imageViewEncryptedEcb);
        Glide.with(requireContext())
                .load(userFile.getValidCbcEncryptedBitmap())
                .fitCenter()
                .into(binding.imageViewEncryptedCBC);
    }


}