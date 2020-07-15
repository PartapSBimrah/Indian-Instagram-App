package com.royalbimrah.likwik.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.royalbimrah.likwik.EditProfileActivity;
import com.royalbimrah.likwik.FollowersActivity;
import com.royalbimrah.likwik.Model.Notification;
import com.royalbimrah.likwik.Model.PhotoInProfileAdapter;
import com.royalbimrah.likwik.Model.Post;
import com.royalbimrah.likwik.Model.User;
import com.royalbimrah.likwik.OptionsActivity;
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
import java.util.HashMap;


public class ProfileFragment extends Fragment {

    private TextView posts_tv, followers_tv, following_tv, username_tv, fullname_tv, bio_tv;
    private ImageView profileImage_iv, options_iv;
    private Button edit_btn, follow_btn;
    private ImageButton photos_ibtn, saved_ibtn;

    private RecyclerView photosRecyclerView;
    private PhotoInProfileAdapter profilePhotosadapter;
    private ArrayList<Post> postsList;

    private ArrayList<SavedPost> savedPostData;   // first publisher id second post id

    private RecyclerView savesRecyclerView;
    private PhotoInProfileAdapter savesPhotosadapter;
    private ArrayList<Post> savedPostsList;


    private FirebaseUser firebaseUser;

    private String profileId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        if (prefs.contains("profileId")) {
            // open other profile
            profileId = prefs.getString("profileId", "none");
        } else {
            // open my profile
            profileId = firebaseUser.getUid();
        }


        posts_tv = view.findViewById(R.id.posts_textview);
        followers_tv = view.findViewById(R.id.followers_textview);
        following_tv = view.findViewById(R.id.following_textview);
        username_tv = view.findViewById(R.id.username_textview);
        fullname_tv = view.findViewById(R.id.fullname_textview);
        bio_tv = view.findViewById(R.id.bio_textview);
        profileImage_iv = view.findViewById(R.id.profileImage_imageview);
        options_iv = view.findViewById(R.id.options_imageview);
        edit_btn = view.findViewById(R.id.editProfile_btn);
        follow_btn = view.findViewById(R.id.follow_btn);
        photos_ibtn = view.findViewById(R.id.myPhotos_imageBtn);
        saved_ibtn = view.findViewById(R.id.saved_imageBtn);

        // main profile posts recycle view
        photosRecyclerView = view.findViewById(R.id.photos_recycleview);
        photosRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 3);
        photosRecyclerView.setLayoutManager(linearLayoutManager);
        postsList = new ArrayList<>();
        profilePhotosadapter = new PhotoInProfileAdapter(getContext(), postsList);
        photosRecyclerView.setAdapter(profilePhotosadapter);

        // saved posts recycle view
        savesRecyclerView = view.findViewById(R.id.saved_recycleview);
        savesRecyclerView.setHasFixedSize(true);
        LinearLayoutManager savesLinearLayoutManager = new GridLayoutManager(getContext(), 3);
        savesRecyclerView.setLayoutManager(savesLinearLayoutManager);
        savedPostsList = new ArrayList<>();
        savesPhotosadapter = new PhotoInProfileAdapter(getContext(), savedPostsList);
        savesRecyclerView.setAdapter(savesPhotosadapter);

        photosRecyclerView.setVisibility(View.VISIBLE);
        savesRecyclerView.setVisibility(View.GONE);


        // update bars info
        updateProfileInfo();
        updateFollowersFollowingNo();
        updatePostsNo();
        // read photos in profile
        readProfilePhotos();
        // load saved posts
        readSavedPosts();

        if (profileId.equals(firebaseUser.getUid())) {
            // my profile
            edit_btn.setText("Edit Profile");
            follow_btn.setVisibility(View.GONE);
        } else {
            // other profile
            setFollowButtonText();
            saved_ibtn.setVisibility(View.GONE);
            edit_btn.setVisibility(View.GONE);
        }



        // my profile then edit
        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), EditProfileActivity.class));
            }
        });

        // follow and unfollow
        follow_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (follow_btn.getText().equals("Follow")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(firebaseUser.getUid()).child("Following")
                            .child(profileId).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(profileId).child("Followers")
                            .child(firebaseUser.getUid()).setValue(true);

                    addNotification();
                } else if (follow_btn.getText().equals("Following")) {
                    // already friends, then un-friend
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(firebaseUser.getUid()).child("Following")
                            .child(profileId).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(profileId).child("Followers")
                            .child(firebaseUser.getUid()).removeValue();

                    removeNotification();
                }
            }
        });

        // showing my posts or saved posts
        photos_ibtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photosRecyclerView.setVisibility(View.VISIBLE);
                savesRecyclerView.setVisibility(View.GONE);
            }
        });
        saved_ibtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                photosRecyclerView.setVisibility(View.GONE);
                savesRecyclerView.setVisibility(View.VISIBLE);
            }
        });

        // open followers list
        followers_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileId);
                intent.putExtra("title", "followers");
                startActivity(intent);
            }
        });

        // open following list
        following_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), FollowersActivity.class);
                intent.putExtra("id", profileId);
                intent.putExtra("title", "following");
                startActivity(intent);
            }
        });

        options_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), OptionsActivity.class);
                startActivity(intent);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    // update profile info depending on preset profile id (my profile or other)
    private void updateProfileInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (getActivity() == null) {
                    // needed to wait until profile image uploaded if changed (EditProfileActivity)
                    return;
                }
                User user = dataSnapshot.getValue(User.class);
                Glide.with(getContext()).load(user.getImageUrl()).into(profileImage_iv);
                username_tv.setText(user.getUsername());
                fullname_tv.setText(user.getFullName());
                bio_tv.setText(user.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // setting text on button (follow or following) in case of other profile
    private void setFollowButtonText() {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("Following");
        // not single event to follow and unfollow many times
        databaseReference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.v("Profile Fragment", "follow button status : "+ follow_btn);
                Log.v("Profile Fragment", "context status : "+ getActivity());

                if (getActivity() == null) {
                    // needed to wait until profile image uploaded if changed (EditProfileActivity)
                    return;
                }

                if (dataSnapshot.child(profileId).exists()) {
                    follow_btn.setText("Following");
                    follow_btn.setBackgroundResource(R.drawable.button_black);
                    follow_btn.setBackgroundTintList(null);
                    follow_btn.setTextColor(getResources().getColor(R.color.black));

                } else {
                    follow_btn.setText("Follow");
                    follow_btn.setBackgroundResource(R.drawable.button_background);
                    follow_btn.setBackgroundTintList(ContextCompat.getColorStateList(getActivity().getApplicationContext(), R.color.colorPrimary));
                    follow_btn.setTextColor(getResources().getColor(R.color.white));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // update number of followers and following
    private void updateFollowersFollowingNo() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Follow").child(profileId).child("Followers");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followers_tv.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        DatabaseReference reference1 = FirebaseDatabase.getInstance()
                .getReference("Follow").child(profileId).child("Following");

        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // -1 to exclude myself
                following_tv.setText("" + (dataSnapshot.getChildrenCount()-1));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // update number of posts
    private void updatePostsNo() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Posts").child(profileId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                posts_tv.setText("" + dataSnapshot.getChildrenCount());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readProfilePhotos() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(profileId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                postsList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Post post = snapshot.getValue(Post.class);
                    postsList.add(post);
                }
                // here we read from single user ,, no need to sort by time (already sorted)
                // in home fragment we sorted them by time as from many users sorted for each user
                // so we needed to sort the whole result by time
                Collections.reverse(postsList);
                profilePhotosadapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /* fill savedPostsIds with ids of posts i saved
        and then read these posts from database calling readSaved
        sort the result posts according to save time not publish time
     */
    private void readSavedPosts() {
        savedPostData = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    // post i saved by j
                    String postId = snapshot.getKey();
                    for (DataSnapshot ss : snapshot.getChildren()) {
                        String publisher = ss.getKey();
                        String timeInM = ss.getValue().toString();

                        savedPostData.add(new SavedPost(postId, publisher,timeInM));
                    }
                }
//                // sort according to save time
                Collections.sort(savedPostData, new Comparator<SavedPost>() {
                    @Override
                    public int compare(SavedPost o1, SavedPost o2) {
                        return o1.getTimeInMillis().compareTo(o2.getTimeInMillis());
                    }
                });
                // just reverse to get last first
                Collections.reverse(savedPostData);
                readSaved();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    // read saved posts using savedPostsId list
    private void readSaved() {

        savedPostsList.clear();

        for (SavedPost savedPost : savedPostData) {
            String postId = savedPost.getPostId();
            String publisher = savedPost.getPublisherId();
            FirebaseDatabase.getInstance().getReference("Posts")
                    .child(publisher).child(postId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Post post = dataSnapshot.getValue(Post.class);
                        savedPostsList.add(post);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

        }
        // no need for sorting as we sorted them in the order they were saved
        savesPhotosadapter.notifyDataSetChanged();
    }

    private class SavedPost {
        String postId;
        String publisherId;
        String timeInMillis;

        SavedPost(String id, String pub, String time) {
            this.postId = id;
            this.publisherId = pub;
            this.timeInMillis = time;
        }

        public String getPostId() {
            return postId;
        }

        public String getPublisherId() {
            return publisherId;
        }

        public String getTimeInMillis() {
            return timeInMillis;
        }
    }

    private void addNotification() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(profileId);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userId", firebaseUser.getUid());
        hashMap.put("note", "started following you");
        hashMap.put("posterId", "");
        hashMap.put("postId", "");
        hashMap.put("commentId", "");
        hashMap.put("isPost", false);

        reference.push().setValue(hashMap);
    }

    // remove follow notification when press unfollow
    private void removeNotification() {
        final DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Notifications").child(profileId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    if (!notification.getIsPost() && notification.getUserId().equals(firebaseUser.getUid())) {
                        reference.child(snapshot.getKey()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

}
