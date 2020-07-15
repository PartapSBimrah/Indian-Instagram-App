package com.royalbimrah.likwik.Model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.royalbimrah.likwik.Fragment.ProfileFragment;
import com.royalbimrah.likwik.MainActivity;
import com.royalbimrah.likwik.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{
    private ArrayList<User> userList;
    // required for getting image
    private Context mContext;
    private boolean isFragment;

    private FirebaseUser firebaseUser;


    public UserAdapter(Context context, ArrayList<User> userList, boolean isFragment) {
        this.mContext = context;
        this.userList = userList;
        this.isFragment = isFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final User user = userList.get(position);

        holder.followBtn.setVisibility(View.VISIBLE);
        holder.followBtn.setVisibility(View.VISIBLE);
        holder.username.setText(user.getUsername());
        holder.fullName.setText(user.getFullName());
        Glide.with(mContext).load(user.getImageUrl()).into(holder.profileImage);
        if (user.getId().equals(firebaseUser.getUid())) {
            holder.followBtn.setVisibility(View.GONE);
        }

        // setting text on follow button
        setFollowButtonText(user.getId(), holder.followBtn);

        // to open clicked user profile
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFragment) {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileId", user.getId());
                    editor.apply();

                    ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ProfileFragment(), "profileFragment").commit();
                } else {
                    // to open from followers activity
                    Intent intent = new Intent(mContext, MainActivity.class);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // to make only one Main activity
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // to make only one Main activity
                    intent.putExtra("publisherId", user.getId());
                    mContext.startActivity(intent);
                }

            }
        });

        // follow and unfollow depending on current case
        holder.followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // not friends
                if (holder.followBtn.getText().toString().equals("Follow")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(firebaseUser.getUid()).child("Following")
                            .child(user.getId()).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(user.getId()).child("Followers")
                            .child(firebaseUser.getUid()).setValue(true);

                    addNotification(user.getId());
                } else {
                    // already friends, then un-friend
                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(firebaseUser.getUid()).child("Following")
                            .child(user.getId()).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("Follow")
                            .child(user.getId()).child("Followers")
                            .child(firebaseUser.getUid()).removeValue();

                    removeNotification(user.getId());
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    // setting text on button (follow or following)
    private void setFollowButtonText(final String userId, final Button followBtn) {
        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("Following");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(userId).exists()) {
                    followBtn.setText("Following");
                    followBtn.setBackgroundResource(R.drawable.button_black);
                    followBtn.setBackgroundTintList(null);
                    followBtn.setTextColor(ContextCompat.getColorStateList(mContext, R.color.black));
                } else {
                    followBtn.setText("Follow");
                    followBtn.setBackgroundResource(R.drawable.button_background);
                    followBtn.setBackgroundTintList(ContextCompat.getColorStateList(mContext, R.color.colorPrimary));
                    followBtn.setTextColor(ContextCompat.getColorStateList(mContext, R.color.white));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNotification(String otherUser) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(otherUser);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userId", firebaseUser.getUid());
        hashMap.put("note", "started following you");
        hashMap.put("posterId", "");
        hashMap.put("postId", "");
        hashMap.put("isPost", false);

        reference.push().setValue(hashMap);
    }

    // remove follow notification when press unfollow
    private void removeNotification(String otherUser) {
        final DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Notifications").child(otherUser);
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView username, fullName;
        CircleImageView profileImage;
        Button followBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username_textview);
            fullName = itemView.findViewById(R.id.fullname_textview);
            profileImage = itemView.findViewById(R.id.profile_image);
            followBtn = itemView.findViewById(R.id.follow_btn);
        }
    }

}
