package com.royalbimrah.likwik;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.royalbimrah.likwik.Model.User;
import com.royalbimrah.likwik.Model.UserAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FollowersActivity extends AppCompatActivity {

    String id;  // id of user or post
    String title;   // title of current activity (follower , following , likes)
    ArrayList<String> idsList;  // contains ids of users (followers or likes or following)

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    ArrayList<User> usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_followers);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        title = intent.getStringExtra("title");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.users_recycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersList = new ArrayList<>();
        userAdapter = new UserAdapter(this, usersList, false);
        recyclerView.setAdapter(userAdapter);

        idsList = new ArrayList<>();

        switch (title) {
            case "followers":
                getFollowers();
                break;
            case "following":
                getFollowing();
                break;
            case "likes":
                getLikes();
                break;
            case "views":
                getViews();
                break;
            default: break;
        }
        showUsers();

    }

    private void getFollowers() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Follow").child(id).child("Followers");
        /*
            onDataChange executes once and stop listening
            saves cost
         */
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idsList.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    idsList.add(snapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getFollowing() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Follow").child(id).child("Following");
        /*
            onDataChange executes once and stop listening
            saves cost
         */
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idsList.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    if (snapshot.getValue().equals(true))
                        idsList.add(snapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getLikes() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Likes").child(id);
        /*
            onDataChange executes once and stop listening
            saves cost
         */
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idsList.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    idsList.add(snapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getViews() {
        String storyId = getIntent().getStringExtra("storyId");
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Story").child(id).child(storyId).child("Views");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                idsList.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    idsList.add(snapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showUsers() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (idsList.contains(user.getId())){
                        usersList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
