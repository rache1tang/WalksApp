package com.example.walksapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.SaveCallback;

import org.parceler.Parcels;

import java.util.HashSet;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class WalksAdapter extends RecyclerView.Adapter<WalksAdapter.ViewHolder> {

    public static final String KEY_DETAILS = "walk_details";
    public static final String TAG = "WalksAdapter";
    public static final int WALK_DETAILS_CODE = 37;

    Context context;
    List<Walk> walks;
    HashSet<String> likedWalks;

    public static int position;

    public WalksAdapter(Context context, List<Walk> walks, HashSet<String> likedWalks) {
        this.context = context;
        this.walks = walks;
        this.likedWalks = likedWalks;
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
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivImage;
        TextView tvName;
        TextView tvLocation;
        TextView tvDescription;
        ImageView ivHeart;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvName = itemView.findViewById(R.id.tvWalkName);
            tvLocation = itemView.findViewById(R.id.tvWalkLocation);
            tvDescription = itemView.findViewById(R.id.tvWalkDescription);
            ivHeart = itemView.findViewById(R.id.ivWalkHeart);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    position = getAdapterPosition();
                    Walk walk = walks.get(position);

                    Intent intent = new Intent(context, WalkDetailsActivity.class);
                    intent.putExtra(KEY_DETAILS, Parcels.wrap(walk));
                    ((Activity) context).startActivityForResult(intent, WALK_DETAILS_CODE);

                }
            });
        }

        public void bind(Walk walk) {
            tvName.setText(walk.getName());
            tvLocation.setText(walk.getLocation());
            tvDescription.setText(walk.getDescription());

            if (!likedWalks.contains(walk.getObjectId())) {
                ivHeart.setVisibility(View.GONE);
            }

            Glide.with(context).load(walk.getImage().getUrl()).into(ivImage);

        }
    }
}
