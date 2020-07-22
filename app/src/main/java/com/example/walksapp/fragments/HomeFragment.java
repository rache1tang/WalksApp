package com.example.walksapp.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.walksapp.AddWalkActivity;
import com.example.walksapp.EndlessRecyclerViewScrollListener;
import com.example.walksapp.Like;
import com.example.walksapp.R;
import com.example.walksapp.Walk;
import com.example.walksapp.WalkDetailsActivity;
import com.example.walksapp.WalksAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";
    public static final int REQUEST_CODE = 42;

    RecyclerView rvWalks;
    protected WalksAdapter adapter;
    protected List<Walk> walks;
    protected ImageView ivAdd;
    protected HashSet<String> likedWalks;
    protected TextView tvNotice;
    public SwipeRefreshLayout swipeContainer;


    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rvWalks = view.findViewById(R.id.rvHome);
        ivAdd = view.findViewById(R.id.ivAddBtn);
        tvNotice = view.findViewById(R.id.tvNoWalksNotice);

        walks = new ArrayList<>();
        likedWalks = new HashSet<>();
        adapter = new WalksAdapter(getContext(), walks, likedWalks);

        swipeContainer = view.findViewById(R.id.swipeContainer);

        // makes the loading symbol change colors
        swipeContainer.setColorSchemeColors(
                getResources().getColor(android.R.color.holo_blue_bright),
                getResources().getColor(android.R.color.holo_green_light),
                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_red_light)
        );

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "fetching new data");
                queryWalks();
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());

        rvWalks.setAdapter(adapter);
        rvWalks.setLayoutManager(linearLayoutManager);

        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to add activity
                Intent intent = new Intent(getContext(), AddWalkActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        queryWalks();
        queryLikes();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if ((requestCode == REQUEST_CODE || requestCode == WalksAdapter.WALK_DETAILS_CODE) && resultCode == RESULT_OK) {

            // refresh feed so it contains new walk
            adapter.clear();
            queryWalks();
            queryLikes();
            adapter.notifyDataSetChanged();
        }
    }

    protected void queryWalks() { //TODO: super inefficient
        String userTags = ParseUser.getCurrentUser().getString("tags");
        Log.i(TAG, "tags: '" + userTags + "'");
        ParseQuery<Walk> query = ParseQuery.getQuery(Walk.class);
        query.include(Walk.KEY_AUTHOR);
        query.addDescendingOrder(Walk.KEY_CREATED_AT);
        List<Walk> objects = null;
        try {
            objects = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        adapter.clear();
        if (objects != null) {
            if (objects.isEmpty()) {
                tvNotice.setVisibility(View.VISIBLE);
            } else {
                tvNotice.setVisibility(View.INVISIBLE);
            }
            if (userTags.isEmpty()) {
                walks.addAll(objects);
                adapter.notifyDataSetChanged();
                swipeContainer.setRefreshing(false);
                return;
            }
            for (Walk ob : objects) {
                List<String> tags = SearchFragment.parseString(ob.getTags());
                for (String tag : tags) {
                    if (userTags.contains(tag)) {
                        walks.add(ob);
                        break;
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
        swipeContainer.setRefreshing(false);

    }


    protected void queryLikes() {
        ParseQuery<Like> query = ParseQuery.getQuery(Like.class);
        query.include(Like.KEY_WALK);
        query.whereEqualTo(Like.KEY_USER, ParseUser.getCurrentUser());

        List<Like> objects = null;
        try {
            objects = query.find();
        } catch (ParseException e) {
            Log.e(TAG, "error querying walks", e);
            e.printStackTrace();
        }
        if (objects != null) {
            for (Like ob : objects) {
                likedWalks.add(ob.getWalk().getObjectId());
            }
        }
        adapter.notifyDataSetChanged();
    }
}