package com.mmaoo.spimag.model;

import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public interface Database {
    Task<Item> add(Item item);
    Task<Item> update(Item item);
    Task<Item> remove(Item item);
    Task<ArrayList<Item>> getAllItems();

    Task<Area> add(Area area);
    Task<Area> update(Area area);
    Task<Area> remove(Area area);
    Task<ArrayList<Area>> getAllAreas();

//    Task<Item> add(Package pack);
}
