package com.xiangyixie.tumblrdemo;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;

import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.Post;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "MainActivity";

    private ListView listview = null;
    private ListviewAdapter adapter = null;
    private SwipeRefreshLayout refreshLayout = null;

    private boolean needRefresh = false;

    private Bitmap defaultImage;
    private ImageCache imageCache;

    private WeakReference<getUserDashboardTask> refreshTaskRef;

    private ListviewAdapter.TumblrLoader tumblrLoader = new ListviewAdapter.TumblrLoader() {
        private Map<String, String> avatarUrlCache = new HashMap<>();

        @Override
        public void loadPostImage(ImageView imageView, String url) {
            loadBitmap(imageView, url);
        }

        @Override
        public void loadAvatarImage(final ImageView imageView, final String blogName) {
            if (avatarUrlCache.containsKey(blogName)) {
                loadBitmap(imageView, avatarUrlCache.get(blogName));
            } else {
                new AvatarUrlTask(blogName, null, new AvatarUrlTask.AvatarUrlListener() {
                    @Override
                    public void onUrl(String url) {
                        avatarUrlCache.put(blogName, url);
                        loadBitmap(imageView, url);
                    }
                }).execute();
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate");
        Log.d(TAG, "imageCache " + imageCache);
        imageCache = new ImageCache(1024 * 1024);
        defaultImage = BitmapFactory.decodeResource(getResources(), R.drawable.ellipsis);

        List<Post> data = new ArrayList<>();
        adapter = new ListviewAdapter(data, tumblrLoader);
        listview = (ListView) findViewById(R.id.listview);
        listview.setAdapter(adapter);

        //pull to refresh, set 'refresh' listener.
        refreshLayout = (SwipeRefreshLayout)findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(this);

        needRefresh = true;
        //onRefresh();

        if (savedInstanceState != null) {
            int testNum = savedInstanceState.getInt("TEST_KEY");
            Log.d(TAG, "testNUM " + testNum);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (needRefresh) {
            Log.d(TAG, "onResume refresh");
            needRefresh = false;
            onRefresh();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        outState.putInt("TEST_KEY", 123);
        Log.d(TAG, "testNUM save");
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        cacheImages();
        super.onDestroy();

    }

    @Override
    public void onRefresh() {
        //get User Dashboard posts.
        getUserDashboardTask task = new getUserDashboardTask(this, adapter, refreshLayout);
        refreshTaskRef = new WeakReference<getUserDashboardTask>(task);
        task.execute();
    }

    private void loadBitmap(ImageView imageView, final String url) {
        if (imageCache.contains(url)) {
            imageView.setImageBitmap(imageCache.get(url));
            return;
        }

        Log.d(TAG, "loadBitmap " + url);

        boolean cancelled = cancelPotentialLoading(imageView, url);
        if (cancelled) {
            Log.d(TAG, "loadBitmap cancelled");
            final WeakReference<ImageView> imageViewRef =
                    new WeakReference<ImageView>(imageView);
            final ImageLoaderTask task = new ImageLoaderTask(url);
            final Resources res = getResources();
            AsyncDrawable asyncDrawable = new AsyncDrawable(res, defaultImage, task);
            imageView.setImageDrawable(asyncDrawable);

            task.execute(new ImageLoaderTask.ImageLoadListener() {
                @Override
                public void onBitmap(Bitmap bitmap) {
                    Log.d(TAG, "onBitmap " + url);
                    ImageView localImageView = imageViewRef.get();
                    if (localImageView != null && bitmap != null) {
                        ImageLoaderTask localTask =
                                getImageLoaderTask(localImageView);
                        if (localTask == task) {
                            localImageView.setImageBitmap(bitmap);
                            imageCache.set(url, bitmap);
                        }
                    }

                }
            });
        }
    }

    private boolean cancelPotentialLoading(ImageView imageView, String url) {
        ImageLoaderTask task = getImageLoaderTask(imageView);
        if (task != null) {
            String taskUrl = task.getUrl();
            if (!taskUrl.equals(url)) {
                task.cancel(true);
                return true;
            } else {
                Log.d(TAG, "taskUrl " + taskUrl);
                Log.d(TAG, "url " + url);
                return false;
            }
        } else {
            Log.d(TAG, "task == null");
            return true;
        }
    }

    private static ImageLoaderTask getImageLoaderTask(ImageView imageView) {
        if (imageView == null) {
            return null;
        }

        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof AsyncDrawable) {
            AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
            return asyncDrawable.getImageLoaderTask();
        } else {
            Log.d(TAG, "drawable == null " + drawable);
            return null;
        }
    }

    private void cacheImages() {
        List<Post> data = adapter.getData();
        Log.d(TAG, "cacheImages " + data);
        if (data == null) {
            return;
        }

        JSONArray jarray = new JSONArray();

        for (Post post : data) {
            JSONObject jobj = null;
            Log.d(TAG, post.getType().toString());
            try {
                if (post.getType().equals("photo")) {
                    jobj = dumpPhotoPost((PhotoPost) post);
                }
            } catch (JSONException e) {
            }

            if (jobj != null) {
                jarray.put(jobj);
            }
        }

        Log.d(TAG, "dump " + jarray.length() + " posts");
    }

    private static JSONObject dumpPhotoPost(PhotoPost post) throws JSONException {
        JSONObject jobj = new JSONObject(post.detail());
        List<Photo> photos = post.getPhotos();

        for (Photo photo : photos) {
            JSONObject jphoto = new JSONObject();
        }

        return jobj;
    }

}


