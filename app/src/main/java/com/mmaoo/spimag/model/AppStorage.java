package com.mmaoo.spimag.model;

public class AppStorage {
    private static Storage storage = null;

    public static Storage getInstance(){
        if(storage != null) return storage;
        else return new FBStorage();
    }
}
