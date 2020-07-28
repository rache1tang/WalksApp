package com.example.walksapp.fragments;

import android.view.View;

import com.example.walksapp.Walk;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;

public class FavoritesFragment extends ProfileScrollFragment {

    @Override
    protected void queryWalks() {
        // get liked walks
        ParseUser user = ParseUser.getCurrentUser();
        JSONArray liked = user.getJSONArray("liked");

        adapter.clear(); // clear adapter for refreshing

        ParseQuery<Walk> query = ParseQuery.getQuery(Walk.class);
        if (liked != null) {

            if (liked.length() == 0) {
                tvNotice.setVisibility(View.VISIBLE);
                return;
            }
            tvNotice.setVisibility(View.INVISIBLE);
            for (int i = 0; i < liked.length(); i++) { // get each walk by objectId
                try {
                    Walk walk = query.get(liked.getString(i));
                    walks.add(walk);
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        adapter.notifyDataSetChanged();
        swipeContainer.setRefreshing(false); // close refresh circle arrow
    }
}
