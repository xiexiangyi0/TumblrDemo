package com.xiangyixie.tumblrdemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by xiangyixie on 1/15/16.
 */

//download Image bitmap from url
public class ImageLoaderTask extends AsyncTask<ImageLoaderTask.ImageLoadListener, Integer, Bitmap> {
    public interface ImageLoadListener {
        void onBitmap(Bitmap bitmap);
    }
    private String TAG = "ImageLoaderTask";

    private ImageLoadListener loadListener;
    private String imageUrl;

    public ImageLoaderTask(String url) {
        imageUrl = url;
    }

    public String getUrl() {
        return imageUrl;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    protected Bitmap doInBackground(ImageLoadListener... args) {
        loadListener = args[0];
        if (isCancelled()) {
            return null;
        }

        Bitmap bmap = null;
        try {
            URL url = new URL(imageUrl);
            Log.d(TAG, "download image task : url == " + url);
            bmap = BitmapFactory.decodeStream((InputStream) url.getContent());
            if(bmap == null){
                Log.d(TAG, "bitmap == NULL");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bmap;
    }

    protected void onPostExecute(Bitmap imageBitmap) {
        loadListener.onBitmap(imageBitmap);
    }
}
