// File: adapter/TimeHeaderAdapter.java
package com.example.sep490_mobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sep490_mobile.R;

import java.util.List;

public class TimeHeaderAdapter extends RecyclerView.Adapter<TimeHeaderAdapter.TimeViewHolder> {

    private final List<String> timeSlots;

    // Định nghĩa 2 loại View Type
    private static final int VIEW_TYPE_START = 0;
    private static final int VIEW_TYPE_MIDDLE = 1;

    public TimeHeaderAdapter(List<String> timeSlots) {
        this.timeSlots = timeSlots;
    }

    @Override
    public int getItemViewType(int position) {
        // Nếu là item đầu tiên, dùng VIEW_TYPE_START
        if (position == 0) {
            return VIEW_TYPE_START;
        }
        // Các item còn lại dùng VIEW_TYPE_MIDDLE
        return VIEW_TYPE_MIDDLE;
    }

    @NonNull
    @Override
    public TimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        // Dựa vào viewType để inflate layout tương ứng
        if (viewType == VIEW_TYPE_START) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_time_header_start, parent, false);
        } else { // VIEW_TYPE_MIDDLE
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_time_header_middle, parent, false);
        }
        return new TimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeViewHolder holder, int position) {
        String time = timeSlots.get(position);
        holder.tvTime.setText(time);
    }

    @Override
    public int getItemCount() {
        return timeSlots != null ? timeSlots.size() : 0;
    }

    static class TimeViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime;

        public TimeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}