package com.group46.infosys1d;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

//public class UsersLikesAdapter extends RecyclerView.Adapter<UsersLikesAdapter.ViewHolder> {
//    private List<UsersLikes> usersLikes;
//    private OnItemClickListener listener;
//
//    // Constructor
//    public void LikedItemsAdapter(List<UsersLikes> likedItems, OnItemClickListener listener) {
//        this.usersLikes = usersLikes;
//        this.listener = listener;
//    }
//
//    // Provide a reference to the views for each data item
//    public static class ViewHolder extends RecyclerView.ViewHolder {
//        // Each data item is just a string in this case
//        public TextView textView;
//        public ViewHolder(View v) {
//            super(v);
//            textView = v.findViewById(R.id.UsersLikes); // Assuming there's a TextView with this ID in the item layout
//        }
//
//        public void bind(final UsersLikes item, final OnItemClickListener listener) {
//            textView.setText(item.getTitle());
//            itemView.setOnClickListener(v -> listener.onItemClick(item));
//        }
//    }
//
//    // Create new views (invoked by the layout manager)
//    @Override
//    public UsersLikesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        // Create a new view
//        View v = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.activity_users_likes, parent, false);
//        return new ViewHolder(v);
//    }
//
//    // Replace the contents of a view (invoked by the layout manager)
//    @Override
//    public void onBindViewHolder(ViewHolder holder, int position) {
//        holder.bind(usersLikes.get(position), listener);
//    }
//
//    // Return the size of your dataset (invoked by the layout manager)
//    @Override
//    public int getItemCount() {
//        return usersLikes.size();
//    }
//
//    // Define the listener interface
//    public interface OnItemClickListener {
//        void onItemClick(UsersLikes item);
//    }

public class UsersLikesAdapter<MainActivity> extends RecyclerView.Adapter<UsersLikesAdapter.ViewHolder> {
    private List<UsersLikes> usersLikes;
    private MainActivity activity; // Assuming MainActivity is the activity where the adapter is being used

    // Constructor
    public UsersLikesAdapter(List<UsersLikes> usersLikes, MainActivity activity) {
        this.usersLikes = usersLikes;
        this.activity = activity;
    }

    // Provide a reference to the views for each data item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Each data item is just a string in this case
        public TextView textView;
        public ViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.UsersLikes); // Assuming there's a TextView with this ID in the item layout
        }

        public void bind(final UsersLikes item, final OnItemClickListener listener) {
            textView.setText(item.getTitle());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.onItemClick(item);
                }
            });
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UsersLikesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_users_likes, parent, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(usersLikes.get(position), new OnItemClickListener() {
            @Override
            public void onItemClick(UsersLikes item) {
                item.viewLikedItemDetails(activity);
            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return usersLikes.size();
    }

    // Define the listener interface
    public interface OnItemClickListener {
        void onItemClick(UsersLikes item);
    }
}