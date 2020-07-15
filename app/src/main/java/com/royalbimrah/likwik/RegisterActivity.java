package com.royalbimrah.likwik;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText usernameEditText, fullnameEditText, emailEditText, passwordEditText;
    Button registerButton;
    TextView loginTextView;

    FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameEditText = findViewById(R.id.username_edittext);
        fullnameEditText = findViewById(R.id.fullname_edittext);
        emailEditText = findViewById(R.id.email_edittext);
        passwordEditText = findViewById(R.id.password_edittext);
        registerButton = findViewById(R.id.register_button);
        loginTextView = findViewById(R.id.login_textview);

        firebaseAuth = FirebaseAuth.getInstance();

        // go to log in page
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LogInActivity.class));
            }
        });

        // creating account
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(RegisterActivity.this);
                progressDialog.setMessage("Please Wait ...");
                progressDialog.show();

                String username = usernameEditText.getText().toString();
                String fullName = fullnameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (username.isEmpty() || fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 8) {
                    Toast.makeText(RegisterActivity.this, "Password must be 8 or more characters!", Toast.LENGTH_SHORT).show();
                } else {
                    createAccount(username, fullName, email, password);
                }

            }
        });

    }

    // creating account using firebase authentication
    private void createAccount(final String username, final String fullName, String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            final String userId = firebaseUser.getUid();
                            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", userId);
                            hashMap.put("username", username.toLowerCase());
                            hashMap.put("fullName", fullName);
                            hashMap.put("bio", "");
                            hashMap.put("imageUrl", "https://firebasestorage.googleapis.com/v0/b/instagram-f3936.appspot.com/o/profileplaceholder.png?alt=media&token=ead92a2e-f6f6-45f9-9f45-96aacd925b1b");

                            databaseReference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        selfFollow(userId);
                                        progressDialog.dismiss();
                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        Log.v("RegisterActivity.java", "account created successfully");
                                    }
                                }
                            });

                        } else {    // account not created
                            progressDialog.dismiss();
                            Toast.makeText(RegisterActivity.this, "Registration Failed!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // to follow himself to see his posts in home
    private void selfFollow(String userId) {
        FirebaseDatabase.getInstance().getReference().child("Follow")
                .child(userId).child("Following")
                .child(userId).setValue(false);
    }
}
