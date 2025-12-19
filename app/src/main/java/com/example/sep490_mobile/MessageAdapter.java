package com.example.sep490_mobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import android.net.Uri;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.stfalcon.imageviewer.StfalconImageViewer;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    private final Context context;
    private final List<ChatMessage> chatMessages;
    private final String currentUserId;

    public MessageAdapter(Context context, List<ChatMessage> chatMessages, String currentUserId) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_me, parent, false);
            return new MessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_message_you, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage chatMessage = chatMessages.get(position);
        MessageViewHolder vh = (MessageViewHolder) holder;

        String type = chatMessage.getType() == null ? "text" : chatMessage.getType();
        String messageContent = chatMessage.getMessage();

        // Ẩn hết trước
        vh.messageText.setVisibility(View.GONE);
        vh.messageImage.setVisibility(View.GONE);
        vh.messageVideo.setVisibility(View.GONE);

        if (type.equals("image")) {
            vh.messageImage.setVisibility(View.VISIBLE);
            String url = messageContent.substring(messageContent.indexOf(']') + 1).split("\\|")[0];

            Glide.with(context)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(600, 600)
                    .into(vh.messageImage);

            // 👉 Chỉ ảnh mới phóng to khi click
            vh.messageImage.setOnClickListener(v -> {
                new StfalconImageViewer.Builder<>(context,
                        Collections.singletonList(url),
                        (imageView, imageUrl) -> Glide.with(context).load(imageUrl).into(imageView))
                        .show();
            });

        } else if (type.equals("gif")) {
            vh.messageImage.setVisibility(View.VISIBLE);
            String url = messageContent.substring(messageContent.indexOf(']') + 1).split("\\|")[0];

            Glide.with(context)
                    .asGif()
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(600, 600)
                    .into(vh.messageImage);

            // 👉 GIF KHÔNG phóng to khi click
            vh.messageImage.setOnClickListener(null);

        } else if (type.equals("video")) {
            vh.messageVideo.setVisibility(View.VISIBLE);
            String videoUrl = messageContent.substring(messageContent.indexOf(']') + 1).split("\\|")[0];

            vh.messageVideo.setVideoURI(Uri.parse(videoUrl));
            MediaController mediaController = new MediaController(context);
            mediaController.setAnchorView(vh.messageVideo);
            vh.messageVideo.setMediaController(mediaController);
            vh.messageVideo.seekTo(1); // Show preview frame

            // 👉 Phóng to video khi click (mở dialog toàn màn hình)
            vh.messageVideo.setOnClickListener(v -> {
                new VideoFullscreenDialog(context, videoUrl).show();
            });

        } else {
            vh.messageText.setVisibility(View.VISIBLE);
            vh.messageText.setText(chatMessage.getMessage());
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        vh.messageTime.setText(sdf.format(new Date(chatMessage.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageText;
        public TextView messageTime;
        public ImageView messageImage;
        public VideoView messageVideo;

        public MessageViewHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.messageText);
            messageTime = itemView.findViewById(R.id.messageTime);
            messageImage = itemView.findViewById(R.id.messageImage);
            messageVideo = itemView.findViewById(R.id.messageVideo);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).getSenderId().equals(currentUserId)) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }
}