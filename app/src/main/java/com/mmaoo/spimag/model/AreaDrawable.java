package com.mmaoo.spimag.model;

import android.graphics.Canvas;
import android.graphics.PointF;

import com.google.android.gms.common.api.GoogleApi;

import okio.Options;

public interface AreaDrawable {

    void draw(Canvas canvas, PointF areaBitmapPos, float scale, String name, Settings settings);
    boolean insideArea(float x, float y);

    public static class Settings{};
}
