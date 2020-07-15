package com.royalbimrah.likwik;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.royalbimrah.likwik.Model.User;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    private TextView changePhoto_tv;
    private ImageView save_iv, close_iv, profileImage_iv;
    private MaterialEditText fullName_met, username_met, bio_met;

    private FirebaseUser firebaseUser;

    private Uri imageUri;
    private StorageTask uploadTask;

    private String currentImageUrl;
    final String placeholder = "https://firebasestorage.googleapis.com/v0/b/instagram-f3936" +
            ".appspot.com/o/profileplaceholder.png?alt=media&token=" +
            "ead92a2e-f6f6-45f9-9f45-96aacd925b1b";

    StorageReference storageReference;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        save_iv = findViewById(R.id.save_iv);
        changePhoto_tv = findViewById(R.id.changePhoto_tv);
        close_iv = findViewById(R.id.close_iv);
        profileImage_iv = findViewById(R.id.profile_iv);
        fullName_met = findViewById(R.id.fullName_met);
        username_met = findViewById(R.id.username_met);
        bio_met = findViewById(R.id.bio_met);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("Profiles");

        setCurrentImageUrl();

        // update current data of user profile
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                fullName_met.setText(user.getFullName());
                username_met.setText(user.getUsername());
                bio_met.setText(user.getBio());
                Glide.with(getApplicationContext()).load(user.getImageUrl()).into(profileImage_iv);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // change profile photo by click change photo text or clicking the photo
        changePhoto_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentImageUrl.equals(placeholder))
                    CropImage.activity().setAspectRatio(1, 1)
                            .setCropShape(CropImageView.CropShape.OVAL)
                            .start(EditProfileActivity.this);
                else
                    changeProfilePhoto();
            }
        });
        profileImage_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentImageUrl.equals(placeholder))
                    CropImage.activity().setAspectRatio(1, 1)
                            .setCropShape(CropImageView.CropShape.OVAL)
                            .start(EditProfileActivity.this);
                else
                    changeProfilePhoto();
            }
        });

        close_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        save_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile(fullName_met.getText().toString(),
                        username_met.getText().toString(), bio_met.getText().toString());
                if (imageUri != null)
                    uploadImage();
                finish();
            }
        });
    }

    private void updateProfile(String fullName, String username, String bio) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("fullName", fullName);
        hashMap.put("username", username);
        hashMap.put("bio", bio);

        reference.updateChildren(hashMap);
    }

    private String getFileExtension(Uri uri) {
        String uriString = uri.toString();
        String extension = uriString.substring(uriString.lastIndexOf(".") +1);
        return extension;
    }

    // get uri of selected image
    // calls upload image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            Glide.with(getApplicationContext()).load(imageUri).into(profileImage_iv);
        }
    }

    private void uploadImage() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading ...");
        progressDialog.show();

        if (imageUri != null) {
            final StorageReference fileReference = storageReference
                    .child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String url = downloadUri.toString();

                        // locate image url in Users.userId
                        DatabaseReference reference = FirebaseDatabase.getInstance()
                                .getReference("Users").child(firebaseUser.getUid());

                        // delete previous image first if not placeholder
                        deletePrevProfilePicture();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageUrl", url);

                        // update profile image url in database
                        reference.updateChildren(hashMap);

                        // no need to dismiss as the whole activity finishes (dismiss gives error)
                        //progressDialog.dismiss(); // dismissed in onDestroy

                    } else {
                        Toast.makeText(EditProfileActivity.this, "Upload Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(EditProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(EditProfileActivity.this, "No image selected!", Toast.LENGTH_SHORT).show();
        }

    }

    private void deletePrevProfilePicture() {
        if (!currentImageUrl.equals(placeholder))
            FirebaseStorage.getInstance().getReferenceFromUrl(currentImageUrl).delete();
    }

    private void changeProfilePhoto() {
        final AlertDialog alertDialog
                = new AlertDialog.Builder(EditProfileActivity.this).create();

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Remove",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deletePrevProfilePicture();
                        // locate image url in Users.userId
                        DatabaseReference reference = FirebaseDatabase.getInstance()
                                .getReference("Users").child(firebaseUser.getUid());

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("imageUrl", placeholder);

                        // update profile image url in database
                        reference.updateChildren(hashMap);

                        dialogInterface.dismiss();
                    }
                });
        // add new profile image
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "New profile photo",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CropImage.activity().setAspectRatio(1, 1)
                                .setCropShape(CropImageView.CropShape.OVAL)
                                .start(EditProfileActivity.this);

                        dialogInterface.dismiss();
                    }
                });
        alertDialog.show();
    }

    private void setCurrentImageUrl() {
        DatabaseReference reference = FirebaseDatabase.getInstance()
                .getReference("Users").child(firebaseUser.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User currentUser = dataSnapshot.getValue(User.class);
                currentImageUrl = currentUser.getImageUrl();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    // needed as dismissing it in uploadImage makes some errors
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }
}
