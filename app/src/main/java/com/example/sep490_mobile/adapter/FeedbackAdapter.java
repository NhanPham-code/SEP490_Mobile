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
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.model.Feedback;
import com.example.sep490_mobile.data.dto.PublicProfileDTO;
import com.example.sep490_mobile.utils.ImageUtils;
import com.stfalcon.imageviewer.StfalconImageViewer;

import java.text.SimpleDateFormat;
import java.util.*;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private final List<Feedback> feedbackList = new ArrayList<>();
    private final int currentUserId;
    private Map<Integer, PublicProfileDTO> userProfiles = new HashMap<>();

    public FeedbackAdapter(int currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setUserProfiles(Map<Integer, PublicProfileDTO> userProfiles) {
        this.userProfiles = userProfiles;
        notifyDataSetChanged();
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
        holder.bind(feedback, userProfiles);
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

    static class FeedbackViewHolder extends RecyclerView.ViewHolder {

        private static final String BASE_URL = "https://10.0.2.2:7221";
        private final TextView textViewUserId, textViewUserName, textViewComment, textViewDate;
        private final RatingBar ratingBarDisplay;
        private final ImageView imageViewFeedback, imageViewUserAvatar;
        private final Context context;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            context = itemView.getContext();
            textViewUserId = itemView.findViewById(R.id.text_view_user_id);
            textViewUserName = itemView.findViewById(R.id.text_view_user_name);
            textViewComment = itemView.findViewById(R.id.text_view_comment);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            ratingBarDisplay = itemView.findViewById(R.id.rating_bar_display);
            imageViewFeedback = itemView.findViewById(R.id.image_view_feedback);
            imageViewUserAvatar = itemView.findViewById(R.id.image_view_user_avatar);
        }

        public void bind(final Feedback feedback, Map<Integer, PublicProfileDTO> userProfiles) {
            textViewUserId.setText("User ID: " + feedback.getUserId());
            ratingBarDisplay.setRating(feedback.getRating());

            // Lấy thông tin user (name, avatar)
            PublicProfileDTO profile = userProfiles != null ? userProfiles.get(feedback.getUserId()) : null;
            if (profile != null) {
                textViewUserName.setText(profile.getFullName());
                String avatarUrl = profile.getAvatarUrl();
                Glide.with(context)
                        .load(ImageUtils.getFullUrl(avatarUrl != null && avatarUrl.length() > 0 ? avatarUrl : ""))
                        .placeholder(R.drawable.ic_default_avatar)
                        .error(R.drawable.ic_default_avatar)
                        .transform(new CircleCrop())
                        .into(imageViewUserAvatar);
            } else {
                textViewUserName.setText("Unknown User");
                imageViewUserAvatar.setImageResource(R.drawable.ic_default_avatar);
            }

            // Xử lý bình luận
            if (feedback.getComment() != null && !feedback.getComment().trim().isEmpty()) {
                textViewComment.setVisibility(View.VISIBLE);
                textViewComment.setText(feedback.getComment());
            } else {
                textViewComment.setVisibility(View.GONE);
            }

            // Xử lý ảnh feedback
            String imagePath = feedback.getImagePath();
            if (imagePath != null && !imagePath.isEmpty()) {
                imageViewFeedback.setVisibility(View.VISIBLE);
                String fullImageUrl = getFullUrl(imagePath);

                Glide.with(context)
                        .load(fullImageUrl)
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_image_error)
                        .into(imageViewFeedback);

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

        private String getFullUrl(String relativePath) {
            if (relativePath == null || relativePath.isEmpty()) {
                return null;
            }
            if (relativePath.startsWith("/")) {
                return BASE_URL + relativePath;
            }
            return BASE_URL + "/" + relativePath;
        }
    }
}