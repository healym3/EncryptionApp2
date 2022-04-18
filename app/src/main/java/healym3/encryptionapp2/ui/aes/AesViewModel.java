package healym3.encryptionapp2.ui.aes;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import healym3.encryptionapp2.data.UserFileBitmap;

public class AesViewModel extends ViewModel {
    private final MutableLiveData<UserFileBitmap> userFile;

    public AesViewModel() {
        userFile = new MutableLiveData<>();
    }

    public MutableLiveData<UserFileBitmap> getUserFile() {
        return userFile;
    }
}