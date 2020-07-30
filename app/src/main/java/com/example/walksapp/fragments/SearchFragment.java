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

import com.example.walksapp.Data;
import com.example.walksapp.R;
import com.example.walksapp.SearchWalksAdapter;
import com.example.walksapp.Walk;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class SearchFragment extends Fragment {

    public static final String TAG = "SearchFragment";

    public static final String tagsID = "aACe2daX3G";
    public static final String meaninglessID = "NhWGCUZNM9";
    public static final String otherID = "WkFD3UICQs";

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

        // get references to items in layout
        rvSearchResults = view.findViewById(R.id.rvSearchResults);
        etSearch = view.findViewById(R.id.etKeywords);
        btnSearch = view.findViewById(R.id.btnSearch);
        tvNoResults = view.findViewById(R.id.tvNoResults);

        // hide the "no results" text
        tvNoResults.setVisibility(View.INVISIBLE);

        // set up new adapter
        walks = new ArrayList<>();
        adapter = new SearchWalksAdapter(getContext(), walks);

        // bind adapter to recycler view
        rvSearchResults.setAdapter(adapter);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(getContext()));

        // on search, run search algorithm
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
        String stripped = str.toLowerCase().trim(); // make everything lowercase (case doesn't matter) and strip white text
        return Arrays.asList(stripped.split(" "));
    }

    // TODO: make into a dictionary for easy searching
    private void queryWalks(final List<String> keywords) { // TODO: super inefficient
        adapter.clear();
        ParseQuery<Walk> query = ParseQuery.getQuery(Walk.class);
        query.include(Walk.KEY_AUTHOR);

        ParseQuery<Data> queryData = ParseQuery.getQuery(Data.class);
        try {
            Data tags = queryData.get(tagsID);
            Data other = queryData.get(otherID);

            JSONObject tagsJson = tags.getData();
            JSONObject otherJson = other.getData();

            HashSet<String> walks = new HashSet<>();
            boolean first = true;

            for (String kw : keywords) {
                if (first == true) {
                    if (tagsJson.has(kw)) {
                        for (Iterator<String> it = tagsJson.getJSONObject(kw).keys(); it.hasNext(); ) {
                            String key = it.next();
                            walks.add(key);
                        }
                    } if (otherJson.has(kw)) {
                        for (Iterator<String> it = otherJson.getJSONObject(kw).keys(); it.hasNext(); ) {
                            String key = it.next();
                            walks.add(key);
                        }
                    }
                    if (tagsJson.has(kw) || otherJson.has(kw))
                        first = false;
                } else {
                    JSONObject tagOb = null;
                    JSONObject otherOb = null;
                    if (tagsJson.has(kw)) {
                        tagOb = tagsJson.getJSONObject(kw);
                    } if (otherJson.has(kw)) {
                        otherOb = otherJson.getJSONObject(kw);
                    }

                    if ((tagOb != null) || (otherOb != null)) {
                        HashSet<String> toDelete = new HashSet<>();
                        for (String walk : walks) {
                            if ((tagOb != null) && (tagOb.has(walk)))
                                break;
                            else if ((otherOb != null) && (otherOb.has(walk)))
                                break;
                            else
                                toDelete.add(walk);
                        }
                        for (String walk : toDelete) walks.remove(walk);
                    }
                }
            }
            for (String walk : walks) {
                this.walks.add(query.get(walk));
            }
        } catch (ParseException | JSONException ex) {
            ex.printStackTrace();
        }

        if (walks.isEmpty()) {
            Log.i(TAG, "hi");
            tvNoResults.setVisibility(View.VISIBLE);
        }
        else
            tvNoResults.setVisibility(View.INVISIBLE);

        adapter.notifyDataSetChanged();
    }
}