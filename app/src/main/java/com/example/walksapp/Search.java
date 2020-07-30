package com.example.walksapp;

import android.util.Log;

import com.example.walksapp.fragments.SearchFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;

public class Search {

    public static final String TAG = "Search";


    // only for creating data
    public static void createSearchTags() {
        ParseQuery<Walk> query = ParseQuery.getQuery(Walk.class);
        query.findInBackground(new FindCallback<Walk>() {
            @Override
            public void done(List<Walk> objects, ParseException e) {
                JSONObject data = new JSONObject();
                for (Walk walk : objects) {
                    JSONArray arr = walk.getTags();
                    for (int i = 0; i < arr.length(); i++) {
                        try {
                            String tag = arr.getString(i);
                            JSONObject ob = null;
                            if (data.has(tag)) {
                                ob = data.getJSONObject(tag);
                            } else {
                                ob = new JSONObject();
                            }
                            ob.put(walk.getObjectId(), 0);
                            data.put(tag, ob);
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                Data dataNew = new Data();
                dataNew.setName("searchTags");
                dataNew.setData(data);
                dataNew.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "error creating search tags", e);
                        }
                    }
                });
            }
        });
    }

    // only for creating data
    public static void createMeaningless() throws JSONException {
        String[] load = {"for", "and", "nor", "but", "or", "yet", "so", "as", "at", "in", "like", "of", "on", "to", "with"};
        JSONObject data = new JSONObject();
        for (String word : load) {
            data.put(word, 0);
        }
        Data dataNew = new Data();
        dataNew.setData(data);
        dataNew.setName("meaningless");
        dataNew.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error creating meaningless", e);
                }
            }
        });
    }

    // only for creating data
    public static void createSearchOther() throws ParseException {
        ParseQuery<Data> queryMeaningless = ParseQuery.getQuery(Data.class);
        queryMeaningless.whereEqualTo("name", "meaningless");
        Data meaningless = queryMeaningless.getFirst();
        final JSONObject meaninglessJSON = meaningless.getData();

        ParseQuery<Data> queryTags = ParseQuery.getQuery(Data.class);
        queryTags.whereEqualTo("name", "searchTags");
        Data tags = queryTags.getFirst();
        final JSONObject tagsJSON = tags.getData();


        ParseQuery<Walk> query = ParseQuery.getQuery(Walk.class);
        query.findInBackground(new FindCallback<Walk>() {
            @Override
            public void done(List<Walk> objects, ParseException e) {
                JSONObject dataNew = new JSONObject();
                for (Walk walk : objects) {
                    String allText = walk.getDescription() + " " + walk.getName() + " " + walk.getLocation().toLowerCase();
                    allText = allText.replaceAll("\\p{Punct}","");
                    if (allText.trim().equals(""))
                        break;
                    List<String> words = SearchFragment.parseString(allText);
                    for (String word : words) {
                        if (!tagsJSON.has(word) && !meaninglessJSON.has(word)) {
                            JSONObject ob = null;
                            try {
                                if (dataNew.has(word)) {
                                    ob = dataNew.getJSONObject(word);
                                } else {
                                    ob = new JSONObject();
                                }
                                ob.put(walk.getObjectId(), 0);
                                dataNew.put(word, ob);
                            } catch (JSONException ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
                Data data = new Data();
                data.setData(dataNew);
                data.setName("searchOther");
                data.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "error saving searchOther", e);
                        }
                    }
                });
            }
        });
    }

    public static void updateOtherJson(String descrNew, String descrOld, String walkId) throws ParseException, JSONException {
        List<String> arrNew = SearchFragment.parseString(descrNew);
        List<String> arrOld = SearchFragment.parseString(descrOld);

        HashSet<String> hashNew = new HashSet<>(arrNew);
        HashSet<String> hashOld = new HashSet<>(arrOld);

        ParseQuery<Data> query = ParseQuery.getQuery(Data.class);
        Data dataOther = query.get(SearchFragment.otherID);
        JSONObject obOther = dataOther.getData();

        Data dataMeaningless = query.get(SearchFragment.meaninglessID);
        JSONObject obMeaningless = dataMeaningless.getData();

        for (String word : hashNew) {
            if (!obMeaningless.has(word)) { // if not a meaningless word
                JSONObject json;
                if (obOther.has(word)) {
                    json = obOther.getJSONObject(word);
                } else {
                    json = new JSONObject();
                }
                json.put(walkId, 0);
                obOther.put(word, json);
            }
        }
        for (String word : hashOld) {
            if (!obMeaningless.has(word)) { // if not a meaningless word
                if (!hashNew.contains(word)) { // no longer part of the description
                    JSONObject json = obOther.getJSONObject(word);
                    json.remove(walkId);
                    obOther.put(word, json);
                }
            }
        }

        dataOther.setData(obOther);
        dataOther.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error saving data for other words");
                }
            }
        });
    }

    public static void makeOtherJson(Walk walk) throws ParseException, JSONException {
        String allText = walk.getDescription() + " " + walk.getName() + " " + walk.getLocation();
        allText = allText.toLowerCase();
        List<String> parsed = SearchFragment.parseString(allText);

        String walkId = walk.getObjectId();

        ParseQuery<Data> query = ParseQuery.getQuery(Data.class);
        Data dataOther = query.get(SearchFragment.otherID);
        JSONObject obOther = dataOther.getData();

        Data dataMeaningless = query.get(SearchFragment.meaninglessID);
        JSONObject obMeaningless = dataMeaningless.getData();

        for (String word : parsed) {
            if (!obMeaningless.has(word)) { // if not a meaningless word
                JSONObject json;
                if (obOther.has(word)) {
                    json = obOther.getJSONObject(word);
                } else {
                    json = new JSONObject();
                }
                json.put(walkId, 0);
                obOther.put(word, json);
            }
        }

        Log.i(TAG, "\n\n" + obOther.toString(4));

        dataOther.setData(obOther);
        dataOther.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error saving data for other words");
                }
            }
        });
    }
}
