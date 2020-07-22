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

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class AddTagsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = "AddTagsActivity";
    public static final String KEY_FINAL_WALK = "new_walk_final";

    Button btnCreateWalk;
    RecyclerView rvTags;
    List<String> tags;
    HashSet<String> selected;
    TagsAdapter adapter;
    ImageView ivTagsCancel;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tags);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnCreateWalk = findViewById(R.id.btnCreateWalk);
        rvTags = findViewById(R.id.rvTags);

        tags = new ArrayList<>();
        selected = new HashSet<>();
        adapter = new TagsAdapter(getApplicationContext(), tags, selected);
        GridLayoutManager layout = new GridLayoutManager(AddTagsActivity.this, 3);

        rvTags.setAdapter(adapter);
        rvTags.setLayoutManager(layout);

        queryTags();

        Intent intent = getIntent();
        final Walk walk = Parcels.unwrap(intent.getParcelableExtra(AddWalkActivity.KEY_NEW_WALK));

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
                walk.setTags(new ArrayList<>(selected));

                // send back to last activity to send to home
                Intent i = new Intent();
                i.putExtra(KEY_FINAL_WALK, Parcels.wrap(walk));
                setResult(RESULT_OK, i);

                finish();
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    private void queryTags() {
        ParseQuery<Walk> query = ParseQuery.getQuery(Walk.class);
        query.findInBackground(new FindCallback<Walk>() {
            HashSet tagSet = new HashSet<>();
            @Override
            public void done(List<Walk> objects, ParseException e) {
                for (Walk walk : objects) {
                    for (String tag : walk.getTags().split(" ")) {
                        if ((!tagSet.contains(tag)) && !tag.equals("")) {
                            tagSet.add(tag);
                            tags.add(tag);
                        }
                    }
                }
                tags.add("+");
                adapter.notifyDataSetChanged();
            }
        });

    }
}