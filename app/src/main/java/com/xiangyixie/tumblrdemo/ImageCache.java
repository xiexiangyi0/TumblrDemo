package com.xiangyixie.tumblrdemo;

import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.util.LruCache;

/**
 * Created by xiangyixie on 1/17/16.
 */
public class ImageCache {
    private LruCache<String, Bitmap> mMemCache;

    public ImageCache(int size) {
        mMemCache = new LruCache<String, Bitmap>(size) {
            @Override
            protected void entryRemoved (boolean evicted, String url,
                                         Bitmap oldValue, Bitmap newValue) {
                if (oldValue != newValue) {
                    oldValue.recycle();
                }
            }

            @Override
            protected int sizeOf(String url, Bitmap bitmap) {
                int size;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    size = bitmap.getAllocationByteCount() / 1024;
                } else {
                    size = bitmap.getRowBytes() * bitmap.getHeight() / 1024;
                }
                return size == 0 ? 1 : size;
            }
        };
    }
    public boolean contains(String url) {
        return mMemCache.get(url) != null;
    }

    public Bitmap get(String url) {
        return mMemCache.get(url);
    }

    public void set(String url, Bitmap img) {
        mMemCache.put(url, img);

    }
}