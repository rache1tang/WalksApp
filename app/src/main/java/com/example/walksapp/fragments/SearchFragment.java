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
import android.widget.TextView;
import android.widget.Toast;

import com.example.walksapp.R;
import com.example.walksapp.SearchWalksAdapter;
import com.example.walksapp.Walk;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchFragment extends Fragment {

    public static final String TAG = "SearchFragment";

    EditText etSearch;
    Button btnSearch;
    TextView tvNoResults;
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
        tvNoResults = view.findViewById(R.id.tvNoResults);

        tvNoResults.setText("");

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

                queryWalks(parsed);
            }
        });


    }

    public static List<String> parseString(String str) {
        String stripped = str.toLowerCase().trim();
        return Arrays.asList(stripped.split(" "));
    }


    private void queryWalks(final List<String> keywords) { // TODO: super inefficient
        adapter.clear();
        ParseQuery<Walk> query = ParseQuery.getQuery(Walk.class);
        query.include(Walk.KEY_AUTHOR);
        query.findInBackground(new FindCallback<Walk>() {
            @Override
            public void done(List<Walk> objects, ParseException e) {
                // walk passes if all keywords pass
                for (Walk walk : objects) {
                    String allText = walk.getDescription() + " " + walk.getName() + " " + walk.getTags() + " " + walk.getLocation().toLowerCase();
                    boolean containsAll = true;
                    for (String word : keywords) {
                        if (!allText.contains(word)) {
                            containsAll = false;
                            break;
                        }
                    }
                    if (containsAll) {
                        walks.add(walk);
                    }
                }

                if (walks.isEmpty()) {
                    tvNoResults.setText("No Walks Found");
                } else {
                    tvNoResults.setText("");
                }

                adapter.notifyDataSetChanged();
            }
        });
    }

    private void searchResults(List<String> keywords) {

    }
}