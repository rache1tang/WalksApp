package com.example.walksapp;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("Tag")
public class Tag extends ParseObject {

    public static final String KEY_TAG = "tag";
    public static final String KEY_WALK = "walk";

    public String getTag() {
        return getString(KEY_TAG);
    }

    public Walk getWalk() {
        return (Walk) getParseObject(KEY_WALK);
    }
}
