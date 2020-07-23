package com.example.walksapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.parse.ParseGeoPoint;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class AddLocationActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String TAG = "AddLocationAcitivity";
    public static final String KEY_NEW_WALK = "new new walk";
    public static final int CODE = 8;
    public static final String KEY_FINAL_WALK = "final final walk";

    ImageView ivCancel;
    private GoogleMap mMap;
    Button btnNext;
    Place selectedPlace = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        Intent i = getIntent();
        final Walk walk = Parcels.unwrap(i.getParcelableExtra(AddWalkActivity.KEY_NEW_WALK));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnNext = findViewById(R.id.btnLocNext);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to selecting tags
                if (selectedPlace == null) {
                    Toast.makeText(getApplicationContext(), "A location is required", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(AddLocationActivity.this, AddTagsActivity.class);
                intent.putExtra(KEY_NEW_WALK, Parcels.wrap(walk));
                startActivityForResult(intent, CODE);
            }
        });

        ivCancel = findViewById(R.id.ivLocCancel);
        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_api_key), Locale.US);
        }

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.placeAutocomplete);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.ID, Place.Field.NAME));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull Place place) {
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getLatLng().toString());
                LatLng latLng = place.getLatLng();
                selectedPlace = place;
                mMap.addMarker(new MarkerOptions().position(latLng).title(place.getName()));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
                walk.setLocation(place.getName());
                walk.setLocationGeo(new ParseGeoPoint(latLng.latitude, latLng.longitude));
            }


            @Override
            public void onError(@NotNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng pole = new LatLng(90, 135);
        mMap.addMarker(new MarkerOptions().position(pole).title("North Pole"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pole));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CODE) {
            Parcelable parcelable = data.getParcelableExtra(AddTagsActivity.KEY_FINAL_WALK);

            Intent i = new Intent();
            i.putExtra(KEY_FINAL_WALK, parcelable);
            setResult(RESULT_OK, i);

            finish();
        }
    }
}