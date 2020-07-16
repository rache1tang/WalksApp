package com.example.walksapp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.parceler.Parcels;

import java.util.List;

public class WalksAdapter extends RecyclerView.Adapter<WalksAdapter.ViewHolder> {

    public static final String KEY_DETAILS = "walk_details";
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

    public void clear() {
        walks.clear();
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    Walk walk = walks.get(position);

                    Intent intent = new Intent(context, WalkDetailsActivity.class);
                    intent.putExtra(KEY_DETAILS, Parcels.wrap(walk));
                    context.startActivity(intent);

                }
            });
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
