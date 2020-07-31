package com.example.walksapp;

import android.util.Log;

import com.example.walksapp.fragments.SearchFragment;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;

public class Delete {

    public static final String TAG = "Delete";

    public static void fromLikes(Walk walk) throws JSONException, ParseException {
        String walkId = walk.getObjectId();
        JSONArray likes = walk.getLikes();

        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        for (int i = 0; i < likes.length(); i++) {
            String userId = likes.getString(i);
            ParseUser user = query.get(userId);

            JSONArray liked = user.getJSONArray("liked");
            int index = likeIndex(liked, walkId);
            if (index == -1) {
                Log.i(TAG, "walk does not exist to this user");
                return;
            }
            liked.remove(index);
            user.put("liked", liked);
            user.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "error removing walk from likes", e);
                    }
                }
            });
        }
    }

    public static int likeIndex(JSONArray liked, String walkId) throws JSONException {
        for (int i = 0; i < liked.length(); i++) {
            String id = liked.getString(i);
            if (id.equals(walkId))
                return i;
        }
        return -1;
    }

    public static void fromData(Walk walk) throws ParseException, JSONException {
        String walkId = walk.getObjectId();
        ParseQuery<Data> query = ParseQuery.getQuery(Data.class);
        Data dataTags = query.get(SearchFragment.tagsID);
        JSONObject obTags = dataTags.getData();
        Data dataOther = query.get(SearchFragment.otherID);
        JSONObject obOther = dataOther.getData();
        Data dataSuggest = query.get(Suggest.suggestId);
        JSONObject obSuggest = dataSuggest.getData();

        // remove from tags
        JSONArray tags = walk.getTags();
        for (int i = 0; i < tags.length(); i++) {
            String tag = tags.getString(i);
            JSONObject json = obTags.getJSONObject(tag);
            json.remove(walkId);
            obTags.put(tag, json);
        }
        dataTags.setData(obTags);
        dataTags.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error saving tags", e);
                }
            }
        });

        // remove from other
        String allText = walk.getDescription() + " " + walk.getName() + " " + walk.getLocation();
        allText = allText.toLowerCase();
        List<String> parsed = SearchFragment.parseString(allText);
        Log.i(TAG, parsed.toString());

        for (String word : parsed) {
            if (obOther.has(word)) {
                JSONObject json = obOther.getJSONObject(word);
                json.remove(walkId);
                obOther.put(word, json);
            }
        }
        dataOther.setData(obOther);
        dataOther.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error saving other", e);
                }
            }
        });

        // remove from suggested
        if (obSuggest.has(walkId)) {
            for (Iterator<String> it = obSuggest.getJSONObject(walkId).keys(); it.hasNext(); ) {
                String key = it.next();
                JSONObject json = obSuggest.getJSONObject(key);
                json.remove(walkId);
                obSuggest.put(key, json);
            }
            obSuggest.remove(walkId);
            dataSuggest.setData(obSuggest);
            dataSuggest.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "error saving suggest", e);
                    }
                }
            });
        }
    }

    public static void fromComments(Walk walk) {
        ParseQuery<Comment> queryComment = ParseQuery.getQuery(Comment.class);
        queryComment.whereEqualTo("walk", walk);
        queryComment.findInBackground(new FindCallback<Comment>() {
            @Override
            public void done(List<Comment> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error querying comments", e);
                    return;
                }
                Log.i(TAG, "size of objects" + objects.size());
                for (Comment comment : objects) {
                    Log.i(TAG, "DELETE");
                    comment.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "error deleting comment", e);
                            }
                        }
                    });
                }
            }
        });

        ParseQuery<CommentPhoto> queryPhoto = ParseQuery.getQuery(CommentPhoto.class);
        queryPhoto.whereEqualTo("walk", walk);
        queryPhoto.findInBackground(new FindCallback<CommentPhoto>() {
            @Override
            public void done(List<CommentPhoto> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error querying photo", e);
                    return;
                }
                Log.i(TAG, "size of objects" + objects.size());
                for (CommentPhoto photo : objects) {
                    Log.i(TAG, "DELETE");
                    photo.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "error deleting photo", e);
                            }
                        }
                    });
                }
            }
        });

    }
}
