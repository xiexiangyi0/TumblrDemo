package com.xiangyixie.tumblrdemo.model;

import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.TextPost;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by xiangyixie on 1/17/16.
 */
public class TumblrPost {
    public enum Type {
        UNSUPPORT("unsupport"),
        PHOTO("photo"),
        TEXT("text");

        private String type;

        Type(String t) {
            type = t;
        }

        public String getValue() {
            return type;
        }

    }

    protected Post post;
    protected String blogName;
    protected long noteCount;

    public TumblrPost() {
        this.post = null;
    }
    public TumblrPost(Post post) {
        this.post = post;
        blogName = post.getBlogName();
        noteCount = post.getNoteCount();
    }

    public Type getType() {
        return Type.UNSUPPORT;
    }

    public String getBlogName() {
        return blogName;
    }

    public long getNoteCount() {
        return noteCount;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject jpost = new JSONObject();
        jpost.put("type", getType().getValue());
        if (getBlogName() == null || getBlogName().isEmpty()) {
            throw new JSONException("null blog_name");
        }
        jpost.put("blog_name", getBlogName());
        jpost.put("note_count", getNoteCount());

        return jpost;
    }

    public static TumblrPost fromJson(JSONObject jpost) throws JSONException {
        String typeStr = jpost.getString("type");
        if (typeStr.equals("photo")) {
            return TumblrPhotoPost.parseJson(jpost, new TumblrPhotoPost());
        } else if (typeStr.equals("text")) {
            return TumblrTextPost.parseJson(jpost, new TumblrTextPost());
        } else {
            return TumblrPost.parseJson(jpost, new TumblrPost());
        }
    }

    public static TumblrPost fromJumblr(Post post) {
        if (post.getType().equals("photo")) {
            return new TumblrPhotoPost((PhotoPost) post);
        } else if (post.getType().equals("text")) {
            return new TumblrTextPost((TextPost) post);
        } else {
            return new TumblrPost(post);
        }
    }

    static TumblrPost parseJson(JSONObject jpost, TumblrPost post) throws JSONException {
        post.blogName = jpost.getString("blog_name");
        post.noteCount = jpost.getLong("note_count");
        return post;
    }
}
