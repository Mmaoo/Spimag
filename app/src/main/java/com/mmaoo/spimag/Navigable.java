package com.mmaoo.spimag;

import android.os.Bundle;

public interface Navigable {
    void navigate(int action);
    void navigate(int action, Bundle bundle);
    void navigateUp();
}
