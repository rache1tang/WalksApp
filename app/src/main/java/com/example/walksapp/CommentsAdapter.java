package com.example.walksapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder> {

    Context context;
    List<Comment> comments;

    public CommentsAdapter(Context context, List<Comment> comments) {
        this.context = context;
        this.comments = comments;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false); // inflate layout
        return new CommentsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvUser;
        TextView tvContent;
        ImageView ivProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvUser = itemView.findViewById(R.id.tvCommentName);
            tvContent = itemView.findViewById(R.id.tvCommentContent);
            ivProfile = itemView.findViewById(R.id.ivCommentDisplayImg);

            ivProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   // go to person's profile
                }
            });
        }

        public void bind(Comment comment) {
            String content = comment.getComment();
            String user = null;
            try {
                user = comment.getUser().fetchIfNeeded().getUsername();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            ParseFile image = comment.getUser().getParseFile("profileImage");

            tvContent.setText(content);
            tvUser.setText("@" + user);
            Glide.with(context).load(image.getUrl()).circleCrop().into(ivProfile);
        }
    }
}
