package com.example.sep490_mobile.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.BookingDetailDTO;
import com.example.sep490_mobile.data.dto.BookingReadDto;
import com.example.sep490_mobile.data.dto.CourtsDTO;
import com.example.sep490_mobile.ui.custom.BookingCellLayout;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class CellAdapter extends RecyclerView.Adapter<CellAdapter.CellViewHolder> {

    public interface OnCellInteractionListener {
        void onCellClick(int courtId, int hour, boolean isSelected);
        boolean isInteractionAllowed();
    }

    private final CourtsDTO court;
    private final List<String> timeSlots;
    private Calendar selectedDate;
    private List<BookingReadDto> bookingsForDay;
    private final OnCellInteractionListener listener;

    private Map<Integer, List<Integer>> courtRelations = new HashMap<>();
    private Map<Integer, List<Integer>> currentSelections = new HashMap<>();


    public CellAdapter(CourtsDTO court, List<String> timeSlots, Calendar selectedDate, List<BookingReadDto> bookingsForDay, OnCellInteractionListener listener) {
        this.court = court;
        this.timeSlots = timeSlots;
        this.selectedDate = selectedDate;
        this.bookingsForDay = bookingsForDay;
        this.listener = listener;
    }

    public void setCourtRelations(Map<Integer, List<Integer>> courtRelations) {
        this.courtRelations = courtRelations;
    }

    public void updateSelections(Map<Integer, List<Integer>> currentSelections) {
        this.currentSelections = currentSelections;
    }

    public void setSelectedDate(Calendar selectedDate) {
        this.selectedDate = selectedDate;
    }

    public void setBookingsForDay(List<BookingReadDto> bookingsForDay) {
        this.bookingsForDay = bookingsForDay;
    }

    @NonNull @Override
    public CellViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_grid_cell, parent, false);
        return new CellViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CellViewHolder holder, int position) {
        String time = timeSlots.get(position);
        int hour = Integer.parseInt(time.split(":")[0]);
        int courtId = court.getId();

        // Reset trạng thái
        holder.cellContainer.setBookingState(BookingCellLayout.STATE_AVAILABLE);
        holder.cellContainer.setSelected(false);
        holder.starIcon.setVisibility(View.GONE);

        // KIỂM TRA ƯU TIÊN CAO NHẤT: Sân này có bị khóa vĩnh viễn không?
        if (!court.isAvailable()) {
            // Nếu sân không khả dụng, vô hiệu hóa toàn bộ các ô trong hàng này
            holder.cellContainer.setEnabled(false); // Kích hoạt màu xám của trạng thái disabled
            holder.cellContainer.setOnClickListener(null);
            return; // Dừng lại, không cần kiểm tra các trạng thái khác
        }

        // Các kiểm tra khác
        boolean isPast = isPastOrPresentHour(selectedDate, time);
        boolean isBooked = (findIfBooked(courtId, hour) != null);
        boolean isRelatedToBooked = findIfRelatedToBooked(courtId, hour);
        boolean isSelected = isCurrentlySelected(courtId, hour);
        boolean isLockedBySelection = isLockedByRelatedSelection(courtId, hour);

        // Áp dụng trạng thái lên UI theo thứ tự ưu tiên
        if (isBooked) {
            holder.cellContainer.setBookingState(BookingCellLayout.STATE_BOOKED);
            holder.starIcon.setVisibility(View.VISIBLE);
            holder.cellContainer.setEnabled(false);
            holder.cellContainer.setOnClickListener(null);
        } else if (isRelatedToBooked) {
            holder.cellContainer.setBookingState(BookingCellLayout.STATE_RELATED);
            holder.cellContainer.setEnabled(false);
            holder.cellContainer.setOnClickListener(null);
        } else if (isPast) {
            holder.cellContainer.setEnabled(false);
            holder.cellContainer.setOnClickListener(null);
        } else if (isLockedBySelection) {
            holder.cellContainer.setBookingState(BookingCellLayout.STATE_RELATED);
            holder.cellContainer.setEnabled(false);
            holder.cellContainer.setOnClickListener(null);
        } else {
            holder.cellContainer.setEnabled(true);
            holder.cellContainer.setSelected(isSelected);
            holder.cellContainer.setOnClickListener(v -> {
                if (listener != null && listener.isInteractionAllowed()) {
                    listener.onCellClick(courtId, hour, !isSelected);
                }
            });
        }
    }

    private boolean isCurrentlySelected(int courtId, int hour) {
        List<Integer> selectedCourts = currentSelections.get(hour);
        return selectedCourts != null && selectedCourts.contains(courtId);
    }

    private boolean isLockedByRelatedSelection(int currentCourtId, int hour) {
        if (courtRelations == null || currentSelections == null) return false;
        List<Integer> relatedCourtIds = courtRelations.get(currentCourtId);
        if (relatedCourtIds == null || relatedCourtIds.isEmpty()) return false;
        List<Integer> selectedCourtsInHour = currentSelections.get(hour);
        if (selectedCourtsInHour == null || selectedCourtsInHour.isEmpty()) return false;
        for (int selectedId : selectedCourtsInHour) {
            if (relatedCourtIds.contains(selectedId)) {
                return true;
            }
        }
        return false;
    }

    private boolean findIfRelatedToBooked(int currentCourtId, int hour) {
        if (courtRelations == null || bookingsForDay == null || bookingsForDay.isEmpty()) return false;
        List<Integer> relatedIDs = courtRelations.get(currentCourtId);
        if (relatedIDs == null || relatedIDs.isEmpty()) return false;

        for (BookingReadDto booking : bookingsForDay) {
            // *** ADD NULL CHECK HERE ***
            if (booking.getBookingDetails() == null) {
                continue; // Skip this booking if details are null
            }
            // *** END NULL CHECK ***

            // Now it's safe to loop
            for (BookingDetailDTO detail : booking.getBookingDetails()) { // This loop could also crash
                if (relatedIDs.contains(detail.getCourtId())) {
                    // Parse LocalDateTime safely, handle potential nulls from API
                    LocalDateTime startTime = null, endTime = null;
                    try {
                        if (detail.getStartTime() != null) {
                            startTime = LocalDateTime.parse(detail.getStartTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                        }
                        if (detail.getEndTime() != null) {
                            endTime = LocalDateTime.parse(detail.getEndTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                        }
                    } catch (Exception e) {
                        Log.e("CellAdapter", "Error parsing related booking detail time", e);
                        continue; // Skip this detail if time parsing fails
                    }

                    // Check if parsing was successful before comparing hours
                    if (startTime != null && endTime != null &&
                            hour >= startTime.getHour() && hour < endTime.getHour()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private BookingDetailDTO findIfBooked(int courtId, int hour) {
        if (bookingsForDay == null || bookingsForDay.isEmpty()) return null;
        for (BookingReadDto booking : bookingsForDay) {
            // *** ADD NULL CHECK HERE ***
            if (booking.getBookingDetails() == null) {
                continue; // Skip this booking if details are null
            }
            // *** END NULL CHECK ***

            // Now it's safe to loop
            for (BookingDetailDTO detail : booking.getBookingDetails()) { // This loop was causing the crash
                if (detail.getCourtId() == courtId) {
                    // Parse LocalDateTime safely, handle potential nulls from API
                    LocalDateTime startTime = null, endTime = null;
                    try {
                        if (detail.getStartTime() != null) {
                            startTime = LocalDateTime.parse(detail.getStartTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                        }
                        if (detail.getEndTime() != null) {
                            endTime = LocalDateTime.parse(detail.getEndTime(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                        }
                    } catch (Exception e) {
                        Log.e("CellAdapter", "Error parsing booking detail time", e);
                        continue; // Skip this detail if time parsing fails
                    }

                    // Check if parsing was successful before comparing hours
                    if (startTime != null && endTime != null &&
                            hour >= startTime.getHour() && hour < endTime.getHour()) {
                        return detail;
                    }
                }
            }
        }
        return null;
    }

    private boolean isPastOrPresentHour(Calendar date, String time) {
        TimeZone vietnamTimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        Calendar today = Calendar.getInstance(vietnamTimeZone);
        boolean isToday = date.get(Calendar.YEAR) == today.get(Calendar.YEAR) && date.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR);
        if (isToday) {
            try {
                int hour = Integer.parseInt(time.split(":")[0]);
                int currentHour = today.get(Calendar.HOUR_OF_DAY);
                return hour <= currentHour;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    public int getItemCount() {
        return timeSlots != null ? timeSlots.size() : 0;
    }

    static class CellViewHolder extends RecyclerView.ViewHolder {
        BookingCellLayout cellContainer;
        ImageView starIcon;
        public CellViewHolder(@NonNull View itemView) {
            super(itemView);
            cellContainer = (BookingCellLayout) itemView;
            starIcon = itemView.findViewById(R.id.iv_star_icon);
        }
    }
}