package com.example.walksapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.List;

public class WalkDetailsActivity extends AppCompatActivity {

    public static final String TAG = "WalkDetailsActivity";

    ImageView ivBackdrop;
    ImageView ivProfile;
    ImageView ivHeart;
    TextView tvName;
    TextView tvLocation;
    TextView tvLikes;
    TextView tvDescription;
    ImageView ivBack;

    private static int likes;
    private static Like like;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_details);

        ivBackdrop = findViewById(R.id.ivDetailsBackdrop);
        ivProfile = findViewById(R.id.ivDetailsProfile);
        ivHeart = findViewById(R.id.ivDetailsHeart);
        tvName = findViewById(R.id.tvDetailsName);
        tvLocation = findViewById(R.id.tvDetailsLocation);
        tvLikes = findViewById(R.id.tvDetailsLikes);
        tvDescription = findViewById(R.id.tvDetailsDescription);
        ivBack = findViewById(R.id.ivDetailsBack);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likes = 0;
                like = null;
                finish();
            }
        });

        Intent intent = getIntent();
        final Walk walk = Parcels.unwrap(intent.getParcelableExtra(WalksAdapter.KEY_DETAILS));

        tvName.setText(walk.getName());
        tvLocation.setText(walk.getLocation());
        tvDescription.setText(walk.getDescription());

        try {
            liked(walk);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        tvLikes.setText(String.valueOf(likes));

        if (like != null) {
            ivHeart.setImageResource(R.drawable.ic_vector_heart);
        } else {
            ivHeart.setImageResource(R.drawable.ic_vector_heart_stroke);
        }

        ivHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (like != null) {
                    ivHeart.setImageResource(R.drawable.ic_vector_heart_stroke);
                    like.deleteInBackground(new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "error deleting like", e);
                            }
                        }
                    });
                    likes--;
                    like = null;
                    tvLikes.setText(String.valueOf(likes));
                } else {
                    ivHeart.setImageResource(R.drawable.ic_vector_heart);
                    like = new Like();
                    like.setUser(ParseUser.getCurrentUser());
                    like.setWalk(walk);
                    like.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "error saving like", e);
                            }
                        }
                    });
                    likes++;
                    tvLikes.setText(String.valueOf(likes));
                }

            }
        });

        Glide.with(getApplicationContext()).load(walk.getImage().getUrl()).into(ivBackdrop);
        Glide.with(getApplicationContext()).load(walk.getAuthor().getParseFile("profileImage")
                .getUrl()).circleCrop().into(ivProfile);
    }

    private void liked(Walk walk) throws ParseException {
        ParseQuery<Like> query = ParseQuery.getQuery(Like.class);
        query.include(Like.KEY_WALK);
        query.whereEqualTo(Like.KEY_WALK, walk);

        List<Like> objects = query.find();

        for (Like ob : objects) {
            likes++;
            try {
                if (ob.getUser().fetchIfNeeded().getUsername().equals(ParseUser.getCurrentUser().getUsername())) {
                    // Toast.makeText(WalkDetailsActivity.this, "found " + ob.toString(), Toast.LENGTH_SHORT).show();
                    like = ob;

                    Log.i(TAG, like.toString());
                }
            } catch (ParseException ex) {
                ex.printStackTrace();
            }
        }
    }
}