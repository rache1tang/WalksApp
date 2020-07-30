package com.example.walksapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.walksapp.fragments.SearchFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class AddTagsActivity extends AppCompatActivity {

    public static final String TAG = "AddTagsActivity";
    public static final String KEY_FINAL_WALK = "new_walk_final";

    Button btnCreateWalk;
    RecyclerView rvTags;
    List<String> tags;
    HashSet<String> selected;
    TagsAdapter adapter;
    ImageView ivTagsCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tags);

        TagsAdapter.newTags = new HashSet<>();

        // get reference to layout items
        btnCreateWalk = findViewById(R.id.btnCreateWalk);
        rvTags = findViewById(R.id.rvTags);

        // set up adapter and layout manager
        tags = new ArrayList<>();
        selected = new HashSet<>();
        adapter = new TagsAdapter(getApplicationContext(), tags, selected);
        GridLayoutManager layout = new GridLayoutManager(AddTagsActivity.this, 3);

        // bind adapter to recycler view
        rvTags.setAdapter(adapter);
        rvTags.setLayoutManager(layout);

        queryTags();

        // get walk from intent
        Intent intent = getIntent();
        final Walk walk = Parcels.unwrap(intent.getParcelableExtra(AddLocationActivity.KEY_NEW_WALK));

        ivTagsCancel = findViewById(R.id.ivTagsCancel);
        ivTagsCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnCreateWalk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParseQuery<Data> query = ParseQuery.getQuery(Data.class);
                try {
                    Data data = query.get(SearchFragment.tagsID);
                    JSONObject ob = data.getData();
                    for (String tag : TagsAdapter.newTags) {
                        JSONObject obNew = new JSONObject();
                        obNew.put(walk.getObjectId(), 0);
                        ob.put(tag, obNew);
                    }
                    JSONArray selectedTags = new JSONArray();
                    for (String tag : selected) {
                        selectedTags.put(tag);
                        JSONObject json = ob.getJSONObject(tag);
                        json.put(walk.getObjectId(), 0);
                        ob.put(tag, json);
                    }
                    walk.setTags(selectedTags);
                    data.setData(ob);
                    data.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "error saving new tag", e);
                            }
                        }
                    });
                } catch (ParseException | JSONException e) {
                    e.printStackTrace();
                }


                // send back to last activity to send to location activity
                Intent i = new Intent();
                i.putExtra(KEY_FINAL_WALK, Parcels.wrap(walk));
                setResult(RESULT_OK, i);

                TagsAdapter.newTags.clear();
                finish();
            }
        });

    }

    private void queryTags() { // find all tags that exist
        ParseQuery<Data> query = ParseQuery.getQuery(Data.class);
        try {
            Data data = query.get(SearchFragment.tagsID);
            JSONObject ob = data.getData();

            for (Iterator<String> it = ob.keys(); it.hasNext(); ) {
                String key = it.next();
                tags.add(key);
            }
            tags.add("+");
            adapter.notifyDataSetChanged();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }
}