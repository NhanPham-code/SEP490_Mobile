package com.example.sep490_mobile.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.booking.BookingManagementDTO;
import com.example.sep490_mobile.data.dto.booking.DailyBookingDTO;
import com.example.sep490_mobile.data.dto.booking.MonthlyBookingDTO;
import com.example.sep490_mobile.ui.booking.BookingHistoryFragmentDirections;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class BookingHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_DAILY = 0;
    private static final int TYPE_MONTHLY = 1;

    private final List<Object> fullList = new ArrayList<>();
    private final List<Object> items = new ArrayList<>();
    private final Context context;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final DecimalFormat priceFormat = new DecimalFormat("#,### 'đ'");

    public BookingHistoryAdapter(Context context) {
        this.context = context;
    }

    public void setData(BookingManagementDTO data) {
        fullList.clear();
        if (data != null) {
            if (data.getDailyBookings() != null) fullList.addAll(data.getDailyBookings());
            if (data.getMonthlyBookings() != null) fullList.addAll(data.getMonthlyBookings());
        }
        filterData("Tất cả loại", "Tất cả trạng thái");
    }

    public void filterData(String typeFilter, String statusFilter) {
        items.clear();
        String statusFilterEng = translateFilterToEng(statusFilter);
        List<Object> filteredList = new ArrayList<>();

        for (Object item : fullList) {
            boolean typeMatch = typeFilter.equals("Tất cả loại") ||
                    (typeFilter.equals("Lịch đặt ngày") && item instanceof DailyBookingDTO) ||
                    (typeFilter.equals("Lịch đặt tháng") && item instanceof MonthlyBookingDTO);

            String itemStatus = "";
            if (item instanceof DailyBookingDTO) itemStatus = ((DailyBookingDTO) item).getBooking().getStatus();
            else if (item instanceof MonthlyBookingDTO) itemStatus = ((MonthlyBookingDTO) item).getMonthlyBooking().getStatus();

            boolean statusMatch = statusFilter.equals("Tất cả trạng thái") ||
                    (itemStatus != null && itemStatus.equalsIgnoreCase(statusFilterEng));

            if (typeMatch && statusMatch) filteredList.add(item);
        }
        items.addAll(filteredList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() { return items.size(); }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof DailyBookingDTO ? TYPE_DAILY : TYPE_MONTHLY;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_DAILY) {
            return new DailyViewHolder(inflater.inflate(R.layout.item_booking_daily, parent, false));
        } else {
            return new MonthlyViewHolder(inflater.inflate(R.layout.item_booking_monthly, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_DAILY) {
            ((DailyViewHolder) holder).bind((DailyBookingDTO) items.get(position));
        } else {
            ((MonthlyViewHolder) holder).bind((MonthlyBookingDTO) items.get(position));
        }
    }

    class DailyViewHolder extends RecyclerView.ViewHolder {
        TextView tvStadiumName, tvBookingDate, tvSubCourts, tvBookingPrice, tvBookingStatus;

        public DailyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStadiumName = itemView.findViewById(R.id.tv_stadium_name);
            tvBookingDate = itemView.findViewById(R.id.tv_booking_date);
            tvSubCourts = itemView.findViewById(R.id.tv_sub_courts);
            tvBookingPrice = itemView.findViewById(R.id.tv_booking_price);
            tvBookingStatus = itemView.findViewById(R.id.tv_booking_status);
        }

        void bind(DailyBookingDTO dailyBooking) {
            if (dailyBooking == null || dailyBooking.getBooking() == null) return;

            tvStadiumName.setText(dailyBooking.getBooking().getStadiumName());
            Date bookingDate = dailyBooking.getBooking().getDate();
            if (bookingDate != null) tvBookingDate.setText("Ngày: " + dateFormat.format(bookingDate));

            Double price = dailyBooking.getBooking().getTotalPrice();
            if (price != null) tvBookingPrice.setText("Tổng tiền: " + priceFormat.format(price));

            if (dailyBooking.getBooking().getBookingDetails() != null && !dailyBooking.getBooking().getBookingDetails().isEmpty()) {
                String courtNames = dailyBooking.getBooking().getBookingDetails().stream()
                        .map(detail -> detail.getCourtName() != null ? detail.getCourtName() : "")
                        .collect(Collectors.joining(", "));
                tvSubCourts.setText("Sân con: " + courtNames);
                tvSubCourts.setVisibility(View.VISIBLE);
            } else {
                tvSubCourts.setVisibility(View.GONE);
            }

            setStatus(tvBookingStatus, dailyBooking.getBooking().getStatus());

            itemView.setOnClickListener(v -> {
                NavDirections action = BookingHistoryFragmentDirections.actionBookingHistoryFragmentToBookingHistoryDetailFragment(dailyBooking);
                Navigation.findNavController(v).navigate(action);
            });
        }
    }

    class MonthlyViewHolder extends RecyclerView.ViewHolder {
        TextView tvStadiumName, tvBookingPeriod, tvTimeFrame, tvBookingPrice, tvBookingStatus;

        public MonthlyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStadiumName = itemView.findViewById(R.id.tv_stadium_name);
            tvBookingPeriod = itemView.findViewById(R.id.tv_booking_period);
            tvTimeFrame = itemView.findViewById(R.id.tv_time_frame);
            tvBookingPrice = itemView.findViewById(R.id.tv_booking_price);
            tvBookingStatus = itemView.findViewById(R.id.tv_booking_status);
        }

        void bind(MonthlyBookingDTO monthlyBooking) {
            if (monthlyBooking == null || monthlyBooking.getMonthlyBooking() == null) return;

            tvStadiumName.setText(monthlyBooking.getMonthlyBooking().getStadiumName());
            tvBookingPeriod.setText("Gói tháng " + monthlyBooking.getMonthlyBooking().getMonth() + "/" + monthlyBooking.getMonthlyBooking().getYear());

            Double price = monthlyBooking.getMonthlyBooking().getTotalPrice();
            if (price != null) tvBookingPrice.setText("Tổng tiền: " + priceFormat.format(price));

            String startTime = parseTime(monthlyBooking.getMonthlyBooking().getStartTime());
            String endTime = parseTime(monthlyBooking.getMonthlyBooking().getEndTime());
            tvTimeFrame.setText("Khung giờ: " + startTime + " - " + endTime);

            setStatus(tvBookingStatus, monthlyBooking.getMonthlyBooking().getStatus());

            itemView.setOnClickListener(v -> {
                NavDirections action = BookingHistoryFragmentDirections.actionBookingHistoryFragmentToBookingHistoryDetailFragment(monthlyBooking);
                Navigation.findNavController(v).navigate(action);
            });
        }
    }

    private void setStatus(TextView textView, String status) {
        textView.setText(translateStatus(status));
        GradientDrawable background = (GradientDrawable) textView.getBackground().mutate();
        int color = ContextCompat.getColor(context, getStatusColor(status));
        background.setColor(color);
    }

    private String translateStatus(String status) {
        if (status == null) return "Không rõ";
        switch (status.toLowerCase()) {
            case "accepted": return "Đã nhận";
            case "completed": return "Hoàn thành";
            case "cancelled": return "Đã hủy";
            case "payfail": return "Thanh toán lỗi";
            case "pending": return "Chờ xử lý";
            case "waiting": return "Chờ thanh toán";
            default: return status;
        }
    }

    private int getStatusColor(String status) {
        if (status == null) return R.color.app_background;
        switch (status.toLowerCase()) {
            case "accepted": return R.color.status_accepted;
            case "completed": return R.color.status_completed;
            case "cancelled": return R.color.status_cancelled;
            case "payfail": return R.color.status_payfail;
            default: return R.color.app_background;
        }
    }

    private String parseTime(String ptTime) {
        if (ptTime == null || !ptTime.startsWith("PT")) return "N/A";
        try { return ptTime.substring(2, ptTime.length() - 1) + ":00"; } catch (Exception e) { return "N/A"; }
    }

    private String translateFilterToEng(String filterVie) {
        switch (filterVie) {
            case "Đã nhận": return "accepted";
            case "Hoàn thành": return "completed";
            case "Đã hủy": return "cancelled";
            case "Thanh toán lỗi": return "payfail";
            case "Chờ xử lý": return "pending";
            case "Chờ thanh toán": return "waiting";
            default: return "";
        }
    }
}