package com.mmaoo.spimag.model;

import android.graphics.Canvas;
import android.graphics.PointF;

public interface AreaDrawable {

    void draw(Canvas canvas, PointF areaBitmapPos, float scale, String name);
    boolean insideArea(float x, float y);
}
