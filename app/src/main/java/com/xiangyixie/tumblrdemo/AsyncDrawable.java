package com.xiangyixie.tumblrdemo;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import java.lang.ref.WeakReference;

/**
 * Created by xiangyixie on 1/17/16.
 */
public class AsyncDrawable extends BitmapDrawable {
    private WeakReference<ImageLoaderTask> taskRef;
    public AsyncDrawable(Resources res, Bitmap bitmap, ImageLoaderTask task) {
        super(res, bitmap);
        taskRef = new WeakReference<ImageLoaderTask>(task);
    }

    public ImageLoaderTask getImageLoaderTask() {
        return taskRef.get();
    }
}
