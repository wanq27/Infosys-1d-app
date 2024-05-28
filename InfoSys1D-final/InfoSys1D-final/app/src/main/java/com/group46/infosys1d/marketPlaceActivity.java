package com.group46.infosys1d;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class marketPlaceActivity extends AppCompatActivity implements ListingAdapter.OnItemClickListener {

    // Declare variables
    private RecyclerView recyclerView;
    private ListingAdapter adapter;
    private ProgressBar progressBar;
//    private FloatingActionButton filterButton;
    FloatingActionButton editlistingButton;
    FloatingActionButton settingsButton;
    private SearchView searchView;
    private boolean isLastPage = false;
    private DocumentSnapshot lastVisibleItem;
    private List<Listing> allListings;
    private List<Listing> searchedListings;
    private String selectedCategory;
    List<String> suggestedKeywords;

    private boolean isLoading = false;
    private LinearLayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Hide AppBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.market_place_activity);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
//        filterButton = findViewById(R.id.filterButton);
        editlistingButton = findViewById(R.id.editlistingButton);
        //settingsButton = findViewById(R.id.settingsButton);
        searchView = findViewById(R.id.searchView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);


        // Instantiate
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new ListingAdapter(this);
        recyclerView.setAdapter(adapter);

        allListings = new ArrayList<>();
        searchedListings = new ArrayList<>();
        suggestedKeywords = new ArrayList<>();
//        BottomSheetDialog bottomSheet = new BottomSheetDialog(this, R.style.BottomSheetDialog); // Rounded Corners
//        View view = LayoutInflater.from(this).inflate(R.layout.filter_dialog_layout, null);
//        bottomSheet.setContentView(view);



//        // Autotext complete suggestions
//        ArrayAdapter<String> SuggestAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,suggestedKeywords);
//        AutoCompleteTextView textView=(AutoCompleteTextView)findViewById(R.id.autoCompleteTextView);
//        textView.setHint("Enter Item Name Here");//display hint
//        textView.setThreshold(3);
//        textView.setAdapter(SuggestAdapter);

        // Call fetch listings method
        fetchListingsFromFirestore();

        // Create a subcollection for search history
        createSearchHistoryCollection();


        // Query text event for searchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
//                String selectedCategory = categoryList.getSelectedItem().toString(); // Get selected category
                addSearchHistory(query);
                Log.d("Firebase", "Successfully added to search history");
                performSearch(query); // Update performSearch method to include category
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

//
//        // Set up listener for text changes in AutoCompleteTextView
//        textView.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                // Update suggestions based on current input
//                updateSuggestions(s.toString());
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {}
//        });

        // Set up listener for recyclerview scroll
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Check if reached the end of recyclerview
                if (!isLoading && !isLastPage) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        // Reached end, fetch next batch of listings
                        fetchListingsFromFirestore();
                    }
                }
            }
        });

        // Handle edit listing button click event

        editlistingButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), editlisting.class);
            startActivity(intent);
        });


        // On click event for filterButton
//        filterButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showFilterDialog();
////                bottomSheet.show();
//                Log.d("Filter Search", "Successfully shown filter dialog");
//            }
//        });

//         Handle settings button click event
        /*
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Settings.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

         */


        // Converting XML menu_resource into a programmable object
//        bottomNavigationView.inflateMenu(R.menu.menu_resource);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {

            // Handle menu items click event
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menu_home) {
                    startActivity(new Intent(marketPlaceActivity.this, main.class));
                    return true;
                } else if (item.getItemId() == R.id.menu_marketplace) {
                    startActivity(new Intent(marketPlaceActivity.this, marketPlaceActivity.class));
                    Log.d("NavigationBar", "successfully directed to marketplace page");
                    return true;
                } else if (item.getItemId() == R.id.menu_deals) {
                    startActivity(new Intent(marketPlaceActivity.this, dealsPage.class));
                    Log.d("NavigationBar", "successfully directed to deals page");
                    return true;
                } else if (item.getItemId() == R.id.menu_account) {
                    startActivity(new Intent(marketPlaceActivity.this, AccountPage.class));
                    Log.d("NavigationBar", "successfully directed to account page");
                    return true;
                }
                return false;
            }
        });
    }

    // Method to fetch listings from Firestore and add to adapter
    private void fetchListingsFromFirestore() {
        if (!isLastPage) {
            isLoading = true;
            progressBar.setVisibility(View.VISIBLE);

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            Query query;
            if (lastVisibleItem != null) {
                query = db.collection("items")
                        .whereEqualTo("listingActive", 1)
                        .limit(10)
                        .startAfter(lastVisibleItem);
            } else {
                query = db.collection("items")
                        .whereEqualTo("listingActive", 1)
                        .limit(10);
            }

            query.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    List<Listing> fetchedListings = new ArrayList<>();
                                    Listing listing = document.toObject(Listing.class);
                                    fetchedListings.add(listing);
                                    allListings.addAll(fetchedListings);
                                    adapter.addListings(fetchedListings);
                                    lastVisibleItem = document; // Update last visible item
                                }
                                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                                if (documents.isEmpty() || documents.size() < 10) {
                                    isLastPage = true; // Indicates no more listings to fetch
                                }
                                isLoading = false;
                                progressBar.setVisibility(View.GONE);
                                Log.d("Firebase!", "Successfully fetch ");
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Log.d("Firebase!", "Failed to fetch listings: " + task.getException());
                                Toast.makeText(marketPlaceActivity.this, "Failed to fetch listings", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    // Method to create search history subcollection for the current user if it doesn't exist
    // Or is it just add manually in firebase lol
    private void createSearchHistoryCollection() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        db.collection("users")
                .document(userId)
                .collection("searchHistory")
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult().isEmpty()) {
                            // Search history subcollection doesn't exist, create it
                            Map<String, Object> initialData = new HashMap<>();
                            initialData.put("created", FieldValue.serverTimestamp());
                            db.collection("users")
                                    .document(userId)
                                    .collection("searchHistory")
                                    .document("initial")
                                    .set(initialData)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d("Firestore", "Search history subcollection created successfully");
                                            } else {
                                                Log.e("Firestore", "Error creating search history subcollection: ", task.getException());
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    // Method to add search history
    private void addSearchHistory(String keyword) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        Map<String, Object> searchHistory = new HashMap<>();
        searchHistory.put("keyword", keyword);
        searchHistory.put("timestamp", FieldValue.serverTimestamp());

        db.collection("users")
                .document(userId)
                .collection("searchHistory")
                .add(searchHistory)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Log.d("Firestore", "Search history added successfully");
                        } else {
                            Log.e("Firestore", "Failed to add search history: ", task.getException());
                        }
                    }
                });
    }

//    // Method to perform search
//    private void performSearch(String keyword) {
//        searchedListings.clear();
//        for (Listing listing : allListings) {
//            if (listing.getListingName().toLowerCase().contains(keyword.toLowerCase())) {
//                searchedListings.add(listing);
//            }
//        }
//        adapter.updateListings(searchedListings);
//        Log.d("Firebase", "Listings searched");
//        }

    // Method to perform search
    private void performSearch(String keyword) {
        searchedListings.clear();
        for (Listing listing : allListings) {
            // Check if listing name contains the keyword
            if (listing.getListingName().toLowerCase().contains(keyword.toLowerCase())) {
                // If a category is selected, filter based on category
                if (selectedCategory != null && !selectedCategory.isEmpty()) {
                    if (listing.getCategory().equalsIgnoreCase(selectedCategory)) {
                        searchedListings.add(listing);
                    }
                } else {
                    // If no category is selected, add all listings that match the keyword
                    searchedListings.add(listing);
                }
            }
        }
        adapter.updateListings(searchedListings);
        Log.d("Firebase", "Listings searched");
    }

//
//    // Method to show the filter dialog
//    private void showFilterDialog() {
//        FilterDialogFragment filterDialog = FilterDialogFragment.newInstance();
//        filterDialog.show(getSupportFragmentManager(), "FilterDialogFragment");
//    }
//
//    // Implement the FilterDialogListener interface
//    @Override
//    public void onFilterApplied(String selectedCategory) {
//        // Update the selected category
//        this.selectedCategory = selectedCategory;
//
//        // Perform search with the updated filter options
//        String keyword = searchView.getQuery().toString();
//        performSearch(keyword);
//    }


    // Method to update suggestions based on user input
    private void updateSuggestions(String input) {
        // Clear previous suggestions
        suggestedKeywords.clear();

        // Query Firestore to fetch keywords
        FirebaseFirestore.getInstance().collection("keywords")
                .whereGreaterThanOrEqualTo("keyword", input)
                .whereLessThan("keyword", input + "\uf8ff")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // Iterate through the documents and add keywords to the list
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String keyword = document.getString("keyword");
                                suggestedKeywords.add(keyword);
                            }
                            // Notify the adapter that data set has changed
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.d("Firebase-Main", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }


    // On click event for listing image
    @Override
    public void onItemClick(Listing listing) {
//        Intent intent = new Intent(this, ListingDetailsActivity.class);
//        intent.putExtra("listing_id", listing.getId());
//        startActivity(intent);
    }

    // On click event for user profile

    @Override
    public void onUserProfileClick(String userId) {
        // You can handle the user profile click event here, for example:
        // Start a new activity to display user profile details
//        Intent userProfileIntent = new Intent(this, UserProfileActivity.class);
//        userProfileIntent.putExtra("user_id", userId);
//        startActivity(userProfileIntent);
    }
}

