package com.group46.infosys1d;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Objects;

public class splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Hide AppBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Declare FirebaseUser & Firestore
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Check for FirebaseUser after delayMillis
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (user != null) {
                // User is signed in
                // Check if user has completed onboarding
                // 'empty' dictates that the user has not completed any phase of onboarding - send to newUser.class
                // '1' dictates that the user has only completed the first phase of onboarding - send to newUser2.class
                // '2' dictates that the user has completed the two phases of onboarding - send to main.class
                DocumentReference docRef = db.collection("users").document(user.getUid());
                docRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int completionCheck;
                        Intent intentOnboarding;
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            completionCheck = Integer.parseInt((String) Objects.requireNonNull(document.getData()).get("onboardingCompletion"));
                            if (completionCheck == 1) {
                                // User has only completed first phase of onboarding
                                intentOnboarding = new Intent(this, newUser2.class);
                            } else if (completionCheck == 2) {
                                // User has completed onboarding
                                intentOnboarding = new Intent(this, main.class);
                            } else {
                                // User has not completed onboarding
                                intentOnboarding = new Intent(this, newUser.class);
                            }
                        } else {
                            // User has not completed onboarding
                            intentOnboarding = new Intent(this, newUser.class);
                        }

                        intentOnboarding.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intentOnboarding);
                    }
                });
            } else {
                // No user signed in
                // Show login screen
                Intent intent;
                intent = new Intent(this, login.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }, 3000);
    }
}