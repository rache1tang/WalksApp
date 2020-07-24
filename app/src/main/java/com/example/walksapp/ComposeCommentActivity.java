package com.example.walksapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ComposeCommentActivity extends AppCompatActivity {

    public static final String TAG = "ComposeCommentActivity";
    public static final int PICK_PHOTO_CODE = 890;

    ImageView ivProfile;
    TextView tvInfo;
    EditText etText;
    Button btnPhotos;
    Button btnPost;
    ImageView ivCancel;

    List<Bitmap> mBitmapsSelected;

    RecyclerView rvPhotos;
    CommentPhotosAdapter adapter;

    Walk walk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_comment);

        ivProfile = findViewById(R.id.ivCommentProfile);
        tvInfo = findViewById(R.id.tvCommentInfo);
        etText = findViewById(R.id.etCommentText);
        btnPhotos = findViewById(R.id.btnCommentPhotos);
        btnPost = findViewById(R.id.btnCommentPost);
        ivCancel = findViewById(R.id.ivCommentCancel);
        rvPhotos = findViewById(R.id.rvCommentPhotos);

        mBitmapsSelected = new ArrayList<>();

        adapter = new CommentPhotosAdapter(mBitmapsSelected, getApplicationContext());
        GridLayoutManager layout = new GridLayoutManager(ComposeCommentActivity.this, 3);

        rvPhotos.setAdapter(adapter);
        rvPhotos.setLayoutManager(layout);

        final ParseUser user = ParseUser.getCurrentUser();

        Intent i = getIntent();
        walk = Parcels.unwrap(i.getParcelableExtra("walk"));

        // fill in profile picture
        ParseFile profile = user.getParseFile("profileImage");
        Glide.with(getApplicationContext()).load(profile.getUrl()).circleCrop().into(ivProfile);

        // fill in display name and username
        tvInfo.setText(user.getString("name") + "\n" + user.getUsername());

        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // make comment
                String content = etText.getText().toString();
                if (content.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Cannot make empty comment", Toast.LENGTH_SHORT).show();
                    return;
                }
                Comment comment = new Comment();
                comment.setUser(user);
                comment.setWalk(walk);
                comment.setComment(content);

                for (Bitmap bm : mBitmapsSelected) {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bm.compress(Bitmap.CompressFormat.PNG, 0, stream);
                    byte[] bitmapBytes = stream.toByteArray();

                    WalkDetailsActivity.photoFiles.add(new ParseFile(bitmapBytes));
                }

                // send comment back to details activity
                Intent intent = new Intent();
                intent.putExtra("commentNew", Parcels.wrap(comment));
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // cancel make comment
                finish();
            }
        });

        btnPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // go to select photos
                onPickPhoto(view);
            }
        });
    }

    // Trigger gallery selection for a photo
    public void onPickPhoto(View view) {
        // Create intent for picking a photo from the gallery
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_PHOTO_CODE);
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
        if (data.getClipData() != null) {
            ClipData mClipData = data.getClipData();
            for (int i = 0; i < mClipData.getItemCount(); i++) {
                ClipData.Item item = mClipData.getItemAt(i);
                Uri uri = item.getUri();

                // Use the loadFromUri method from above
                Bitmap bitmap = loadFromUri(uri);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
                byte[] bitmapBytes = stream.toByteArray();

                mBitmapsSelected.add(bitmap);
            }
            adapter.notifyDataSetChanged();
            Log.i(TAG, "size of bitmap array: " + mBitmapsSelected.size());
        }
    }
}