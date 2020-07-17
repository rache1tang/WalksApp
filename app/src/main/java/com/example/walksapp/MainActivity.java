package com.example.walksapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.walksapp.fragments.FavoritesFragment;
import com.example.walksapp.fragments.HomeFragment;
import com.example.walksapp.fragments.ProfileFragment;
import com.example.walksapp.fragments.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigation;
    private static int id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        final FragmentManager fragmentManager = getSupportFragmentManager();

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment fragment;
                switch (item.getItemId()) {
                    case R.id.actionHome:
                        id = R.id.actionHome;
                        fragment = new HomeFragment();
                        break;
                    case R.id.actionProfile:
                        id = R.id.actionProfile;
                        fragment = new ProfileFragment();
                        break;
                    case R.id.actionSearch:
                        id = R.id.actionSearch;
                        fragment = new SearchFragment();
                        break;
                    case R.id.actionFavorites:
                    default:
                        id = R.id.actionFavorites;
                        fragment = new FavoritesFragment();

                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });

        // set default selection
        id = R.id.actionHome;
        bottomNavigation.setSelectedItemId(R.id.actionHome);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == WalksAdapter.WALK_DETAILS_CODE && resultCode == RESULT_OK) {
            Fragment fragment;
            switch (id) {
                case R.id.actionHome:
                    fragment = new HomeFragment();
                    break;
                case R.id.actionProfile:
                    fragment = new ProfileFragment();
                    break;
                case R.id.actionSearch:
                    fragment = new SearchFragment();
                    break;
                case R.id.actionFavorites:
                default:
                    fragment = new FavoritesFragment();
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.flContainer, fragment).commit();
        }
    }
}