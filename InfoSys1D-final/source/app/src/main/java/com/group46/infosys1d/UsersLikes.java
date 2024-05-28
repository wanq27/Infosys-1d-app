package com.group46.infosys1d;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import com.group46.infosys1d.UserModel;


public class UsersLikes extends AppCompatActivity {
    private RecyclerView recyclerView;
    private UsersLikesAdapter adapter;
    private List<UsersLikes> likedItems = new ArrayList<>(); // This will be populated with actual data from Firestore

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_likes);
        recyclerView = findViewById(R.id.likes_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Populate the likedItems list with items from Firestore
        fetchLikedItemsFromFirestore();

        /*
        adapter = new UsersLikesAdapter(likedItems, item -> {
            // Handle the click event for the item
            viewLikedItemDetails(item);
        });
        */
        recyclerView.setAdapter(adapter);
    }

    void viewLikedItemDetails(Object item) {
        if (item instanceof UserModel) {
            UserModel userModel = (UserModel) item;
            String itemId = userModel.getItemID();
            String itemName = userModel.getItemName();

            // You can display a toast with the item details
            Toast.makeText(this, "Clicked on item: " + itemName, Toast.LENGTH_SHORT).show();

            // Alternatively, you can navigate to a new activity to show more details about the clicked item
            Intent intent = new Intent(this, marketPlaceActivity.class); // link to marketplace acitivity class
            intent.putExtra("itemId", itemId);
            startActivity(intent);
        } else {
            // Handle the case where the item is not of type UserModel
            Toast.makeText(this, "Invalid item type", Toast.LENGTH_SHORT).show();
        }
    }


    private void fetchLikedItemsFromFirestore() {
        // Code to fetch liked items from Firestore and update the likedItems list
        FirebaseFirestore.getInstance();
        // Assuming 'likedItems' is a list of UserModel objects
        adapter = new UsersLikesAdapter(likedItems, new UsersLikesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(UsersLikes item) {
                viewLikedItemDetails(item);

            }

        });
        adapter.notifyDataSetChanged();
    }

    // Method to handle the click event for an item
//    private void viewLikedItemDetails(Object item) {
    // Method to handle the click event for an item


    // Retrieve details about the clicked item and perform actions accordingly
//        UsersLikesModel usersLikesModel = (UsersLikesModel) item;
//        String itemId = UsersLikesModel.getItemTitle();
//        String itemName = UsersLikesModel.getItemDescription();
//        // You can display a toast with the item details
//        Toast.makeText(this, "Clicked on item: " + itemName, Toast.LENGTH_SHORT).show();
//
//        // Alternatively, you can navigate to a new activity to show more details about the clicked item
//        Intent intent = new Intent(this, marketPlaceActivity.class);
//        intent.putExtra("itemId", itemId);
//        startActivity(intent);

    // Or perform any other actions based on the clicked item
}








