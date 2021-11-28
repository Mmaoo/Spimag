package com.mmaoo.spimag.model;

public class AppDatabase {

    private static Database database = null;

    public static Database getInstance() {
        if(database == null) database = new FBDatabase();//MySqlDatabase();//FSDatabase();//
        return database;
    }

}
