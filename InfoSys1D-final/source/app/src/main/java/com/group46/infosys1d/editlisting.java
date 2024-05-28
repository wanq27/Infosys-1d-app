package com.group46.infosys1d;

import static java.util.Collections.sort;

import android.content.Context;
import android.content.Intent;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

public class editlisting extends AppCompatActivity {
    private EditText listingname;
    private EditText description;
    private ImageView listingimage;
    private Button active;
    private ImageButton upload;
    public String lastPathSegment;
    public ArrayList<String> listingnamehints = new ArrayList<>();
    private ImageButton back;

    public void uploadImage(Uri uri, ProgressBar progressBar1) {
        // Called to update UI when user has selected a photo - commence uploading procedure
        progressBar1.setVisibility(View.VISIBLE);
        listingimage.setImageURI(uri);
        listingimage.setTag(uri);
        lastPathSegment = uri.getLastPathSegment();
        progressBar1.setVisibility(View.INVISIBLE);
    }

    public void imageuploadfailed(View mainLayout) {
        // Called to update UI when upload has failed (back to original state) with a SnackBar message
        Snackbar.make(mainLayout, "Could not upload image. Please try again!", Snackbar.LENGTH_LONG).show();
    }

    public void analyseImage(Context context, Uri uri) {
        InputImage image;
        try {
            image = InputImage.fromFilePath(context, uri);
            ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
            labeler.process(image)
                    .addOnSuccessListener(labels -> {
                        // Task completed successfully
                        class Sortbyconfidence implements Comparator<ImageLabel>{
                            public int compare(ImageLabel o1, ImageLabel o2) {
                                return Float.compare(o1.getConfidence(),o2.getConfidence());
                            }
                        }
                        Collections.sort(labels, new Sortbyconfidence());
                        int i=0;
                        for (ImageLabel label : labels) {
                            String text = label.getText();
                            float confidence = label.getConfidence();
                            int index = label.getIndex();
                            listingnamehints.add(text);
                            Log.d("LabelInfo", label.getText());
                            Log.d("listingnamehints", listingnamehints.toString());
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Task failed with an exception
                        Log.e("MLKit_Labels", "Labeling failed", e);

                    });
        }
        catch (IOException e) {
            e.printStackTrace();}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.editlisting);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialisation
        description = findViewById(R.id.description);
        listingimage = findViewById(R.id.listingimage); // Corrected to ImageView
        active = findViewById(R.id.active);
        upload = findViewById(R.id.uploadimg);
        ProgressBar progressBar1 = findViewById(R.id.progressBar1);
        ProgressBar progressBar2 = findViewById(R.id.progressBar2);
        View mainLayout = findViewById(R.id.main);
        Spinner listingcategory = findViewById(R.id.listingcategory);
        MultiAutoCompleteTextView meetuplocation = findViewById(R.id.meetuplocation);
        back=findViewById(R.id.back);

        back.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), main.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        uploadImage(uri, progressBar1);
                        analyseImage(this,uri);
                    } else {
                        imageuploadfailed(mainLayout);
                    }
                });

        upload.setOnClickListener(v -> {
            // Called when user requests to pick an image
            // Launch the photo picker and let the user choose only images
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });



        //listing category
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        listingcategory.setAdapter(adapter);

        //meetup location
        meetuplocation.setThreshold(0);
        String[] MeetupLocationList = getResources().getStringArray(R.array.MeetupLocation);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, MeetupLocationList);
        meetuplocation.setAdapter(adapter1);
        // initiate a MultiAutoCompleteTextView
        meetuplocation.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        //listing name
        AutoCompleteTextView listingname = findViewById(R.id.listingname);
        runOnUiThread(() -> {
            ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, listingnamehints);
            listingname.setAdapter(adapter2);

            // This handler post is redundant if you're already on the main UI thread, which runOnUiThread ensures.
            // If you need to delay showing the dropdown or ensure it runs after the current UI thread operations complete,
            // you might keep it. Otherwise, it's safe to call listingname.showDropDown() directly here.
            new Handler(Looper.getMainLooper()).post(() -> {
                if (listingname.getAdapter().getCount() > 0) {
                    listingname.showDropDown();
                }
            });
        });



        //make listing active
        active.setOnClickListener(v -> {
            String listname = listingname.getText().toString();
            String desc = description.getText().toString();
            String category = listingcategory.getSelectedItem().toString();
            String location = meetuplocation.getText().toString();
            if (listname.isEmpty()) {
                listingname.setError("Listing name is required");
                listingname.requestFocus();
                return;
            }

            if (desc.isEmpty()) {
                description.setError("Description is required");
                description.requestFocus();
                return;
            }

            if (location.isEmpty()) {
                meetuplocation.setError("Meetup location is required");
                meetuplocation.requestFocus();
                return;
            }

            if (lastPathSegment == null || lastPathSegment.isEmpty()) {
                Snackbar.make(mainLayout, "No Image Uploaded!", Snackbar.LENGTH_LONG).show();
                return;
            }

            progressBar2.setVisibility(View.VISIBLE);
            // Declare FirebaseUser
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            // Prepare to upload to Firebase Cloud Storage
            FirebaseStorage storage = FirebaseStorage.getInstance();
            // Create a storage reference from our app
            StorageReference storageRef = storage.getReference();
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            String filename = "listing_" + userId + "_" + System.currentTimeMillis() + ".jpg";


            StorageReference imageRef = storageRef.child("userlistingImages/" + filename);
            Uri imageUri = (Uri) listingimage.getTag();
            UploadTask uploadTask = imageRef.putFile(imageUri);
            long timestamp = System.currentTimeMillis();


            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("items").whereEqualTo("ownerID", userId).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        int listingCount = queryDocumentSnapshots.size() + 1;


                        // Register observers to listen for when the download is done or if it fails
                        uploadTask.addOnFailureListener(exception -> {
                            // Handle unsuccessful uploads
                            Snackbar.make(mainLayout, "Upload failed: " + uploadTask.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                        }).addOnSuccessListener(taskSnapshot -> uploadTask.continueWithTask(task -> {
                            if (!task.isSuccessful()) {
                                throw Objects.requireNonNull(task.getException());
                            }
                            return imageRef.getDownloadUrl();
                        }).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Uri downloadUri = task.getResult();
                                Map<String, Object> userItems = new HashMap<>();
                                userItems.put("listingName", listname);
                                userItems.put("listingPhotoURL", downloadUri);
                                assert user != null;
                                userItems.put("listingDescription", desc);
                                userItems.put("listingCategory", category);
                                userItems.put("listingMeetupLocation", location);
                                userItems.put("listingLikes", 0);
                                userItems.put("listingActive", 1);
                                userItems.put("ownerID", user.getUid());
                                userItems.put("createdDate", timestamp);
                                DocumentReference document = db.collection("items").document();
                                userItems.put("listingID", document.getId());
                                db.collection("items").document(document.getId())
                                        .set(userItems);

                                Map<String, Object> tempData = new HashMap<>();
                                tempData.put("ownerID", user.getUid());
                                tempData.put("progress", 0);
                                tempData.put("requests", "");
                                db.collection("deals").document(document.getId()).set(tempData);
                            } else {
                                assert user != null;
                                Snackbar.make(mainLayout, "Upload failed: " + task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                            }
                        }));
                        progressBar2.setVisibility(View.INVISIBLE);
                    });
        });
    }
}