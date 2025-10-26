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
import com.example.sep490_mobile.adapter.CalendarClickListener;
import com.example.sep490_mobile.data.model.CalendarCell;
import com.example.sep490_mobile.data.model.CalendarCellType;

public class CalendarAdapter extends ListAdapter<CalendarCell, RecyclerView.ViewHolder> {

    private final CalendarClickListener listener;
    private final Context context;

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_DAY = 1;
    private static final int VIEW_TYPE_EMPTY = 2;

    public CalendarAdapter(Context context, CalendarClickListener listener) {
        super(new CalendarAdapter.CalendarDiffCallback());
        this.context = context;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        CalendarCellType type = getItem(position).getType();
        if (type == CalendarCellType.DAY_HEADER) {
            return VIEW_TYPE_HEADER;
        } else if (type == CalendarCellType.DAY_CELL) {
            return VIEW_TYPE_DAY;
        } else {
            return VIEW_TYPE_EMPTY;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                View headerView = inflater.inflate(R.layout.item_calendar_header, parent, false);
                return new CalendarAdapter.HeaderViewHolder(headerView, listener, context);
            case VIEW_TYPE_DAY:
                View dayView = inflater.inflate(R.layout.item_calendar_day, parent, false);
                return new CalendarAdapter.DayViewHolder(dayView, listener, context);
            default:
                View emptyView = inflater.inflate(R.layout.item_calendar_empty, parent, false);
                return new CalendarAdapter.EmptyViewHolder(emptyView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CalendarCell cell = getItem(position);
        if (holder.getItemViewType() == VIEW_TYPE_HEADER) {
            ((CalendarAdapter.HeaderViewHolder) holder).bind(cell);
        } else if (holder.getItemViewType() == VIEW_TYPE_DAY) {
            ((CalendarAdapter.DayViewHolder) holder).bind(cell);
        }
    }

    // === VIEW HOLDERS ===

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDayHeader;
        final CalendarClickListener listener;
        final Context context;

        HeaderViewHolder(@NonNull View itemView, CalendarClickListener listener, Context context) {
            super(itemView);
            tvDayHeader = itemView.findViewById(R.id.tvDayHeader);
            this.listener = listener;
            this.context = context;
        }

        void bind(final CalendarCell cell) {
            tvDayHeader.setText(cell.getDayHeader());

            // Cập nhật style cho header
            if (cell.isSelected()) {
                tvDayHeader.setBackgroundResource(R.drawable.day_header_background_selected);
                tvDayHeader.setTextColor(ContextCompat.getColor(context, R.color.calendar_header_text_selected));
            } else {
                tvDayHeader.setBackgroundResource(R.drawable.day_header_background_normal);
                tvDayHeader.setTextColor(ContextCompat.getColor(context, R.color.calendar_header_text_normal));
            }

            itemView.setOnClickListener(v -> {
                if (cell.getDayOfWeek() != null) {
                    listener.onDayHeaderClicked(cell.getDayOfWeek());
                }
            });
        }
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        final TextView tvDay;
        final View dayCellContainer;
        final CalendarClickListener listener;
        final Context context;

        DayViewHolder(@NonNull View itemView, CalendarClickListener listener, Context context) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            dayCellContainer = itemView.findViewById(R.id.dayCellContainer);
            this.listener = listener;
            this.context = context;
        }

        void bind(final CalendarCell cell) {
            tvDay.setText(String.valueOf(cell.getDate().getDayOfMonth()));

            int backgroundRes;
            int textColorRes;

            if (cell.isDisabled()) {
                backgroundRes = R.drawable.day_background_disabled;
                textColorRes = R.color.calendar_day_text_disabled;
            } else if (cell.isSelected()) {
                backgroundRes = R.drawable.day_background_selected;
                textColorRes = R.color.calendar_day_text_selected;
            } else if (cell.isWeekdaySelected()) {
                backgroundRes = R.drawable.day_background_weekday_selected;
                textColorRes = R.color.calendar_day_text_normal;
            } else {
                backgroundRes = R.drawable.day_background_normal;
                textColorRes = R.color.calendar_day_text_normal;
            }

            dayCellContainer.setBackgroundResource(backgroundRes);
            tvDay.setTextColor(ContextCompat.getColor(context, textColorRes));

            itemView.setClickable(!cell.isDisabled());
            itemView.setOnClickListener(v -> {
                if (!cell.isDisabled() && cell.getDate() != null) {
                    listener.onDayCellClicked(cell.getDate());
                }
            });
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    // === DIFF CALLBACK ===
    static class CalendarDiffCallback extends DiffUtil.ItemCallback<CalendarCell> {
        @Override
        public boolean areItemsTheSame(@NonNull CalendarCell oldItem, @NonNull CalendarCell newItem) {
            if (oldItem.getType() != newItem.getType()) return false;
            if (oldItem.getType() == CalendarCellType.DAY_CELL) {
                return oldItem.getDate().equals(newItem.getDate());
            }
            if (oldItem.getType() == CalendarCellType.DAY_HEADER) {
                return oldItem.getDayHeader().equals(newItem.getDayHeader());
            }
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull CalendarCell oldItem, @NonNull CalendarCell newItem) {
            if (oldItem.getType() == CalendarCellType.DAY_CELL) {
                return oldItem.isSelected() == newItem.isSelected() &&
                        oldItem.isWeekdaySelected() == newItem.isWeekdaySelected() &&
                        oldItem.isDisabled() == newItem.isDisabled();
            }
            if (oldItem.getType() == CalendarCellType.DAY_HEADER) {
                return oldItem.isSelected() == newItem.isSelected();
            }
            return true;
        }
    }
}