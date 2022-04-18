package healym3.encryptionapp2.ui.bitmap;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import healym3.encryptionapp2.data.UserFileBitmap;

public class BitmapViewModel extends ViewModel {
    private final MutableLiveData<UserFileBitmap> userFile;

    public BitmapViewModel() {
        userFile = new MutableLiveData<>();
    }

    public MutableLiveData<UserFileBitmap> getUserFile() {
        return userFile;
    }
}