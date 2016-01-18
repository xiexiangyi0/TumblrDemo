package com.xiangyixie.tumblrdemo.model;

import com.tumblr.jumblr.types.Post;

/**
 * Created by xiangyixie on 1/17/16.
 */
public class TumblrPost {
    enum Type {
        UNSUPPORT, PHOTO, TEXT
    }

    protected Post post;
}
