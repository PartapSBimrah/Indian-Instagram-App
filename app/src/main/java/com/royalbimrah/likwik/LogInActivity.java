package com.royalbimrah.likwik;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LogInActivity extends AppCompatActivity {

    EditText emailEditText, passwordEditText;
    Button loginButton;
    TextView signUpTextView;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        emailEditText = findViewById(R.id.email_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        loginButton = findViewById(R.id.login_button);
        signUpTextView = findViewById(R.id.signup_textview);

        firebaseAuth = FirebaseAuth.getInstance();

        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LogInActivity.this, RegisterActivity.class));
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ProgressDialog progressDialog = new ProgressDialog(LogInActivity.this);
                progressDialog.setMessage("Please wait ...");
                progressDialog.show();

                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LogInActivity.this, "All fields are required!", Toast.LENGTH_SHORT);
                } else {    // log in
                    signIn(email, password, progressDialog);
                }

            }
        });


    }

    private void signIn(String email, String password, final ProgressDialog pd) {

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LogInActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {  // signed in
                            String userId = firebaseAuth.getCurrentUser().getUid();
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference()
                                    .child("Users").child(userId);


                            // auto forward to main page on successful login
                            databaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    pd.dismiss();
                                    Intent intent = new Intent(LogInActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    pd.dismiss();
                                }
                            });

                        } else {    // sign in failed
                            pd.dismiss();
                            Toast.makeText(LogInActivity.this, "Incorrect email or password!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



}
