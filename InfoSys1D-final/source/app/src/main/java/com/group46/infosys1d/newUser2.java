package com.group46.infosys1d;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class newUser2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user2);

        // Hide AppBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Declare Views
        TextView callText = findViewById(R.id.callText);
        EditText nicknameText = findViewById(R.id.nicknameText);
        Button confirmButton = findViewById(R.id.confirmButton);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        View parentLayout = findViewById(android.R.id.content);

        // Declare FirebaseUser
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        confirmButton.setOnClickListener(v -> {
            if (!nicknameText.getText().toString().isEmpty()) {
                // Update UI to reflect applying changes
                callText.setText("Cool name!");
                nicknameText.setVisibility(View.INVISIBLE);
                nicknameText.setEnabled(false);
                nicknameText.setFocusable(false);
                confirmButton.setVisibility(View.INVISIBLE);
                confirmButton.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);

                // Store nickname in Firestore Database - User UID Document
                // Set onboardingCompletion to '2'
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Map<String, Object> userData = new HashMap<>();
                userData.put("name", nicknameText.getText().toString());
                userData.put("onboardingCompletion", "2");
                assert user != null;
                db.collection("users").document(user.getUid())
                        .update(userData)
                        .addOnSuccessListener(documentReference -> {
                            // Successfully stored nickname
                            // Push user to main activity
                            Intent intent;
                            intent = new Intent(getApplicationContext(), main.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            // Unable to store nickname
                            // Alert user with SnackBar and reset UI
                            callText.setText("What should we call you?");
                            nicknameText.setVisibility(View.VISIBLE);
                            nicknameText.setEnabled(true);
                            nicknameText.setFocusable(true);
                            confirmButton.setVisibility(View.VISIBLE);
                            confirmButton.setEnabled(true);
                            progressBar.setVisibility(View.INVISIBLE);
                            Snackbar.make(parentLayout, "Updating name failed. Please try again!", Snackbar.LENGTH_LONG).show();
                        });
            } else {
                nicknameText.setError("Nickname cannot be empty!");
            }
        });
    }
}