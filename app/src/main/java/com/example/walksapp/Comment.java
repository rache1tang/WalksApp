package com.example.walksapp;

import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@ParseClassName("Comment")
public class Comment extends ParseObject {

    public static final String KEY_USER = "user";
    public static final String KEY_WALK = "walk";
    public static final String KEY_COMMENT = "comment";
    public static final String KEY_PHOTOS = "photos";

    public static final String TAG = "Comment";

    public ParseUser getUser() {
        return getParseUser(KEY_USER);
    }

    public void setUser(ParseUser user) {
        put(KEY_USER, user);
    }

    public Walk getWalk() {
        return (Walk) getParseObject(KEY_WALK);
    }

    public void setWalk(Walk walk) {
        put(KEY_WALK, walk);
    }

    public String getComment() {
        return getString(KEY_COMMENT);
    }

    public void setComment(String comment) {
        put(KEY_COMMENT, comment);
    }

}
