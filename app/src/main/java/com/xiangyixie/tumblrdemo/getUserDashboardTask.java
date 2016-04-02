package com.xiangyixie.tumblrdemo;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Post;
import com.xiangyixie.tumblrdemo.adapter.PostListviewAdapter;
import com.xiangyixie.tumblrdemo.model.TumblrPost;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiangyixie on 1/16/16.
 */
public class getUserDashboardTask extends AsyncTask<Void, Integer, List<TumblrPost>> {
    private final String TAG = "getUserDashboardTask";

    private Activity activity = null;
    private static JumblrClient client = null;
    private PostListviewAdapter adapter = null;

    private SwipeRefreshLayout refreshLayout = null;


    public getUserDashboardTask(Activity activity, JumblrClient client, PostListviewAdapter adapter, SwipeRefreshLayout refreshLayout) {
        this.client = client;
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
    protected List<TumblrPost> doInBackground(Void... params) {
        // get UserDashboard posts request
        List<Post> posts = client.userDashboard();
        ArrayList<TumblrPost> tumblrPosts = new ArrayList<>();

        for (Post post : posts) {
            TumblrPost tumblrPost = TumblrPost.fromJumblr(post);
            tumblrPosts.add(tumblrPost);
        }

        return tumblrPosts;
    }

    @Override
    protected void onPostExecute(List<TumblrPost> result) {
        // set refresh circle to stop.
        refreshLayout.setRefreshing(false);
        adapter.setData(result);
        adapter.notifyDataSetChanged();
    }
}


