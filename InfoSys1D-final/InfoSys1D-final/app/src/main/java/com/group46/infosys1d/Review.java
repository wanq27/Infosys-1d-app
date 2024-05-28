package com.group46.infosys1d;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Review extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText editTextReview;
    private Button buttonSubmitReview;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ratingBar = findViewById(R.id.ratingBar);
        editTextReview = findViewById(R.id.editTextReview);
        buttonSubmitReview = findViewById(R.id.buttonSubmitReview);

        buttonSubmitReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitReview();
            }
        });
    }

    private void submitReview() {
        float rating = ratingBar.getRating();
        String comment = editTextReview.getText().toString();

        // Assuming 'newUser' contains the ID of the user being reviewed, which needs clarification
        String userId = "0Psdc712kXYm5YlD9ztDrukjGTy1"; // not sure what to call here

        // Obtain the current user's ID as the reviewer's ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseUser user = null;

        String reviewerId = user.getUid(); // Initialize to null to handle the case where no user is logged in

        if (currentUser != null) {
            reviewerId = currentUser.getUid(); // Get the current user's ID
        }

        // Ensure we have a valid reviewerId before proceeding
        if (reviewerId == null) {
            // Handle the case where there is no authenticated user
            Toast.makeText(this, "No authenticated user found.", Toast.LENGTH_SHORT).show();
            return;
        }


        // Construct a review object
        ReviewModel review = new ReviewModel();
        review.setUserId(userId);
        review.setReviewerId(reviewerId);
        review.setRating(rating);
        review.setComment(comment);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("reviews")
                .add(review)
                .addOnSuccessListener(documentReference -> {
                    // Handle successful review submission
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                });






    }
}