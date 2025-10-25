package com.example.sep490_mobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.model.TimeSlot;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;
import com.google.android.material.card.MaterialCardView;
import java.util.Locale;

public class TimeSlotAdapter extends ListAdapter<TimeSlot, TimeSlotAdapter.TimeSlotViewHolder> {

    private final OnTimeSlotClickListener listener;

    public interface OnTimeSlotClickListener {
        void onTimeSlotClick(int hour);
    }

    public TimeSlotAdapter(OnTimeSlotClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public TimeSlotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_slot, parent, false);
        // Sửa ở đây: Truyền `this` (chính là TimeSlotAdapter) vào ViewHolder
        return new TimeSlotViewHolder(view, listener, this);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeSlotViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class TimeSlotViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTime;
        private final MaterialCardView cardView;
        private final Context context;

        public TimeSlotViewHolder(@NonNull View itemView, OnTimeSlotClickListener listener, TimeSlotAdapter adapter) {
            super(itemView);
            context = itemView.getContext();
            tvTime = itemView.findViewById(R.id.tvTime);
            cardView = itemView.findViewById(R.id.timeSlotCard);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onTimeSlotClick(adapter.getItem(position).getHour());
                }
            });
        }

        public void bind(TimeSlot timeSlot) {
            tvTime.setText(String.format(Locale.getDefault(), "%02d:00", timeSlot.getHour()));

            if (timeSlot.isSelected()) {
                // Trạng thái được chọn
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.calendar_accent_color));
                tvTime.setTextColor(ContextCompat.getColor(context, R.color.white));
                cardView.setStrokeWidth(0); // Bỏ viền khi được chọn
            } else {
                // Trạng thái không được chọn
                cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
                tvTime.setTextColor(ContextCompat.getColor(context, R.color.calendar_text_primary));
                cardView.setStrokeWidth(2); // Dùng strokeWidth > 0 để hiển thị viền
                cardView.setStrokeColor(ContextCompat.getColor(context, R.color.material_on_surface_disabled));
            }
        }
    }

    private static final DiffUtil.ItemCallback<TimeSlot> DIFF_CALLBACK = new DiffUtil.ItemCallback<TimeSlot>() {
        @Override
        public boolean areItemsTheSame(@NonNull TimeSlot oldItem, @NonNull TimeSlot newItem) {
            return oldItem.getHour() == newItem.getHour();
        }

        @Override
        public boolean areContentsTheSame(@NonNull TimeSlot oldItem, @NonNull TimeSlot newItem) {
            return oldItem.isSelected() == newItem.isSelected();
        }
    };


}