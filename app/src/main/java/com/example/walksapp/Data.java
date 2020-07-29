package com.example.walksapp;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import org.json.JSONObject;

@ParseClassName("Data")
public class Data extends ParseObject {

    public static final String KEY_NAME = "name";
    public static final String KEY_DATA = "data";

    public String getName() {
        return getString(KEY_NAME);
    }

    public void setName(String name) {
        put(KEY_NAME, name);
    }

    public JSONObject getData() {
        return getJSONObject(KEY_DATA);
    }

    public void setData(JSONObject data) {
        put(KEY_DATA, data);
    }
}
