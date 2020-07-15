package com.royalbimrah.likwik.Model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.royalbimrah.likwik.AddStoryActivity;
import com.royalbimrah.likwik.R;
import com.royalbimrah.likwik.StoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Story> stories;

    private FirebaseUser firebaseUser;

    public StoryAdapter(Context mContext, ArrayList<Story> stories) {
        this.mContext = mContext;
        this.stories = stories;
    }

    // to set layout for addStoryItem if position = 0
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {    // add story item
            View view = LayoutInflater.from(mContext).inflate(R.layout.add_story_item, parent, false);
            return new ViewHolder(view);
        } else {
            View view = LayoutInflater.from(mContext).inflate(R.layout.story_item, parent, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Story story = stories.get(position);
        // load story image and publisher name
        updatePublisherInfo(holder, story.getUserId(), position);

        // open stories
        if (holder.getAdapterPosition() != 0) {
            readOthersStory(holder, story.getUserId());
        }

        // open my story
        if (holder.getAdapterPosition() == 0) {
            setMyStoryStatus(holder.addStory_tv, holder.addStoryIcon_iv);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.getAdapterPosition() == 0) { // open my story or add new one
                    openMyStory();
                } else {
                    Intent intent = new Intent(mContext, StoryActivity.class);
                    intent.putExtra("userId", story.getUserId());
                    mContext.startActivity(intent);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // storyImage_iv used for story image whether story or in add new story
        ImageView storyImage_iv, seenStoryImage_iv, addStoryIcon_iv;
        TextView storyUsername_tv, addStory_tv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            storyImage_iv = itemView.findViewById(R.id.storyImage_civ);
            seenStoryImage_iv = itemView.findViewById(R.id.storyImage_seen_civ);
            addStoryIcon_iv = itemView.findViewById(R.id.addStoryIcon_civ);
            storyUsername_tv = itemView.findViewById(R.id.storyUsername_tv);
            addStory_tv = itemView.findViewById(R.id.addStory_tv);

            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {    // add story item
            return 0;
        }
        // other stories
        return 1;
    }

    /*
        load story publisher profile image (my profile image in position 0)
        and publisher username if not in add_new_story position
     */
    private void updatePublisherInfo(final ViewHolder holder, final String userId, final int position) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Users").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                Glide.with(mContext).load(user.getImageUrl())
                        .apply(new RequestOptions().placeholder(R.drawable.placeholder))
                        .into(holder.storyImage_iv);
                if (position != 0) {
                    Glide.with(mContext).load(user.getImageUrl())
                            .apply(new RequestOptions().placeholder(R.drawable.placeholder))
                            .into(holder.seenStoryImage_iv);
                    holder.storyUsername_tv.setText(user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // set add_story text (add or show) & (+ image) invisible if have active story
    private void setMyStoryStatus(final TextView addStory_tv, final ImageView addStoryIcon) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Story").child(firebaseUser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                long currentTime = System.currentTimeMillis();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Story story = snapshot.getValue(Story.class);
                    if (currentTime > story.getStartTime() && currentTime < story.getEndTime()) {
                        count++;
                    }
                }

                if (count > 0) {    // currently have active stories
                    //addStory_tv.setText("My Story");
                    addStoryIcon.setVisibility(View.GONE);
                } else {    // have no active stories
                    //addStory_tv.setText("Add story");
                    addStoryIcon.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*
        check if i have active stories => then open my story or add new one
        if have no active stories => go to addStory activity
     */
    private void openMyStory() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Story").child(firebaseUser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                long currentTime = System.currentTimeMillis();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Story story = snapshot.getValue(Story.class);
                    if (currentTime > story.getStartTime() && currentTime < story.getEndTime()) {
                        count++;
                    }
                }

                if (count > 0) {    // have active story
                    final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                    // open story
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "View Story",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(mContext, StoryActivity.class);
                                intent.putExtra("userId", firebaseUser.getUid());
                                mContext.startActivity(intent);
                                dialogInterface.dismiss();
                            }
                        });
                    // go to add story
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(mContext, AddStoryActivity.class);
                                    mContext.startActivity(intent);
                                    dialogInterface.dismiss();
                                }
                            });
                    alertDialog.show();

                } else {    // have no stories
                    Intent intent = new Intent(mContext, AddStoryActivity.class);
                    mContext.startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    /*
        count active stories that i didn't open
        if any not open yet color red
        else mark as seen
     */
    private void readOthersStory(final ViewHolder holder, String userId) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Story").child(userId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int i = 0;
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Story story = snapshot.getValue(Story.class);
                    if (!snapshot.child("Views").child(firebaseUser.getUid()).exists() &&
                            System.currentTimeMillis() < story.getEndTime()) {
                        i++;
                    }
                }

                if (i > 0) {
                    holder.storyImage_iv.setVisibility(View.VISIBLE);
                    holder.seenStoryImage_iv.setVisibility(View.GONE);
                } else {
                    holder.storyImage_iv.setVisibility(View.GONE);
                    holder.seenStoryImage_iv.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
