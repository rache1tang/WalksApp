package com.example.walksapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.walksapp.fragments.ProfileFragment;
import com.example.walksapp.fragments.SearchFragment;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class EditProfileActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = "EditProfileActivity";

    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1047;

    Button btnLogout;
    Button btnSave;
    EditText etName;
    EditText etUser;
    EditText etLocation;
    EditText etEmail;
    RecyclerView rvEditTags;
    ImageView ivEditProfile;
    ImageView ivExit;
    List<String> tags;
    HashSet<String> selected;
    TagsAdapter adapter;
    Button btnPass;
    Switch switchPrivacy;

    ParseFile photoFile;

    GoogleMap map;
    ParseUser user;
    Place selectedPlace;
    boolean priv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etName = findViewById(R.id.etEditName);
        etUser = findViewById(R.id.etEditUser);
        etEmail = findViewById(R.id.etEditEmail);

        user = ParseUser.getCurrentUser();
        etName.setText(user.getString("name"));
        etUser.setText(user.getUsername());
        etEmail.setText(user.getEmail());

        rvEditTags = findViewById(R.id.rvEditTags);

        tags = new ArrayList<>();
        selected = new HashSet<>();
        getSelected(user);
        adapter = new TagsAdapter(getApplicationContext(), tags, selected);
        GridLayoutManager layout = new GridLayoutManager(EditProfileActivity.this, 3);

        rvEditTags.setLayoutManager(layout);
        rvEditTags.setAdapter(adapter);

        queryTags();

        ivExit = findViewById(R.id.ivEditExit);
        ivExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ivEditProfile = findViewById(R.id.ivEditProfileImg);
        Glide.with(getApplicationContext()).applyDefaultRequestOptions(new RequestOptions().disallowHardwareConfig()).load(user.getParseFile("profileImage").getUrl()).circleCrop().into(ivEditProfile);
        ivEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to selecting an image
                onPickPhoto(view);
            }
        });

        switchPrivacy = findViewById(R.id.switchPrivacy);
        switchPrivacy.setChecked(priv = user.getBoolean("private"));
        switchPrivacy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                switchPrivacy.setChecked(b);
                priv = b;
            }
        });

        btnSave = findViewById(R.id.btnEditSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // save user and refresh page/go back to profile page
                String name = etName.getText().toString();
                String username = etUser.getText().toString();
                String email = etEmail.getText().toString();

                // check that all fields are filled out
                if (name.isEmpty()) Toast.makeText(EditProfileActivity.this, "Name field is required", Toast.LENGTH_SHORT).show();
                else if (username.isEmpty()) Toast.makeText(EditProfileActivity.this, "Username field is required", Toast.LENGTH_SHORT).show();
                else if (email.isEmpty()) Toast.makeText(EditProfileActivity.this, "Email field is required", Toast.LENGTH_SHORT).show();
                else {
                    user.put("name", name);
                    user.setUsername(username);
                    user.setEmail(email);
                    user.put("private", priv);

                    if (photoFile != null) {
                        user.put("profileImage", photoFile);
                    }

                    JSONArray sel = new JSONArray();
                    for (String tag : selected) {
                        sel.put(tag);
                    }

                    user.put("tags", sel);

                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "error saving user", e);
                                return;
                            }
                            Intent i = new Intent();
                            setResult(RESULT_OK, i);
                            finish();
                        }
                    });
                }
            }
        });

        btnLogout = findViewById(R.id.btnEditLogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // log out user and go to login page
                ParseUser.getCurrentUser().logOut();
                Toast.makeText(getApplicationContext(), "Logout Successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        btnPass = findViewById(R.id.btnEditPassword);
        btnPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to edit password activity
                Intent intent = new Intent(EditProfileActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_api_key), Locale.US);
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.placeAutocomplete);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull Place place) {
                LatLng latLng = place.getLatLng();
                selectedPlace = place;
                map.addMarker(new MarkerOptions().position(latLng).title(place.getName()));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
                user.put("location", place.getName());
                user.put("locationGeo", new ParseGeoPoint(latLng.latitude, latLng.longitude));
            }


            @Override
            public void onError(@NotNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    private void getSelected(ParseUser user) {
        JSONArray tags = user.getJSONArray("tags");
        if (tags == null)
            return;
        for (int i = 0; i < tags.length(); i++) {
            try {
                selected.add(tags.getString(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void queryTags() {
        ParseQuery<Data> query = ParseQuery.getQuery(Data.class);
        try {
            Data data = query.get(SearchFragment.tagsID);
            JSONObject ob = data.getData();

            for (Iterator<String> it = ob.keys(); it.hasNext(); ) {
                String key = it.next();
                tags.add(key);
            }
            adapter.notifyDataSetChanged();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    // Trigger gallery selection for a photo
    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        // If you call startActivityForResult() using an intent that no app can handle, your app will crash.
        // So as long as the result is not null, it's safe to use the intent.
        if (intent.resolveActivity(getPackageManager()) != null) {
            // Bring up gallery to select a photo
            startActivityForResult(intent, PICK_PHOTO_CODE);
        }
    }

    public Bitmap loadFromUri(Uri photoUri) {
        Bitmap image = null;
        try {
            // check version of Android on device
            if(Build.VERSION.SDK_INT > 27){
                // on newer versions of Android, use the new decodeBitmap method
                ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), photoUri);
                image = ImageDecoder.decodeBitmap(source);
            } else {
                // support older versions of Android by using getBitmap
                image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
            }
        } catch (IOException e) {
            Log.e(TAG, "error loading from uri", e);
            //e.printStackTrace();
        }
        Log.i(TAG, image.toString());
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((data != null) && requestCode == PICK_PHOTO_CODE) {
            Uri photoUri = data.getData();

            // Load the image located at photoUri into selectedImage
            Bitmap selectedImage = loadFromUri(photoUri);
            // Bitmap circularBitmap = getCircularBitmap(selectedImage);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.PNG, 0, stream);
            byte[] bitmapBytes = stream.toByteArray();
            photoFile = new ParseFile(bitmapBytes);
            ProfileFragment.profileFile = new ParseFile(bitmapBytes);

            //Glide.with(getApplicationContext()).load(photoFile.getUrl()).circleCrop().into(ivEditProfile);

            // Load the selected image into a preview
            ivEditProfile.setImageBitmap(selectedImage);


        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        ParseGeoPoint loc = user.getParseGeoPoint("locationGeo");

        if (loc != null) {
            LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());

            map.addMarker(new MarkerOptions().position(latLng).title(user.getString("location")));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
        } else {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0,0), 12.0f));
        }
    }

    public static Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        // return _bmp;
        return output;

    }
}