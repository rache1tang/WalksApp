package com.example.walksapp.fragments;

import android.util.Log;

import com.example.walksapp.Like;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class FavoritesFragment extends ProfileScrollFragment {

    @Override
    protected void queryWalks() {
        ParseQuery<Like> query = ParseQuery.getQuery(Like.class);
        query.include(Like.KEY_WALK);
        query.whereEqualTo(Like.KEY_USER, ParseUser.getCurrentUser());
        query.addAscendingOrder(Like.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<Like>() {
            @Override
            public void done(List<Like> objects, ParseException e) {
                if (e != null){
                    Log.e("FavoritesFragment", "error querying favorites", e);
                    return;
                }
                for (Like like : objects) {
                    walks.add(like.getWalk());
                }
                adapter.notifyDataSetChanged();
            }
        });

    }
}
