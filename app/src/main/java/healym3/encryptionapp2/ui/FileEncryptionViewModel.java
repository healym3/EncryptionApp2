package healym3.encryptionapp2.ui;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import healym3.encryptionapp2.data.UserFile;

public class FileEncryptionViewModel extends ViewModel {
    private final MutableLiveData<UserFile> userFile;

    public FileEncryptionViewModel(){
        userFile = new MutableLiveData<>();
    }

    public MutableLiveData<UserFile> getUserFile() {
        return userFile;
    }
}