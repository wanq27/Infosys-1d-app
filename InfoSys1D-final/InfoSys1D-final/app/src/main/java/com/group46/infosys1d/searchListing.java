package com.group46.infosys1d;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class searchListing extends AppCompatActivity {

    private EditText searchEditText;
    private Spinner categorySpinner;
    private Button searchButton;
    private RecyclerView searchResultsRecyclerView;
    private ListingAdapter searchResultsAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_listing_activity);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        searchEditText = findViewById(R.id.searchEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        searchButton = findViewById(R.id.searchButton);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);

        // Initialize RecyclerView and its adapter
        searchResultsAdapter = new ListingAdapter(this);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);

        // Set up category spinner
        setUpCategorySpinner();

        // Set up search button click listener
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });

//        // Assuming you have a FirebaseUser object representing the current user
//        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//
//        if (currentUser != null) {
//            String userId = currentUser.getUid();
//
//            // Reference to the Firestore database
//            FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//            // Create a new document in the "users" collection for the current user
//            DocumentReference userDocRef = db.collection("users").document(userId);
//
//            // Create a new document in the "search_history" subcollection
//            // This document represents a single search history entry
//            Map<String, Object> searchHistoryData = new HashMap<>();
//            searchHistoryData.put("keyword", "Your search keyword");
//            searchHistoryData.put("category", "Your search category");
//            searchHistoryData.put("timestamp", FieldValue.serverTimestamp());
//
//            // Add the search history document to the "search_history" subcollection
//            userDocRef.collection("search_history")
//                    .add(searchHistoryData)
//                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                        @Override
//                        public void onSuccess(DocumentReference documentReference) {
//                            // Search history document added successfully
//                            Log.d(TAG, "Search history added for user: " + userId);
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            // Failed to add search history document
//                            Log.e(TAG, "Failed to add search history for user: " + userId, e);
//                        }
//                    });
//        }
    }

    private void setUpCategorySpinner() {
        // Retrieve the categories array from resources
        String[] categories = getResources().getStringArray(R.array.Categories);

        // Create an ArrayAdapter using the categories array
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Set the adapter to the Spinner
        categorySpinner.setAdapter(adapter);
    }

    private void performSearch() {
        // Get search keyword and category
        String keyword = searchEditText.getText().toString();
        String category = categorySpinner.getSelectedItem().toString();

        // Construct query to search listings based on keyword and category
        Query query = db.collection("items")
                .whereEqualTo("listingCategory", category) // Adjust as per your data structure
                .orderBy("listingName"); // Adjust as per your sorting preference

        // Execute query
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<Listing> searchResults = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    // Convert Firestore document to Listing object and add to search results
                    Listing listing = document.toObject(Listing.class);
                    searchResults.add(listing);
                }
                // Update RecyclerView with search results
                searchResultsAdapter.setListings(searchResults);

                // Display toast message
                Toast.makeText(searchListing.this, "Search performed. Keyword: " + keyword + ", Category: " + category, Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle failure
                Toast.makeText(searchListing.this, "Failed to perform search", Toast.LENGTH_SHORT).show();
            }
        });
    }



}

