package com.example.walksapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.walksapp.fragments.SearchFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class EditWalkActivity extends AppCompatActivity {

    public static final String TAG = "EditWalkActivity";
    public static final String KEY_EDITED = "editedWalk";

    EditText etName;
    EditText etDescription;
    ImageView ivBanner;
    Button btnDelete;
    Button btnSave;
    ImageView ivCancel;

    RecyclerView rvTags;
    TagsAdapter adapter;
    List<String> tags;
    HashSet<String> selected;

    ParseFile photoFile;

    Walk walk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_walk);

        photoFile = null;

        etName = findViewById(R.id.etEditWalkName);
        etDescription = findViewById(R.id.etEditWalkDescription);
        ivBanner = findViewById(R.id.ivEditBanner);
        btnDelete = findViewById(R.id.btnDeleteWalk);
        btnSave = findViewById(R.id.btnEditSave);
        ivCancel = findViewById(R.id.ivEditCancel);
        rvTags = findViewById(R.id.rvEditTags);

        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Intent intent = getIntent();
        walk = Parcels.unwrap(intent.getParcelableExtra(WalkDetailsActivity.KEY_EDIT_WALK));

        tags = new ArrayList<>();
        selected = new HashSet<>();
        adapter = new TagsAdapter(getApplicationContext(), tags, selected);
        GridLayoutManager layout = new GridLayoutManager(EditWalkActivity.this, 3);

        rvTags.setAdapter(adapter);
        rvTags.setLayoutManager(layout);

        queryTags();
        getSelectedTags();
        adapter.notifyDataSetChanged();

        etName.setText(walk.getName());
        etDescription.setText(walk.getDescription());

        ivBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // select new photo
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show popup for "are you sure?"
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // save walk and send back to walk details activity
                String newName = etName.getText().toString();
                walk.put("name", newName);
                String newDescription = etDescription.getText().toString();
                walk.put("description", newDescription);
                walk.setTags(new ArrayList<>(selected));
                if (photoFile != null) {
                    walk.setImage(photoFile);
                }
                Intent i = new Intent();
                i.putExtra(KEY_EDITED, Parcels.wrap(walk));
                setResult(RESULT_OK, i);

                Log.i(TAG, walk.getName() + walk.getDescription());

                finish();
            }
        });
    }

    public void getSelectedTags() {
        String walkTags = walk.getTags();
        if (walkTags != null) {
            selected.addAll(SearchFragment.parseString(walkTags));
        }
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