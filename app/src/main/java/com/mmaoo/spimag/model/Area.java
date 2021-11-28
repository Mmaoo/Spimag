package com.mmaoo.spimag.model;

import android.util.Pair;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Area implements Serializable {

    private String id;
    private String name;
    private ArrayList<Pair<Area,AreaElement>> areas = new ArrayList<>();
    private ArrayList<Pair<Item,AreaElement>> items = new ArrayList<>();
    private int width = 1024;
    private int height = 1024;


    public Area() { }

    public Area(String id) {
        this.id = id;
    }


    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("timestamp", ServerValue.TIMESTAMP);
        result.put("width", width);
        result.put("height", height);

        HashMap<String,Object> it = new HashMap<>();
        for (Pair<Item,AreaElement> i : items) {
//            HashMap<String, Object> temp = new HashMap<>();
//            temp.put("item",i.first.toMap());
//            temp.put("areaElement",i.second.toMap());
//            it.put(i.first.getId(),temp);
            it.put(i.first.getId(),i.first.toMap());
        }
        result.put("items",it);

        HashMap<String,Object> ar = new HashMap<>();
        for (Pair<Area,AreaElement> i : areas) {
            HashMap<String, Object> temp = new HashMap<>();
            temp.put("areaElement",i.second.toMap());
            ar.put(i.first.getId(),temp);
        }
        result.put("items",it);
        result.put("areas",ar);

//
//        HashMap<String,Object> areasHM = new HashMap<>();
//        for (Pair<Area,AreaElement> areaPair: this.areas) {
//            areasHM.put(areaPair.first.id)
//        }
//        result.put("areas", areasHM);
//
//        if(areaElement != null) {
//            HashMap<String, Object> resultArea = new HashMap<>();
//            resultArea.put("x", areaElement.x);
//            resultArea.put("y", areaElement.y);
//            resultArea.put("id", areaElement.areaId);
//            result.put("area", resultArea);
//        }
        return result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public ArrayList<Area> getAreas() {
//        return areas;
//    }
//
//    public void setAreas(ArrayList<Area> areas) {
//        this.areas = areas;
//    }
//
//    public ArrayList<Item> getItems() {
//        return items;
//    }
//
//    public void setItems(ArrayList<Item> items) {
//        this.items = items;
//    }


    public ArrayList<Pair<Area, AreaElement>> getAreas() {
        return areas;
    }

    public void setAreas(ArrayList<Pair<Area, AreaElement>> areas) {
        this.areas = areas;
    }

    public ArrayList<Pair<Item, AreaElement>> getItems() {
        return items;
    }

    public void setItems(ArrayList<Pair<Item, AreaElement>> items) {
        this.items = items;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "Area{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", areas=" + areas +
                ", items=" + items +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
