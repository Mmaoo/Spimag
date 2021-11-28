package com.mmaoo.spimag.ui.areaShow;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AreaShowViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AreaShowViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is surface fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}