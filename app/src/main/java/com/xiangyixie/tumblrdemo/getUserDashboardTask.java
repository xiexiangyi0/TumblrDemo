package com.xiangyixie.tumblrdemo;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;

import com.tumblr.jumblr.JumblrClient;

import java.util.List;

/**
 * Created by xiangyixie on 1/16/16.
 */
public class getUserDashboardTask extends AsyncTask<Void, Integer, Void> {
    private final String TAG = "getUserDashboardTask";

    private Activity activity = null;
    private static JumblrClient client = null;
    private ListviewAdapter adapter = null;

    private SwipeRefreshLayout refreshLayout = null;


    public getUserDashboardTask(Activity activity, ListviewAdapter adapter, SwipeRefreshLayout refreshLayout) {
        // Authenticate via OAuth
        client = new JumblrClient(
                AppConfigKey.consumerKey,
                AppConfigKey.consumerSecret
        );
        client.setToken(
                AppConfigKey.oAuthToken,
                AppConfigKey.oAuthSecret
        );
        this.adapter = adapter;
        this.activity = activity;
        this.refreshLayout = refreshLayout;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //dialog = ProgressDialog.show(activity, "", "Please wait...");
    }

    @Override
    protected Void doInBackground(Void... params) {
        // get UserDashboard posts request
        List<com.tumblr.jumblr.types.Post> posts = client.userDashboard();
        adapter.setData(posts);

        Log.d(TAG, "posts size = " + posts.size());
        for (com.tumblr.jumblr.types.Post p : posts) {
            Log.d(TAG, "url = " + p.getPostUrl());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        // set refresh circle to stop.
        refreshLayout.setRefreshing(false);
        adapter.notifyDataSetChanged();
    }
}


