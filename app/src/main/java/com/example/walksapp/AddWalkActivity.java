package com.example.walksapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class AddWalkActivity extends AppCompatActivity {

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
                // add to new walk object
                // send over to tags / map activity
            }
        });
    }
}