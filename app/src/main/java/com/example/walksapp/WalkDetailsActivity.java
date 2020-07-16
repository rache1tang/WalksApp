package com.example.walksapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.parceler.Parcels;

public class WalkDetailsActivity extends AppCompatActivity {

    ImageView ivBackdrop;
    ImageView ivProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_details);

        ivBackdrop = findViewById(R.id.ivDetailsBackdrop);
        ivProfile = findViewById(R.id.ivDetailsProfile);

        Intent intent = getIntent();
        Walk walk = Parcels.unwrap(intent.getParcelableExtra(WalksAdapter.KEY_DETAILS));

        Glide.with(getApplicationContext()).load(walk.getImage().getUrl()).into(ivBackdrop);
        Glide.with(getApplicationContext()).load(walk.getAuthor().getParseFile("profileImage")
                .getUrl()).circleCrop().into(ivProfile);
    }
}