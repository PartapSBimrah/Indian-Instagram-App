package com.royalbimrah.likwik;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.royalbimrah.likwik.Model.Story;
import com.royalbimrah.likwik.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener {

    int counter = 0;
    long pressTime = 0L;
    long limit = 500L;

    StoriesProgressView storiesProgressView;

    ImageView storyImage_iv, publisherImage_iv;
    TextView publisherUsername_tv;
    View prev_v, next_v;
    LinearLayout seen_ll;
    TextView viewsNumber_tv;
    ImageView deleteStory_iv;

    ArrayList<String> imagesUrls;
    ArrayList<String> storiesIds;
    String userId;

    FirebaseUser firebaseUser;

    private View.OnTouchListener onTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;
                case MotionEvent.ACTION_UP:
                    long curTime = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < curTime - pressTime;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        storiesProgressView = findViewById(R.id.stories_spv);
        storyImage_iv = findViewById(R.id.storyImage_iv);
        publisherImage_iv = findViewById(R.id.publisherImage_iv);
        publisherUsername_tv = findViewById(R.id.publisherUsername_tv);
        prev_v = findViewById(R.id.prev_v);
        next_v = findViewById(R.id.next_v);
        seen_ll = findViewById(R.id.seen_ll);
        viewsNumber_tv = findViewById(R.id.viewsNumber_tv);
        deleteStory_iv = findViewById(R.id.deleteStory_iv);

        // shown in your story only
        seen_ll.setVisibility(View.GONE);
        deleteStory_iv.setVisibility(View.GONE);

        userId = getIntent().getStringExtra("userId");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        readStories();
        updatePublisherInfo();

        if (userId.equals(firebaseUser.getUid())) {   // shown in my story
            seen_ll.setVisibility(View.VISIBLE);
            deleteStory_iv.setVisibility(View.VISIBLE);
        }

        // get previous story
        prev_v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.reverse();
            }
        });
        prev_v.setOnTouchListener(onTouchListener); // on touch hold up

        // get next story
        next_v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storiesProgressView.skip();
            }
        });
        next_v.setOnTouchListener(onTouchListener); // on touch hold up

        seen_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoryActivity.this, FollowersActivity.class);
                intent.putExtra("id", userId);
                intent.putExtra("title", "views");
                intent.putExtra("storyId", storiesIds.get(counter));
                startActivity(intent);
            }
        });

        deleteStory_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteStory();
            }
        });

        publisherImage_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoryActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // to make only one Main activity
                intent.putExtra("publisherId", userId);
                startActivity(intent);
            }
        });
        publisherUsername_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoryActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // to make only one Main activity
                intent.putExtra("publisherId", userId);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onNext() {
        counter++;
        Glide.with(getApplicationContext()).load(imagesUrls.get(counter)).into(storyImage_iv);

        addView(storiesIds.get(counter));
        updateViewsNumber(storiesIds.get(counter));
    }

    @Override
    public void onPrev() {
        if (counter == 0)
            return;
        counter--;
        Glide.with(getApplicationContext()).load(imagesUrls.get(counter)).into(storyImage_iv);

        updateViewsNumber(storiesIds.get(counter));
    }

    @Override
    public void onComplete() {
        finish();
    }

    @Override
    protected void onDestroy() {
        storiesProgressView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        storiesProgressView.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        storiesProgressView.resume();
        super.onResume();
    }

    // read stories of given user
    private void readStories() {
        imagesUrls = new ArrayList<>();
        storiesIds = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                imagesUrls.clear();
                storiesIds.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Story story = snapshot.getValue(Story.class);
                    long curTime = System.currentTimeMillis();
                    if (curTime > story.getStartTime() && curTime < story.getEndTime()) {
                        imagesUrls.add(story.getImageUrl());
                        storiesIds.add(story.getStoryId());
                    }
                }

                storiesProgressView.setStoriesCount(imagesUrls.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(StoryActivity.this);
                storiesProgressView.startStories(counter);

                Glide.with(getApplicationContext()).load(imagesUrls.get(counter)).into(storyImage_iv);

                addView(storiesIds.get(counter));
                updateViewsNumber(storiesIds.get(counter));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // update username and profile image of story publisher
    private void updatePublisherInfo() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                publisherUsername_tv.setText(user.getUsername());
                Glide.with(getApplicationContext()).load(user.getImageUrl()).into(publisherImage_iv);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // add current user to story views
    private void addView(String storyId) {
        Log.v("StoryActivity", "publisher id : " + userId);
        Log.v("StoryActivity", "current user id : " + firebaseUser.getUid());
        Log.v("StoryActivity", "story id : " + storyId);

        if (!userId.equals(firebaseUser.getUid()))    // don't add publisher as a view
            FirebaseDatabase.getInstance().getReference("Story").child(userId).child(storyId)
                .child("Views").child(firebaseUser.getUid()).setValue(true);
    }

    // set story number of views
    private void updateViewsNumber(String storyId) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Story").child(userId).child(storyId)
                .child("Views");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                viewsNumber_tv.setText(""+ dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // delete story from database and image from storage
    private void deleteStory() {
        // to make sure before deletion
        final AlertDialog alertDialog = new AlertDialog.Builder(StoryActivity.this).create();
        alertDialog.setTitle("Delete Story?");
        // do nothing
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        storiesProgressView.resume();
                    }
                });
        // delete image
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        DatabaseReference reference = FirebaseDatabase.getInstance()
                                .getReference("Story").child(userId).child(storiesIds.get(counter));
                        reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(StoryActivity.this, "Story Deleted", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            }
                        });

                        // delete image from firebase storage
                        FirebaseStorage.getInstance().getReferenceFromUrl(imagesUrls.get(counter)).delete();
                    }
                });
        alertDialog.show();
        storiesProgressView.pause();
    }

}
