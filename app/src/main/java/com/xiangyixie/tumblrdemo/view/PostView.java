package com.xiangyixie.tumblrdemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangyixie.tumblrdemo.R;

/**
 * Created by xiangyixie on 1/17/16.
 */
public class PostView extends LinearLayout {
    private ImageView avatarView;
    private TextView blogNameView;
    private FrameLayout bodyView;
    private TextView noteView;
    private LinearLayout shareLayout;
    private ImageView shareImg;
    private LinearLayout likeLayout;
    private ImageView likeImg;


    public PostView(Context context) {
        super(context);
        initLayout(context);
    }

    public PostView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout(context);
    }

    public ImageView getAvatarView() {
        return avatarView;
    }

    public void setBlogName(String name) {
        blogNameView.setText(name);
    }

    public void setNoteView(long num) {
        if(num > 0){
            String noteStr = "" + num + " notes";
            noteView.setText(noteStr);
        }
    }

    public void setBodyView(View v) {
        bodyView.removeAllViews();
        bodyView.addView(v);
    }

    private void initLayout(Context context) {
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.listview_item, this, true);

        // avatar
        avatarView = (ImageView) findViewById(R.id.post_user_avatar);

        // blogname
        blogNameView = (TextView) findViewById(R.id.post_user_blogname);
        blogNameView.setTextSize(13);

        // note
        noteView = (TextView) findViewById(R.id.noteCount);

        // body
        bodyView = (FrameLayout) findViewById(R.id.listview_item_body);

        // share btn
        shareLayout = (LinearLayout) findViewById(R.id.shareBtn);
        shareImg = (ImageView) findViewById(R.id.shareImgView);

        // like btn
        likeLayout = (LinearLayout) findViewById(R.id.likeBtn);
        likeImg = (ImageView) findViewById(R.id.likeImgView);
    }

    public LinearLayout getShareLayout (){
        return shareLayout;
    }

    public LinearLayout getLikeLayout (){
        return likeLayout;
    }

    public ImageView getShareImg(){
        return shareImg;
    }

    public ImageView getLikeImg(){
        return likeImg;
    }

    public TextView getNoteCountView(){
        return noteView;
    }
}
