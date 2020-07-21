package com.example.walksapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import java.util.ArrayList;
import java.util.List;

public class WalkDetailsActivity extends AppCompatActivity {

    public static final String TAG = "WalkDetailsActivity";
    public static final int COMMENT_CODE = 921;

    ImageView ivBackdrop;
    ImageView ivProfile;
    ImageView ivHeart;
    TextView tvName;
    TextView tvLocation;
    TextView tvLikes;
    TextView tvDescription;
    ImageView ivBack;
    ImageView ivComment;

    RecyclerView rvComments;
    CommentsAdapter commentsAdapter;
    List<Comment> comments;

    Walk walk;

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
        ivComment = findViewById(R.id.ivDetailsComment);
        rvComments = findViewById(R.id.rvComments);

        comments = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(getApplicationContext(), comments);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likes = 0;
                like = null;

                Intent i = new Intent();
                setResult(RESULT_OK, i);

                finish();
            }
        });

        Intent intent = getIntent();
        walk = Parcels.unwrap(intent.getParcelableExtra(WalksAdapter.KEY_DETAILS));

        ivComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to compose comment activity
                Intent intent = new Intent(WalkDetailsActivity.this, ComposeCommentActivity.class);
                intent.putExtra("walk", Parcels.wrap(walk));
                startActivityForResult(intent, COMMENT_CODE);
            }
        });

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
        try {
            Glide.with(getApplicationContext()).load(walk.getAuthor().fetchIfNeeded().getParseFile("profileImage")
                    .getUrl()).circleCrop().into(ivProfile);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvComments.setAdapter(commentsAdapter);
        rvComments.setLayoutManager(linearLayoutManager);
        queryComments();
    }

    private void queryComments() {
        ParseQuery<Comment> query = ParseQuery.getQuery(Comment.class);
        query.whereEqualTo(Comment.KEY_WALK, walk);
        query.addAscendingOrder("createdAt");
        query.setLimit(20);
        query.findInBackground(new FindCallback<Comment>() {
            @Override
            public void done(List<Comment> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error querying comments", e);
                    return;
                }
                comments.addAll(objects);
                commentsAdapter.notifyDataSetChanged();
            }
        });
    }

    private void liked(Walk walk) throws ParseException {
        ParseQuery<Like> query = ParseQuery.getQuery(Like.class);
        query.include(Like.KEY_WALK);
        query.whereEqualTo(Like.KEY_WALK, walk);

        List<Like> objects = query.find();
        like = null;
        likes = 0;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == COMMENT_CODE && resultCode == RESULT_OK) {
            // get comment from intent
            Comment comment = Parcels.unwrap(data.getParcelableExtra("commentNew"));

            // insert into comments recycler view
            comments.add(0, comment);
            commentsAdapter.notifyItemInserted(0);
            rvComments.scrollToPosition(0);

        }
    }
}