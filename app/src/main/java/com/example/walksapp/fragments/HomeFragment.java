package com.example.walksapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.walksapp.R;
import com.example.walksapp.Walk;
import com.example.walksapp.WalksAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";

    RecyclerView rvWalks;
    WalksAdapter adapter;
    List<Walk> walks;
    ImageView ivAdd;


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

        walks = new ArrayList<>();
        adapter = new WalksAdapter(getContext(), walks);

        rvWalks.setAdapter(adapter);
        rvWalks.setLayoutManager(new LinearLayoutManager(getContext()));

        queryWalks();
    }

    private void queryWalks() {
        ParseQuery<Walk> query = ParseQuery.getQuery(Walk.class);
        query.include(Walk.KEY_AUTHOR);
        query.setLimit(15);
        query.addDescendingOrder(Walk.KEY_UPDATED_AT); //maybe use created at instead?
        query.findInBackground(new FindCallback<Walk>() {
            @Override
            public void done(List<Walk> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error querying walks");
                    return;
                }
                walks.addAll(objects);
                adapter.notifyDataSetChanged();
            }
        });

    }
}