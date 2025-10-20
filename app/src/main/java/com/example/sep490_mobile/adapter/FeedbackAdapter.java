package com.example.sep490_mobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.model.Feedback; // Đảm bảo import đúng model của bạn

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.FeedbackViewHolder> {

    private List<Feedback> feedbackList = new ArrayList<>();
    private final OnItemInteractionListener listener;
    private final int currentUserId; // ID của người dùng đang đăng nhập

    /**
     * Interface để Fragment (hoặc bất kỳ lớp nào) có thể lắng nghe
     * các sự kiện click từ một item trong Adapter.
     */
    public interface OnItemInteractionListener {
        void onDeleteClick(Feedback feedback);
    }

    /**
     * Constructor của Adapter.
     * @param listener Một đối tượng implement interface để xử lý sự kiện (thường là Fragment).
     * @param currentUserId ID của người dùng đang đăng nhập để so sánh quyền xóa.
     */
    public FeedbackAdapter(OnItemInteractionListener listener, int currentUserId) {
        this.listener = listener;
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public FeedbackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tạo view cho một item từ file layout item_feedback.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feedback, parent, false);
        return new FeedbackViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeedbackViewHolder holder, int position) {
        // Lấy dữ liệu của item tại vị trí `position`
        Feedback feedback = feedbackList.get(position);
        // Gắn dữ liệu đó vào ViewHolder
        holder.bind(feedback, listener, currentUserId);
    }

    @Override
    public int getItemCount() {
        // Trả về tổng số item trong danh sách
        return feedbackList.size();
    }

    /**
     * Cập nhật danh sách dữ liệu mới cho Adapter và thông báo cho RecyclerView để vẽ lại giao diện.
     * @param newFeedbackList Danh sách feedback mới từ ViewModel.
     */
    public void submitList(List<Feedback> newFeedbackList) {
        this.feedbackList.clear();
        if (newFeedbackList != null) {
            this.feedbackList.addAll(newFeedbackList);
        }
        notifyDataSetChanged(); // Cập nhật lại toàn bộ RecyclerView
    }

    /**
     * Lớp ViewHolder: Giữ các tham chiếu đến các View bên trong một item
     * để tránh phải gọi `findViewById` nhiều lần.
     */
    static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewUserId, textViewComment, textViewDate;
        private final RatingBar ratingBarDisplay;
        private final ImageButton buttonDelete;

        public FeedbackViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các View từ layout
            textViewUserId = itemView.findViewById(R.id.text_view_user_id);
            textViewComment = itemView.findViewById(R.id.text_view_comment);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            ratingBarDisplay = itemView.findViewById(R.id.rating_bar_display);
            buttonDelete = itemView.findViewById(R.id.button_delete);
        }

        /**
         * Gắn dữ liệu từ đối tượng Feedback vào các View tương ứng.
         */
        public void bind(final Feedback feedback, final OnItemInteractionListener listener, int currentUserId) {
            textViewUserId.setText("User ID: " + feedback.getUserId());
            textViewComment.setText(feedback.getComment());
            ratingBarDisplay.setRating(feedback.getRating());

            // Định dạng lại ngày tháng cho dễ đọc hơn
            if (feedback.getCreatedAt() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
                textViewDate.setText(dateFormat.format(feedback.getCreatedAt()));
            }

            // Logic quan trọng: Chỉ hiển thị nút xóa nếu feedback này là của người dùng hiện tại
            if (feedback.getUserId() == currentUserId) {
                buttonDelete.setVisibility(View.VISIBLE);
                // Thiết lập sự kiện click, gọi lại interface để Fragment xử lý
                buttonDelete.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDeleteClick(feedback);
                    }
                });
            } else {
                // Nếu không phải, ẩn nút xóa đi
                buttonDelete.setVisibility(View.GONE);
            }
        }
    }
}