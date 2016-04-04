package com.xiangyixie.tumblrdemo.model;

import com.tumblr.jumblr.types.Photo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xiangyixie on 1/17/16.
 */
public class TumblrPhoto {

    private Photo photo;
    private String url;

    public TumblrPhoto() {

    }

    public TumblrPhoto(Photo photo) {
        photo = photo;
        url = photo.getSizes().get(0).getUrl();
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jphoto = new JSONObject();
        jphoto.put("url", getUrl());

        return jphoto;
    }

    static TumblrPhoto parseJson(JSONObject jphoto) throws JSONException {
        TumblrPhoto photo = new TumblrPhoto();
        photo.url = jphoto.getString("url");

        return photo;
    }
}
