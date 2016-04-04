package com.xiangyixie.tumblrdemo.model;

import android.util.Base64;
import android.util.Log;

import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiangyixie on 1/17/16.
 */
public class TumblrPhotoPost extends TumblrPost {

    private String caption;
    private List<String> tags;
    private List<TumblrPhoto> photos;

    public TumblrPhotoPost(PhotoPost post) {
        super(post);
        Log.d("MYDEBUG", post.toString());
        List<Photo> jumblrPhotos = photoPost().getPhotos();
        photos = new ArrayList<>();
        for (Photo photo : jumblrPhotos) {
            photos.add(new TumblrPhoto(photo));
        }

        tags = photoPost().getTags();
        caption = photoPost().getCaption();
    }

    public TumblrPhotoPost() {
        super();
    }

    @Override
    public Type getType() {
        return Type.PHOTO;
    }

    public String getCaption() {
        return caption == null ? "" : caption;
    }
    public void setCaption(String caption) {
        this.caption = caption;
    }

    public List<String> getTags() {
        return tags;
    }
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<TumblrPhoto> getPhotos() {
        return photos;
    }
    public void setPhotos(List<TumblrPhoto> photos) {
        this.photos = photos;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jpost = super.toJson();
        jpost.put("caption", Base64.encodeToString(getCaption().getBytes(), Base64.URL_SAFE));

        JSONArray jtags = new JSONArray();
        for (String tag : tags) {
            jtags.put(tag);
        }
        jpost.put("tags", jtags);

        JSONArray jphotos = new JSONArray();
        for (TumblrPhoto photo : getPhotos()) {
            jphotos.put(photo.toJson());
        }
        jpost.put("photos", jphotos);

        return jpost;
    }

    private PhotoPost photoPost() {
        return (PhotoPost) post;
    }

    static TumblrPhotoPost parseJson(JSONObject jpost, TumblrPhotoPost post) throws JSONException {
        TumblrPost.parseJson(jpost, post);
        post.caption = new String(
                Base64.decode(jpost.getString("caption").getBytes(), Base64.URL_SAFE));

        JSONArray jtags = jpost.getJSONArray("tags");
        List<String> tags = new ArrayList<>();
        int size = jtags.length();
        for (int i=0; i<size; ++i) {
            tags.add(jtags.getString(i));
        }
        post.tags = tags;

        JSONArray jphotos = jpost.getJSONArray("photos");
        List<TumblrPhoto> photos = new ArrayList<>();
        size = jphotos.length();
        for (int i=0; i<size; ++i) {
            photos.add(TumblrPhoto.parseJson(jphotos.getJSONObject(i)));
        }
        post.photos = photos;
        return post;
    }
}
