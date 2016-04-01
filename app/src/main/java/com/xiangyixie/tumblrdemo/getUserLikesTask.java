package com.xiangyixie.tumblrdemo;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;

import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Post;
import com.xiangyixie.tumblrdemo.AppConfig.AppConfigKey;
import com.xiangyixie.tumblrdemo.model.TumblrPost;
import com.xiangyixie.tumblrdemo.view.PostListviewAdapter;

import java.util.ArrayList;
import java.util.List;


public class getUserLikesTask extends AsyncTask<Void, Integer, List<TumblrPost>> {
    private final String TAG = "getUserLikesTask";

    private Activity activity = null;
    private static JumblrClient client = null;
    private PostListviewAdapter adapter = null;

    private SwipeRefreshLayout refreshLayout = null;


    public getUserLikesTask(Activity activity, PostListviewAdapter adapter, SwipeRefreshLayout refreshLayout) {
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
    protected List<TumblrPost> doInBackground(Void... params) {
        // get UserDashboard posts request
        List<Post> posts = client.userLikes();
        ArrayList<TumblrPost> tumblrPosts = new ArrayList<>();

        for (Post post : posts) {
            TumblrPost tumblrPost = TumblrPost.fromJumblr(post);
            tumblrPosts.add(tumblrPost);
            post.getShortUrl();
        }

        return tumblrPosts;
    }

    @Override
    protected void onPostExecute(List<TumblrPost> result) {
        // set refresh circle to stop.
        refreshLayout.setRefreshing(false);
        //adapter.setData(result);
        //adapter.notifyDataSetChanged();
    }
}



