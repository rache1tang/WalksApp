package com.example.walksapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class AddWalkActivity extends AppCompatActivity {

    public static final String TAG = "AddWalkActivity";
    public static final  String KEY_NEW_WALK = "new_walk";
    public static final int REQUEST_CODE = 20;

    // PICK_PHOTO_CODE is a constant integer
    public final static int PICK_PHOTO_CODE = 1046;

    EditText etName;
    EditText etDescription;
    ImageView ivImage;
    Button btnNext;
    ImageView ivAddCancel;
    ParseFile photoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_walk);

        etName = findViewById(R.id.etAddName);
        etDescription = findViewById(R.id.etAddDescription);
        ivImage = findViewById(R.id.ivAddImage);
        btnNext = findViewById(R.id.btnNext);
        photoFile = null;

        ivImage.setImageResource(R.drawable.add_image_icon);

        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPickPhoto(view);
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // extract info from edit texts
                String name = etName.getText().toString();
                String  description = etDescription.getText().toString();

                boolean noPass = false;

                if (name.isEmpty() || name.equals("Required Field")) {
                    noPass = true;
                    //Toast.makeText(getApplicationContext(), "Name is Required", Toast.LENGTH_SHORT).show();
                    etName.setHintTextColor(Color.parseColor("#FF0000"));
                }
                if (noPass) return;

                // add to new walk object
                Walk walk = new Walk();
                walk.setAuthor(ParseUser.getCurrentUser());
                walk.setDescription(description);
                walk.setName(name);

                // send over to tags / map activity
                Intent intent = new Intent(AddWalkActivity.this, AddLocationActivity.class);
                intent.putExtra(KEY_NEW_WALK, Parcels.wrap(walk));

                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        ivAddCancel = findViewById(R.id.ivAddCancel);
        ivAddCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
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

            // Load the selected image into a preview
            ivImage.setImageBitmap(selectedImage);
        }

        else if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Walk walk = Parcels.unwrap(data.getParcelableExtra(AddLocationActivity.KEY_FINAL_WALK));
            if (photoFile != null) {
                walk.setImage(photoFile);
            }
            try {
                walk.save();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Intent i = new Intent();
            setResult(RESULT_OK, i);

            finish();

        } else {
            finish();
        }
    }
}