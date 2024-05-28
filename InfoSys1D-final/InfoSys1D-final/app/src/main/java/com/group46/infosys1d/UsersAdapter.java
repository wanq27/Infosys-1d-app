package com.group46.infosys1d;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.MyViewHolder> {
    private Context context;
    private List<UserModel> userModelList;

    public UsersAdapter(Context context) {
        this.context = context;
        this.userModelList = new ArrayList<>();
    }
    public void add(UserModel userModel){
        userModelList.add(userModel);

    }
    public void clear(){
        userModelList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UsersAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_chatlist,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.MyViewHolder holder, int position) {
        UserModel userModel = userModelList.get(position);
        holder.name.setText(userModel.getUserName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                Intent intent = new Intent(context, Chat.class); // to link to the chat class
                intent.putExtra("id", userModel.getUserID());
                //see if wanna add email
                context.startActivity(intent);
*/
            }
        });

    }

    @Override
    public int getItemCount() {
        return userModelList.size();
    }
    public List<UserModel> getUserModelList(){
        return userModelList;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView name, email;
//        public MessageAdapter.MyViewHolder(@NonNull View itemView){
//            super(itemView);
//            name = itemView.findViewById(R.id.username);
//
//        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.username);
        }
    }
}