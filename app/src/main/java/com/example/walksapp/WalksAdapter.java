package com.example.walksapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class WalksAdapter extends RecyclerView.Adapter<WalksAdapter.ViewHolder> {

    public static final String TAG = "WalksAdapter";

    Context context;
    List<Walk> walks;

    public WalksAdapter(Context context, List<Walk> walks) {
        this.context = context;
        this.walks = walks;
    }

    @NonNull
    @Override
    public WalksAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_walk, parent, false); // inflate layout
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WalksAdapter.ViewHolder holder, int position) {
        Walk walk = walks.get(position);
        holder.bind(walk);
    }

    @Override
    public int getItemCount() {
        return walks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivImage;
        TextView tvName;
        TextView tvLocation;
        TextView tvDescription;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvName = itemView.findViewById(R.id.tvWalkName);
            tvLocation = itemView.findViewById(R.id.tvWalkLocation);
            tvDescription = itemView.findViewById(R.id.tvWalkDescription);
        }

        public void bind(Walk walk) {
            tvName.setText(walk.getName());
            tvLocation.setText(walk.getLocation());
            tvDescription.setText(walk.getDescription());

            Log.i(TAG, walk.getTags().toString());

            Glide.with(context).load(walk.getImage().getUrl()).into(ivImage);

        }
    }
}
