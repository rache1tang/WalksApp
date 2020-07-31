package com.example.walksapp;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Suggest {

    public static final String TAG = "Suggest";
    public static final String suggestId = "G1jy4mcJzm";

    public static JSONObject likeGraph;

    // only for initially setting up everything
    public static void createSuggest() {
        likeGraph = new JSONObject();
        ParseQuery<Walk> queryWalk = ParseQuery.getQuery(Walk.class);
        queryWalk.include("author");
        queryWalk.findInBackground(new FindCallback<Walk>() {
            @Override
            public void done(List<Walk> objects, ParseException e) {
                for (Walk walk : objects) {
                    JSONObject neighbors = null;
                    try {
                        neighbors = setUpNeighbors(walk);
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                    try {
                        likeGraph.put(walk.getObjectId(), neighbors);
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
                Data data = new Data();
                data.setName("suggest");
                data.setData(likeGraph);
                data.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null)
                            Log.e(TAG, "error saving data", e);
                    }
                });
            }
        });
    }

    // only for initially setting up everything
    public static JSONObject setUpNeighbors(Walk walk) throws JSONException, ParseException {
        String walkId = walk.getObjectId();
        JSONObject res = new JSONObject();
        ParseQuery<ParseUser> queryUser = ParseQuery.getQuery(ParseUser.class);
        JSONArray likers = walk.getLikes();
        for (int i = 0; i < likers.length(); i++) { // loop through likers
            ParseUser user = queryUser.get(likers.getString(i));
            JSONArray userLikes = user.getJSONArray("liked");
            for (int j = 0; j < userLikes.length(); j++) { // loop through likers' liked
                String id = userLikes.getString(j);

                // set id's and counts it appears
                if (res.has(id)) {
                    res.put(id, res.getInt(id) + 1);
                } else if (!walkId.equals(id)) {
                    res.put(id, 1);
                }

            }
        }
        return res;
    }

    public static void removeLike(final String walkId, final JSONArray userLikes) {
        ParseQuery<Data> query = ParseQuery.getQuery(Data.class);
        query.whereEqualTo(Data.KEY_NAME, "suggest");
        query.getFirstInBackground(new GetCallback<Data>() {
            @Override
            public void done(Data object, ParseException e) {
                JSONObject data = object.getData();
                JSONObject dataWalk = null;

                try {
                    dataWalk = data.getJSONObject(walkId);
                    for (int i = 0; i < userLikes.length(); i++) {
                        String id = userLikes.getString(i);
                        JSONObject dataNeighbor = data.getJSONObject(id);
                        dataNeighbor.put(walkId, dataNeighbor.getInt(walkId) - 1);
                        if (dataNeighbor.getInt(walkId) == 0) {
                            dataNeighbor.remove(walkId);
                        }
                        dataWalk.put(id, dataWalk.getInt(id) - 1);
                        if (dataWalk.getInt(id) == 0) {
                            dataWalk.remove(id);
                        }
                        data.put(id, dataNeighbor);
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                try {
                    data.put(walkId, dataWalk);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }
                object.setData(data);

                object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null)
                            Log.e(TAG, "error saving removed like from graph", e);
                    }
                });
            }
        });
    }

    public static void addLike(final String walkId, final JSONArray userLikes) {
        ParseQuery<Data> query = ParseQuery.getQuery(Data.class);
        query.whereEqualTo(Data.KEY_NAME, "suggest");
        query.getFirstInBackground(new GetCallback<Data>() {
            @Override
            public void done(Data object, ParseException e) {
                JSONObject data = object.getData();
                JSONObject dataWalk = null;

                try {
                    if (data.has(walkId)) {
                        dataWalk = data.getJSONObject(walkId);
                        for (int i = 0; i < userLikes.length(); i++) {
                            String id = userLikes.getString(i);
                            if (id.equals(walkId)) break;
                            if (dataWalk.has(id)) {
                                dataWalk.put(id, dataWalk.getInt(id) + 1);
                                JSONObject dataNeighbor = data.getJSONObject(id);
                                dataNeighbor.put(walkId, dataNeighbor.getInt(walkId) + 1);
                            } else {
                                dataWalk.put(id, 1);
                                JSONObject dataNeighbor = null;
                                if (data.has(id))
                                    dataNeighbor = data.getJSONObject(id);
                                else
                                    dataNeighbor = new JSONObject();
                                dataNeighbor.put(walkId, 1);
                                data.put(id, dataNeighbor);
                            }
                        }
                    } else {
                        dataWalk = new JSONObject();
                        for (int i = 0; i < userLikes.length(); i++) {
                            String id = userLikes.getString(i);
                            if (id.equals(walkId)) break;
                            dataWalk.put(id, 1);
                            JSONObject dataNeighbor = null;
                            if (data.has(id)) {
                                dataNeighbor = data.getJSONObject(id);
                            } else {
                                dataNeighbor = new JSONObject();
                            }
                            dataNeighbor.put(walkId, 1);
                            data.put(id, dataNeighbor);
                        }
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }

                try {
                    data.put(walkId, dataWalk);
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }

                object.setData(data);
                object.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null)
                            Log.e(TAG, "error adding like to graph", e);
                    }
                });

            }
        });
    }
}
