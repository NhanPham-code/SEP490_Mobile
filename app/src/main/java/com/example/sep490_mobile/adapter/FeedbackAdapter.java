package com.example.sep490_mobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.model.Feedback;
import com.stfalcon.imageviewer.StfalconImageViewer; // ĐÃ SỬA LẠI IMPORT

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private final List<Feedback> feedbackList = new ArrayList<>();
    private final int currentUserId;

    public FeedbackAdapter(int currentUserId) {
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        Feedback feedback = feedbackList.get(position);
        holder.bind(feedback);
    }

    @Override
    public int getItemCount() {
        return feedbackList.size();
    }

    public void submitList(List<Feedback> newFeedbackList) {
        this.feedbackList.clear();
        if (newFeedbackList != null) {
            this.feedbackList.addAll(newFeedbackList);
        }
        notifyDataSetChanged();
    }

    /**
     * Lớp ViewHolder: Giữ các tham chiếu đến các View bên trong một item.
     */
    static class FeedbackViewHolder extends RecyclerView.ViewHolder {

        // === THÊM BASE_URL VÀO ĐÂY ===
        // Địa chỉ IP này dùng cho máy ảo Android để kết nối đến server trên máy tính.
        private static final String BASE_URL = "https://10.0.2.2:7221";

        private final TextView textViewUserId, textViewComment, textViewDate;
        private final RatingBar ratingBarDisplay;
        private final ImageView imageViewFeedback;
        private final Context context;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            textViewUserId = itemView.findViewById(R.id.text_view_user_id);
            textViewComment = itemView.findViewById(R.id.text_view_comment);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            ratingBarDisplay = itemView.findViewById(R.id.rating_bar_display);
            imageViewFeedback = itemView.findViewById(R.id.image_view_feedback);
        }

        /**
         * Hàm tiện ích để tạo URL đầy đủ, giống như trong ImageUtils.
         */
        private String getFullUrl(String relativePath) {
            if (relativePath == null || relativePath.isEmpty()) {
                return null;
            }
            if (relativePath.startsWith("/")) {
                return BASE_URL + relativePath;
            }
            return BASE_URL + "/" + relativePath;
        }

        public void bind(final Feedback feedback) {
            textViewUserId.setText("User ID: " + feedback.getUserId());
            ratingBarDisplay.setRating(feedback.getRating());

            // Xử lý bình luận
            if (feedback.getComment() != null && !feedback.getComment().trim().isEmpty()) {
                textViewComment.setVisibility(View.VISIBLE);
                textViewComment.setText(feedback.getComment());
            } else {
                textViewComment.setVisibility(View.GONE);
            }

            // === LOGIC XỬ LÝ ẢNH (ĐÃ CẬP NHẬT) ===
            String imagePath = feedback.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                imageViewFeedback.setVisibility(View.VISIBLE);

                // Sử dụng hàm getFullUrl() ngay trong ViewHolder này
                String fullImageUrl = getFullUrl(imagePath);

                Glide.with(context)
                        .load(fullImageUrl)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_error)
                        .into(imageViewFeedback);

                // === THÊM CHỨC NĂNG PHÓNG TO ẢNH KHI CLICK ===
                imageViewFeedback.setOnClickListener(v -> {
                    new StfalconImageViewer.Builder<>(context, Collections.singletonList(fullImageUrl), (imageView, url) -> {
                        Glide.with(context)
                                .load(url)
                                .placeholder(R.drawable.ic_image_placeholder)
                                .error(R.drawable.ic_image_error)
                                .into(imageView);
                    }).show();
                });

            } else {
                imageViewFeedback.setVisibility(View.GONE);
                // Bỏ OnClickListener nếu không có ảnh
                imageViewFeedback.setOnClickListener(null);
            }

            // Xử lý ngày tháng
            if (feedback.getCreatedAt() != null) {
                textViewDate.setVisibility(View.VISIBLE);
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                textViewDate.setText(dateFormat.format(feedback.getCreatedAt()));
            } else {
                textViewDate.setVisibility(View.GONE);
            }
        }
    }
}