package com.example.walksapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.walksapp.fragments.ProfileFragment;
import com.example.walksapp.fragments.SearchFragment;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {

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

    ParseFile photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        etName = findViewById(R.id.etEditName);
        etUser = findViewById(R.id.etEditUser);
        etLocation = findViewById(R.id.etEditLocation);
        etEmail = findViewById(R.id.etEditEmail);

        final ParseUser user = ParseUser.getCurrentUser();
        etName.setText(user.getString("name"));
        etUser.setText(user.getUsername());
        etLocation.setText(user.getString("location"));
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
        Glide.with(getApplicationContext()).load(user.getParseFile("profileImage").getUrl()).circleCrop().into(ivEditProfile);
        ivEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to selecting an image
                onPickPhoto(view);
            }
        });

        btnSave = findViewById(R.id.btnEditSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // save user and refresh page/go back to profile page
                String name = etName.getText().toString();
                String username = etUser.getText().toString();
                String location = etLocation.getText().toString();
                String email = etEmail.getText().toString();

                // check that all fields are filled out
                if (name.isEmpty()) Toast.makeText(EditProfileActivity.this, "Name field is required", Toast.LENGTH_SHORT).show();
                else if (username.isEmpty()) Toast.makeText(EditProfileActivity.this, "Username field is required", Toast.LENGTH_SHORT).show();
                //else if (location.isEmpty()) Toast.makeText(EditProfileActivity.this, "Location field is required", Toast.LENGTH_SHORT).show();
                else if (email.isEmpty()) Toast.makeText(EditProfileActivity.this, "Email field is required", Toast.LENGTH_SHORT).show();
                else {
                    user.put("name", name);
                    user.setUsername(username);
                    user.put("location", location);
                    user.setEmail(email);

                    if (photoFile != null) {
                        user.put("profileImage", photoFile);
                    }

                    user.put("tags", String.join(" ", selected));

                    user.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "error saving user", e);
                            }
                        }
                    });

                    Intent i = new Intent();
                    setResult(RESULT_OK, i);

                    finish();
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
    }

    private void getSelected(ParseUser user) {
        String tagString = user.getString("tags");
        if (tagString != null)
            selected.addAll(SearchFragment.parseString(tagString));
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
                adapter.notifyDataSetChanged();
            }
        });
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
}