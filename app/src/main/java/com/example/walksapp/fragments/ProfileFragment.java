package com.example.walksapp.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.walksapp.R;
import com.parse.ParseUser;


public class ProfileFragment extends Fragment {

    ImageView ivProfileImg;
    TextView tvProfileName;
    TextView tvProfileUser;
    TextView tvProfileLoc;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ivProfileImg = view.findViewById(R.id.ivProfileImg);
        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileUser = view.findViewById(R.id.tvProfileUser);
        tvProfileLoc = view.findViewById(R.id.tvProfileLoc);

        ParseUser user = ParseUser.getCurrentUser();

        tvProfileName.setText(user.getString("name"));
        tvProfileLoc.setText(user.getString("location"));
        tvProfileUser.setText("@"+ user.getUsername());
        Glide.with(getContext()).load(user.getParseFile("profileImage").getUrl()).into(ivProfileImg);

        Fragment fragment = new ProfileScrollFragment();

        final FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flProfile, fragment).commit();

    }
}