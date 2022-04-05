package healym3.encryptionapp2.ui.aes;

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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import healym3.encryptionapp2.databinding.FragmentAesBinding;

public class AesFragment extends Fragment {

    private final int CHOOSE_BMP_FROM_DEVICE = 1000;
    private FragmentAesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AesViewModel AESViewModel =
                new ViewModelProvider(this).get(AesViewModel.class);

        binding = FragmentAesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        binding.buttonLoadBitmap.setOnClickListener(view -> {
            chooseBmpFromDevice();
        });
        //final TextView textView = binding.textNotifications;
        //AESViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
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
        startActivityForResult(intent, CHOOSE_BMP_FROM_DEVICE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CHOOSE_BMP_FROM_DEVICE && resultCode == Activity.RESULT_OK){

            if(data != null){
                Uri imageUriOriginal = data.getData();
                Log.d("TAG", "onActivityResult: " + imageUriOriginal.getPath());
                //textView.setText(imageUriOriginal.getEncodedPath());
                Glide.with(getContext())
                        .load(imageUriOriginal)
                        .fitCenter()
                        .into(binding.imageViewOriginal);
                //imageUri.
                //bitmap
            }
        }
    }

}