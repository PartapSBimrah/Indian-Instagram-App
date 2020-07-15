package com.royalbimrah.likwik.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.royalbimrah.likwik.Fragment.PostDetailsFragment;
import com.royalbimrah.likwik.Fragment.ProfileFragment;
import com.royalbimrah.likwik.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Notification> notificationsList;

    public NotificationAdapter(Context mContext, ArrayList<Notification> notificationsList) {
        this.mContext = mContext;
        this.notificationsList = notificationsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Notification notification = notificationsList.get(position);

        holder.note_tv.setText(notification.getNote());
        updateUserInfo(holder.profileImage_civ, holder.username_tv, notification.getUserId());
        // if post update post image
        if (notification.getIsPost()) {
            holder.postImage_iv.setVisibility(View.VISIBLE);
            updatePostImage(holder.postImage_iv, notification.getPosterId(), notification.getPostId());
        } else {
            holder.postImage_iv.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (notification.getIsPost()) {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("postId", notification.getPostId());
                    editor.putString("publisherId", notification.getPosterId());
                    editor.apply();

                    ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new PostDetailsFragment(), "PostDetailsFragment")
                            .commit();
                } else {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileId", notification.getUserId());
                    editor.apply();

                    ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment(), "PostDetailsFragment")
                            .commit();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notificationsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public CircleImageView profileImage_civ;
        public ImageView postImage_iv;
        public TextView username_tv, note_tv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage_civ = itemView.findViewById(R.id.profileImage_iv);
            postImage_iv = itemView.findViewById(R.id.postImage_iv);
            username_tv = itemView.findViewById(R.id.username_tv);
            note_tv = itemView.findViewById(R.id.note_tv);
        }
    }

    // update notifier info (profile & username)
    private void updateUserInfo(final ImageView profile, final TextView username, String publisherId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(publisherId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    Glide.with(mContext).load(user.getImageUrl()).into(profile);
                    username.setText(user.getUsername());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // load post photo
    private void updatePostImage(final ImageView postImage, String posterId, final String postId) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Posts").child(posterId).child(postId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    Glide.with(mContext).load(post.getImageUrl()).into(postImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
