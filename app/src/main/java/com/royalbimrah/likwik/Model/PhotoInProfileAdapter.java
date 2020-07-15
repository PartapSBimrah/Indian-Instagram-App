package com.royalbimrah.likwik.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.royalbimrah.likwik.Fragment.PostDetailsFragment;
import com.royalbimrah.likwik.R;

import java.util.ArrayList;

public class PhotoInProfileAdapter extends RecyclerView.Adapter<PhotoInProfileAdapter.ViewHolder> {

    private Context mContext;
    private ArrayList<Post> postsList;

    public PhotoInProfileAdapter(Context mContext, ArrayList<Post> postsList) {
        this.mContext = mContext;
        this.postsList = postsList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.photo_in_profile_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Post post = postsList.get(position);

        Glide.with(mContext).load(post.getImageUrl()).into(holder.photo);

        // open post when click on image
        holder.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openPost(post);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView photo;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            photo = itemView.findViewById(R.id.photoInProfile_imageview);
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
}
