package com.mmaoo.spimag.model;

import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;

import com.google.android.gms.tasks.Task;

public interface Storage {
    Task<Bitmap> getAreaBackground(String areaId);
    Task<Bitmap> putAreaBackground(String areaId, Uri uri);
}
