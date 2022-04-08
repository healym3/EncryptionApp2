package healym3.encryptionapp2.ui.aes;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Random;

import healym3.encryptionapp2.data.UserFile;
import healym3.encryptionapp2.data.UserFileBitmap;

public class AesViewModel extends ViewModel {
    private final MutableLiveData<UserFileBitmap> userFile;
    //private final MutableLiveData<String> mText;

    public AesViewModel() {
        //mText = new MutableLiveData<>();
        userFile = new MutableLiveData<>();
        //Random random = new Random();
        //mText.setValue(String.valueOf(random.nextInt(50)));

    }

//    public LiveData<String> getText() {
//        return mText;
//    }

    public MutableLiveData<UserFileBitmap> getUserFile() {
        return userFile;
    }
}