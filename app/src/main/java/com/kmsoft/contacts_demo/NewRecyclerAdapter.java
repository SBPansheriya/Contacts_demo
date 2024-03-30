package com.kmsoft.contacts_demo;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NewRecyclerAdapter extends RecyclerView.Adapter<NewRecyclerAdapter.ViewHolder> {

    MainActivity mainActivity;
    ArrayList<Users> usersArrayList;

    public NewRecyclerAdapter(MainActivity mainActivity, ArrayList<Users> usersArrayList) {
        this.mainActivity = mainActivity;
        this.usersArrayList = usersArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_item_list_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.personName.setText(usersArrayList.get(position).getFullName());

        if (TextUtils.isEmpty(usersArrayList.get(position).image)) {
            holder.personImage.setImageResource(R.drawable.ic_launcher_foreground);
        } else {
            Picasso.get().load(usersArrayList.get(position).image).into(holder.personImage);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mainActivity, UpdateActivity.class);
            intent.putExtra("User",usersArrayList.get(position));
            mainActivity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView personName;
        ImageView personImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            personName = itemView.findViewById(R.id.personName1);
            personImage = itemView.findViewById(R.id.personImage1);
        }
    }
}
