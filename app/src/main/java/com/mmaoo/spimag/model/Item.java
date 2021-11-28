package com.mmaoo.spimag.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Item implements Serializable {
    private String id = null;
    private String name = null;
    //private Package pack = null;
    private String shortName = null;
    private String pack = null;
    private float amount = 0;
    private AreaElement areaElement;
    private Long timestamp;

    public Item(){};

    public Item(AreaElement areaElement){
        this.id = id;
        this.areaElement = areaElement;
    }

    public Item(String id, String name, String shortName, String pack, /*Package pack, */float amount) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.pack = pack;
        this.amount = amount;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("name", name);
//        if(pack != null) result.put("pack", pack.toMap());
        result.put("shortName",shortName);
        result.put("pack",pack);
        result.put("amount", amount);
        result.put("timestamp", ServerValue.TIMESTAMP);
        if(areaElement != null) result.put("areaElement",areaElement.toMap());
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

//    public Package getPack() {
//        return pack;
//    }
//
//    public void setPack(Package pack) {
//        this.pack = pack;
//    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getPack() {
        return pack;
    }

    public void setPack(String pack) {
        this.pack = pack;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public AreaElement getAreaElement() {
        return areaElement;
    }

    public void setAreaElement(AreaElement areaElement) {
        this.areaElement = areaElement;
    }

    public Long getTimestamp() { return timestamp; }

    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", shortName='" + shortName + '\'' +
                ", pack='" + pack + '\'' +
                ", amount=" + amount +
                ", areaElement=" + ((areaElement != null) ? areaElement.toString() : "null") +
                ", timestamp=" + timestamp +
                '}';
    }
}
