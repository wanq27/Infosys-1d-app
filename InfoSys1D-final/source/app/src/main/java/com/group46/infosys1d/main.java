package com.group46.infosys1d;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class main extends AppCompatActivity {

    DocumentSnapshot document;
    RecyclerView newestListingsRecyclerView;
    RecyclerView recommendationsRecyclerView;
    ListingAdapter newestListingsAdapter;
    ListingAdapter recommendationsAdapter;
    TextView seeWhatsNewText;
    TextView recommendationsText;
    TextView nameText;
    FloatingActionButton editlistingButton;
    FloatingActionButton settingsButton;
    private ProgressBar progressBar;
    private LinearLayoutManager layoutManager;

    String lastSearchedCategory;
    private int totalItemCount;
    private boolean isLastPage = false;
    private DocumentSnapshot lastVisibleItem;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide AppBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Declare Views
        nameText = findViewById(R.id.nameText);
        //editlistingButton = findViewById(R.id.editlistingButton);
        //settingsButton = findViewById(R.id.settingsButton);
        seeWhatsNewText = findViewById(R.id.seeWhatsNewText);
        recommendationsText = findViewById(R.id.recommendationsText);
        newestListingsRecyclerView = findViewById(R.id.newestListingsRecyclerView);
        recommendationsRecyclerView = findViewById(R.id.recommendationsRecyclerView);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        progressBar = findViewById(R.id.progressBar); // Initialize progressBar


        // Initialize RecyclerViews and Adapters
        newestListingsAdapter = new ListingAdapter(this);
        recommendationsAdapter = new ListingAdapter(this);
        newestListingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));


        // Declare FirebaseUser & Get Attributes
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        assert user != null;
        DocumentReference docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                document = task.getResult();
                updateUI(document, nameText);

                // Load newest listings
                loadNewestListings();
                Log.d("Firebase-Main", "Newest listings are loaded");


//                 Load recommendations based on last searched category
                loadRecommendations();

            }
        });

        // Display newest listings and recommendations
//        newestListingsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newestListingsRecyclerView.setAdapter(newestListingsAdapter);

        recommendationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recommendationsRecyclerView.setAdapter(recommendationsAdapter);

        // Implement recyclerView scrolling
        newestListingsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        loadNewestListings();
                    }
                }
            }
        });

        recommendationsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        loadRecommendations();
                    }
                }
            }
        });


        // Handle edit listing button click event
        /*
        editlistingButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), editlisting.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

         */

        // Handle settings button click event
//        settingsButton.setOnClickListener()(v -> {
//            Intent intent = new Intent(getApplicationContext(), settings.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//        });


        // Converting XML menu_resource into a programmable object
//        bottomNavigationView.inflateMenu(R.menu.menu_resource);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {

            // Handle menu items click event
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.menu_home) {
                    startActivity(new Intent(main.this, main.class));
                    return true;
                } else if (item.getItemId() == R.id.menu_marketplace) {
                    startActivity(new Intent(main.this, marketPlaceActivity.class));
                    Log.d("NavigationBar", "successfully directed to marketplace page");
                    return true;
                } else if (item.getItemId() == R.id.menu_deals) {
                    startActivity(new Intent(main.this, dealsPage.class));
                    Log.d("NavigationBar", "successfully directed to deals page");
                    return true;
                } else if (item.getItemId() == R.id.menu_account) {
                    startActivity(new Intent(main.this, AccountPage.class));
                    Log.d("NavigationBar", "successfully directed to account page");
                    return true;
                }
                return false;
            }
        });
    }


    // Function to display user name
    public void updateUI(DocumentSnapshot document, TextView nameText) {
        nameText.setText(Objects.requireNonNull(Objects.requireNonNull(document.getData()).get("name")).toString());
    }

    // Function to fetch newest listings
    private void loadNewestListings() {
        Log.d("Firebase-Main", "Load starts");

        // Initialize Cloud Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (!isLastPage) {
            isLoading = true;
            progressBar.setVisibility(View.VISIBLE);

            Query query;
            if (lastVisibleItem != null) {
                query = db.collection("items")
                        .whereEqualTo("listingActive", 1)
//                        .orderBy("createdDate", Query.Direction.DESCENDING)
                        .limit(10)
                        .startAfter(lastVisibleItem);
            } else {
                query = db.collection("items")
                        .whereEqualTo("listingActive", 1)
//                        .orderBy("createdDate", Query.Direction.DESCENDING)
                        .limit(10);
            }

            query.get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("Firebase-Main", document.getId() + " => " + document.getData());
                                    List<Listing> newListings = new ArrayList<>();
                                    Listing listing = document.toObject(Listing.class);
                                    newListings.add(listing);
                                    newestListingsAdapter.addListings(newListings);
                                    lastVisibleItem = document; // Update last visible item
                                    Log.d("Firebase-Main", "DocumentSnapshot data: " + document.getData());
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
                                Toast.makeText(main.this, "Failed to fetch listings", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void loadRecommendations() {

        // Initialize Cloud Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (!isLastPage) {
            isLoading = true;
            progressBar.setVisibility(View.VISIBLE);

            // if user has not searched before, should load listings with likes >= 5

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
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("Firebase-Main", document.getId() + " => " + document.getData());
                                    List<Listing> recommendedListings = new ArrayList<>();
                                    Listing listing = document.toObject(Listing.class);
                                    recommendedListings.add(listing);
                                    recommendationsAdapter.addListings(recommendedListings);
                                    lastVisibleItem = document; // Update last visible item
                                    Log.d("Firebase-Main", "DocumentSnapshot data: " + document.getData());
                                }
                                List<DocumentSnapshot> documents = task.getResult().getDocuments();
                                if (documents.isEmpty() || documents.size() < 10) {
                                    isLastPage = true; // Indicates no more listings to fetch
                                }
                                isLoading = false;
                                progressBar.setVisibility(View.GONE);
                                Log.d("Firebase!", "Successfully fetch recommended listings");
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Log.d("Firebase!", "Failed to fetch listings: " + task.getException());
                                Toast.makeText(main.this, "Failed to fetch listings", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .collection("searchHistory")
//                .orderBy("createdDate", Query.Direction.DESCENDING)
//                .limit(1)
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                        lastSearchedCategory = documentSnapshot.getString("category");
//                        fetchListingsByCategory(lastSearchedCategory);
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    // Handle failure
//                });
    }
}

//    private void fetchListingsByCategory(String category) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("items")
//                .whereEqualTo("listingCategory", category)
//                .limit(10) // Adjust limit as per your requirement
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    List<Listing> recommendations = new ArrayList<>();
//                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
//                        Listing listing = document.toObject(Listing.class);
//                        recommendations.add(listing);
//                    }
//                    recommendationsAdapter.addListings(recommendations);
//                })
//                .addOnFailureListener(e -> {
//                    // Handle failure
//                });
//    }
//}
