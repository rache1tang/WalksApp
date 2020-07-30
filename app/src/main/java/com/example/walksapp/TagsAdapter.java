package com.example.walksapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
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
    PopupWindow popup;
    public static HashSet<String> newTags;

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

                    if (tag.equals("+")) {
                        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        final View popupView = inflater.inflate(R.layout.popup_add_tag, null);
                        popup = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                        final EditText etNewTag = popupView.findViewById(R.id.etNewTag);

                        Button btnCancel = popupView.findViewById(R.id.btnAddCancel);
                        btnCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                popup.dismiss();
                            }
                        });

                        Button btnAdd = popupView.findViewById(R.id.btnAddTag);
                        btnAdd.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String newTag = etNewTag.getText().toString();
                                tags.add(tags.size() - 1, newTag);
                                selected.add(newTag);
                                newTags.add(newTag);
                                notifyDataSetChanged();
                                popup.dismiss();
                            }
                        });
                        popup.setFocusable(true);
                        popup.update();
                        popup.setAnimationStyle(R.style.Animation);
                        popup.showAtLocation(itemView, Gravity.CENTER, 0, 0);
                        return;
                    }

                    if (selected.contains(tag)) {
                        selected.remove(tag);
                    } else {
                        selected.add(tag);
                    }
                    notifyDataSetChanged();
                }
            });
        }

        public void bind(String tag) {
            tvTag.setText(tag);
            if (selected.contains(tag))
                cvTagRoot.setCardBackgroundColor(Color.parseColor("#FFAB40"));
            else cvTagRoot.setCardBackgroundColor(Color.parseColor("#FFD740"));
        }
    }
}
