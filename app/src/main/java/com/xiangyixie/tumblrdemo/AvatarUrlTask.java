package com.xiangyixie.tumblrdemo;

import android.os.AsyncTask;

/**
 * Created by xiangyixie on 1/17/16.
 */
public class AvatarUrlTask extends AsyncTask<Void, Integer, String> {
    public interface AvatarUrlListener {
        void onUrl(String url);
    }

    private String blogName;
    private Integer size;

    private AvatarUrlListener listener;
    public AvatarUrlTask(String blogName, Integer size, AvatarUrlListener l) {
        this.blogName = blogName;
        this.size = size;
        this.listener = l;
    }
    @Override
    protected String doInBackground(Void... params) {
        return new JumblrClientHelper().blogAvatar(blogName, size);
    }

    @Override
    protected void onPostExecute(String avatarUrl) {
        listener.onUrl(avatarUrl);
    }
}
