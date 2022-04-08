package healym3.encryptionapp2.ui.files;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import healym3.encryptionapp2.data.UserFile;

public class FileEncryptionViewModel extends ViewModel {
    private final MutableLiveData<UserFile> originalFile;
    private final MutableLiveData<UserFile> encryptedFile;

    public FileEncryptionViewModel(){
        originalFile = new MutableLiveData<>();
        encryptedFile = new MutableLiveData<>();
    }

    public MutableLiveData<UserFile> getOriginalFile() {
        return originalFile;
    }

    public MutableLiveData<UserFile> getEncryptedFile() {
        return encryptedFile;
    }
}