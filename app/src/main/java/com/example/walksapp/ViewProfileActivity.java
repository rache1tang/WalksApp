package com.example.walksapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.walksapp.fragments.ViewProfileScrollFragment;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import org.parceler.Parcels;

public class ViewProfileActivity extends AppCompatActivity {

    ImageView ivProfileImg;
    TextView tvProfileName;
    TextView tvProfileUser;
    TextView tvProfileLoc;
    ImageView ivBack;
    public static ParseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        Intent i = getIntent();
        user = Parcels.unwrap(i.getParcelableExtra("author"));

        ivProfileImg = findViewById(R.id.ivProfileImg);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileUser = findViewById(R.id.tvProfileUser);
        tvProfileLoc = findViewById(R.id.tvProfileLoc);
        ivBack = findViewById(R.id.ivProfileBack);

        ParseFile image = null;
        try {
            image = user.fetchIfNeeded().getParseFile("profileImage");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String name = user.getString("name");
        String username = user.getUsername();
        String location = user.getString("location");

        tvProfileLoc.setText(location);
        tvProfileName.setText(name);
        tvProfileUser.setText(username);
        Glide.with(getApplicationContext()).load(image.getUrl()).circleCrop().into(ivProfileImg);

        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Fragment fragment = new ViewProfileScrollFragment();

        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flProfileView, fragment).commit();
    }
}