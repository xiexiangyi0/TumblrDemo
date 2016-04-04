package com.xiangyixie.tumblrdemo.Activity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;

import com.tumblr.jumblr.JumblrClient;
import com.xiangyixie.tumblrdemo.AppConfig.AppConfigKey;
import com.xiangyixie.tumblrdemo.AppConfig.AppSwitch;
import com.xiangyixie.tumblrdemo.AsyncDrawable;
import com.xiangyixie.tumblrdemo.AvatarUrlTask;
import com.xiangyixie.tumblrdemo.ImageLoaderTask;
import com.xiangyixie.tumblrdemo.R;
import com.xiangyixie.tumblrdemo.adapter.PostListviewAdapter;
import com.xiangyixie.tumblrdemo.cache.ImageCache;
import com.xiangyixie.tumblrdemo.cache.PostCache;
import com.xiangyixie.tumblrdemo.getUserDashboardTask;
import com.xiangyixie.tumblrdemo.model.TumblrPhoto;
import com.xiangyixie.tumblrdemo.model.TumblrPhotoPost;
import com.xiangyixie.tumblrdemo.model.TumblrPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "MainActivity";
    private static final String JSON_CACHE_FILE = "TumblrDemeJsonCache";

    private JumblrClient client = null;
    private ListView listview = null;
    private PostListviewAdapter adapter = null;
    private SwipeRefreshLayout refreshLayout = null;

    private Bitmap defaultImage;
    private ImageCache imageCache;

    private PostCache postCache;

    private WeakReference<getUserDashboardTask> refreshTaskRef;



    private PostListviewAdapter.TumblrLoader tumblrLoader = new PostListviewAdapter.TumblrLoader() {
        private Map<String, String> avatarUrlCache = new HashMap<>();

        @Override
        public void loadPostImage(ImageView imageView, String url) {
            loadBitmap(imageView, url);
        }

        @Override
        public void loadAvatarImage(final ImageView imageView, final String blogName) {
            if (avatarUrlCache.containsKey(blogName)) {
                loadBitmap(imageView, avatarUrlCache.get(blogName));
            } else if (isNetworkConnected()) {
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

        // Authenticate via OAuth
        client = new JumblrClient(
                AppConfigKey.consumerKey,
                AppConfigKey.consumerSecret
        );
        client.setToken(
                AppConfigKey.oAuthToken,
                AppConfigKey.oAuthSecret
        );

        imageCache = new ImageCache(1024 * 1024);
        defaultImage = BitmapFactory.decodeResource(getResources(), R.drawable.loading);

        postCache = new PostCache(this);

        List<TumblrPost> data = new ArrayList<>();
        adapter = new PostListviewAdapter(this, client, data, tumblrLoader);
        listview = (ListView) findViewById(R.id.listview);
        listview.setAdapter(adapter);

        //pull to refresh, set 'refresh' listener.
        refreshLayout = (SwipeRefreshLayout)findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(this);

        boolean loaded = loadFromCache();
        if (!loaded) {
            onRefresh();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter.getData().size() == 0) {
            onRefresh();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        cachePosts();
        super.onDestroy();

    }

    @Override
    public void onRefresh() {

        if (!isNetworkConnected()) {
            refreshLayout.setRefreshing(false);
            return;
        }

        //get User Dashboard posts.
        imageCache = new ImageCache(1024 * 1024);
        getUserDashboardTask task = new getUserDashboardTask(this, client, adapter, refreshLayout);

        //getUserLikesTask task2 = new getUserLikesTask(this, client, adapter2, refreshLayout);

        refreshTaskRef = new WeakReference<getUserDashboardTask>(task);
        task.execute();
    }

    private void loadBitmap(ImageView imageView, final String url) {
        if (imageCache.contains(url)) {
            imageView.setImageBitmap(imageCache.get(url));
            return;
        }

        if (!isNetworkConnected()) {
            return;
        }

        boolean cancelled = cancelPotentialLoading(imageView, url);
        if (cancelled) {
            final WeakReference<ImageView> imageViewRef =
                    new WeakReference<ImageView>(imageView);
            final ImageLoaderTask task = new ImageLoaderTask(url);
            final Resources res = getResources();
            AsyncDrawable asyncDrawable = new AsyncDrawable(res, defaultImage, task);
            imageView.setImageDrawable(asyncDrawable);

            task.execute(new ImageLoaderTask.ImageLoadListener() {
                @Override
                public void onBitmap(Bitmap bitmap) {
                    //Log.d(TAG, "onBitmap " + bitmap);
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
                return false;
            }
        } else {
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
            return null;
        }
    }

    private void cachePosts() {
        List<TumblrPost> data = adapter.getData();
        if (data == null) {
            return;
        }

        if (AppSwitch.USE_SQL_CACHE) {
            postCache.reset();
            postCache.save(data);
            for (TumblrPost post : data) {
                if (post.getType() == TumblrPost.Type.PHOTO) {
                    cachePhotos(((TumblrPhotoPost)post).getPhotos());
                }
            }
            return;
        }

        JSONArray jarray = new JSONArray();

        for (TumblrPost post : data) {
            JSONObject jobj = null;
            try {
                if (post.getType() != TumblrPost.Type.UNSUPPORT) {
                    jobj = post.toJson();
                    if (post.getType() == TumblrPost.Type.PHOTO) {
                        TumblrPhotoPost photoPost = (TumblrPhotoPost) post;
                        cachePhotos(photoPost.getPhotos());
                    }
                }
            } catch (JSONException e) {
            }

            if (jobj != null) {
                jarray.put(jobj);
            }
        }

        File cacheFile = new File(getCacheDir(), JSON_CACHE_FILE);
        FileOutputStream outputStream;
        try {
            outputStream = new FileOutputStream(cacheFile);
            outputStream.write(jarray.toString().getBytes());
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cachePhotos(List<TumblrPhoto> photos) {
        for (TumblrPhoto photo : photos) {
            String url = photo.getUrl();
            if (imageCache.contains(url)) {
                Bitmap bitmap = imageCache.get(url);
                File imageFile = new File(
                        getCacheDir(), Base64.encodeToString(url.getBytes(), Base64.URL_SAFE));

                FileOutputStream outputStream;
                try {
                    outputStream = new FileOutputStream(imageFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
                    outputStream.flush();
                    outputStream.close();
                } catch (FileNotFoundException e) {
                    Log.w(TAG, "no file exist: " + imageFile.getName());
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.w(TAG, "fail to cache file " + imageFile.getName());
                    e.printStackTrace();
                }
            }
        }

    }

    private boolean loadFromCache() {
        if (AppSwitch.USE_SQL_CACHE) {
            List<TumblrPost> posts = postCache.load();
            if (posts == null || posts.isEmpty()) {
                return false;
            }
            loadBitmapFromPostList(posts);
            adapter.setData(posts);
            adapter.notifyDataSetChanged();
            return true;
        }
        File cacheFile = new File(getCacheDir(), JSON_CACHE_FILE);
        if (!cacheFile.exists()) {
            return false;
        }

        Log.d(TAG, "cache file exists");

        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(cacheFile);
            StringBuffer sb = new StringBuffer();
            int c;
            while ((c = inputStream.read()) != -1) {
                sb.append((char) c);
            }
            JSONArray jsonArray = new JSONArray(sb.toString());
            if (jsonArray.length() == 0) {
                return false;
            }
            loadFromJson(jsonArray);
        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void loadFromJson(JSONArray jsonArray) throws JSONException {
        int size = jsonArray.length();
        List<TumblrPost> posts = new ArrayList<>();
        for (int i=0; i<size; ++i) {
            JSONObject jpost = jsonArray.getJSONObject(i);
            TumblrPost post = TumblrPost.fromJson(jpost);

            posts.add(post);
        }

        loadBitmapFromPostList(posts);

        adapter.setData(posts);
        adapter.notifyDataSetChanged();
    }

    private void loadBitmapFromPostList(List<TumblrPost> posts) {
        for (TumblrPost post : posts) {
            if (post.getType() == TumblrPost.Type.PHOTO) {
                List<TumblrPhoto> photos = ((TumblrPhotoPost)post).getPhotos();
                for (TumblrPhoto photo : photos) {
                    File imageFile = new File(
                            getCacheDir(),
                            Base64.encodeToString(photo.getUrl().getBytes(), Base64.URL_SAFE));
                    if (imageFile.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                        imageCache.set(photo.getUrl(), bitmap);
                    }
                }
            }
        }

    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm =
                (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}


