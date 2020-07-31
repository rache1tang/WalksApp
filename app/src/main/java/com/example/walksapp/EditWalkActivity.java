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

import com.example.walksapp.fragments.HomeFragment;
import com.example.walksapp.fragments.SearchFragment;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
    HashSet<String> originalTags;

    ParseFile photoFile;

    String oldDescriptionName;

    public static Walk walk;

    PopupWindow popup;

    byte[] bitmapBytes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_walk);

        photoFile = null;
        TagsAdapter.newTags = new HashSet<>();
        originalTags = new HashSet<>();

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
        oldDescriptionName = walk.getDescription() + " " + walk.getName();

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
                        Delete.fromComments(walk);
                        try {
                            Delete.fromData(walk);
                            Delete.fromLikes(walk);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (HomeFragment.likedWalks.contains(walk.getObjectId()))
                            HomeFragment.likedWalks.remove(walk.getObjectId());
                        HomeFragment.walks.remove(WalksAdapter.position);
                        HomeFragment.adapter.notifyDataSetChanged();
                        walk.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) {
                                    Log.e(TAG, "error deleting walk", e);
                                    return;
                                }
                                Intent i = new Intent();
                                setResult(RESULT_CANCELED, i);
                                popup.dismiss();
                                finish();
                            }
                        });
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

                ParseQuery<Data> query = ParseQuery.getQuery(Data.class);
                try {
                    Data data = query.get(SearchFragment.tagsID);
                    JSONObject ob = data.getData();
                    for (String tag : TagsAdapter.newTags) {
                        JSONObject obNew = new JSONObject();
                        obNew.put(walk.getObjectId(), 0);
                        ob.put(tag, obNew);
                    }
                    JSONArray arr = new JSONArray();
                    for (String tag : selected) {
                        arr.put(tag);
                        JSONObject json = ob.getJSONObject(tag);
                        json.put(walk.getObjectId(), 0);
                        ob.put(tag, json);
                    }
                    for (String tag : originalTags) {
                        JSONObject json = ob.getJSONObject(tag);
                        if (!selected.contains(tag)) {
                            json.remove(walk.getObjectId());
                            Log.i(TAG, "removing tag from data  " + json.toString());
                            ob.put(tag, json);
                        }
                    }
                    walk.setTags(arr);
                    data.setData(ob);
                    data.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Log.e(TAG, "error saving new tag", e);
                            }
                        }
                    });
                } catch (ParseException | JSONException ex) {
                    ex.printStackTrace();
                }

                // save walk and send back to walk details activity
                String newName = etName.getText().toString();
                walk.put("name", newName);
                String newDescription = etDescription.getText().toString();
                walk.put("description", newDescription);
                if (photoFile != null) walk.setImage(photoFile);

                try {
                    Search.updateOtherJson(newName + " " + newDescription, oldDescriptionName, walk.getObjectId());
                } catch (ParseException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                walk.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "error saving walk", e);
                            return;
                        }
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                });




            }
        });
    }

    public void getSelectedTags() {
        JSONArray walkTags = walk.getTags();
        if (walkTags != null) {
            for (int i = 0; i < walkTags.length(); i++) {
                try {
                    originalTags.add(walkTags.getString(i));
                    selected.add(walkTags.getString(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            adapter.notifyDataSetChanged();
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
            tags.add("+");
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

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            selectedImage.compress(Bitmap.CompressFormat.PNG, 0, stream);
            bitmapBytes = stream.toByteArray();
            photoFile = new ParseFile(bitmapBytes);

            // Load the selected image into a preview
            ivBanner.setImageBitmap(selectedImage);
            WalkDetailsActivity.ivBackdrop.setImageBitmap(selectedImage);
        }
    }
}