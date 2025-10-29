package com.example.sep490_mobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.notification.NotificationDTO;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class NotificationAdapter extends ListAdapter<NotificationDTO, NotificationAdapter.NotificationViewHolder> {

    public NotificationAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<NotificationDTO> DIFF_CALLBACK = new DiffUtil.ItemCallback<NotificationDTO>() {
        @Override
        public boolean areItemsTheSame(@NonNull NotificationDTO oldItem, @NonNull NotificationDTO newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull NotificationDTO oldItem, @NonNull NotificationDTO newItem) {
            // So sánh các trường sẽ ảnh hưởng đến giao diện
            return oldItem.isRead() == newItem.isRead()
                    && oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.getMessage().equals(newItem.getMessage());
        }
    };

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        NotificationDTO notification = getItem(position);
        holder.bind(notification);
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        // Khai báo các view
        private final ImageView iconType;
        private final TextView textTitle;
        private final TextView textMessage;
        private final TextView textTime;
        private final View indicatorUnread;
        private final View itemView;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            // Ánh xạ view từ layout
            iconType = itemView.findViewById(R.id.icon_notification_type);
            textTitle = itemView.findViewById(R.id.text_notification_title);
            textMessage = itemView.findViewById(R.id.text_notification_message);
            textTime = itemView.findViewById(R.id.text_notification_time);
            indicatorUnread = itemView.findViewById(R.id.indicator_unread);
        }

        public void bind(NotificationDTO notification) {
            // Gán dữ liệu
            textTitle.setText(notification.getTitle());
            textMessage.setText(notification.getMessage());
            textTime.setText(getTimeAgo(notification.getCreatedAt()));

            // == TỐI ƯU HÓA LOGIC HIỂN THỊ TRẠNG THÁI ==
            // 1. Kích hoạt trạng thái "activated" nếu chưa đọc.
            // Drawable selector (notification_item_background_selector.xml) sẽ tự động xử lý việc đổi màu nền.
            itemView.setActivated(notification.isRead());

            // 2. Hiển thị/ẩn chấm tròn chỉ báo
            indicatorUnread.setVisibility(notification.isRead() ? View.INVISIBLE : View.VISIBLE);

            // (Tùy chọn) Thay đổi icon dựa trên loại thông báo
            updateIcon(notification.getType());
        }

        private void updateIcon(String type) {
            if (type == null) {
                iconType.setImageResource(R.drawable.ic_notification_bell);
                return;
            }
            // Ví dụ về thay đổi icon
            switch (type) {
                case "Booking.New":
                case "Booking.Status":
                    // iconType.setImageResource(R.drawable.ic_booking_confirmation);
                    break;
                case "Discount.New":
                    // iconType.setImageResource(R.drawable.ic_discount_tag);
                    break;
                default:
                    iconType.setImageResource(R.drawable.ic_notification_bell);
                    break;
            }
        }

        private String getTimeAgo(Date date) {
            if (date == null) return "";
            long time = date.getTime();
            long now = System.currentTimeMillis();
            long diff = now - time;

            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            long days = TimeUnit.MILLISECONDS.toDays(diff);

            if (minutes < 1) {
                return "Vừa xong";
            } else if (minutes < 60) {
                return minutes + " phút trước";
            } else if (hours < 24) {
                return hours + " giờ trước";
            } else if (days < 7) {
                return days + " ngày trước";
            } else {
                // Nếu lâu hơn, có thể hiển thị ngày tháng cụ thể
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
                return sdf.format(date);
            }
        }
    }
}