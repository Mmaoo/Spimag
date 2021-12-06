
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
import java.util.Random;

public class RectAreaElement extends AreaElement implements Serializable {

    public int width = 0;
    public int height = 0;

    public RectAreaElement(){
        color = (new Random()).nextInt();
    }

    @Override
    public String toString() {
        return "RectAreaElement{" +
                "x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                ", color=" + color +
                '}';
    }

    @Override
    public void draw(Canvas canvas, PointF areaBitmapPos, float scale, String name, AreaDrawable.Settings settings) {
        Paint paint = new Paint();
        paint.setColor(color);
        //int size = getWidth() < getHeight() ? getWidth()/4 : getHeight()/4;
        //canvas.drawRect(aE.x, aE.y, aE.x + size, aE.y + size, paint);
        float x1 = areaBitmapPos.x + this.x;
        float y1 = areaBitmapPos.y + this.y;
        float x2 = x1 + this.width;
        float y2 = y1 + this.height;
        x1 *= scale;
        y1 *= scale;
        x2 *= scale;
        y2 *= scale;
//        Log.d("test","Bx="+areaBitmapPos.x+", By="+areaBitmapPos.y+"; x1="+x1+", y1="+y1+", x2="+x2+", y2="+y2);
        if(settings != null && (settings instanceof Settings)){
            Settings set = (Settings) settings;
            if(set.color != null) paint.setColor(set.color);
        }

        canvas.drawRect(x1, y1, x2, y2, paint);

        Paint textPaint = new Paint();
        textPaint.setTextSize((float)width/5);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
        float w;
        if(textPaint.measureText(name) >= width) {
            do {
                w = textPaint.measureText(name);
                if (name.length() <= 3) {
                    name = "...";
                    break;
                } else if (w < width) {
                    name = name.substring(0, name.length() - 1 - 3);
                    name += "...";
                    break;
                } else {
                    name = name.substring(0, name.length() - 1);
                }
            } while (true);
        }
        canvas.drawText(name,(x1+((float)width/2)),(y1+((float)height/2)),textPaint);
    }

    @Override
    public boolean insideArea(float x, float y) {
        return x >= this.x && x <= this.x+width
        && y >= this.y && y <= this.y+height;
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String,Object> result = super.toMap();
        result.put("width",width);
        result.put("height",height);
        result.put("class","RectAreaElement");
        return result;
    }
}
