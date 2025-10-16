package com.example.sep490_mobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class GifAdapter extends RecyclerView.Adapter<GifAdapter.GifViewHolder> {

    private Context context;
    private List<String> gifUrls;
    private OnGifSelectedListener listener;

    public interface OnGifSelectedListener {
        void onGifSelected(String gifUrl);
    }

    public GifAdapter(Context context, List<String> gifUrls, OnGifSelectedListener listener) {
        this.context = context;
        this.gifUrls = gifUrls;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GifViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_gif, parent, false);
        return new GifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GifViewHolder holder, int position) {
        String url = gifUrls.get(position);
        Glide.with(context)
                .asGif()
                .load(url)
                .into(holder.gifImageView);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onGifSelected(url);
            }
        });
    }

    @Override
    public int getItemCount() {
        return gifUrls.size();
    }

    static class GifViewHolder extends RecyclerView.ViewHolder {
        ImageView gifImageView;
        public GifViewHolder(@NonNull View itemView) {
            super(itemView);
            gifImageView = itemView.findViewById(R.id.gifImageView);
        }
    }
}