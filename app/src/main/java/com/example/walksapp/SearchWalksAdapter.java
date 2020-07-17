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

public class SearchWalksAdapter extends RecyclerView.Adapter<SearchWalksAdapter.ViewHolder> {

    public static final String TAG = "SearchWalksAdapter";

    Context context;
    List<Walk> walks;

    public SearchWalksAdapter(Context context, List<Walk> walks) {
        this.context = context;
        this.walks = walks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_walk, parent, false); // inflate layout
        return new SearchWalksAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

        ImageView ivSearchImg;
        TextView tvSearchName;
        TextView tvSearchLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSearchImg = itemView.findViewById(R.id.ivSearchImage);
            tvSearchLocation = itemView.findViewById(R.id.tvSearchLocation);
            tvSearchName = itemView.findViewById(R.id.tvSearchName);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    Walk walk = walks.get(pos);

                    Intent intent = new Intent(context, WalkDetailsActivity.class);
                    intent.putExtra(WalksAdapter.KEY_DETAILS, Parcels.wrap(walk));
                    context.startActivity(intent);

                }
            });

        }

        public void bind(Walk walk) {
            Log.i(TAG, "binding walk");
            tvSearchName.setText(walk.getName());
            tvSearchLocation.setText(walk.getLocation());

            Glide.with(context).load(walk.getImage().getUrl()).into(ivSearchImg);
        }
    }
}
