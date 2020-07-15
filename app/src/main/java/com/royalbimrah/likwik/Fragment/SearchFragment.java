package com.royalbimrah.likwik.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.royalbimrah.likwik.Model.User;
import com.royalbimrah.likwik.Model.UserAdapter;
import com.royalbimrah.likwik.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class SearchFragment extends Fragment {
    private RecyclerView usersRecycleView;
    private UserAdapter userAdapter;
    private ArrayList<User> users;
    private EditText search_et;
    private Context mContext;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        usersRecycleView = view.findViewById(R.id.search_recycleView);
        usersRecycleView.setHasFixedSize(true);
        usersRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
        search_et = view.findViewById(R.id.search_editText);

        users = new ArrayList<>();
        userAdapter = new UserAdapter(getContext(), users, true);
        usersRecycleView.setAdapter(userAdapter);


//        readAllUsers();

        // begin search on entering any input
        search_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().equals("")) {
                    users.clear();
                    userAdapter.notifyDataSetChanged();
                } else {
                    searchForUsers(charSequence.toString().trim());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    /*
        search for current input username
        adapter and user list updated here
     */
    private void searchForUsers(String username) {
        Query query = FirebaseDatabase.getInstance().getReference("Users").
                orderByChild("username").startAt(username).endAt(username + "\uf8ff");

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    users.add(user);
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readAllUsers() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // if empty search get all users
                if (search_et.getText().toString().equals("")) {
                    users.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        User user = snapshot.getValue(User.class);
                        // to prevent showing current user in search
//                    if (! user.getId().equals(firebaseUser.getUid())) {
//                        users.add(user);
//                    }
                        users.add(user);
                    }
                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
