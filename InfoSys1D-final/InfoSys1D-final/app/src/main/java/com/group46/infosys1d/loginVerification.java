package com.group46.infosys1d;

import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import java.util.Objects;

public class loginVerification extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_verification);

        // Hide AppBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Get value of email address from storage
        SharedPreferences storage = getSharedPreferences("storage", MODE_PRIVATE);
        String emailAddress = storage.getString("email", "");

        // Declare FirebaseAuth & Intent
        FirebaseAuth auth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        // Purpose of Objects.requireNonNull is to ensure Fail-Fast
        // Throws a null immediately instead of going through halfway before failing
        String emailLink = Objects.requireNonNull(intent.getData()).toString();

        // Confirm the link is a sign-in with email link
        if (auth.isSignInWithEmailLink(emailLink)) {
            // Client SDK will parse the code from the link
            auth.signInWithEmailLink(emailAddress, emailLink)
                    .addOnCompleteListener(task -> {
                        Intent activityIntent;
                        if (task.isSuccessful()) {
                            // Verification successful
                            AuthResult result = task.getResult();
                            if (Objects.requireNonNull(result.getAdditionalUserInfo()).isNewUser()) {
                                // User does not exist
                                // Push user to newUser activity
                                activityIntent = new Intent(this, newUser.class);
                            } else {
                                // User already exists
                                // Send user back to splash.class for onboarding verification
                                activityIntent = new Intent(this, splash.class);
                            }
                        } else {
                            // Verification failed
                            // Push user back to login activity
                            activityIntent = new Intent(this, login.class);
                            activityIntent.putExtra("temp", "failed");
                        }
                        activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(activityIntent);
                    });
        }

    }
}
