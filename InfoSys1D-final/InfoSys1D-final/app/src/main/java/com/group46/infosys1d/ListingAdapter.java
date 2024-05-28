

package com.group46.infosys1d;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ToggleButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.squareup.picasso.Picasso;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.ArrayList;
import java.util.List;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.ListingViewHolder> {

    private static List<Listing> listings;
    private Context context;
    private static OnItemClickListener listener;

    @NonNull
    @Override
    public ListingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.listing_item, parent, false);
        return new ListingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ListingViewHolder holder, int position) {
        Listing currentListing = listings.get(position);
        holder.textViewListingName.setText(currentListing.getListingName());
        holder.textViewListingDescription.setText(getShortDescription(currentListing));

        // Set initial state of ToggleButton based on expanded state
        holder.seeMoreToggleButton.setChecked(currentListing.isExpanded());
        holder.seeMoreToggleButton.setText(currentListing.isExpanded() ? "See Less" : "See More");

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                intent = new Intent(context, viewItem.class);
                intent.putExtra("itemUID", listings.get(position).getListingID());
                context.startActivity(intent);
            }
        });

        // Set click listener for ToggleButton
        holder.seeMoreToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleDescriptionState(currentListing);
                holder.textViewListingDescription.setText(getShortDescription(currentListing));
                holder.seeMoreToggleButton.setText(currentListing.isExpanded() ? "See Less" : "See More");
            }
        });

        // Show/hide ToggleButton based on description length
        holder.seeMoreToggleButton.setVisibility(isDescriptionLong(currentListing) ? View.VISIBLE : View.GONE);


        // Fetch listing photo URL from Firestore
        fetchListingPhotoUrl(currentListing.getOwnerID(), new OnPhotoUrlFetchedListener() {
            @Override
            public void onPhotoUrlFetched(String photoUrl) {
                // Load image using Picasso
                Picasso.get().load(currentListing.getListingPhotoURL()).into(holder.imageViewListingImage);
//                Picasso.get().load(photoUrl).into(holder.imageViewListingImage);
                Log.d("Firebase-Main", "Successfully display listing photo");
            }
        });


        // Set click listener for ImageView
        holder.imageViewListingImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int position = holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(currentListing);
                    }
                }
            }
        });

        // Fetch user photo URL from Firestore
        fetchUserPhotoUrl(currentListing.getOwnerID(), new OnPhotoUrlFetchedListener() {
            @Override
            public void onPhotoUrlFetched(String photoUrl) {
                // Load image using Picasso
                Picasso.get().load(photoUrl).into(holder.userProfileButton);
            }
        });

        // Set click listener for user profile button
        holder.userProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    int position = holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onUserProfileClick(currentListing.getSellerId());
                    }
                }
            }
        });
    }

    // Method to fetch user photo URL from Firestore
    private static void fetchUserPhotoUrl(String ownerId, OnPhotoUrlFetchedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(ownerId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String photoUrl = documentSnapshot.getString("photoURL");
                            listener.onPhotoUrlFetched(photoUrl);
                            Log.d("Firebase-Main", "Successfully fetched user photourl");
                        }
                        else{
                            listener.onPhotoUrlFetched(null);
                        }
                    }

                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onPhotoUrlFetched(null);
                    }
                });

    }

    // Method to fetch listing photo URL from Firestore
    private void fetchListingPhotoUrl(String ownerId, OnPhotoUrlFetchedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("items").document(ownerId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String photoUrl = documentSnapshot.getString("listingPhotoUrl");
                            listener.onPhotoUrlFetched(photoUrl);
                            Log.d("Firebase-Main", "Successfully fetched listing photourl");
                        }
                        else{
                            listener.onPhotoUrlFetched(null);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onPhotoUrlFetched(null);
                    }
                });
    }

    // Method to get the short portion of the description
    private String getShortDescription(Listing listing) {
        if (listing.isExpanded()) {
            return listing.getListingDescription(); // Return full description if expanded
        } else {
            // Return short portion of the description (e.g., first 3 lines)
            return listing.getListingDescription().substring(0, Math.min(listing.getListingDescription().length(), 50));
        }
    }

    // Method to toggle the expanded state of the description
    private void toggleDescriptionState(Listing listing) {
        listing.setExpanded(!listing.isExpanded());
    }

    // Method to check if the description is long
    private boolean isDescriptionLong(Listing listing) {
        // Check if the description has more than 50 characters
        return listing.getListingDescription().length() > 50;
    }

    // Interface for callback when photo URL is fetched
    interface OnPhotoUrlFetchedListener {
        void onPhotoUrlFetched(String photoUrl);
    }


    public ListingAdapter(Context context) {
        this.context = context;
        this.listings = new ArrayList<>();
    }

    public void setListings(List<Listing> listings) {
        this.listings = listings;
        notifyDataSetChanged();
    }

    public void addListings(List<Listing> newlistings) {
        int startPosition = listings.size();
        listings.addAll(newlistings);
        notifyItemRangeInserted(startPosition, newlistings.size());
    }

    public void updateListings(List<Listing> newListings) {
        listings.clear();
        listings.addAll(newListings);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
//        // Implement the FilterDialogListener interface
//        void onFilterApplied(String selectedCategory);

        void onItemClick(Listing listing);
        void onUserProfileClick(String userId);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return listings.size();
    }

    static class ListingViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewListingImage;
        TextView textViewListingDescription;
        TextView textViewListingName;
        ImageButton userProfileButton;
        ToggleButton seeMoreToggleButton;

        ListingViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewListingImage = itemView.findViewById(R.id.imageViewListingImage);
            textViewListingDescription = itemView.findViewById(R.id.textViewListingDescription);
            textViewListingName = itemView.findViewById(R.id.textViewListingName);
            userProfileButton = itemView.findViewById(R.id.userProfileButton);
            seeMoreToggleButton = itemView.findViewById(R.id.seeMoreToggleButton);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(listings.get(position));
                    }
                }
            });
        }
    }
}
