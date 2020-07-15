package com.royalbimrah.likwik.Model;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.royalbimrah.likwik.CommentActivity;
import com.royalbimrah.likwik.FollowersActivity;
import com.royalbimrah.likwik.Fragment.PostDetailsFragment;
import com.royalbimrah.likwik.Fragment.ProfileFragment;
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
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private ArrayList<Post> postsList;
    // required for getting image
    private Context mContext;

    private FirebaseUser firebaseUser;

    public PostAdapter(Context context, ArrayList<Post> posts) {
        this.mContext = context;
        this.postsList = posts;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Post post = postsList.get(position);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // update post specs & show placeholder till load complete
        Glide.with(mContext).load(post.getImageUrl())
                .apply(new RequestOptions().placeholder(R.drawable.placeholder))
                .into(holder.postImage_iv);
        if (post.getDescription().equals("")) {
            holder.description_tv.setVisibility(View.GONE);
        } else {
            holder.description_tv.setText(post.getDescription());
        }
        updatePublisherInfo(post.getPublisher(), holder.username_tv,
                holder.publisher_tv, holder.profile_iv);

        // update below image items (likes comments buttons...)
        update_LikeButton_NoLikes(post.getPostId(), holder.like_iv, holder.likes_tv);
        updateNumberOfComments(post.getPublisher(), post.getPostId(), holder.comments_tv);
        updateSaveButton(post.getPostId(), post.getPublisher(), holder.save_iv);

        // like & unlike post
        holder.like_iv.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                likePost(holder, post, false);
            }
        });
        // single click open post
        // double click to like & unlike post
        holder.postImage_iv.setOnClickListener(new DoubleClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onDoubleClick() {
                likePost(holder, post, true);
            }

            @Override
            public void onSingleClick() {
                openPost(post);
            }
        });

        // go to comments through comment button or view comments text
        holder.comment_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra("postId", post.getPostId());
                intent.putExtra("publisherId", post.getPublisher());
                mContext.startActivity(intent);

            }
        });
        holder.comments_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentActivity.class);
                intent.putExtra("postId", post.getPostId());
                intent.putExtra("publisherId", post.getPublisher());
                mContext.startActivity(intent);

            }
        });

        // save and un-save posts
        holder.save_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePost(holder, post);
            }
        });

        // open profile by image or username
        holder.profile_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openProfile(post);
            }
        });
        holder.username_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openProfile(post);
            }
        });
        holder.publisher_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openProfile(post);
            }
        });

        // used before double click like
//        // open post when click on image
//        holder.postImage_iv.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
//                editor.putString("postId", post.getPostId());
//                editor.putString("publisherId", post.getPublisher());
//                editor.apply();
//
//                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.fragment_container, new PostDetailsFragment()).commit();
//            }
//        });

        // open list of like users
        holder.likes_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, FollowersActivity.class);
                intent.putExtra("id", post.getPostId());
                intent.putExtra("title", "likes");
                mContext.startActivity(intent);
            }
        });

        // edit post
        holder.more_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.edit_menu:
                                editPost(post.getPostId(), post.getPublisher());
                                return true;
                            case R.id.delete_menu:
                                deletePost(post.getPostId(), post.getPublisher(), post.getImageUrl());
                                return true;
                            case R.id.report_menu:
                                reportPost(post.getPostId(), post.getPublisher());
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popupMenu.inflate(R.menu.post_menu);
                // hide edit and delete if not my post
                if (!post.getPublisher().equals(firebaseUser.getUid())) {
                    popupMenu.getMenu().findItem(R.id.edit_menu).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.delete_menu).setVisible(false);
                } else {
                    popupMenu.getMenu().findItem(R.id.report_menu).setVisible(false);
                }
                popupMenu.show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profile_iv, postImage_iv, like_iv, comment_iv, save_iv, more_iv;
        TextView username_tv, likes_tv, publisher_tv, description_tv, comments_tv;
        // like animation
        ImageView like_av;
        AnimatedVectorDrawableCompat avdCompat;
        AnimatedVectorDrawable avd;
        Drawable drawable;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profile_iv = itemView.findViewById(R.id.profile_image);
            postImage_iv = itemView.findViewById(R.id.post_image);
            like_iv = itemView.findViewById(R.id.like_imageview);
            comment_iv = itemView.findViewById(R.id.comment_imageview);
            save_iv = itemView.findViewById(R.id.save_imageview);

            username_tv = itemView.findViewById(R.id.username_textview);
            likes_tv = itemView.findViewById(R.id.likes_textview);
            publisher_tv = itemView.findViewById(R.id.publisher_textview);
            description_tv = itemView.findViewById(R.id.description_textview);
            comments_tv = itemView.findViewById(R.id.viewComments_textview);

            more_iv = itemView.findViewById(R.id.more_icon);

            like_av = itemView.findViewById(R.id.like_av);
            drawable = like_av.getDrawable();

        }
    }

    // open post if homepage (not already opened)
    private void openPost(Post post) {
        Fragment postDetails = ((FragmentActivity)mContext).getSupportFragmentManager().findFragmentByTag("PostDetailsFragment");
        Log.v("PostAdapter", "PostDetailsFragment status : "+postDetails);

        if (postDetails == null) {  // no need to check visible or not
            // not in postDetails fragment
            SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
            editor.putString("postId", post.getPostId());
            editor.putString("publisherId", post.getPublisher());
            editor.apply();

            ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new PostDetailsFragment(), "PostDetailsFragment").commit();
        }
    }

    private void openProfile(Post post) {
        SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
        editor.putString("profileId", post.getPublisher());
        editor.apply();

        ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ProfileFragment(), "profileFragment").commit();
    }

    // on click like button
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void likePost(ViewHolder holder, Post post, boolean isDoubleClick) {
        // like post
        if (holder.like_iv.getTag().equals("like")) {
            FirebaseDatabase.getInstance().getReference()
                    .child("Likes").child(post.getPostId()).child(firebaseUser.getUid()).setValue(true);
            addNotification(post.getPublisher(), post.getPostId());
            // show like animation if double click like
            if (isDoubleClick)
                likeAnimation(holder);
        } else {
            // remove like
            FirebaseDatabase.getInstance().getReference()
                    .child("Likes").child(post.getPostId()).child(firebaseUser.getUid()).removeValue();
            removeNotification(post.getPublisher(), post.getPostId());
        }
    }

    private void savePost(ViewHolder holder, Post post) {
        if (holder.save_iv.getTag().equals("save")) {
            // save post in database as Saves - my id - publisher id - post id
            FirebaseDatabase.getInstance().getReference("Saves")
                    .child(firebaseUser.getUid()).child(post.getPostId())
                    .child(post.getPublisher()).setValue(System.currentTimeMillis());
        } else {
            FirebaseDatabase.getInstance().getReference("Saves")
                    .child(firebaseUser.getUid()).child(post.getPostId())
                    .child(post.getPublisher()).removeValue();
        }
    }

    private void addNotification(String posterId, String postId) {
        // don't send notification if i liked my post
        if (firebaseUser.getUid().equals(posterId))
            return;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(posterId);

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("userId", firebaseUser.getUid());
        hashMap.put("note", "liked your post");
        hashMap.put("posterId", posterId);
        hashMap.put("postId", postId);
        hashMap.put("commentId", "");
        hashMap.put("isPost", true);

        reference.push().setValue(hashMap);
    }

    // remove like notification
    private void removeNotification(final String posterId, final String postId) {
        final DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Notifications").child(posterId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Notification notification = snapshot.getValue(Notification.class);
                    // notification by me & is this post & like notification type
                    if (notification.getUserId().equals(firebaseUser.getUid())
                            && notification.getIsPost()  && notification.getPostId().equals(postId)
                            && notification.getNote().equals("liked your post")) {
                        reference.child(snapshot.getKey()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void updatePublisherInfo(final String userId, final TextView username_tv,
                                     final TextView publisher_tv, final ImageView profile_iv) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (mContext != null) {
                    // needed to wait until profile image uploaded if changed (EditProfileActivity)
                    User user = dataSnapshot.getValue(User.class);
                    username_tv.setText(user.getUsername());
                    publisher_tv.setText(user.getUsername());
                    Glide.with(mContext).load(user.getImageUrl()).into(profile_iv);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // update like button whether this post liked or not && update number of likes
    private void update_LikeButton_NoLikes(String postId,
                                           final ImageView like_iv, final TextView likes_tv) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()) {
                    like_iv.setImageResource(R.drawable.ic_liked);
                    like_iv.setTag("liked");
                } else {
                    like_iv.setImageResource(R.drawable.ic_like);
                    like_iv.setTag("like");
                }
                long likesCount = dataSnapshot.getChildrenCount();
                if (likesCount == 1)
                    likes_tv.setText(likesCount +" like");
                else
                    likes_tv.setText(likesCount +" likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    // update number of comments
    private void updateNumberOfComments(String postPublisher, String postId, final TextView comments_tv) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Comments").child(postPublisher).child(postId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                long commentsCount = dataSnapshot.getChildrenCount();
                if (commentsCount == 0) {
                    comments_tv.setVisibility(View.GONE);
                } else if (commentsCount == 1) {
                    comments_tv.setVisibility(View.VISIBLE);
                    comments_tv.setText("View " + commentsCount + " comment");
                } else {
                    comments_tv.setVisibility(View.VISIBLE);
                    comments_tv.setText("View all " + commentsCount + " comments");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // update save button (image view)
    private void updateSaveButton(final String postId, final String publisherId, final ImageView save_iv) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Saves").child(firebaseUser.getUid()).child(postId).child(publisherId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // already saved
                    save_iv.setImageResource(R.drawable.ic_saved);
                    save_iv.setTag("saved");
                } else {
                    // press to save
                    save_iv.setImageResource(R.drawable.ic_save);
                    save_iv.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // edit post description
    private void editPost(final String postId, final String publisherId) {
        AlertDialog.Builder alertDialogue = new AlertDialog.Builder(mContext);
        alertDialogue.setTitle("Edit Post");

        final EditText editText = new EditText(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editText.setLayoutParams(layoutParams);
        alertDialogue.setView(editText);

        getPostDescription(postId, publisherId, editText);

        alertDialogue.setPositiveButton("Edit",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("description", editText.getText().toString());

                        FirebaseDatabase.getInstance().getReference("Posts")
                                .child(publisherId).child(postId).updateChildren(map);
                    }
                });
        alertDialogue.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        alertDialogue.show();
    }

    // can't return the description, so change the edit text within the function
    // used in editPost function
    private void getPostDescription(final String postId, String publisherId, final EditText editText) {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Posts").child(publisherId).child(postId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Post post = dataSnapshot.getValue(Post.class);
                editText.setText(post.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // delete post from database and image from storage
    private void deletePost(final String postId, final String publisherId, final String imageUrl) {
        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setTitle("Delete Post?");
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
                        FirebaseDatabase.getInstance().getReference("Posts")
                                .child(publisherId).child(postId).removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(mContext, "Post Deleted", Toast.LENGTH_SHORT).show();
                                            mContext.startActivity(new Intent(mContext, MainActivity.class));
                                            ((Activity)mContext).finish();
                                        }
                                    }
                                });

                        // delete image from firebase storage
                        FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl).delete();
                        // remove likes, comments & notifications
                        deletePostSpecs(postId, publisherId);

                        dialogInterface.dismiss();
                    }
                });
        alertDialog.show();
    }

    // remove likes, comments & notifications
    private void deletePostSpecs(final String postId, final String publisherId) {
        // delete likes & comments
        FirebaseDatabase.getInstance().getReference("Comments").child(publisherId).child(postId).removeValue();
        FirebaseDatabase.getInstance().getReference("Likes").child(postId).removeValue();

        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(publisherId);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    Notification n = snapshot.getValue(Notification.class);
                    if (n.getPostId().equals(postId)) {
                        reference.child(snapshot.getKey()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void reportPost(final String postId, final String publisherId) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Reports");

        HashMap<String, Object> map = new HashMap<>();
        map.put("postId", postId);
        map.put("publisherId", publisherId);

        String reportId = reference.push().getKey();

        reference.child(reportId).setValue(map);

        Toast.makeText(mContext, "Post Reported!", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void likeAnimation(ViewHolder holder) {
        holder.like_av.setAlpha(0.70f);

        if (holder.drawable instanceof  AnimatedVectorDrawableCompat) {
            holder.avdCompat = (AnimatedVectorDrawableCompat) holder.drawable;
            holder.avdCompat.start();
        } else if (holder.drawable instanceof  AnimatedVectorDrawable) {
            holder.avd = (AnimatedVectorDrawable) holder.drawable;
            holder.avd.start();
        }
    }

    public abstract class DoubleClickListener implements View.OnClickListener {

        private static final long DOUBLE_CLICK_TIME_DELTA = 200;//milliseconds

        long lastClickTime = 0;
        private boolean doubleClicked = false;

        @Override
        public void onClick(final View v) {
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA){
                doubleClicked = true;
                onDoubleClick();
            } else {
                doubleClicked = false;
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (!doubleClicked) {
                            onSingleClick();
                        }

                    }
                }, 180);

            }
            lastClickTime = clickTime;
        }

        public abstract void onSingleClick();
        public abstract void onDoubleClick();
    }
}
