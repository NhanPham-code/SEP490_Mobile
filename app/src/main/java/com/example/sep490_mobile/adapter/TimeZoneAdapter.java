package com.example.sep490_mobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.model.TimeZone; // Import lớp mới
import java.util.List;
import java.util.Locale;

public class TimeZoneAdapter extends RecyclerView.Adapter<TimeZoneAdapter.TimeZoneViewHolder> {

    private List<TimeZone> timeZones;
    private final OnTimeZoneClickListener listener;
    private final Context context;

    public interface OnTimeZoneClickListener {
        void onTimeZoneClick(TimeZone timeZone);
    }

    public TimeZoneAdapter(Context context, OnTimeZoneClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void submitList(List<TimeZone> newTimeZones) {
        this.timeZones = newTimeZones;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TimeZoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time_slot, parent, false);
        return new TimeZoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TimeZoneViewHolder holder, int position) {
        TimeZone timeZone = timeZones.get(position);
        holder.bind(timeZone, listener);
    }

    @Override
    public int getItemCount() {
        return timeZones != null ? timeZones.size() : 0;
    }

    class TimeZoneViewHolder extends RecyclerView.ViewHolder {
        TextView timeSlotText;

        public TimeZoneViewHolder(@NonNull View itemView) {
            super(itemView);
            // SỬA LỖI: Đổi ID cho khớp với file item_time_slot.xml
            timeSlotText = itemView.findViewById(R.id.tvTime);
        }

        void bind(final TimeZone timeZone, final OnTimeZoneClickListener listener) {
            timeSlotText.setText(String.format(Locale.getDefault(), "%02d:00", timeZone.getHour()));

            if (timeZone.isPast()) {
                itemView.setEnabled(false);
                timeSlotText.setTextColor(ContextCompat.getColor(context, R.color.calendar_disabled_text));
                itemView.setBackgroundResource(R.drawable.item_time_slot_disabled_background);
            } else {
                itemView.setEnabled(true);
                itemView.setOnClickListener(v -> listener.onTimeZoneClick(timeZone));

                if (timeZone.isSelected()) {
                    timeSlotText.setTextColor(ContextCompat.getColor(context, R.color.white));
                    itemView.setBackgroundResource(R.drawable.item_time_slot_selected_background);
                } else {
                    timeSlotText.setTextColor(ContextCompat.getColor(context, R.color.black));
                    itemView.setBackgroundResource(R.drawable.item_time_slot_default_background);
                }
            }
        }
    }
}