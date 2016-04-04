package com.xiangyixie.tumblrdemo.cache;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.xiangyixie.tumblrdemo.model.TumblrPhoto;
import com.xiangyixie.tumblrdemo.model.TumblrPhotoPost;
import com.xiangyixie.tumblrdemo.model.TumblrPost;
import com.xiangyixie.tumblrdemo.model.TumblrTextPost;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xiexiangyi on 4/2/16.
 */

/*
 * Table POST = ID:id_t, is_liked:boolean, reblog_key:string, url:string, short_url:string,
 *              blog_name:string, note_count:long, type:string
 * Table PHOTO_POST = ID:id_t, post_id:id_t, caption:string
 * Table PHOTO_TAG = ID:id_t, post_id:id_t, tag:string
 * Table PHOTO = ID:id_t, post_id:id_t, url:string, file_path:string
 * Table TEXT_POST = ID:id_t, post_id:id_t, title:string, body:string
 */

public class PostCache {
    private final static String TAG = "PostCache";
    static class NameType {
        String name;
        String type;
        public NameType(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    abstract static class DbTable implements BaseColumns {
        final static String TYPE_INT = "INTEGER";
        final static String TYPE_BOOL = "INTEGER";
        final static String TYPE_STRING = "TEXT";

        public abstract String tableName();
        public abstract NameType primaryKey();
        public abstract NameType[] columns();
    }

    static class TablePost extends DbTable {
        final static String TABLE_NAME = "POST";
        final static String COL_TYPE = "type";
        final static String COL_ID = "post_id";
        final static String COL_LIKE = "is_liked";
        final static String COL_REBLOG = "reblog_key";
        final static String COL_URL = "url";
        final static String COL_SHORT_URL = "short_url";
        final static String COL_BLOG_NAME = "blog_name";
        final static String COL_NOTE_COUNT = "note_count";

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public NameType primaryKey() {
            return new NameType(COL_ID, TYPE_INT);
        }

        @Override
        public NameType[] columns() {
            return new NameType[] {
                    new NameType(COL_TYPE, TYPE_STRING),
                    new NameType(COL_LIKE, TYPE_BOOL),
                    new NameType(COL_REBLOG, TYPE_STRING),
                    new NameType(COL_URL, TYPE_STRING),
                    new NameType(COL_SHORT_URL, TYPE_STRING),
                    new NameType(COL_BLOG_NAME, TYPE_STRING),
                    new NameType(COL_NOTE_COUNT, TYPE_INT),
            };
        }
    }

    static class TablePhotoPost extends DbTable {
        final static String TABLE_NAME = "PHOTO_POST";
        final static String COL_ID = "post_photo_id";
        final static String COL_POST_ID = "post_id";
        final static String COL_CAPTION = "caption";

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public NameType primaryKey() {
            return new NameType(COL_ID, TYPE_INT);
        }

        @Override
        public NameType[] columns() {
            return new NameType[] {
                    new NameType(COL_POST_ID, TYPE_STRING),
                    new NameType(COL_CAPTION, TYPE_STRING),
            };
        }
    }

    static class TablePhotoTag extends DbTable {
        final static String TABLE_NAME = "PHOTO_TAG";
        final static String COL_ID = "photo_tag_id";
        final static String COL_POST_ID = "post_id";
        final static String COL_CONTENT = "content";

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public NameType primaryKey() {
            return new NameType(COL_ID, TYPE_STRING);
        }

        @Override
        public NameType[] columns() {
            return new NameType[] {
                    new NameType(COL_POST_ID, TYPE_INT),
                    new NameType(COL_CONTENT, TYPE_STRING),
            };
        }
    }

    static class TablePhoto extends DbTable {
        final static String TABLE_NAME = "PHOTO";
        final static String COL_ID = "photo_id";
        final static String COL_POST_ID = "post_id";
        final static String COL_URL = "url";

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public NameType primaryKey() {
            return new NameType(COL_ID, TYPE_STRING);
        }

        @Override
        public NameType[] columns() {
            return new NameType[] {
                    new NameType(COL_POST_ID, TYPE_INT),
                    new NameType(COL_URL, TYPE_STRING),
            };
        }
    }

    static class TableTextPost extends DbTable {
        final static String TABLE_NAME = "TEXT_POST";
        final static String COL_ID = "text_post_id";
        final static String COL_POST_ID = "post_id";
        final static String COL_TITLE = "title";
        final static String COL_BODY = "body";

        @Override
        public String tableName() {
            return TABLE_NAME;
        }

        @Override
        public NameType primaryKey() {
            return new NameType(COL_ID, TYPE_INT);
        }

        @Override
        public NameType[] columns() {
            return new NameType[] {
                    new NameType(COL_POST_ID, TYPE_INT),
                    new NameType(COL_TITLE, TYPE_STRING),
                    new NameType(COL_BODY, TYPE_STRING),
            };
        }
    }

    static class DbHelper extends SQLiteOpenHelper {
        private final static String DB_NAME = "POST_DB";
        private final static int DB_VERSION = 1;

        private DbTable[] mTables = new DbTable[] {
                new TablePost(),
                new TablePhotoPost(),
                new TablePhotoTag(),
                new TablePhoto(),
                new TableTextPost(),
        };

        public DbHelper(Context context) {
            super(context, context.getApplicationContext().getPackageName() + "." + DB_NAME,
                    null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
           for (DbTable table : mTables) {
               db.execSQL(createTable(table));
           }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            for (DbTable table : mTables) {
                db.execSQL(dropTable(table));
            }
            onCreate(db);
        }

        private static String createTable(DbTable table) {
            StringBuilder stringBuilder = new StringBuilder(
                    "CREATE TABLE " + table.tableName()
                            + "(" + table.primaryKey().name + " "
                            + table.primaryKey().type + " PRIMARY KEY");
            NameType[] columns = table.columns();
            for (NameType column : columns) {
                stringBuilder.append(", ").append(column.name).append(" ").append(column.type);
            }
            stringBuilder.append(")");

            return stringBuilder.toString();
        }

        private static String dropTable(DbTable table) {
            return "DROP TABLE IF EXISTS " + table.tableName();
        }
    }

    private Context mContext;
    private DbHelper mDbHelper;

    public PostCache(Context context) {
        mContext = context;
        mDbHelper = new DbHelper(context);
    }

    // save
    public void save(List<TumblrPost> posts) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        for (TumblrPost post : posts) {
            if (post.getType() != TumblrPost.Type.UNSUPPORT) {
                savePost(post, db);
            }
        }
    }

    public void savePost(TumblrPost post, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TablePost.COL_ID, post.getPostId());
        contentValues.put(TablePost.COL_TYPE, post.getType().getValue());
        contentValues.put(TablePost.COL_LIKE, post.getIsLiked());
        contentValues.put(TablePost.COL_URL, post.getUrl());
        contentValues.put(TablePost.COL_SHORT_URL, post.getShortUrl());
        contentValues.put(TablePost.COL_REBLOG, post.getReblogKey());
        contentValues.put(TablePost.COL_BLOG_NAME, post.getBlogName());
        contentValues.put(TablePost.COL_NOTE_COUNT, post.getNoteCount());
        db.replace(TablePost.TABLE_NAME, null, contentValues);

        if (post.getType() == TumblrPost.Type.PHOTO) {
            savePhotoPost((TumblrPhotoPost) post, db);
        } else if (post.getType() == TumblrPost.Type.TEXT) {
            saveTextPost((TumblrTextPost) post, db);
        }
    }

    private void saveTextPost(TumblrTextPost post, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TableTextPost.COL_ID, post.getPostId());
        contentValues.put(TableTextPost.COL_POST_ID, post.getPostId());
        contentValues.put(TableTextPost.COL_TITLE, post.getTitle());
        contentValues.put(TableTextPost.COL_BODY, post.getBody());

        db.replace(TableTextPost.TABLE_NAME, null, contentValues);
    }

    public void savePhotoPost(TumblrPhotoPost post, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TablePhotoPost.COL_ID, post.getPostId());
        contentValues.put(TablePhotoPost.COL_POST_ID, post.getPostId());
        contentValues.put(TablePhotoPost.COL_CAPTION, post.getCaption());
        db.replace(TablePhotoPost.TABLE_NAME, null, contentValues);

        // tags
        for (String tag : post.getTags()) {
            ContentValues tagContentValues = new ContentValues();
            tagContentValues.put(TablePhotoTag.COL_ID, "" + post.getPostId() + "_" + tag);
            tagContentValues.put(TablePhotoTag.COL_POST_ID, post.getPostId());
            tagContentValues.put(TablePhotoTag.COL_CONTENT, tag);

            db.replace(TablePhotoTag.TABLE_NAME, null, tagContentValues);
        }

        // photos
        for (TumblrPhoto photo : post.getPhotos()) {
            savePhoto(photo, post.getPostId(), db);
        }
    }

    public void savePhoto(TumblrPhoto photo, long postId, SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TablePhoto.COL_ID, "" + postId + "_" + photo.toString());
        contentValues.put(TablePhoto.COL_POST_ID, postId);
        contentValues.put(TablePhoto.COL_URL, photo.getUrl());

        db.replace(TablePhoto.TABLE_NAME, null, contentValues);
    }

    // load
    public List<TumblrPost> load() {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        List<TumblrPost> posts = new ArrayList<>();
        String orderBy = TablePost._ID + " ASC";
        Cursor c = db.query(TablePost.TABLE_NAME, null, null, null, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            TumblrPost post = null;
            TumblrPost.Type type = TumblrPost.Type.valueFor(c.getString(c.getColumnIndex(TablePost.COL_TYPE)));
            if (type == TumblrPost.Type.PHOTO) {
                TumblrPhotoPost photoPost = new TumblrPhotoPost();
                loadPost(photoPost, c);
                loadPhotoPost(photoPost, db);
                post = photoPost;
            } else if (type == TumblrPost.Type.TEXT) {
                TumblrTextPost textPost = new TumblrTextPost();
                loadPost(textPost, c);
                loadTextPost(textPost, db);
                post = textPost;
            }

            if (post != null) {
                posts.add(post);
            }

            c.moveToNext();
        }
        return posts;
    }

    private void loadPost(TumblrPost post, Cursor c) {
        // set post variables
        post.setPostId(c.getLong(c.getColumnIndex(TablePost.COL_ID)));
        post.setBlogName(c.getString(c.getColumnIndex(TablePost.COL_BLOG_NAME)));
        post.setLiked(dbInt2Bool(c.getInt(c.getColumnIndex(TablePost.COL_LIKE))));
        post.setNoteCount(c.getLong(c.getColumnIndex(TablePost.COL_NOTE_COUNT)));
        post.setReblogKey(c.getString(c.getColumnIndex(TablePost.COL_REBLOG)));
        post.setShortUrl(c.getString(c.getColumnIndex(TablePost.COL_SHORT_URL)));
        post.setUrl(c.getString(c.getColumnIndex(TablePost.COL_URL)));
    }

    private void loadPhotoPost(TumblrPhotoPost post, SQLiteDatabase db) {
        Cursor c = db.query(TablePhotoPost.TABLE_NAME, null, TablePhotoPost.COL_POST_ID + "=?",
                new String[]{"" + post.getPostId()}, null, null, null);

        if (c.getCount() != 1) {
            Log.e(TAG, "find " + c.getCount() + " photo post");
        }
        c.moveToFirst();
        // set post variables
        post.setCaption(c.getString(c.getColumnIndex(TablePhotoPost.COL_CAPTION)));

        ArrayList<String> tags = new ArrayList<>();
        String orderBy = TablePhotoPost._ID + " ASC";
        c = db.query(TablePhotoTag.TABLE_NAME, null, TablePhotoTag.COL_POST_ID + "=?",
                new String[]{"" + post.getPostId()}, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            // Load tag
            String tag = c.getString(c.getColumnIndex(TablePhotoTag.COL_CONTENT));
            tags.add(tag);
            c.moveToNext();
        }
        post.setTags(tags);

        ArrayList<TumblrPhoto> photos = new ArrayList<>();
        c = db.query(TablePhoto.TABLE_NAME, null, TablePhoto.COL_POST_ID + "=?",
                new String[]{"" + post.getPostId()}, null, null, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            // Load photo
            TumblrPhoto photo = new TumblrPhoto();
            photo.setUrl(c.getString(c.getColumnIndex(TablePhoto.COL_URL)));
            photos.add(photo);
            c.moveToNext();
        }
        post.setPhotos(photos);
    }

    private void loadTextPost(TumblrTextPost post, SQLiteDatabase db) {
        Cursor c = db.query(TableTextPost.TABLE_NAME, null, TableTextPost.COL_POST_ID + "=?",
                new String[]{"" + post.getPostId()}, null, null, null);

        if (c.getCount() != 1) {
            Log.e(TAG, "find " + c.getCount() + " text post");
        }
        c.moveToFirst();
        // load text post
        post.setBody(c.getString(c.getColumnIndex(TableTextPost.COL_BODY)));
        post.setTitle(c.getString(c.getColumnIndex(TableTextPost.COL_TITLE)));
    }

    // clear
    public void reset() {
        mContext.deleteDatabase(mDbHelper.getDatabaseName());
        mDbHelper = new DbHelper(mContext);
    }

    // helpers
    boolean dbInt2Bool(int b) {
        return b == 1;
    }
}
