package com.royalbimrah.likwik;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    Uri imageUri;
    String url = "";
    StorageTask uploadTask;
    StorageReference storageReference;

    ImageView closeImageview, addedImageImageview;
    TextView postTextview;
    EditText descriptionEdittext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        postTextview = findViewById(R.id.post_textview);
        closeImageview = findViewById(R.id.close_icon);
        addedImageImageview = findViewById(R.id.added_image_imageview);
        descriptionEdittext = findViewById(R.id.description_edittext);

        storageReference = FirebaseStorage.getInstance().getReference("Posts");

        closeImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(PostActivity.this, MainActivity.class));
                finish();
            }
        });

        postTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });

        CropImage.activity().setAspectRatio(1, 1).start(PostActivity.this);



    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
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
                        url = downloadUri.toString();

                        // locate image url in posts.userId
                        DatabaseReference reference = FirebaseDatabase.getInstance()
                                .getReference("Posts")
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                        String postId = reference.push().getKey();

                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("postId", postId);
                        hashMap.put("imageUrl", url);
                        hashMap.put("description", descriptionEdittext.getText().toString());
                        hashMap.put("publisher", FirebaseAuth.getInstance().getCurrentUser().getUid());
                        // to sort posts according to time
                        hashMap.put("timeInMillis", System.currentTimeMillis()+"");

                        // generate unique id for each post in current user
                        reference.child(postId).setValue(hashMap);

                        progressDialog.dismiss();

                        startActivity(new Intent(PostActivity.this, MainActivity.class)
                                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));  // to make only one Main activity
                        finish();
                    } else {
                        Toast.makeText(PostActivity.this, "Upload Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(PostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            Toast.makeText(PostActivity.this, "No image selected!", Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    // get uri of selected image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            addedImageImageview.setImageURI(imageUri);
        } else {
            //Toast.makeText(this, "Post Failed!", Toast.LENGTH_SHORT).show();
            //startActivity(new Intent(PostActivity.this, MainActivity.class));
            finish();
        }


    }

    private String getFileExtension(Uri uri) {
        String uriString = uri.toString();
        String extension = uriString.substring(uriString.lastIndexOf(".") +1);
        return extension;
    }

}
