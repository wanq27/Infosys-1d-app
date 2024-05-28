package com.group46.infosys1d;

import static java.util.Collections.sort;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Settings extends AppCompatActivity {

    private EditText changeUsername;
    private EditText changeEmail;
    private ImageButton changeProfilePic;
    private Button viewPrivacy;
    private Button savechangesbutton;
    private Button deleteAccount;
    private Button reauthenticate;
    private Button changeEmailButton;
    private Button signout;
    private ImageButton backbutton;
    public String lastPathSegment;
    private FirebaseUser currentUser;

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return email.matches(emailPattern);
    }
    private void saveChanges(String userId) {
        String newUsername = changeUsername.getText().toString().trim();

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newUsername);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        Uri imageUri = (Uri) changeProfilePic.getTag(); // Get the image URI
        if (imageUri != null) {
            StorageReference imageRef = storageRef.child("userImages/" + lastPathSegment);
            UploadTask uploadTask = imageRef.putFile(imageUri);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    updates.put("photoURL", uri.toString());
                    userRef.update(updates).addOnSuccessListener(aVoid -> {
                        Snackbar.make(findViewById(android.R.id.content), "Changes saved successfully", Snackbar.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Snackbar.make(findViewById(android.R.id.content), "Failed to save changes", Snackbar.LENGTH_SHORT).show();
                    });
                }).addOnFailureListener(e -> {
                    Snackbar.make(findViewById(android.R.id.content), "Failed to get image URL", Snackbar.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(exception -> {
                Snackbar.make(findViewById(android.R.id.content), "Upload failed: " + exception.getMessage(), Snackbar.LENGTH_LONG).show();
            });
        } else {
            // If image is not changed, update user information without uploading image
            userRef.update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Snackbar.make(findViewById(android.R.id.content), "Changes saved successfully", Snackbar.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Snackbar.make(findViewById(android.R.id.content), "Failed to save changes", Snackbar.LENGTH_SHORT).show();
                    });
        }

        if (newUsername.isEmpty()) {
            changeUsername.setError("New username cannot be empty");
            changeUsername.requestFocus();
            return;
        }
    }

    public void uploadImage(Uri uri, ProgressBar progressBar3) {
        // Called to update UI when user has selected a photo - commence uploading procedure
        progressBar3.setVisibility(View.VISIBLE);
        Glide.with(this)
                .load(uri)
                .transform(new CircleCrop())
                .into(changeProfilePic);
        changeProfilePic.setTag(uri);
        lastPathSegment = uri.getLastPathSegment();
        progressBar3.setVisibility(View.INVISIBLE);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        changeProfilePic = findViewById(R.id.changeProfilePic);
        changeUsername = findViewById(R.id.changeUsername);
        changeEmail = findViewById(R.id.changeEmail);
        changeEmail.setVisibility(View.INVISIBLE);
        changeEmailButton=findViewById(R.id.changeEmailButton);
        viewPrivacy = findViewById(R.id.viewPrivacy);
        savechangesbutton = findViewById(R.id.savechanges);
        deleteAccount = findViewById(R.id.deleteAccount);
        reauthenticate = findViewById(R.id.reauthenticate);
        signout = findViewById(R.id.signout);
        backbutton = findViewById(R.id.backbutton);
        ProgressBar progressBar3 = findViewById(R.id.progressBar3);
        progressBar3.setVisibility(View.INVISIBLE);

        backbutton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), main.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getUid();
        DocumentReference docRef = db.collection("users").document(userId);

        ActionCodeSettings actionCodeSettings =
                ActionCodeSettings.newBuilder()
                        .setUrl("https://group46verification.page.link/GO")
                        // setHandleCodeInApp must be true
                        .setHandleCodeInApp(true)
                        .setAndroidPackageName(
                                "com.group46.infosys1d",
                                true, /* installIfNotAvailable */
                                "12"    /* minimumVersion */)
                        .build();

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String username = documentSnapshot.getString("name");
//                String email = documentSnapshot.getString("registeredEmail");
                String profilePicUrl = documentSnapshot.getString("photoURL");

                changeUsername.setText(username);
//                changeEmail.setText(email);

                Glide.with(this)
                        .load(profilePicUrl)
                        .transform(new CircleCrop())
                        .into(changeProfilePic);


            } else {
                Snackbar.make(findViewById(android.R.id.content), "User data not found", Snackbar.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Snackbar.make(findViewById(android.R.id.content), "Failed to fetch user data", Snackbar.LENGTH_SHORT).show();
            Log.e("Settings", "Error fetching user data", e);
        });

        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        uploadImage(uri, progressBar3);
                    } else {
                        Snackbar.make(findViewById(android.R.id.content), "Could not upload image. Please try again!", Snackbar.LENGTH_LONG).show();
                    }
                });

        changeProfilePic.setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });

        changeEmailButton.setVisibility(View.INVISIBLE);
//        changeEmailButton.setOnClickListener(v -> {
//            String newEmail=changeEmail.getText().toString();
//            if (isValidEmail(newEmail)) {
//                user.verifyBeforeUpdateEmail(newEmail)
//                        .addOnCompleteListener(
//                                new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if (task.isSuccessful()) {
//                                            Snackbar.make(findViewById(android.R.id.content), "Confirmation Email Sent to " + newEmail, Snackbar.LENGTH_LONG).show();
//                                            // Email sent.
//                                            // User must click the email link before the email is updated.
//                                        } else {
//                                            Snackbar.make(findViewById(android.R.id.content), "Failed to send confirmation email "+ newEmail, Snackbar.LENGTH_SHORT).show();
//                                        }
//                                    }
//                                });
//            }
//        });

        deleteAccount.setOnClickListener(v -> {
            if (user != null) {
                user.delete()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Account deletion successful
                                Snackbar.make(findViewById(android.R.id.content), "User account deleted", Snackbar.LENGTH_SHORT).show();
                                FirebaseAuth.getInstance().signOut();
                            } else {
                                // Account deletion failed
                                //set button visibility to visible
                                reauthenticate.setVisibility(View.VISIBLE);

                                Snackbar.make(findViewById(android.R.id.content), "Failed to delete account: " + task.getException().getMessage(), Snackbar.LENGTH_SHORT).show();
                            }
                        });
            } else {
                // User is not signed in
                Snackbar.make(findViewById(android.R.id.content), "User not signed in", Snackbar.LENGTH_SHORT).show();
                reauthenticate.setVisibility(View.VISIBLE);
            }
        });

        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        reauthenticate.setOnClickListener(v -> {
            auth.sendSignInLinkToEmail(Objects.requireNonNull(currentUser.getEmail()), actionCodeSettings)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Snackbar.make(findViewById(android.R.id.content), "Email sent!", Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(findViewById(android.R.id.content), "Could not send verification email. Please try again!", Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });

            Intent intent = getIntent();
            Uri data = intent.getData();
            if (data != null) {
                String emailLink = data.toString();
                if (FirebaseAuth.getInstance().isSignInWithEmailLink(emailLink)) {
                    AuthCredential credential = EmailAuthProvider.getCredentialWithLink(currentUser.getEmail(), emailLink);
                    currentUser.reauthenticate(credential)
                            .addOnSuccessListener(aVoid -> {
                                Snackbar.make(findViewById(android.R.id.content), "Authenticated.", Snackbar.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Snackbar.make(findViewById(android.R.id.content), "Failed to authenticate", Snackbar.LENGTH_SHORT).show();
                            });
                }
            }
        });

        signout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            finish();
        });

        // Implement click listener for saving changes
        savechangesbutton.setOnClickListener(v -> saveChanges(userId));

        viewPrivacy.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
            bottomSheetDialog.setContentView(R.layout.privacypolicylayout);
            bottomSheetDialog.show();
        });
    }
}