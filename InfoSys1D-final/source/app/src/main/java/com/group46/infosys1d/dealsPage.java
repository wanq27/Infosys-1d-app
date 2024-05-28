package com.group46.infosys1d;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class dealsPage extends AppCompatActivity {

    Map<String, Object> toAcceptData = new HashMap<>();
    Map<String, Object> toAcceptUserDetails = new HashMap<>();
    Map<String, Object> toAcceptItemDetails = new HashMap<>();
    Map<String, Object> upcomingData = new HashMap<>();
    Map<String, Object> upcomingUserDetails = new HashMap<>();
    Map<String, Object> upcomingItemDetails = new HashMap<>();
    int progress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_deals_page);

        // Hide AppBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize Views
        TextView upcomingText = findViewById(R.id.upcomingText);
        LinearLayout toAcceptLayout = findViewById(R.id.toAcceptLinearLayout);
        LinearLayout upcomingLayout = findViewById(R.id.upcomingLayout);
        View parentLayout = findViewById(android.R.id.content);

        // Start Progress Timer
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (progress == 0) {
                getToAccept(db, parentLayout, user.getUid());
            } else if (progress == 1) {
                getUpcoming(db, parentLayout, user.getUid());
            } else if (progress == 2) {
                getUserDetails(db, parentLayout);
            } else if (progress == 3) {
                getItemDetails(db, parentLayout);
            } else if (progress == 4) {
                setViews(upcomingText, toAcceptLayout, upcomingLayout);
            } else {
                // STOP SCHEDULER
                // UPDATE UI
                runOnUiThread(() -> {
                    scheduler.shutdown();
                });
            }
        }, 0, 200, TimeUnit.MILLISECONDS);
    }

    void getToAccept(FirebaseFirestore db, View parentLayout, String uid) {
        db.collection("deals")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String tempID = document.getId();
                            Map<String, Object> tempData = document.getData();
                            if (tempData.get("ownerID").equals(uid)) {
                                if (tempData.get("progress").equals("0") && tempData.containsKey("requests")) {
                                    toAcceptData.put(tempID, tempData);
                                }
                            }
                        }

                        progress = 1;
                    } else {
                        Snackbar.make(parentLayout, "Couldn't connect. Connection issues?", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    void getUpcoming(FirebaseFirestore db, View parentLayout, String uid) {
        db.collection("deals")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String tempID = document.getId();
                            Map<String, Object> tempData = document.getData();
                            if (tempData.get("ownerID").equals(uid)) {
                                if (tempData.get("progress").equals("1") && tempData.containsKey("selectedRequest")) {
                                    upcomingData.put(tempID, tempData);
                                }
                            }
                        }

                        progress = 2;
                    } else {
                        Snackbar.make(parentLayout, "Couldn't connect. Connection issues?", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    void getUserDetails(FirebaseFirestore db, View parentLayout) {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String tempID = document.getId();
                            Map<String, Object> tempData = document.getData();

                            if (!toAcceptData.isEmpty()) {
                                String[] tempRequests = toAcceptData.get("requests").toString().split(";");
                                for (int i = 0; i < tempRequests.length; i++) {
                                    if (tempID.equals(tempRequests[i].split("SPLIT")[0])) {
                                        if (!toAcceptUserDetails.containsKey(tempID)) {
                                            toAcceptUserDetails.put(tempID, tempData);
                                        }
                                    }
                                }
                            }

                            // Upcoming
                            if (!upcomingData.isEmpty()) {
                                if (tempID.equals(upcomingData.get("selectedRequest").toString().split("SPLIT")[0])) {
                                    upcomingUserDetails.put(tempID, tempData);
                                }
                            }
                        }

                        progress = 3;
                    } else {
                        Snackbar.make(parentLayout, "Couldn't connect. Connection issues?", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    void getItemDetails(FirebaseFirestore db, View parentLayout) {

        // To Accept
        for (String key : toAcceptData.keySet()) {
            db.collection("items")
                    .document(key)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                toAcceptItemDetails.put(document.getId(), document.getData());
                            } else {
                                Snackbar.make(parentLayout, "Oops! Couldn't find the item.", Snackbar.LENGTH_LONG).show();
                            }
                        } else {
                            Snackbar.make(parentLayout, "Couldn't connect. Connection issues?", Snackbar.LENGTH_LONG).show();
                        }
                    });
        }

        for (String key : upcomingData.keySet()) {
            db.collection("items")
                    .document(key)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                upcomingItemDetails.put(document.getId(), document.getData());
                            } else {
                                Snackbar.make(parentLayout, "Oops! Couldn't find the item.", Snackbar.LENGTH_LONG).show();
                            }
                        } else {
                            Snackbar.make(parentLayout, "Couldn't connect. Connection issues?", Snackbar.LENGTH_LONG).show();
                        }
                    });
        }

        progress = 4;
    }

    void setViews(TextView upcomingText, LinearLayout toAcceptLayout, LinearLayout upcomingLayout) {

        // To Accept
        for (int i = 0; i < toAcceptData.size(); i++) {
            View view = new View(this);

        }

        // Upcoming
        for (String key : upcomingData.keySet()) {
            Object value = upcomingData.get(key);

            // Initialize Views
            View view = new View(this);
            TextView itemName = new TextView(this);
            TextView dealDetails = new TextView(this);
            ImageView itemImage = new ImageView(this);
            LinearLayout containerLayout = new LinearLayout(this);
            RelativeLayout containerRelativeLayout = new RelativeLayout(this);

            // Set Views

            // Set TextView and ImageView
            for (String itemKey : upcomingItemDetails.keySet()) {
                if (itemKey.equals(key)) {
                    itemName.setText(upcomingItemDetails.get("listingName").toString());
                }
            }

            for (String userKey: upcomingUserDetails.keySet()) {
                if (userKey.equals(upcomingData.get("ownerID").toString())) {
                    Picasso.get().load(upcomingUserDetails.get("photoURL").toString()).fit().into(itemImage);
                }
            }

            String formattedDate = getFormattedDate(upcomingData.get("selectedRequest").toString().split("SPLIT")[1]);
            String formattedTime = getFormattedTime(upcomingData.get("selectedRequest").toString().split("SPLIT")[1]);
            dealDetails.setText(upcomingData.get("selectedRequest").toString().split("SPLIT")[2] + "\n" + formattedDate + " " + formattedTime);

            // Add itemImage, itemName, dealDetails to containerLayout
            containerLayout.addView(itemImage);
            containerLayout.addView(itemName);
            containerLayout.addView(dealDetails);

            // Add view and containerLayout to containerRelativeLayout
            containerRelativeLayout.addView(view);
            containerRelativeLayout.addView(containerLayout);
        }

        progress = 5;
    }

    String getFormattedDate(String timestamp) {
        Instant instant = Instant.ofEpochMilli(Long.parseLong(timestamp));
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault());
        return dateFormat.format(dateTime);
    }

    String getFormattedTime(String timestamp) {
        Instant instant = Instant.ofEpochMilli(Long.parseLong(timestamp));
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault());
        return timeFormat.format(dateTime);
    }
}