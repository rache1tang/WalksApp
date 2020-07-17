package com.example.walksapp.fragments;

import android.content.Intent;
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

import com.example.walksapp.AddWalkActivity;
import com.example.walksapp.R;
import com.example.walksapp.Walk;
import com.example.walksapp.WalksAdapter;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {

    public static final String TAG = "HomeFragment";
    public static final int REQUEST_CODE = 42;

    RecyclerView rvWalks;
    protected WalksAdapter adapter;
    protected List<Walk> walks;
    protected ImageView ivAdd;


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

        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to add activity
                Intent intent = new Intent(getContext(), AddWalkActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        queryWalks();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {

            // refresh feed so it contains new walk
            adapter.clear();
            queryWalks();
        }
    }

    protected void queryWalks() {
        ParseQuery<Walk> query = ParseQuery.getQuery(Walk.class);
        query.include(Walk.KEY_AUTHOR);
        query.setLimit(15);
        query.addDescendingOrder(Walk.KEY_CREATED_AT); //maybe use created at instead?
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