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
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.walksapp.R;
import com.example.walksapp.SearchWalksAdapter;
import com.example.walksapp.Tag;
import com.example.walksapp.Walk;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchFragment extends Fragment {

    public static final String TAG = "SearchFragment";

    EditText etSearch;
    Button btnSearch;
    RecyclerView rvSearchResults;
    List<Walk> walks;
    SearchWalksAdapter adapter;

    public SearchFragment() {
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
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        etSearch = view.findViewById(R.id.etKeywords);
        btnSearch = view.findViewById(R.id.btnSearch);

        walks = new ArrayList<>();
        adapter = new SearchWalksAdapter(getContext(), walks);

        rvSearchResults.setAdapter(adapter);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String search = etSearch.getText().toString();
                List<String> parsed = parseString(search);

                Log.i(TAG, parsed.toString());

                Toast.makeText(getContext(), "search", Toast.LENGTH_SHORT).show();

                queryWalks(parsed);
            }
        });


    }

    public List<String> parseString(String str) {
        String stripped = str.toLowerCase().trim();
        return Arrays.asList(stripped.split(" "));
    }

    private void queryWalks(List<String> keywords) {
        ParseQuery<Tag> query = ParseQuery.getQuery(Tag.class);
        query.include(Tag.KEY_WALK);
        query.whereContainedIn(Tag.KEY_TAG, keywords);
        query.findInBackground(new FindCallback<Tag>() {
            @Override
            public void done(List<Tag> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error querying search results", e);
                    return;
                }
                adapter.clear();
                for (Tag tag : objects) { // TODO: CHANGE TO MAKE MORE EFFICIENT
                    if (!has(walks, tag.getWalk()))
                    walks.add(tag.getWalk());
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private boolean has(List<Walk> walks, Walk walk) {
        String id = walk.getObjectId();
        for (Walk item : walks) {
            if (id.equals(item.getObjectId())) {
                return true;
            }
        }
        return false;
    }
}