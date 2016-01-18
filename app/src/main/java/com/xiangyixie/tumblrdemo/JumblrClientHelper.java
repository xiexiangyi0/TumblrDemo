package com.xiangyixie.tumblrdemo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by xiangyixie on 1/17/16.
 */
public class JumblrClientHelper {
    private static final String TAG = "JumblrClientHelper";
    private static final String API_URL = "https://api.tumblr.com/v2";
    private static final String DEFAULT_AVATAR = "";

    public JumblrClientHelper() {
    }

    public String blogAvatar(String blogName, Integer size) {
        String blogAddr = blogName.contains(".")?blogName:blogName + ".tumblr.com";
        String pathExt = size == null?"":"/" + size.toString();
        String path = API_URL + "/blog/" + blogAddr + "/avatar" + pathExt;

        String response;
        try {
            response = getHttpResponse(path);
        } catch (IOException e) {
            e.printStackTrace();
            return DEFAULT_AVATAR;
        }

        try {
            JSONObject json = new JSONObject(response);
            int returnCode = json.getJSONObject("meta").getInt("status");

            if (returnCode != 301) {
                return DEFAULT_AVATAR;
            }

            return json.getJSONObject("response").getString("avatar_url");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return DEFAULT_AVATAR;
    }

    private String getHttpResponse(String path) throws IOException {
        URL url = new URL(path);

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setInstanceFollowRedirects(false);

            InputStream is = new BufferedInputStream(urlConnection.getInputStream());
            StringBuilder sb = new StringBuilder();

            int c;
            while((c = is.read()) != -1) {
                sb.append((char) c);
            }

            return sb.toString();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}
