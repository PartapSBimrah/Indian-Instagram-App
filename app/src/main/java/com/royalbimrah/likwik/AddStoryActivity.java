package com.royalbimrah.likwik;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class AddStoryActivity extends AppCompatActivity {

    private String storyUrl = "";
    private Uri imageUri;
    private StorageTask uploadTask;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);

        storageReference = FirebaseStorage.getInstance().getReference("Story");

        CropImage.activity().setAspectRatio(9, 16)
                .start(AddStoryActivity.this);

    }

    private void uploadStory() {
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
                        storyUrl = downloadUri.toString();

                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference reference = FirebaseDatabase.getInstance()
                                .getReference("Story").child(currentUserId);

                        String story = reference.push().getKey();
                        long endTime = System.currentTimeMillis() + 86400000;   // add 24 hours

                        HashMap<String, Object> map = new HashMap<>();
                        map.put("imageUrl", storyUrl);
                        map.put("userId", currentUserId);
                        map.put("storyId", story);
                        map.put("startTime", ServerValue.TIMESTAMP);
                        map.put("endTime", endTime);

                        reference.child(story).setValue(map);
                        progressDialog.dismiss();
                        finish();
                    } else {
                        Toast.makeText(AddStoryActivity.this, "Upload Failed!", Toast.LENGTH_SHORT).show();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddStoryActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    // get uri of selected image & upload it
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();

            uploadStory();

        } else {
            //Toast.makeText(this, "Post Failed!", Toast.LENGTH_SHORT).show();
            //startActivity(new Intent(AddStoryActivity.this, MainActivity.class));
            finish();
        }


    }

    private String getFileExtension(Uri uri) {
        String uriString = uri.toString();
        String extension = uriString.substring(uriString.lastIndexOf(".") +1);
        return extension;
    }

}
