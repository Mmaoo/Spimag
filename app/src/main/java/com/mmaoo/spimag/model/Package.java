package com.mmaoo.spimag.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Package  implements Serializable {
    private String  id = null;
    private String type = null; // słoik
    private String unit = null; // ml
    private float amount = 0; // 350

    public Package(String type, String unit, float amount) {
        this.type = type;
        this.unit = unit;
        this.amount = amount;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("type", type);
        result.put("unit", unit);
        result.put("amount", amount);
        result.put("timestamp", ServerValue.TIMESTAMP);
        return result;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}
