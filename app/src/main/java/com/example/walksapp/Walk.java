package com.example.walksapp;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Objects;

@ParseClassName("Walk")
public class Walk extends ParseObject {

    public static final String KEY_NAME = "name";
    public static final String KEY_LOCATION = "location";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_AUTHOR = "author";
    public static final String KEY_TAGS = "tags";
    public static final String KEY_IMG = "image";
    public static final String KEY_UPDATED_AT = "updatedAt";
    public static final String KEY_CREATED_AT = "createdAt";
    public static final String KEY_LOCATION_LOWER = "locationLower";

    public String getName() {
        return getString(KEY_NAME);
    }

    public void setName(String name) {
        put(KEY_NAME, name);
    }

    public String getLocation() {
        return getString(KEY_LOCATION);
    }

    public void setLocation(String location) {
        put(KEY_LOCATION, location);
        put(KEY_LOCATION_LOWER, location.toLowerCase());
    }

    public String getDescription() {
        return getString(KEY_DESCRIPTION);
    }

    public void setDescription(String description) {
        put(KEY_DESCRIPTION, description);
    }

    public ParseUser getAuthor() {
        return getParseUser(KEY_AUTHOR);
    }

    public void setAuthor(ParseUser user) {
        put(KEY_AUTHOR, user);
    }

    public String getTags() {
        return getString(KEY_TAGS);
    }

    public void setTags(ArrayList<String> tags) {
            put(KEY_TAGS, String.join(" ", tags));
    }

    public ParseFile getImage() {
        return getParseFile(KEY_IMG);
    }

    public void setImage(ParseFile img) {
        put(KEY_IMG, img);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getObjectId());
    }
}
