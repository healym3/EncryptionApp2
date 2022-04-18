package healym3.encryptionapp2.ui.home;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import healym3.encryptionapp2.algorithms.Substitution;
import healym3.encryptionapp2.data.SubstitutionKey;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<SubstitutionKey> key;
    private final MutableLiveData<Substitution> substitution;

    public HomeViewModel() {
        key = new MutableLiveData<>();
        substitution = new MutableLiveData<>();
        SubstitutionKey substitutionKey = new SubstitutionKey();
        substitutionKey.generateKey();
        key.setValue(substitutionKey);
        substitution.setValue(new Substitution(key.getValue()));
    }

    public MutableLiveData<SubstitutionKey> getKey() {
        return key;
    }

    public MutableLiveData<Substitution> getSubstitution() {
        return substitution;
    }


}