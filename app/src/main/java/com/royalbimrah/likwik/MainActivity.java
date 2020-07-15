package com.royalbimrah.likwik;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.royalbimrah.likwik.Fragment.HomeFragment;
import com.royalbimrah.likwik.Fragment.NotificationFragment;
import com.royalbimrah.likwik.Fragment.ProfileFragment;
import com.royalbimrah.likwik.Fragment.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;
    String tag = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation_bar);

        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        // open commenter profile if clicked (sent from comment adapter)
        Bundle intent = getIntent().getExtras();
        if (intent != null) {   // opening other user profile from comments
            String publisherId = intent.getString("publisherId");

            SharedPreferences.Editor e = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
            e.putString("profileId", publisherId);
            e.apply();

            tag = "profileFragment";
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProfileFragment(), "profileFragment").commit();
        } else {
            // open my homepage
            tag = "homeFragment";
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment(), tag).commit();
        }


    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    setIcons(menuItem.getItemId());  // set icons

                    switch (menuItem.getItemId()) {
                        case R.id.home_nav:
                            selectedFragment = new HomeFragment();
                            tag = "homeFragment";
                            break;
                        case R.id.search_nav:
                            selectedFragment = new SearchFragment();
                            tag = "searchFragment";
                            break;
                        case R.id.add_nav:
                            selectedFragment = null;
                            startActivity(new Intent(MainActivity.this, PostActivity.class));
//                            finish();
                            break;
                        case R.id.favorite_nav:
                            selectedFragment = new NotificationFragment();
                            tag = "notificationFragment";
                            break;
                        case R.id.profile_nav:
                            SharedPreferences.Editor e = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
                            e.putString("profileId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                            e.apply();
                            selectedFragment = new ProfileFragment();
                            tag = "profileFragment";
                            break;
                    }

                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment, tag).commit();
                    }

                    return true;
                }
            };

    @Override
    public void onBackPressed() {
        Fragment homeFragment = getSupportFragmentManager().findFragmentByTag("homeFragment");
        //Fragment postDetailsFragment = getSupportFragmentManager().findFragmentByTag("PostDetailsFragment");

        if (homeFragment != null && homeFragment.isVisible()) {
            Log.v("Main Activity", "homeFragment status : "+homeFragment);
            super.onBackPressed();
        } else {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment(), "homeFragment").commit();
            bottomNavigationView.setSelectedItemId(R.id.home_nav);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateIcons();
    }

    private void updateIcons() {
        bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.ic_home);
        bottomNavigationView.getMenu().getItem(1).setIcon(R.drawable.ic_search);
        bottomNavigationView.getMenu().getItem(3).setIcon(R.drawable.ic_notification);
        bottomNavigationView.getMenu().getItem(4).setIcon(R.drawable.ic_profile);

        switch (tag) {
            case "homeFragment":
                bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.ic_home_clicked);
                bottomNavigationView.getMenu().getItem(0).setChecked(true);
                break;
            case "PostDetailsFragment":
                break;
            case "searchFragment":
                bottomNavigationView.getMenu().getItem(1).setIcon(R.drawable.ic_search_clicked);
                bottomNavigationView.getMenu().getItem(1).setChecked(true);
                break;
            case "notificationFragment":
                bottomNavigationView.getMenu().getItem(3).setIcon(R.drawable.ic_notification_clicked);
                bottomNavigationView.getMenu().getItem(3).setChecked(true);
                break;
            case "profileFragment":
                bottomNavigationView.getMenu().getItem(4).setIcon(R.drawable.ic_profile_clicked);
                bottomNavigationView.getMenu().getItem(4).setChecked(true);
                break;
            default:
                break;
        }


    }

    private void setIcons(int id) {
        if (id == R.id.home_nav) {
            bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.ic_home_clicked);
        } else {
            bottomNavigationView.getMenu().getItem(0).setIcon(R.drawable.ic_home);
        }

        if (id == R.id.search_nav) {
            bottomNavigationView.getMenu().getItem(1).setIcon(R.drawable.ic_search_clicked);
        } else {
            bottomNavigationView.getMenu().getItem(1).setIcon(R.drawable.ic_search);
        }

        if (id == R.id.favorite_nav) {
            bottomNavigationView.getMenu().getItem(3).setIcon(R.drawable.ic_notification_clicked);
        } else {
            bottomNavigationView.getMenu().getItem(3).setIcon(R.drawable.ic_notification);
        }

        if (id == R.id.profile_nav) {
            bottomNavigationView.getMenu().getItem(4).setIcon(R.drawable.ic_profile_clicked);
        } else {
            bottomNavigationView.getMenu().getItem(4).setIcon(R.drawable.ic_profile);
        }
    }
}
