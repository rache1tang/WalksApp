package com.example.walksapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.walksapp.Walk;
import com.example.walksapp.fragments.HomeFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Date;
import java.util.List;

public class ProfileScrollFragment extends HomeFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivAdd.setVisibility(View.GONE);
    }

    @Override
    protected void queryWalks() {
        ParseQuery<Walk> query = ParseQuery.getQuery(Walk.class);
        query.include(Walk.KEY_AUTHOR);
        query.setLimit(15);
        query.whereEqualTo(Walk.KEY_AUTHOR, ParseUser.getCurrentUser()); // filter posts by current user
        query.addDescendingOrder(Walk.KEY_CREATED_AT); //maybe use created at instead?
        query.findInBackground(new FindCallback<Walk>() {
            @Override
            public void done(List<Walk> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error querying walks");
                    return;
                }
                if (objects.isEmpty()) {
                    tvNotice.setVisibility(View.VISIBLE);
                } else {
                    tvNotice.setVisibility(View.INVISIBLE);
                }
                adapter.clear();
                walks.addAll(objects);
                adapter.notifyDataSetChanged();
                swipeContainer.setRefreshing(false);
            }
        });
    }
}
