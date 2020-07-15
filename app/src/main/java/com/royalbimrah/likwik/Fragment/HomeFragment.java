package com.royalbimrah.likwik.Fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.royalbimrah.likwik.Model.Post;
import com.royalbimrah.likwik.Model.PostAdapter;
import com.royalbimrah.likwik.Model.Story;
import com.royalbimrah.likwik.Model.StoryAdapter;
import com.royalbimrah.likwik.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class HomeFragment extends Fragment {
    private RecyclerView postsRecycleView;
    private PostAdapter postAdapter;
    private ArrayList<Post> postsList;

    private RecyclerView storiesRecycleView;
    private StoryAdapter storyAdapter;
    private ArrayList<Story> storiesList;

    private ProgressBar progressBar;    // gone after loading posts

    private FirebaseUser firebaseUser;

    private ArrayList<String> followingList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        postsRecycleView = view.findViewById(R.id.posts_recycleView);
        postsRecycleView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postsRecycleView.setLayoutManager(linearLayoutManager);

        postsList = new ArrayList<>();
        followingList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postsList);
        postsRecycleView.setAdapter(postAdapter);

        storiesRecycleView = view.findViewById(R.id.stories_recycleView);
        storiesRecycleView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        storiesRecycleView.setLayoutManager(linearLayoutManager1);

        storiesList = new ArrayList<>();
        storyAdapter = new StoryAdapter(getContext(), storiesList);
        storiesRecycleView.setAdapter(storyAdapter);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        progressBar = view.findViewById(R.id.progressBar);

        fillFollowingList();
        readPosts();
        readStories();

        // Inflate the layout for this fragment
        return view;
    }

    // fill followingList with users who i follow
    private void fillFollowingList() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Follow").child(firebaseUser.getUid()).child("Following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    followingList.add(snapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readPosts() {

        final DatabaseReference postReference = FirebaseDatabase.getInstance().getReference("Posts");

        postReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postsList.clear();
                // Posts contains each user id ,, and each user id contains his posts
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // check if following this user
                    for (String id : followingList) {
                        if (id.equals(snapshot.getKey())) {
                            // add his posts
                            for (DataSnapshot postsSnapshot : snapshot.getChildren()) {
                                Post post = postsSnapshot.getValue(Post.class);
                                postsList.add(post);
                            }
                        }
                    }
                }
                // sort according to post time
                Collections.sort(postsList, new Comparator<Post>() {
                    @Override
                    public int compare(Post o1, Post o2) {
                        return o1.getTimeInMillis().compareTo(o2.getTimeInMillis());
                    }
                });
                postAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readStories() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long currentTime = System.currentTimeMillis();
                storiesList.clear();
                storiesList.add(new Story("", firebaseUser.getUid(), "", 0, 0));
                // stories of my followings
                for (String userId: followingList) {
                    // skip if my story
                    if (userId.equals(firebaseUser.getUid()))
                        continue;

                    int count = 0;
                    Story story = null;
                    for (DataSnapshot snapshot: dataSnapshot.child(userId).getChildren()) {
                        story = snapshot.getValue(Story.class);
                        if (currentTime > story.getStartTime() && currentTime < story.getEndTime()) {
                            count++;
                        }
                    }
                    if (count > 0) {
                        storiesList.add(story);
                    }
                }
                storyAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
