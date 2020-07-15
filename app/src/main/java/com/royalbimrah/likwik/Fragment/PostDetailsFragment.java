package com.royalbimrah.likwik.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.royalbimrah.likwik.Model.Post;
import com.royalbimrah.likwik.Model.PostAdapter;
import com.royalbimrah.likwik.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/*
    reads single post same way as in home page (but with single post)
 */
public class PostDetailsFragment extends Fragment {

    private String postId;
    private String publisherId;

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private ArrayList<Post> postsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_details, container, false);

        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);
        postId = prefs.getString("postId", "none");
        publisherId = prefs.getString("publisherId", "none");

        recyclerView = view.findViewById(R.id.details_rv);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        postsList = new ArrayList<>();
        adapter = new PostAdapter(getContext(), postsList);
        recyclerView.setAdapter(adapter);

        readPost();

        // Inflate the layout for this fragment
        return view;
    }

    private void readPost() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Posts").child(publisherId).child(postId);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    postsList.clear();
                    Post post = dataSnapshot.getValue(Post.class);
                    postsList.add(post);
                    adapter.notifyDataSetChanged();
                }
                else {
                    Log.v("Post details fragment", "error with : " + postId + " publisher : " + publisherId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
