package healym3.encryptionapp2.ui.aes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Random;

public class AesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AesViewModel() {
        mText = new MutableLiveData<>();
        Random random = new Random();
        mText.setValue(String.valueOf(random.nextInt(50)));
    }

    public LiveData<String> getText() {
        return mText;
    }
}