package com.example.sep490_mobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.BookingReadDto;
import com.example.sep490_mobile.data.dto.CourtsDTO;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleRowAdapter extends RecyclerView.Adapter<ScheduleRowAdapter.RowViewHolder> {

    private final List<CourtsDTO> courts;
    private final List<String> hourBlocks;
    private final List<RecyclerView> horizontalRecyclerViews;
    private final RecyclerView.OnScrollListener horizontalScrollListener;
    private Calendar selectedDate;
    private List<BookingReadDto> bookingsForDay;
    private final RecyclerView.ItemDecoration itemDecoration;
    private final CellAdapter.OnCellInteractionListener cellInteractionListener;

    private Map<Integer, List<Integer>> courtRelations = new HashMap<>();
    private Map<Integer, List<Integer>> currentSelections = new HashMap<>();

    public ScheduleRowAdapter(List<CourtsDTO> courts, List<String> hourBlocks, List<RecyclerView> horizontalRecyclerViews, RecyclerView.OnScrollListener horizontalScrollListener, Calendar selectedDate, List<BookingReadDto> bookingsForDay, RecyclerView.ItemDecoration itemDecoration, CellAdapter.OnCellInteractionListener listener) {
        this.courts = courts;
        this.hourBlocks = hourBlocks;
        this.horizontalRecyclerViews = horizontalRecyclerViews;
        this.horizontalScrollListener = horizontalScrollListener;
        this.selectedDate = selectedDate;
        this.bookingsForDay = bookingsForDay;
        this.itemDecoration = itemDecoration;
        this.cellInteractionListener = listener;
    }

    public void setBookingsForDay(List<BookingReadDto> bookingsForDay) {
        this.bookingsForDay = bookingsForDay;
        notifyDataSetChanged();
    }

    public void setCourtRelations(Map<Integer, List<Integer>> relations) {
        this.courtRelations = relations;
        notifyDataSetChanged();
    }

    public void updateSelections(Map<Integer, List<Integer>> selections) {
        this.currentSelections = selections;
        notifyDataSetChanged();
    }

    public void setSelectedDate(Calendar selectedDate) {
        this.selectedDate = selectedDate;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_schedule_row, parent, false);
        RowViewHolder holder = new RowViewHolder(view, itemDecoration);

        horizontalRecyclerViews.add(holder.rowRecyclerView);
        holder.rowRecyclerView.addOnScrollListener(horizontalScrollListener);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RowViewHolder holder, int position) {
        CourtsDTO court = courts.get(position);
        holder.bind(court, hourBlocks, selectedDate, bookingsForDay, cellInteractionListener, courtRelations, currentSelections);
    }

    @Override
    public int getItemCount() {
        return courts != null ? courts.size() : 0;
    }

    static class RowViewHolder extends RecyclerView.ViewHolder {
        RecyclerView rowRecyclerView;
        private CellAdapter cellAdapter;

        public RowViewHolder(@NonNull View itemView, RecyclerView.ItemDecoration itemDecoration) {
            super(itemView);
            rowRecyclerView = (RecyclerView) itemView;
            rowRecyclerView.setLayoutManager(new LinearLayoutManager(itemView.getContext(), LinearLayoutManager.HORIZONTAL, false));
            rowRecyclerView.setNestedScrollingEnabled(false);

            // Thêm padding và clipToPadding cho mỗi hàng con
            int paddingStart = (int) (120 * itemView.getResources().getDisplayMetrics().density);
            int paddingEnd = (int) (50 * itemView.getResources().getDisplayMetrics().density);
            rowRecyclerView.setPadding(paddingStart, 0, paddingEnd, 0);
            rowRecyclerView.setClipToPadding(false);

            if (rowRecyclerView.getItemDecorationCount() > 0) {
                rowRecyclerView.removeItemDecorationAt(0);
            }
            if (itemDecoration != null) {
                rowRecyclerView.addItemDecoration(itemDecoration);
            }
        }

        public void bind(CourtsDTO court, List<String> hourBlocks, Calendar selectedDate, List<BookingReadDto> bookingsForDay, CellAdapter.OnCellInteractionListener listener, Map<Integer, List<Integer>> relations, Map<Integer, List<Integer>> selections) {
            if (cellAdapter == null) {
                cellAdapter = new CellAdapter(court, hourBlocks, selectedDate, bookingsForDay, listener);
                rowRecyclerView.setAdapter(cellAdapter);
            }

            cellAdapter.setSelectedDate(selectedDate);
            cellAdapter.setCourtRelations(relations);
            cellAdapter.updateSelections(selections);
            cellAdapter.setBookingsForDay(bookingsForDay);
            cellAdapter.notifyDataSetChanged();
        }
    }
}