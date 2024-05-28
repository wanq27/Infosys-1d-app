package com.group46.infosys1d;

import com.facebook.shimmer.ShimmerFrameLayout;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class viewItem extends AppCompatActivity {

    Map<String, Object> retrievedData = new HashMap<>();
    Map<String, Object> listingOwnerData = new HashMap<>();

    Map<String, Object> questionUserDetails = new HashMap<>();
    Map<String, Object> dealDetails = new HashMap<>();

    boolean liked = false;
    int dealProgress = 0;
    int progress = 0;
    String itemUID = "";
    String dateText = "";
    String currentUserPhotoURL = "";
    String currentUserLikesData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_item);

        // Hide AppBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize Views
        ImageView listingImage = findViewById(R.id.listingImage);
        TextView listingTitle = findViewById(R.id.listingTitle);
        TextView listingDate = findViewById(R.id.listingDate);
        TextView listingCategory = findViewById(R.id.listingCategory);
        TextView listingDescription = findViewById(R.id.listingDescription);
        TextView askQuestionText = findViewById(R.id.askQuestionText);
        TextView sellerNameText = findViewById(R.id.sellerNameText);
        TextView actionText = findViewById(R.id.actionText);
        ImageView ownerImage = findViewById(R.id.ownerImage);
        ImageButton likesButton = findViewById(R.id.likesButton);
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        View parentLayout = findViewById(android.R.id.content);
        View dealView = findViewById(R.id.dealView);
        ShimmerFrameLayout shimmerView = findViewById(R.id.shimmerView);
        ConstraintLayout mainDataLayout = findViewById(R.id.mainDataLayout);

        // Set Views
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this, R.style.BottomSheetDialog); // Rounded Corners
        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_layout, null);
        LinearLayout userQuestionsLayout = view.findViewById(R.id.userQuestionsLayout);
        LinearLayout otherQuestionsLayout = view.findViewById(R.id.otherQuestionsLayout);
        EditText sendQuestionEditText = view.findViewById(R.id.addQuestionEditText);
        ImageButton sendQuestionButton = view.findViewById(R.id.sendQuestionButton);
        bottomSheet.setContentView(view);

        BottomSheetDialog dealBottomSheet = new BottomSheetDialog(this, R.style.BottomSheetDialog); // Rounded Corners
        View dealBottomSheetView = LayoutInflater.from(this).inflate(R.layout.deal_bottom_sheet_layout, null);
        TextView selectedLocation = dealBottomSheetView.findViewById(R.id.locationSelectedText);
        TextView selectedDate = dealBottomSheetView.findViewById(R.id.dateSelectedText);
        TextView selectedTime = dealBottomSheetView.findViewById(R.id.timeSelectedText);
        View locationView = dealBottomSheetView.findViewById(R.id.locationView);
        View dateView = dealBottomSheetView.findViewById(R.id.dateView);
        View timeView = dealBottomSheetView.findViewById(R.id.timeView);
        View requestButton = dealBottomSheetView.findViewById(R.id.confirmView);
        dealBottomSheet.setContentView(dealBottomSheetView);

        BottomSheetDialog locationBottomSheet = new BottomSheetDialog(this, R.style.BottomSheetDialog); // Rounded Corners
        View locationBottomSheetView = LayoutInflater.from(this).inflate(R.layout.location_bottom_sheet_layout, null);
        LinearLayout radioButtonLayout = locationBottomSheetView.findViewById(R.id.radioButtonLayout);
        locationBottomSheet.setContentView(locationBottomSheetView);
        String[] meetupLocations = getResources().getStringArray(R.array.MeetupLocation);
        List<RadioButton> locationButtons = new ArrayList<>();
        for (int i = 0; i < meetupLocations.length; i++) {
            locationButtons.add(new RadioButton(this));
            locationButtons.get(i).setText(meetupLocations[i]);
            int finalI = i;
            locationButtons.get(i).setOnClickListener(v -> {
                setLocation(finalI, meetupLocations, selectedLocation, locationBottomSheet, locationButtons);
            });
            radioButtonLayout.addView(locationButtons.get(i));
        }

        shimmerView.startShimmer();
        mainDataLayout.setVisibility(View.INVISIBLE);

        // When question is added and the UI needs to be refreshed
        Bundle extras = getIntent().getExtras();
        BottomSheetDialog addedBottomSheet = new BottomSheetDialog(this, R.style.BottomSheetDialog); // Rounded Corners
        View addedView = LayoutInflater.from(this).inflate(R.layout.added_bottom_sheet_layout, null);
        addedBottomSheet.setContentView(addedView);
        addedBottomSheet.setCancelable(false);
        if (extras.getBoolean("added")) {
            addedBottomSheet.show();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                addedBottomSheet.dismiss();
            }, 4000);
        }

        // Start Progress Timer
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            if (progress == 0) {
                getItemUID();
            } else if (progress == 1) {
                getItemDetails(db, parentLayout);
            } else if (progress == 2) {
                getUserDetails(db, user.getUid());
            } else if (progress == 3) {
                getDeals(db, user.getUid(), parentLayout);
            } else if (progress == 4) {
                getDateText();
            } else if (progress == 5){
                setViews(user.getUid(), listingImage, likesButton, listingTitle, listingDate, listingCategory, listingDescription, ownerImage, sellerNameText, ratingBar, userQuestionsLayout, otherQuestionsLayout, actionText);
            } else {
                // STOP SCHEDULER
                // UPDATE UI
                runOnUiThread(() -> {
                    shimmerView.stopShimmer();
                    shimmerView.setVisibility(View.GONE);
                    mainDataLayout.setVisibility(View.VISIBLE);
                    scheduler.shutdown();

                    selectedLocation.setText("Not yet selected");
                    selectedDate.setText("Not yet selected");
                    selectedTime.setText("Not yet selected");

                    // Set Button Actions
                    askQuestionText.setOnClickListener(v -> {
                        bottomSheet.show();
                        askQuestionText.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS);
                        sendQuestionButton.setOnClickListener(v1 -> {
                            if (!sendQuestionEditText.getText().toString().isEmpty()) {
                                addQuestion(db, parentLayout, user.getUid(), sendQuestionButton, sendQuestionEditText);
                            } else {
                                sendQuestionEditText.setError("No question asked!");
                                sendQuestionButton.performHapticFeedback(HapticFeedbackConstants.REJECT);
                            }
                        });
                    });

                    likesButton.setOnClickListener(v -> {
                        if (liked) {
                            // Set like to False
                            setLikes(false, db, user.getUid(), parentLayout, likesButton);
                        } else {
                            // Set like to True
                            setLikes(true, db, user.getUid(), parentLayout, likesButton);
                        }
                    });

                    dealView.setOnClickListener(v -> {
                        dealBottomSheet.show();
                        askQuestionText.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS);
                    });

                    locationView.setOnClickListener(v -> {
                        locationBottomSheet.show();
                    });

                    dateView.setOnClickListener(v -> {
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);

                        // Create DatePickerDialog instance
                        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view1, year1, month1, dayOfMonth) -> setDate(selectedDate, year1, month1, dayOfMonth), year, month, day);

                        // Show the dialog
                        datePickerDialog.show();
                    });

                    timeView.setOnClickListener(v -> {
                        // Get current time
                        Calendar calendar = Calendar.getInstance();
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);

                        // Create TimePickerDialog instance
                        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view12, hourOfDay, minute1) -> setTime(selectedTime, hourOfDay, minute1), hour, minute, false);

                        // Show the dialog
                        timePickerDialog.show();
                    });

                    requestButton.setOnClickListener(v -> {
                        if (selectedLocation.getText().equals("Not yet selected") || selectedDate.getText().equals("Not yet selected") || selectedTime.getText().equals("Not yet selected")) {
                            Snackbar.make(parentLayout, "One or more fields not selected!", Snackbar.LENGTH_LONG).show();
                            requestButton.performHapticFeedback(HapticFeedbackConstants.TOGGLE_ON);
                        } else {
                            sendRequest(db, parentLayout, user.getUid(), selectedLocation, selectedDate, selectedTime);
                        }
                    });
                });
            }
        }, 0, 200, TimeUnit.MILLISECONDS);
    }

    void getItemUID() {
        // Retrieve item UID from previous screen
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            itemUID = extras.getString("itemUID");
        }
        progress = 1;
    }

    void getItemDetails(FirebaseFirestore db, View parentLayout) {
        db.collection("items")
                .document(itemUID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            retrievedData = document.getData();
                            progress = 2;
                        } else {
                            Snackbar.make(parentLayout, "Oops! Couldn't find the item.", Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        Snackbar.make(parentLayout, "Couldn't connect. Connection issues?", Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    void getUserDetails(FirebaseFirestore db, String uid) {
        db.collection("users")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String tempID = document.getId();
                            Map<String, Object> tempData = document.getData();

                            // Listing Owner Details
                            if (tempID.equals(retrievedData.get("ownerID").toString())) {
                                listingOwnerData = tempData;
                            }

                            // Current User Details
                            if (tempID.equals(uid)) {
                                currentUserPhotoURL = tempData.get("photoURL").toString();
                                if (tempData.containsKey("likes")) {
                                    currentUserLikesData = tempData.get("likes").toString(); // Used to add or remove a like later
                                    liked = Arrays.asList(tempData.get("likes").toString().split(";")).contains(itemUID);
                                }
                                if (tempData.containsKey("dealWith")) {
                                    if (uid.equals(retrievedData.get("dealWith").toString())) {
                                        dealProgress = Integer.parseInt(retrievedData.get("dealProgress").toString());
                                    }
                                }
                            }

                            // Questions User Details
                            if (retrievedData.containsKey("questions")) {
                                questionUserDetails.put(tempID, tempData.get("photoURL") + "SPLIT" + tempData.get("name"));
                            }
                        }
                        // Set Progress
                        progress = 3;
                    }
                });
    }

    void getDeals(FirebaseFirestore db, String uid, View parentLayout) {
        db.collection("deals")
                .document(itemUID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> tempData = document.getData();
                            if (tempData.containsKey("requests")) {
                                if (Arrays.stream(tempData.get("requests").toString().split(";"))
                                        .map(s -> s.split("SPLIT")[0])
                                        .collect(Collectors.toList()).
                                        contains(uid)) {
                                    if (tempData.get("progress").toString().equals("0")) {
                                        // Seller has not selected a request
                                        dealProgress = 1;
                                    } else {
                                        // Seller selected a request
                                        if (tempData.get("selectedRequest").toString().split("SPLIT")[0].equals(uid)) {
                                            // Seller selected current user for the request
                                            dealProgress = 2;
                                            dealDetails = tempData;
                                        } else {
                                            // Seller did not select current user for the request
                                            dealProgress = 3;
                                        }
                                    }
                                } else {
                                    dealProgress = 0;
                                }
                            } else {
                                dealProgress = 0;
                            }
                        }
                    } else {
                        Snackbar.make(parentLayout, "Couldn't connect. Connection issues?", Snackbar.LENGTH_LONG).show();
                    }
                    // Set Progress
                    progress = 4;
                });
    }

    void getDateText() {
        runOnUiThread(() -> {
            Instant now = Instant.now();
            Instant pastInstant = Instant.ofEpochMilli(Long.parseLong((retrievedData.get("createdDate").toString())));
            Duration duration = Duration.between(pastInstant, now);

            long seconds = duration.getSeconds();
            long absSeconds = Math.abs(seconds);
            long minutes = absSeconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            long months = days / 30;
            long years = months / 12;

            if (seconds <= 60) {
                dateText = "Moments Ago";
            } else if (minutes <= 60) {
                dateText = minutes + " minutes ago";
            } else if (hours <= 24) {
                dateText = hours + " hours ago";
            } else if (days <= 30) {
                dateText = days + " days ago";
            } else if (months <= 12) {
                dateText = months + " months ago";
            } else {
                dateText = years + " years ago";
            }
        });
        progress = 5;
    }

    void setViews(String uid, ImageView listingImage, ImageButton likesButton, TextView listingTitle, TextView listingDate, TextView listingCategory, TextView listingDescription, ImageView ownerImage, TextView sellerNameText, RatingBar ratingBar, LinearLayout userQuestionsLayout, LinearLayout otherQuestionsLayout, TextView actionText) {
        runOnUiThread(() -> {
            if (liked) {
                likesButton.setBackgroundResource(R.drawable.icon_heart_on);
            } else {
                likesButton.setBackgroundResource(R.drawable.icon_heart);
            }

            Picasso.get().load(retrievedData.get("listingPhotoURL").toString()).fit().into(listingImage);
            listingTitle.setText(retrievedData.get("listingName").toString());
            listingDate.setText(dateText);
            listingCategory.setText(retrievedData.get("listingCategory").toString());
            listingDescription.setText(retrievedData.get("listingDescription").toString());

            Picasso.get().load(listingOwnerData.get("photoURL").toString()).fit().into(ownerImage);
            sellerNameText.setText(listingOwnerData.get("name").toString());
            ratingBar.setRating(Float.parseFloat(listingOwnerData.get("rating").toString()));

            if (dealProgress == 0) {
                // No requests made by current user
                actionText.setText("GET IT NOW");
            } else if (dealProgress == 1) {
                // Waiting for seller to reply and accept to a request
                actionText.setText("WAITING");
            } else if (dealProgress == 2) {
                // Seller selected current user for the request
                Instant instant = Instant.ofEpochMilli(Long.parseLong(dealDetails.get("selectedRequest").toString().split("SPLIT")[1].toString()));
                LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault());
                DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault());
                String formattedDate = dateFormat.format(dateTime);
                String formattedTime = timeFormat.format(dateTime);
                actionText.setText(dealDetails.get("selectedRequest").toString().split("SPLIT")[2] + "\n" + formattedDate + " " + formattedTime);
            } else {
                // Seller did not select current user for the request
                actionText.setText("RESERVED");
            }

            if (retrievedData.containsKey("questions")) {
                String[] tempQuestions = retrievedData.get("questions").toString().split(";");

                // Generate Base Layout
                List<TextView> questionView = new ArrayList<>();
                List<TextView> answerView = new ArrayList<>();
                List<TextView> nameView = new ArrayList<>();
                List<LinearLayout> linearLayoutBox = new ArrayList<>();
                List<RelativeLayout> relativeLayoutBox = new ArrayList<>();
                List<CardView> profilePictureCardView = new ArrayList<>();
                List<ImageView> profilePictureView = new ArrayList<>();

                for (int i = 0; i < tempQuestions.length; i++) {
                    questionView.add(new TextView(this));
                    answerView.add(new TextView(this));
                    nameView.add(new TextView(this));
                    linearLayoutBox.add(new LinearLayout(this));
                    relativeLayoutBox.add(new RelativeLayout(this));
                    profilePictureCardView.add(new CardView(this));
                    profilePictureView.add(new ImageView(this));

                    // Set Views
                    questionView.get(i).setTextSize(14);
                    questionView.get(i).setPadding(60, 160, 55, 0);
                    questionView.get(i).setTextColor(Color.BLACK);
                    answerView.get(i).setTypeface(null, Typeface.BOLD);
                    answerView.get(i).setTextSize(14);
                    answerView.get(i).setPadding(60, 0, 55, 0);
                    nameView.get(i).setLayoutParams(new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT){{setMargins(160, 75, 20, bottomMargin);}});
                    nameView.get(i).setTextColor(Color.BLACK);
                    nameView.get(i).setTypeface(null, Typeface.BOLD);
                    linearLayoutBox.get(i).setOrientation(LinearLayout.VERTICAL);
                    profilePictureCardView.get(i).setRadius(30);
                    profilePictureCardView.get(i).setElevation(0);
                    profilePictureCardView.get(i).setLayoutParams(new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT){{setMargins(60, 60, 20, bottomMargin);}});
                    profilePictureView.get(i).setLayoutParams(new ViewGroup.LayoutParams(80, 80));
                }

                for (int i = 0; i < tempQuestions.length; i++) {
                    String tempQuestion = tempQuestions[i].split("SPLIT")[0];
                    String tempUserID = tempQuestions[i].split("SPLIT")[1];
                    String tempAnswer = tempQuestions[i].split("SPLIT")[2];

                    if (tempAnswer.equals("-")) {
                        // Owner Has Not Replied
                        tempAnswer = "Not yet answered";
                        answerView.get(i).setTextColor(Color.GRAY);
                    } else {
                        answerView.get(i).setTextColor(Color.BLUE);
                    }

                    // Set texts to TextView
                    questionView.get(i).setText(tempQuestion);
                    answerView.get(i).setText(tempAnswer);

                    // Add TextView to LinearLayoutBox
                    linearLayoutBox.get(i).addView(questionView.get(i));
                    linearLayoutBox.get(i).addView(answerView.get(i));

                    // Set image and text of owner who posted the question
                    Picasso.get().load(questionUserDetails.get(tempUserID).toString().split("SPLIT")[0]).fit().into(profilePictureView.get(i));
                    nameView.get(i).setText(questionUserDetails.get(tempUserID).toString().split("SPLIT")[1]);

                    // Set RelativeLayoutBox
                    profilePictureCardView.get(i).addView(profilePictureView.get(i));
                    relativeLayoutBox.get(i).addView(profilePictureCardView.get(i));
                    relativeLayoutBox.get(i).addView(nameView.get(i));
                    relativeLayoutBox.get(i).addView(linearLayoutBox.get(i));
                    userQuestionsLayout.addView(relativeLayoutBox.get(i));
                }
            }
        });
        progress = 6;
    }

    void setLikes(boolean setLikeTo, FirebaseFirestore db, String uid, View parentLayout, ImageButton likesButton) {
        // Contingency when updating fails
        String oldData = currentUserLikesData;

        if (setLikeTo) {
            // Add itemUID to user's "likes" attribute
            currentUserLikesData += itemUID + ";";
        } else {
            currentUserLikesData = currentUserLikesData.replace(itemUID + ";", "");
        }

        DocumentReference docRef = db.collection("users").document(uid);
        docRef
                .update("likes", currentUserLikesData)
                .addOnSuccessListener(aVoid -> {
                    liked = setLikeTo;
                    if (setLikeTo) {
                        // Set like to True
                        likesButton.setBackgroundResource(R.drawable.icon_heart_on);
                        likesButton.performHapticFeedback(HapticFeedbackConstants.TOGGLE_ON);
                    } else {
                        // Set like to False
                        likesButton.setBackgroundResource(R.drawable.icon_heart);
                        likesButton.performHapticFeedback(HapticFeedbackConstants.TOGGLE_OFF);
                    }
                })
                .addOnFailureListener(e -> {
                    currentUserLikesData = oldData;
                    likesButton.performHapticFeedback(HapticFeedbackConstants.REJECT);
                    Snackbar.make(parentLayout, "Oops! Please try again.", Snackbar.LENGTH_LONG).show();
                });
    }

    void addQuestion(FirebaseFirestore db, View parentLayout, String uid, ImageButton sendQuestionButton, EditText askQuestionText) {

        String currentQuestions = "";
        if (retrievedData.containsKey("questions")) {
            currentQuestions = retrievedData.get("questions").toString();
        }
        currentQuestions += askQuestionText.getText().toString() + "SPLIT" + uid + "SPLIT-;";

        DocumentReference docRef = db.collection("items").document(itemUID);
        docRef
                .update("questions", currentQuestions)
                .addOnSuccessListener(aVoid -> {
                    // Refresh screen
                    Intent intent = getIntent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra("added", true);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Snackbar.make(parentLayout, "Oops! Please try again.", Snackbar.LENGTH_LONG).show();
                    sendQuestionButton.performHapticFeedback(HapticFeedbackConstants.REJECT);
                });
    }

    void setDate(TextView dateSelectedText, int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

        // Create a SimpleDateFormat instance to format the date
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
        // Format the date
        String formattedDate = dateFormat.format(calendar.getTime());
        dateSelectedText.setText(formattedDate);
    }

    void setTime(TextView selectedTime, int hourOfDay, int minute) {
        // Create a SimpleDateFormat instance to format the time
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);

        // Format the time
        String formattedTime = timeFormat.format(calendar.getTime());
        selectedTime.setText(formattedTime);
    }

    void setLocation(int i, String[] meetupLocations, TextView selectedLocation, BottomSheetDialog bottomSheetDialog, List<RadioButton> locationButtons) {
        selectedLocation.setText(meetupLocations[i]);
        locationButtons.get(i).setChecked(false);
        bottomSheetDialog.dismiss();
    }

    void sendRequest(FirebaseFirestore db, View parentLayout, String uid, TextView selectedLocation, TextView selectedDate, TextView selectedTime) {

        long millisecondsSinceEpoch = 0;
        SimpleDateFormat combinedDateFormat = new SimpleDateFormat("d MMMM yyyy hh:mm a", Locale.getDefault());
        try {
            Date combinedDateTime = combinedDateFormat.parse(selectedDate.getText() + " " + selectedTime.getText());
            millisecondsSinceEpoch = combinedDateTime.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            System.out.println(e);
            Snackbar.make(parentLayout, "Error 500. Please contact the developer!", Snackbar.LENGTH_LONG).show();
            return;
        }

        // Read Current Requests
        long finalMillisecondsSinceEpoch = millisecondsSinceEpoch;
        db.collection("deals")
                .document(itemUID)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String tempRequestString = "";
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> tempData = document.getData();
                            if (tempData.containsKey("requests")) {
                                tempRequestString = tempData.get("requests").toString();
                            }
                        }

                        // Write to Request
                        tempRequestString += uid + "SPLIT" + Long.toString(finalMillisecondsSinceEpoch) + "SPLIT" + selectedLocation.getText() + ";";
                        DocumentReference docRef = db.collection("deals").document(itemUID);
                        docRef
                                .update("requests", tempRequestString)
                                .addOnSuccessListener(aVoid -> {
                                    // Refresh screen
                                    Intent intent = getIntent();
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    intent.putExtra("added", true);
                                    startActivity(intent);
                                })
                                .addOnFailureListener(e -> {
                                    Snackbar.make(parentLayout, "Oops! Please try again.", Snackbar.LENGTH_LONG).show();
                                });
                    } else {
                        Snackbar.make(parentLayout, "Couldn't connect. Connection issues?", Snackbar.LENGTH_LONG).show();
                    }
                });
    }
}