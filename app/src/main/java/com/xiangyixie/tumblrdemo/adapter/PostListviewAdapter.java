package com.xiangyixie.tumblrdemo.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tumblr.jumblr.JumblrClient;
import com.xiangyixie.tumblrdemo.R;
import com.xiangyixie.tumblrdemo.model.TumblrPhoto;
import com.xiangyixie.tumblrdemo.model.TumblrPhotoPost;
import com.xiangyixie.tumblrdemo.model.TumblrPost;
import com.xiangyixie.tumblrdemo.model.TumblrTextPost;
import com.xiangyixie.tumblrdemo.view.PhotoBodyView;
import com.xiangyixie.tumblrdemo.view.PostView;
import com.xiangyixie.tumblrdemo.view.TextBodyView;

import java.util.List;

/**
 * Created by xiangyixie on 1/15/16.
 */

public class PostListviewAdapter extends BaseAdapter {
    public interface TumblrLoader {
        void loadPostImage(ImageView imageView, String url);

        void loadAvatarImage(ImageView imageView, String blogName);
    }

    private final String TAG = "ListviewAdapter";

    private Activity activity;
    private JumblrClient client;
    private List<TumblrPost> data = null;
    private TumblrLoader tumblrLoader;

    public PostListviewAdapter(Activity activity, JumblrClient client, List<TumblrPost> data, TumblrLoader loader) {
        this.activity = activity;
        this.client = client;
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

        final TumblrPost post = data.get(position);
        Log.d(TAG, "post type = " + post.getType());


        //header
        //blog avatar
        final ImageView avatarImageView = view.getAvatarView();
        tumblrLoader.loadAvatarImage(avatarImageView, post.getBlogName());

        //blog name
        String blogname = post.getBlogName();
        view.setBlogName(blogname);


        //body
        //photo post
        if (post.getType() == TumblrPost.Type.PHOTO) {
            view.setBodyView(buildPhotoBody((TumblrPhotoPost) post, parent.getContext()));
        }
        //text post
        else if (post.getType() == TumblrPost.Type.TEXT) {
            view.setBodyView(buildTextBody((TumblrTextPost) post, parent.getContext()));
        }
        //default, empty post
        else {
            view.setBodyView(new View(parent.getContext()));
        }



        //footer
        //note count
        view.setNoteView(post.getNoteCount());
        final TextView noteCountTxtView = (TextView) view.getNoteCountView();

        //share button
        LinearLayout shareLayout = (LinearLayout) view.getShareLayout();
        shareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                //share post short url
                String shareMsg = post.getShortUrl();
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "See this Tumblr post:");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareMsg);

                activity.startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });

        //like button initiate
        final LinearLayout likeLayout = (LinearLayout) view.getLikeLayout();
        final ImageView likeImg = (ImageView) view.getLikeImg();
        if (post.getIsLiked()) {
            likeImg.setImageResource(R.drawable.like_red);
        } else {
            likeImg.setImageResource(R.drawable.like);
        }

        // click 'like'
        final String reblogKey = post.getReblogKey();
        final Long postId = post.getPostId();
        likeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!post.getIsLiked()) {
                    likeImg.setImageResource(R.drawable.like_red);
                    // 'like' action backend operation
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            client.like(postId, reblogKey);
                            return null;
                        }
                    }.execute();
                    // set 'liked' flag for TumblrPost post wrapper
                    post.setLiked(true);
                    // note count + 1 in UI
                    Long noteCount = post.getNoteCount() + 1;
                    post.setNoteCount(noteCount);
                    noteCountTxtView.setText(noteCount + " notes");

                } else {
                    likeImg.setImageResource(R.drawable.like);
                    // 'unlike' action backend operation
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            client.unlike(postId, reblogKey);
                            return null;
                        }
                    }.execute();
                    // set 'unliked' flag for TumblrPost post wrapper
                    post.setLiked(false);
                    // note count - 1 in UI
                    Long noteCount = post.getNoteCount() - 1;
                    post.setNoteCount(noteCount);
                    if(noteCount > 0){
                        noteCountTxtView.setText(noteCount + " notes");
                    }else{
                        noteCountTxtView.setText("");
                    }
                }
            }
        });

        return view;
    }

    public List<TumblrPost> getData() {
        return data;
    }

    public void setData(List<TumblrPost> data) {
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

    private PhotoBodyView buildPhotoBody(TumblrPhotoPost photoPost, Context context) {
        PhotoBodyView photoView = new PhotoBodyView(context);
        //photos
        List<TumblrPhoto> photos = photoPost.getPhotos();
        //Log.d(TAG, "A photoPost: photo size = " + photos.size());
        for (TumblrPhoto photo : photos) {
            ImageView imgView = new ImageView(context);
            photoView.addImageView(imgView);
            String url = photo.getUrl();
            Log.d(TAG, "photo url " + url);
            tumblrLoader.loadPostImage(imgView, url);
        }

        //caption
        String caption = photoPost.getCaption();
        //photoView.setWebViewContent(caption);
        photoView.setCaption(trim(Html.fromHtml(caption)));

        //tags
        List<String> tags = photoPost.getTags();
        String tagStr = "";
        for (String tag : tags) {
            tagStr = tagStr + " #" + tag;
        }
        photoView.setTagText(tagStr);

        return photoView;
    }

    private TextBodyView buildTextBody(TumblrTextPost textPost, Context context) {
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

    static class ViewHolder {
        TextView text;
        ImageView icon;
    }
}

