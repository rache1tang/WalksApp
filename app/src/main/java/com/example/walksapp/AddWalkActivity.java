package com.example.walksapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.parse.ParseUser;

import org.parceler.Parcels;

public class AddWalkActivity extends AppCompatActivity {

    public static final  String KEY_NEW_WALK = "new_walk";

    EditText etName;
    EditText etLocation;
    EditText etDescription;
    ImageView ivImage;
    Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_walk);

        etName = findViewById(R.id.etAddName);
        etLocation = findViewById(R.id.etAddLocation);
        etDescription = findViewById(R.id.etAddDescription);
        ivImage = findViewById(R.id.ivAddImage);
        btnNext = findViewById(R.id.btnNext);

        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // upload or take picture
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // extract info from edit texts
                String name = etName.getText().toString();
                String location = etLocation.getText().toString();
                String  description = etDescription.getText().toString();

                // add to new walk object
                Walk walk = new Walk();
                walk.setAuthor(ParseUser.getCurrentUser());
                walk.setDescription(description);
                walk.setName(name);
                walk.setLocation(location);

                // send over to tags / map activity
                Intent intent = new Intent(AddWalkActivity.this, AddTagsActivity.class);
                intent.putExtra(KEY_NEW_WALK, Parcels.wrap(walk));

                startActivity(intent);
            }
        });
    }
}