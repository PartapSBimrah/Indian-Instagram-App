package com.royalbimrah.likwik.Model;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.royalbimrah.likwik.MainActivity;
import com.royalbimrah.likwik.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>{
    private Context mContext;
    private ArrayList<Comment> commentsList;

    private String posterId;
    private String postId;

    private FirebaseUser firebaseUser;

    public CommentAdapter(Context mContext, ArrayList<Comment> commentsList, String postId, String posterId) {
        this.mContext = mContext;
        this.commentsList = commentsList;
        this.postId = postId;
        this.posterId = posterId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Comment comment = commentsList.get(position);

        holder.comment_tv.setText(comment.getComment());
        updateCommenterInfo(comment.getPublisher(), holder.username_tv, holder.profileImage_iv);


        // open commenter profile on clicking his name or profile image
        holder.username_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // to make only one Main activity
                intent.putExtra("publisherId", comment.getPublisher());
                mContext.startActivity(intent);
            }
        });
        holder.profileImage_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // to make only one Main activity
                intent.putExtra("publisherId", comment.getPublisher());
                mContext.startActivity(intent);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                deleteComment(comment);
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView username_tv, comment_tv;
        public ImageView profileImage_iv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username_tv = itemView.findViewById(R.id.username_textview);
            comment_tv = itemView.findViewById(R.id.comment_textview);
            profileImage_iv = itemView.findViewById(R.id.profile_imageview);

        }
    }

    // update username and profile image of user (user to comment on the post)
    private void updateCommenterInfo(String publisherId, final TextView username, final ImageView profileImage) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(publisherId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(mContext).load(user.getImageUrl()).into(profileImage);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // delete my comment
    private void deleteComment(final Comment comment) {
        if (comment.getPublisher().equals(firebaseUser.getUid())) { // my comment
            AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
            alertDialog.setTitle("Delete Comment?");
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Delete",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            FirebaseDatabase.getInstance().getReference("Comments")
                                    .child(posterId).child(postId).child(comment.getCommentId())
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {{
                                        Toast.makeText(mContext, "Comment Deleted", Toast.LENGTH_SHORT).show();
                                        removeNotification(comment);
                                    }}
                                }
                            });
                            dialogInterface.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }


    // remove comment notification when press unfollow
    // called in delete post if task successful
    private void removeNotification(final Comment comment) {
        final DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Notifications").child(posterId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    Log.v("CommentAdapter", "comment status : " + comment);
                    Log.v("CommentAdapter", "poster status : " + posterId);
                    Log.v("CommentAdapter", "comment ID status : " + comment.getCommentId());

                    if (notification.getUserId().equals(firebaseUser.getUid())
                        && notification.getCommentId().equals(comment.getCommentId())) {
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
