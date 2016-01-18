package com.xiangyixie.tumblrdemo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangyixie.tumblrdemo.R;

/**
 * Created by xiangyixie on 1/17/16.
 */
public class PhotoBodyView extends LinearLayout {
    LinearLayout photoList;
    TextView captionView;
    TextView tagView;
    public PhotoBodyView(Context context) {
        super(context);
        initLayout(context);
    }

    public PhotoBodyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout(context);
    }

    public void addImageView(ImageView imageView) {
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setAdjustViewBounds(true);
        LinearLayout.LayoutParams bottomMargin = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bottomMargin.bottomMargin = 24;
        photoList.addView(imageView, bottomMargin);
    }

    public void setCaption(CharSequence caption) {
        if (caption == null || caption.length() == 0) {
            captionView.setVisibility(GONE);
        } else {
            captionView.setVisibility(VISIBLE);
            captionView.setText(caption);
        }
    }

    public void setTagText(CharSequence tags) {
        if (tags == null || tags.length() == 0) {
            tagView.setVisibility(GONE);
        } else {
            tagView.setVisibility(VISIBLE);
            tagView.setText(tags);
        }
    }

    private void initLayout(Context context) {
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.listview_item_body_photo, this, true);

        photoList = (LinearLayout) findViewById(R.id.photopost_photolist);
        captionView = (TextView) findViewById(R.id.photopost_caption);
        tagView = (TextView) findViewById(R.id.photopost_tag);
    }
}
