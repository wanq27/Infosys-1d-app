package com.group46.infosys1d;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class newUser extends AppCompatActivity {

    public void uploadImageUI(Uri uri, ImageView profilePicture, TextView imagePicker, TextView skipPicker,
                              ProgressBar progressBar, TextView selfieText) {
        // Called to update UI when user has selected a photo - commence uploading procedure
        profilePicture.setImageURI(uri);
        imagePicker.setVisibility(View.INVISIBLE);
        skipPicker.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        selfieText.setText("Uploading Image!");
    }

    public void uploadFailedUI(ImageView profilePicture, TextView imagePicker, TextView skipPicker,
                               ProgressBar progressBar, TextView selfieText, View parentLayout) {
        // Called to update UI when upload has failed (back to original state) with a SnackBar message
        profilePicture.setImageURI(null);
        imagePicker.setVisibility(View.VISIBLE);
        skipPicker.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
        selfieText.setText("Selfie Please!");
        Snackbar.make(parentLayout, "Could not upload image. Please try again!", Snackbar.LENGTH_LONG).show();
    }

    public void deletePhoto(FirebaseUser user, ImageView profilePicture, TextView imagePicker,
                            TextView skipPicker, ProgressBar progressBar, TextView selfieText, View parentLayout) {
        // Called when any of the operations after uploading of photo failed
        // Saves space in Cloud Storage as user will chose another photo - new link generated
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef  = db.collection("users").document(user.getUid());
        Map<String, Object> update = new HashMap<>();
        update.put("photoURL", FieldValue.delete());
        docRef.update(update).addOnCompleteListener(task1 -> uploadFailedUI(profilePicture, imagePicker, skipPicker,
                progressBar, selfieText, parentLayout));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);

        // Hide AppBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Declare Views
        TextView imagePicker = findViewById(R.id.imagePickText);
        TextView skipPicker = findViewById(R.id.skipText);
        TextView selfieText = findViewById(R.id.selfieText);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        ImageView profilePicture = findViewById(R.id.imageView);
        View parentLayout = findViewById(android.R.id.content);

        // Declare FirebaseUser
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Registers a photo picker activity launcher in single-select mode
        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the photo picker
                    if (uri != null) {
                        // Photo chosen by user
                        // Update UI to reflect uploading state
                        uploadImageUI(uri, profilePicture, imagePicker, skipPicker,
                                progressBar, selfieText);

                        // Prepare to upload to Firebase Cloud Storage
                        FirebaseStorage storage = FirebaseStorage.getInstance();
                        // Create a storage reference from our app
                        StorageReference storageRef = storage.getReference();
                        StorageReference imageRef = storageRef.child("userImages/" + uri.getLastPathSegment());
                        UploadTask uploadTask = imageRef.putFile(uri);
                        // Register observers to listen for when the download is done or if it fails
                        uploadTask.addOnFailureListener(exception -> {
                            // Handle unsuccessful uploads
                           uploadFailedUI(profilePicture, imagePicker, skipPicker,
                                   progressBar, selfieText, parentLayout);
                        }).addOnSuccessListener(taskSnapshot -> uploadTask.continueWithTask(task -> {
                            if (!task.isSuccessful()) {
                                throw Objects.requireNonNull(task.getException());
                            }

                            // Continue with the task to get the download URL
                            return imageRef.getDownloadUrl();
                        }).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Get download URL from uploaded image
                                Uri downloadUri = task.getResult();

                                // Store download URL in Firestore Database - User UID Document
                                // Set onboardingCompletion to '1'
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("photoURL", downloadUri);
                                assert user != null;
                                userData.put("registeredEmail", user.getEmail());
                                userData.put("onboardingCompletion", "1");
                                db.collection("users").document(user.getUid())
                                        .set(userData)
                                        .addOnSuccessListener(documentReference -> {
                                            // Successfully stored download URL
                                            // Push user to newUser2 activity
                                            Intent intent;
                                            intent = new Intent(getApplicationContext(), newUser2.class);
                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                        })
                                        .addOnFailureListener(e -> {
                                            // Unable to store download URL
                                            // Delete image from Cloud Storage & Refresh UI
                                            deletePhoto(user, profilePicture, imagePicker, skipPicker,
                                                    progressBar, selfieText, parentLayout);
                                        });
                            } else {
                                assert user != null;
                                deletePhoto(user, profilePicture, imagePicker, skipPicker,
                                        progressBar, selfieText, parentLayout);
                            }
                        }));
                    }
                });

        imagePicker.setOnClickListener(v -> {
            // Called when user requests to pick an image
            // Launch the photo picker and let the user choose only images
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        skipPicker.setOnClickListener(v -> {
            // Called when user requests to skip this step (uploading an image)
            Intent intent;
            intent = new Intent(this, newUser2.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}