package com.example.walksapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.parceler.Parcels;

public class AddTagsActivity extends AppCompatActivity {

    public static final String TAG = "AddTagsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_tags);

        Intent intent = getIntent();
        Walk walk = Parcels.unwrap(intent.getParcelableExtra(AddWalkActivity.KEY_NEW_WALK));

    }
}