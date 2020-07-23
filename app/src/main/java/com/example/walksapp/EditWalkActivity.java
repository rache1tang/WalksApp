package com.example.walksapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.walksapp.fragments.SearchFragment;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class EditWalkActivity extends AppCompatActivity {

    public static final String TAG = "EditWalkActivity";
    public static final String KEY_EDITED = "editedWalk";
    public static final int PICK_PHOTO_CODE = 2;

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

    PopupWindow popup;

    byte[] bitmapBytes;

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
                onPickPhoto(view);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // show popup for "are you sure?"
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View popupView = inflater.inflate(R.layout.popup_delete_walk, null);
                popup = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                Button btnCancel = popupView.findViewById(R.id.btnDeleteCancel);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        popup.dismiss();
                    }
                });

                Button btnDel = popupView.findViewById(R.id.btnDeleteSure);
                btnDel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        walk.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.e(TAG, "error deleting walk", e);
                                }
                            }
                        });

                        Intent i = new Intent();
                        setResult(RESULT_CANCELED, i);
                        finish();
                    }
                });

                popup.setFocusable(true);
                popup.update();
                popup.setAnimationStyle(R.style.Animation);
                popup.showAtLocation(view, Gravity.CENTER, 0, 0);

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
                if (photoFile != null) walk.setImage(photoFile);
                walk.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "error saving walk", e);
                        }
                    }
                });
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
            bitmapBytes = stream.toByteArray();
            photoFile = new ParseFile(bitmapBytes);

            // Load the selected image into a preview
            ivBanner.setImageBitmap(selectedImage);
        }
    }
}