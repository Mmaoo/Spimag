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
    public void draw(Canvas canvas, PointF areaBitmapPos, float scale, String name) {
        Paint paint = new Paint();
        paint.setColor(color);
        float x1 = areaBitmapPos.x + this.x;
        float y1 = areaBitmapPos.y + this.y;
        paint.setStrokeWidth(100*scale);
//        Log.d("test","Bx="+areaBitmapPos.x+", By="+areaBitmapPos.y+"; x="+x1+", y="+y1);
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
}
