package com.example.fantom.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fantom.R;
import com.example.fantom.model.FavoriteMessage;

import java.util.ArrayList;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder> {

    private final List<FavoriteMessage> favoriteMessages = new ArrayList<>();
    private final OnFavoriteClickListener listener;

    public interface OnFavoriteClickListener {
        void onFavoriteClick(FavoriteMessage message);
        void onFavoriteRemove(FavoriteMessage message);
    }

    public FavoritesAdapter(OnFavoriteClickListener listener) {
        this.listener = listener;
    }

    public void setFavoriteMessages(List<FavoriteMessage> messages) {
        favoriteMessages.clear();
        if (messages != null) {
            favoriteMessages.addAll(messages);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_message, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        FavoriteMessage message = favoriteMessages.get(position);
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return favoriteMessages.size();
    }

    class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar, imgPinned;
        TextView tvSenderName, tvMessageText, tvTimestamp;

        FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            imgPinned = itemView.findViewById(R.id.imgPinned);
            tvSenderName = itemView.findViewById(R.id.tvSenderName);
            tvMessageText = itemView.findViewById(R.id.tvMessageText);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onFavoriteClick(favoriteMessages.get(position));
                }
            });
        }

        void bind(FavoriteMessage message) {
            tvSenderName.setText(message.getSenderName());
            tvMessageText.setText(message.getMessageText());
            String time = android.text.format.DateFormat.format("HH:mm", message.getTimestamp()).toString();
            tvTimestamp.setText(time);
            imgPinned.setVisibility(message.isPinned() ? View.VISIBLE : View.GONE);

            Glide.with(imgAvatar.getContext())
                    .load(message.getSenderAvatarUrl())
                    .placeholder(R.drawable.ic_default_avatar)
                    .circleCrop()
                    .into(imgAvatar);
        }
    }
}
