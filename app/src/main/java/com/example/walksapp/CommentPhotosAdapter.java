package com.example.walksapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;

import java.util.List;

public class CommentPhotosAdapter extends RecyclerView.Adapter<CommentPhotosAdapter.ViewHolder> {

    public static final String TAG = "CommentPhotosAdapter";

    List<Bitmap> bitmaps;
    Context context;
    
    public CommentPhotosAdapter(List<Bitmap> bitmaps, Context context) {
        this.context = context;
        this.bitmaps = bitmaps;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment_photo, parent, false); // inflate layout
        return new CommentPhotosAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bitmap file = bitmaps.get(position);
        holder.bind(file);
    }

    @Override
    public int getItemCount() {
        Log.i(TAG, String.valueOf(bitmaps.size()));
        return bitmaps.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView img;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.ivPhotoCommented);
        }

        public void bind(Bitmap file) {
            Log.i(TAG, "drawing");
            img.setImageBitmap(file);
        }
    }
}
