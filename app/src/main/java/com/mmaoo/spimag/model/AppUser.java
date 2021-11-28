package com.mmaoo.spimag.model;

import com.google.firebase.auth.FirebaseAuth;

public class AppUser implements User {

    private static User appUser = null;

    public static User getInstance(){
        if(appUser == null) appUser = new AppUser();
        return appUser;
    }

    @Override
    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
    }

    @Override
    public String getName() {
        return FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : null;
    }

    @Override
    public String getEmail() {
        return FirebaseAuth.getInstance().getCurrentUser() != null ? FirebaseAuth.getInstance().getCurrentUser().getEmail() : null;
    }
}
