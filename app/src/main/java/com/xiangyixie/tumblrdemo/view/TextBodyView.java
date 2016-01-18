package com.xiangyixie.tumblrdemo.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xiangyixie.tumblrdemo.R;

/**
 * Created by xiangyixie on 1/17/16.
 */
public class TextBodyView extends LinearLayout {
    private TextView titleView;
    private TextView contentView;
    public TextBodyView(Context context) {
        super(context);
        initLayout(context);
    }

    public void setTitle(CharSequence title) {
        if (title == null || title.length() == 0) {
            titleView.setVisibility(GONE);
        } else {
            titleView.setVisibility(VISIBLE);
            titleView.setText(title);
        }
    }

    public void setContent(CharSequence content) {
        if (content == null || content.length() == 0) {
            contentView.setVisibility(GONE);
        } else {
            contentView.setVisibility(VISIBLE);
            contentView.setText(content);
        }
    }

    private void initLayout(Context context) {
        LayoutInflater inflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.listview_item_body_text, this, true);

        titleView = (TextView) findViewById(R.id.textpost_title);
        contentView = (TextView) findViewById(R.id.textpost_content);
    }
}
