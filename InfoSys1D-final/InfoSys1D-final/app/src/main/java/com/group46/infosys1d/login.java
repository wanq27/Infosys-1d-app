package com.group46.infosys1d;

import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Hide AppBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Declare Views
        EditText emailAddress = findViewById(R.id.emailAddressEditText);
        Button loginButton = findViewById(R.id.loginButton);
        ProgressBar progressBar = findViewById(R.id.progressBar);
        View parentLayout = findViewById(android.R.id.content);

        // Show SnackBar if verification failed
        // Will be called when coming back from loginVerification class
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Show toast message
            Snackbar.make(parentLayout, "Verification failed. Please try again!", Snackbar.LENGTH_LONG).show();
        }

        // Declare SharedPreferences Storage
        SharedPreferences storage = getSharedPreferences("storage", MODE_PRIVATE);

        loginButton.setOnClickListener(view -> {
            // Check if email address entered contains @SUTD domain
            // If @SUTD domain is found, ensure that text before the domain is not empty
            if (emailAddress.getText().toString().contains("@gmail.com") &&
                !emailAddress.getText().toString().replaceAll("@gmail.com", "").isEmpty()) {
                // Disable EditText
                emailAddress.setFocusable(false);
                emailAddress.setEnabled(false);
                // Hide Button & Show ProgressBar
                loginButton.setVisibility(View.INVISIBLE);
                loginButton.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);

                // Continue with login process
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
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.sendSignInLinkToEmail(emailAddress.getText().toString(), actionCodeSettings)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Successfully sent login email
                                // Store email address in storage for future verification
                                SharedPreferences.Editor editStorage = storage.edit();
                                editStorage.putString("email", emailAddress.getText().toString());
                                editStorage.apply();
                                Snackbar.make(parentLayout, "Verification email sent!", Snackbar.LENGTH_LONG).show();
                            } else {
                                Snackbar.make(parentLayout, "Could not send verification email. Please try again!", Snackbar.LENGTH_LONG).show();
                            }
                            emailAddress.setFocusable(true);
                            emailAddress.setEnabled(true);
                            loginButton.setVisibility(View.VISIBLE);
                            loginButton.setEnabled(true);
                            progressBar.setVisibility(View.INVISIBLE);
                        });
            } else {
                // Display error
                emailAddress.setError("Invalid email address!");
            }
        });
    }
}