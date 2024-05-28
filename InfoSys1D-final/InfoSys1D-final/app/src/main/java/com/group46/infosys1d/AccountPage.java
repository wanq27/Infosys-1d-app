package com.group46.infosys1d;

import static androidx.fragment.app.FragmentManager.TAG;
import static java.util.Collections.sort;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

public class AccountPage extends AppCompatActivity {
    private ImageView userImage;
    private TextView userName;
    private TextView userRatings;
    private ImageButton backbutton2;
    FloatingActionButton settingsButton;

    public class LikeCounter {

        // Function to compile total likes for each item
        public Map<String, Integer> getTotalLikesForEachItem() throws ExecutionException, InterruptedException {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference usersCollection = db.collection("users");

            // Query all users
            Task<QuerySnapshot> userQuery = usersCollection.get();
            QuerySnapshot userSnapshot = Tasks.await(userQuery);

            // Map to hold item IDs and their total likes count
            Map<String, Integer> totalLikesMap = new HashMap<>();

            // Iterate through user documents
            for (QueryDocumentSnapshot userDoc : userSnapshot) {
                // Get likes array from each user document
                List<String> userLikes = (List<String>) userDoc.get("likes");

                // Iterate through likes array and count likes for each item
                for (String itemId : userLikes) {
                    // Update total likes count for the item
                    totalLikesMap.put(itemId, totalLikesMap.getOrDefault(itemId, 0) + 1);
                }
            }

            return totalLikesMap;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_account_page);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        userImage = findViewById(R.id.userImage);
        userName = findViewById(R.id.userName);
        userRatings = findViewById(R.id.userRatings);
        backbutton2 = findViewById(R.id.backbutton2);
        View mainLayout = findViewById(R.id.main);
        settingsButton = findViewById(R.id.settingsButton);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        LinearLayout listingContainer = findViewById(R.id.listingContainer);
        LayoutInflater inflater = LayoutInflater.from(this);


        backbutton2.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), main.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Settings.class);
            startActivity(intent);
        });

        DocumentReference docRef = db.collection("users").document(userId);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String name = document.getString("name");
                        String rating = document.getString("rating");
                        userName.setText(name);
                        userRatings.setText(rating);
                        String photoURL=document.getString("photoURL");
                        Glide.with(AccountPage.this)
                                .load(photoURL)
                                .override(350, 350) // Specify the desired width and height
                                .centerCrop()
                                .transform(new CircleCrop())
                                .into(userImage);
                    } else {
                        Snackbar.make(mainLayout, "No Account Found!", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(mainLayout, "Retrieval Failed!", Snackbar.LENGTH_LONG).show();
                }
            }
        });


        Query query = db.collection("items").whereEqualTo("ownerID", userId);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {

                        // Your processing code for each listing document
                        String listingDescription = document.getString("listingDescription");
                        String listingName = document.getString("listingName");
                        String listingPhotoURL = document.getString("listingPhotoURL");
                        View listingView = inflater.inflate(R.layout.listing_item_layout, null);
                        ImageView listingimage=listingView.findViewById(R.id.listingImage);

                        TextView listingNameTextView = listingView.findViewById(R.id.listingName);
                        TextView listingDescriptionTextView = listingView.findViewById(R.id.listingDescription);
//                        TextView listingLikesTextView = listingView.findViewById(R.id.listingLikes);


                        String itemId = document.getId();
//                        Integer likesCount = likesMap.getOrDefault(itemId, 0);
//                        // Update the listingLikes field in the document
//                        document.getReference().update("listingLikes", likesCount)
//                                .addOnSuccessListener(aVoid -> {
//                                    // Display a Snackbar on success
//                                    Snackbar.make(mainLayout, "Listing likes updated for item: " + itemId, Snackbar.LENGTH_SHORT).show();
//                                })
//                                .addOnFailureListener(e -> {
//                                    // Handle failure if needed
//                                    Snackbar.make(mainLayout, "Listing likes update failed: " + itemId, Snackbar.LENGTH_SHORT).show();
//                                });
//
//
//                        String listingLikes = document.getString("listingLikes");
                        listingNameTextView.setText(listingName);
//                        listingNameTextView.setText(listingLikes);
                        listingDescriptionTextView.setText(listingDescription);

                        Glide.with(AccountPage.this)
                                .load(listingPhotoURL)
                                .override(350, 350) // Specify the desired width and height
                                .centerCrop()
                                .into(listingimage);

                        listingView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent;
                                intent = new Intent(getApplicationContext(), viewItem.class);
                                intent.putExtra("itemUID", itemId);
                                startActivity(intent);
                            }
                        });

                        listingContainer.addView(listingView);
                    }
                } else {
                    Snackbar.make(mainLayout, "No Listings Found!", Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}