package com.xiangyixie.tumblrdemo.model;

import android.util.Base64;

import com.tumblr.jumblr.types.TextPost;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xiangyixie on 1/17/16.
 */
public class TumblrTextPost extends TumblrPost {
    private String title;
    private String body;

    public TumblrTextPost(TextPost post) {
        super(post);
        title = post.getTitle();
        body = post.getBody();
    }

    public TumblrTextPost() {
        super();
    }

    @Override
    public Type getType() {
        return Type.TEXT;
    }

    public String getTitle() {
        return title == null ? "" : title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body == null ? "" : body;
    }
    public void setBody(String body) {
        this.body = body;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jpost = super.toJson();
        jpost.put("title", Base64.encodeToString(getTitle().getBytes(), Base64.URL_SAFE));
        jpost.put("body", Base64.encodeToString(getBody().getBytes(), Base64.URL_SAFE));

        return jpost;
    }

    static TumblrTextPost parseJson(JSONObject jpost, TumblrTextPost post) throws JSONException {
        TumblrPost.parseJson(jpost, post);
        post.title = new String(Base64.decode(
                jpost.getString("title").getBytes(), Base64.URL_SAFE));
        post.body = new String(Base64.decode(jpost.getString("body"), Base64.URL_SAFE));
        return post;
    }
}
