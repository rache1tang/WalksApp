package com.example.walksapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

public class WalkDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = "WalkDetailsActivity";
    public static final int COMMENT_CODE = 921;
    public static final int EDIT_CODE = 1999;
    public static final String KEY_EDIT_WALK = "walkToEdit";

    public static ImageView ivBackdrop;
    ImageView ivProfile;
    ImageView ivHeart;
    TextView tvName;
    TextView tvLocation;
    TextView tvLikes;
    TextView tvDescription;
    ImageView ivBack;
    ImageView ivComment;
    ImageView ivEdit;

    RecyclerView rvComments;
    CommentsAdapter commentsAdapter;
    List<Comment> comments;

    RecyclerView rvPhotos;
    PhotosAdapter photosAdapter;
    List<ParseFile> photos;

    TextView tvNoComments;

    public static List<ParseFile> photoFiles;

    Walk walk;

    GoogleMap map;

    List<LatLng> path;

    private static int likes;
    private static Like like;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_details);

        photoFiles = new ArrayList<>();

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
        tvNoComments = findViewById(R.id.tvNoCommentsNotice);
        ivEdit = findViewById(R.id.ivDetailsEdit);
        rvPhotos = findViewById(R.id.rvPhotos);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.detailsMap);
        mapFragment.getMapAsync(this);

        comments = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(getApplicationContext(), comments);

        photos = new ArrayList<>();
        photosAdapter = new PhotosAdapter(getApplicationContext(), photos);
        GridLayoutManager layout = new GridLayoutManager(WalkDetailsActivity.this, 3);

        rvPhotos.setAdapter(photosAdapter);
        rvPhotos.setLayoutManager(layout);

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

        try {
            if (walk.getPath() != null)
                translateToLatLng(walk.getPath());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            if (ParseUser.getCurrentUser().fetchIfNeeded().getUsername().equals(walk.getAuthor().getUsername())) {
                ivEdit.setVisibility(View.VISIBLE);
            } else {
                ivEdit.setVisibility(View.INVISIBLE);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to edit activity
                Intent intent = new Intent(WalkDetailsActivity.this, EditWalkActivity.class);
                intent.putExtra(KEY_EDIT_WALK, Parcels.wrap(walk));
                startActivityForResult(intent, EDIT_CODE);
            }
        });

        ivComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to compose comment activity
                photoFiles.clear();
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

        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // view profile
                Intent i = new Intent(WalkDetailsActivity.this, ViewProfileActivity.class);
                i.putExtra("author", Parcels.wrap(walk.getAuthor()));
                startActivity(i);
            }
        });

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
        queryCommentPhotos();
    }

    private void queryCommentPhotos() {
        ParseQuery<CommentPhoto> query = ParseQuery.getQuery(CommentPhoto.class);
        query.whereEqualTo(CommentPhoto.KEY_WALK, walk);
        query.setLimit(20);
        query.addAscendingOrder("createdAt");
        query.findInBackground(new FindCallback<CommentPhoto>() {
            @Override
            public void done(List<CommentPhoto> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error querying commented photos", e);
                    return;
                }
                if (objects.isEmpty()) {
                    rvPhotos.setVisibility(View.INVISIBLE);
                } else {
                    rvPhotos.setVisibility(View.VISIBLE);
                }
                for (CommentPhoto ob : objects) {
                    photos.add(ob.getFile());
                }
                photosAdapter.notifyDataSetChanged();
            }
        });
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
                if (objects.isEmpty())
                    tvNoComments.setVisibility(View.VISIBLE);
                else
                    tvNoComments.setVisibility(View.INVISIBLE);
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

            tvNoComments.setVisibility(View.INVISIBLE);

            // save comment
            comment.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e(TAG, "error saving comment", e);
                    }
                }
            });

            for (ParseFile file : photoFiles) {
                CommentPhoto commentPhoto = new CommentPhoto();
                commentPhoto.setComment(comment);
                commentPhoto.setWalk(walk);
                commentPhoto.setFile(file);

                try {
                    commentPhoto.save();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                photos.add(0, file);
                photosAdapter.notifyItemInserted(0);
                rvPhotos.scrollToPosition(0);
            }

        } else if (requestCode == EDIT_CODE && resultCode == RESULT_OK) {
            return;
        } else if (resultCode == RESULT_CANCELED) {
            finish();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        ParseGeoPoint loc = walk.getLocationGeo();
        LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());

        map.addMarker(new MarkerOptions().position(latLng).title(walk.getLocation()));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0f));

        if (path != null) {
            PolylineOptions poly = new PolylineOptions().addAll(path);
            map.addPolyline(poly);
        }
    }

    public void translateToLatLng(JSONArray points) throws JSONException {
        path = new ArrayList<>();

        for (int i = 0; i < points.length(); i ++) {
            JSONArray coord = points.getJSONArray(i);
            double lat = coord.getDouble(0);
            double lng = coord.getDouble(1);
            LatLng point = new LatLng(lat, lng);
            path.add(point);
        }
    }
}