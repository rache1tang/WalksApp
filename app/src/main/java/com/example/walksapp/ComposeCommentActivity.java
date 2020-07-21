package com.example.walksapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
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
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.parceler.Parcels;

public class ComposeCommentActivity extends AppCompatActivity {

    public static final String TAG = "ComposeCommentActivity";

    ImageView ivProfile;
    TextView tvInfo;
    EditText etText;
    Button btnPhotos;
    Button btnPost;
    ImageView ivCancel;

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

        final ParseUser user = ParseUser.getCurrentUser();

        Intent i = getIntent();
        final Walk walk = Parcels.unwrap(i.getParcelableExtra("walk"));

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

                // save comment
                comment.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Log.e(TAG, "error saving comment", e);
                        }
                    }
                });

                // send comment back to details activity
                Intent intent = new Intent();
                intent.putExtra("comment", Parcels.wrap(comment));
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
    }
}