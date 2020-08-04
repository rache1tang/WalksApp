package com.example.walksapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
    TextView tvSuggestedNotice;

    RecyclerView rvComments;
    CommentsAdapter commentsAdapter;
    List<Comment> comments;

    RecyclerView rvPhotos;
    PhotosAdapter photosAdapter;
    List<ParseFile> photos;

    RecyclerView rvSuggested;
    SearchWalksAdapter suggestedAdapter;
    List<Walk> suggested;

    TextView tvNoComments;
    TextView tvNoRelated;

    public static List<ParseFile> photoFiles;

    Walk walk;

    GoogleMap map;

    List<LatLng> path;

    ParseUser author;

    JSONArray likers;
    HashSet<String> likeHash;
    int likes;
    String userId;
    boolean likeBegin;
    JSONArray likeRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk_details);

        photoFiles = new ArrayList<>(); // set photos to empty array to populate later

        // get references to all components on page
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
        rvSuggested = findViewById(R.id.rvSuggested);
        tvNoRelated = findViewById(R.id.tvNoRelated);
        tvSuggestedNotice = findViewById(R.id.tvSuggested);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.detailsMap);
        mapFragment.getMapAsync(this);

        // set up comments adapter
        comments = new ArrayList<>();
        commentsAdapter = new CommentsAdapter(getApplicationContext(), comments);

        // set up photos adapter
        photos = new ArrayList<>();
        photosAdapter = new PhotosAdapter(getApplicationContext(), photos);

        // set up suggested adapter
        suggested = new ArrayList<>();
        suggestedAdapter = new SearchWalksAdapter(getApplicationContext(), suggested);

        // set up layout manager for suggested
        LinearLayoutManager layoutManage = new LinearLayoutManager(getApplicationContext());
        // bind with adapter
        rvSuggested.setLayoutManager(layoutManage);
        rvSuggested.setAdapter(suggestedAdapter);

        // set up layout manager for photos
        GridLayoutManager layout = new GridLayoutManager(WalkDetailsActivity.this, 3);

        // bind adapter to recycler view
        rvPhotos.setAdapter(photosAdapter);
        rvPhotos.setLayoutManager(layout);

        // exit details view -- update like if needed
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                setResult(RESULT_OK, i);

                // delete user from walk if unliked
                if (!likeHash.contains(userId) && likeBegin) {

                    // save likes with user
                    walk.setLikes(likeRes);
                    walk.saveInBackground(new SaveCallback() { // save walk
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "error saving walk", e);
                            }
                        }
                    });

                    // get liked
                    ParseUser user = ParseUser.getCurrentUser();
                    JSONArray userLiked = user.getJSONArray("liked");

                    // remove like from user
                    JSONArray newLike = new JSONArray();
                    if (userLiked != null) {
                        for (int j = 0; j < userLiked.length(); j++) {
                            try {
                                String likeId = userLiked.getString(j);
                                if (!likeId.equals(walk.getObjectId()))
                                    newLike.put(likeId);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    // remove from graph of liked walks
                    Suggest.removeLike(walk.getObjectId(), newLike);

                    user.put("liked", newLike);
                    user.saveInBackground(new SaveCallback() { // save user
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.i(TAG, "error saving user like", e);
                            }
                        }
                    });

                }
                // save like if liked
                if (likeHash.contains(userId) && !likeBegin) {

                    // save user to walk
                    likeRes.put(userId);
                    walk.setLikes(likeRes);
                    walk.saveInBackground(new SaveCallback() { // save walk
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "error saving walk", e);
                            }
                        }
                    });

                    // get liked
                    ParseUser user = ParseUser.getCurrentUser();
                    JSONArray userLiked = user.getJSONArray("liked");

                    // add like to graph of likes
                    Suggest.addLike(walk.getObjectId(), userLiked);

                    userLiked.put(walk.getObjectId()); // add walk to liked
                    user.put("liked", userLiked);
                    user.saveInBackground(new SaveCallback() { // save user
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.i(TAG, "error saving user like", e);
                            }
                        }
                    });
                }
                finish();
            }
        });

        // get walk from intent
        Intent intent = getIntent();
        walk = Parcels.unwrap(intent.getParcelableExtra(WalksAdapter.KEY_DETAILS));

        try {
            author = walk.getAuthor().fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // if author's profile setting is on private, profile picture does not show (cannot view profile)
        if (author.getBoolean("private"))
            ivProfile.setVisibility(View.INVISIBLE);
        else
            ivProfile.setVisibility(View.VISIBLE);

        // get latitude and longitude pairs
        try {
            if (walk.getPath() != null)
                translateToLatLng(walk.getPath());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // if current user is the author of the walk, they can edit it
        try {
            if (ParseUser.getCurrentUser().fetchIfNeeded().getUsername().equals(walk.getAuthor().getUsername())) {
                ivEdit.setVisibility(View.VISIBLE);
            } else {
                ivEdit.setVisibility(View.INVISIBLE);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // go to edit activity
        ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to edit activity
                Intent intent = new Intent(WalkDetailsActivity.this, EditWalkActivity.class);
                intent.putExtra(KEY_EDIT_WALK, Parcels.wrap(walk));
                startActivityForResult(intent, EDIT_CODE);
            }
        });

        // go to compose comment activity
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

        // set text for walk information
        tvName.setText(walk.getName());
        tvLocation.setText(walk.getLocation());
        tvDescription.setText(walk.getDescription());

        // go to author's profile
        ivProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // view profile
                Intent i = new Intent(WalkDetailsActivity.this, ViewProfileActivity.class);
                i.putExtra("author", Parcels.wrap(walk.getAuthor()));
                startActivity(i);
            }
        });

        // get users' objectIDs who have liked this post
        likers = walk.getLikes();
        // set number of likes and appearance of heart
        userId = ParseUser.getCurrentUser().getObjectId();
        likeHash = new HashSet<>();
        likeRes = new JSONArray();
        if (likers != null) {
            for (int i = 0; i < likers.length(); i++) {
                try {
                    String likeId = likers.getString(i);
                    likeHash.add(likeId);
                    if (!likeId.equals(userId))
                        likeRes.put(likeId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        if (likeHash.contains(userId)) {
            likeBegin = true;
            ivHeart.setImageResource(R.drawable.ic_vector_heart);
        } else {
            likeBegin = false;
            ivHeart.setImageResource(R.drawable.ic_vector_heart_stroke);
        }
        likes = likeHash.size();
        tvLikes.setText(String.valueOf(likes));

        // like/unlike (toggle image + edit number of likes)
        ivHeart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (likeHash.contains(userId)) { // already liked -- unlike
                    likeHash.remove(userId);
                    likes--;
                    tvLikes.setText(String.valueOf(likes));
                    ivHeart.setImageResource(R.drawable.ic_vector_heart_stroke);
                } else { // like
                    likeHash.add(userId);
                    likes++;
                    tvLikes.setText(String.valueOf(likes));
                    ivHeart.setImageResource(R.drawable.ic_vector_heart);
                }
            }
        });

        // load in banner image and profile image for walk
        Glide.with(getApplicationContext()).load(walk.getImage().getUrl()).into(ivBackdrop);
        Glide.with(getApplicationContext()).load(author.getParseFile("profileImage")
                .getUrl()).circleCrop().into(ivProfile);


        // bind comments adapter
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvComments.setAdapter(commentsAdapter);
        rvComments.setLayoutManager(linearLayoutManager);

        // query comments and photos
        queryComments();
        queryCommentPhotos();

        // query suggested walks
        querySuggested();
    }

    private void querySuggested() {
        ParseQuery<Walk> query = ParseQuery.getQuery(Walk.class);
        query.include("*");
        ParseQuery<Data> queryData = ParseQuery.getQuery(Data.class);
        queryData.whereEqualTo(Data.KEY_NAME, "suggest");
        try {
            final Data suggest = queryData.getFirst();
            JSONObject data = suggest.getData();
            if (data.has(walk.getObjectId())) {
                JSONObject dataWalk = data.getJSONObject(walk.getObjectId());
                if (dataWalk.length() == 0) {
                    tvNoRelated.setVisibility(View.VISIBLE);
                } else {
                    tvNoRelated.setVisibility(View.INVISIBLE);
                }
                for (Iterator<String> it = dataWalk.keys(); it.hasNext(); ) {
                    String key = it.next();
                    suggested.add(query.get(key));
                }
            } else {
                tvNoRelated.setVisibility(View.VISIBLE);
            }
            suggestedAdapter.notifyDataSetChanged();
        } catch (ParseException | JSONException e) {
            e.printStackTrace();
        }
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
                    rvPhotos.setVisibility(View.GONE);
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

            if (comments.isEmpty())
                tvNoComments.setVisibility(View.VISIBLE);
            else
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

            // save commented photos
            if (photoFiles.isEmpty()) {
                rvPhotos.setVisibility(View.GONE);
            } else {
                rvPhotos.setVisibility(View.VISIBLE);
            }
            for (ParseFile file : photoFiles) {

                CommentPhoto commentPhoto = new CommentPhoto();
                commentPhoto.setComment(comment);
                commentPhoto.setWalk(walk);
                commentPhoto.setFile(file);

                try {
                    // not in background because parse files need to be saved before using
                    commentPhoto.save();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                // insert photo into recycler view
                photos.add(0, file);
                photosAdapter.notifyItemInserted(0);
                rvPhotos.scrollToPosition(0);
            }

        } else if (requestCode == EDIT_CODE && resultCode == RESULT_OK) {
            walk = EditWalkActivity.walk;
            tvName.setText(walk.getName());
            tvDescription.setText(walk.getDescription());
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

        for (int i = 0; i < points.length(); i++) {
            JSONArray coord = points.getJSONArray(i);
            double lat = coord.getDouble(0);
            double lng = coord.getDouble(1);
            LatLng point = new LatLng(lat, lng);
            path.add(point);
        }
    }


}