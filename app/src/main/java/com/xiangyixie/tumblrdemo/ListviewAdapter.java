package com.xiangyixie.tumblrdemo;

import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.PhotoSize;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.TextPost;
import com.xiangyixie.tumblrdemo.view.PhotoBodyView;
import com.xiangyixie.tumblrdemo.view.PostView;
import com.xiangyixie.tumblrdemo.view.TextBodyView;

import java.util.List;

/**
 * Created by xiangyixie on 1/15/16.
 */


public class ListviewAdapter extends BaseAdapter {
    public interface TumblrLoader {
        void loadPostImage(ImageView imageView, String url);
        void loadAvatarImage(ImageView imageView, String blogName);
    }

    private final String TAG = "ListviewAdapter";

    private List<com.tumblr.jumblr.types.Post> data = null;
    private TumblrLoader tumblrLoader;

    public ListviewAdapter(List<com.tumblr.jumblr.types.Post> data, TumblrLoader loader) {
        this.data = data;
        this.tumblrLoader = loader;
    }

    @Override
    public int getCount() {
        Log.d(TAG, "Count = " + data.size());
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < data.size()) {
            return data.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "pos = " + position);
        PostView view = (PostView) convertView;

        if (view == null) {
            view = new PostView(parent.getContext());
        }

        Post post = data.get(position);
        Log.d(TAG, "post type = " + post.getType());

        //header
        //blog avatar
        final ImageView avatarImageView = view.getAvatarView();
        tumblrLoader.loadAvatarImage(avatarImageView, post.getBlogName());

        //blog name
        String blogname = post.getBlogName();
        view.setBlogName(blogname);

        //photo post
        if (post.getType().equals("photo")) {
            view.setBodyView(buildPhotoBody((PhotoPost)post, parent.getContext()));
        }
        //text post
        else if(post.getType().equals("text")){
            TextPost textPost = (TextPost)post;
            view.setBodyView(buildTextBody(textPost, parent.getContext()));
        }
        // default, empty
        else {
            view.setBodyView(new View(parent.getContext()));
        }

        //footer
        view.setNote(post.getNoteCount());

        return view;
    }

    public List<com.tumblr.jumblr.types.Post> getData() {
        return data;
    }

    public void setData(List<com.tumblr.jumblr.types.Post> data) {
        this.data = data;
    }

    public static CharSequence trim(CharSequence s) {
        return trim(s, 0, s.length());
    }

    public static CharSequence trim(CharSequence s, int start, int end) {
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }

        return s.subSequence(start, end);
    }

    private PhotoBodyView buildPhotoBody(PhotoPost photoPost, Context context) {
        PhotoBodyView photoView = new PhotoBodyView(context);
        //photos
        List<Photo> photos = photoPost.getPhotos();
        //Log.d(TAG, "A photoPost: photo size = " + photos.size());
        for (int i = 0; i < photos.size(); ++i) {
            //Log.d(TAG, "photo index = " + i);
            List<PhotoSize> allPhotos = photos.get(i).getSizes();
            for (PhotoSize size : allPhotos) {
                ImageView imgView = new ImageView(context);
                photoView.addImageView(imgView);

                String strUrl = size.getUrl();
                Log.d(TAG, "listview image url = " + strUrl);
                tumblrLoader.loadPostImage(imgView, strUrl);
                break;
            }
        }

        //caption
        String caption = photoPost.getCaption();
        //photoView.setWebViewContent(caption);
        photoView.setCaption(trim(Html.fromHtml(caption)));

        //tags
        List<String> tags = photoPost.getTags();
        String tagStr = "";
        for(String tag : tags){
            tagStr = tagStr + " #" + tag;
        }
        photoView.setTagText(tagStr);

        return photoView;
    }

    private TextBodyView buildTextBody(TextPost textPost, Context context) {
        TextBodyView bodyView = new TextBodyView(context);
        if (textPost.getTitle() == null) {
            Log.d(TAG, "null title");
            bodyView.setTitle("");
        } else {
            bodyView.setTitle(trim(Html.fromHtml(textPost.getTitle())));
        }
        if (textPost.getBody() == null) {
            Log.d(TAG, "null body");
            bodyView.setContent("");
        } else {
            bodyView.setContent(trim(Html.fromHtml(textPost.getBody())));
        }
        return bodyView;
    }


}

