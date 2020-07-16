package com.example.walksapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TagsAdapter extends RecyclerView.Adapter<TagsAdapter.ViewHolder> {

    Context context;
    List<String> tags;
    HashSet<String> selected;

    public TagsAdapter(Context context, List<String> tags, HashSet<String> selected) {
        this.context = context;
        this.tags = tags;
        this.selected = selected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tag, parent, false); // inflate layout
        return new TagsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String tag = tags.get(position);
        holder.bind(tag);
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvTag;
        CardView cvTagRoot;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            tvTag = itemView.findViewById(R.id.tvTagName);
            cvTagRoot = itemView.findViewById(R.id.cvTagRoot);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    String tag = tags.get(position);
                    if (selected.contains(tag)) {
                        //Toast.makeText(context, "unselect", Toast.LENGTH_SHORT).show();
                        cvTagRoot.setCardBackgroundColor(Color.parseColor("#FFD740"));
                        selected.remove(tag);
                    } else {
                        //Toast.makeText(context, "select", Toast.LENGTH_SHORT).show();
                        cvTagRoot.setCardBackgroundColor(Color.parseColor("#FFAB40"));
                        selected.add(tag);
                    }
                    notifyDataSetChanged();
                }
            });
        }

        public void bind(String tag) {
            tvTag.setText(tag);
        }
    }
}
