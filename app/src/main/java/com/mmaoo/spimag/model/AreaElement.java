package com.mmaoo.spimag.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AreaElement implements AreaDrawable, Serializable {
    public String areaId = null;
    public float x = 0;
    public float y = 0;

    public int color = Color.BLACK;
//    public String name = "";

    public AreaElement(){}

    public AreaElement(String areaId) {
        this.areaId = areaId;
    }

    @Override
    public void draw(Canvas canvas, PointF areaBitmapPos, float scale, String name, AreaDrawable.Settings settings) {
        Paint paint = new Paint();
        paint.setColor(color);
        float x1 = areaBitmapPos.x + this.x;
        float y1 = areaBitmapPos.y + this.y;
        paint.setStrokeWidth(100*scale);
//        Log.d("test","Bx="+areaBitmapPos.x+", By="+areaBitmapPos.y+"; x="+x1+", y="+y1);
        if(settings != null && (settings instanceof Settings)){
            Settings set = (Settings) settings;
            if(set.color != null) paint.setColor(set.color);
        }
        canvas.drawPoint(x1,y1,paint);
    }

    @Override
    public boolean insideArea(float x, float y) {
        return (x == this.x) && (y == this.y);
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String,Object> result = new HashMap<>();
        result.put("x",x);
        result.put("y",y);
        result.put("areaId",areaId);
        result.put("color",color);
        result.put("class","AreaElement");
        return result;
    }

    @Override
    public String toString() {
        return "AreaElement{" +
                "areaId='" + areaId + '\'' +
                ", x=" + x +
                ", y=" + y +
                ", color=" + color +
                '}';
    }

    public static class Settings extends AreaDrawable.Settings {
        Integer color;

        public Settings(int color) {
            this.color = color;
        }

        public static class Builder {
            Integer color;

            public Builder() {
            }

            public Builder setColor(int color){
                this.color = color;
                return this;
            }

            public Settings build(){
                return new Settings(color);
            }
        }
    }
}
