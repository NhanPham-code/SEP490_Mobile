package com.example.sep490_mobile.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast; // Thêm Toast nếu cần thông báo lỗi
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.booking.BookingReadDTO; // Import nếu cần
import com.example.sep490_mobile.data.dto.booking.DailyBookingDTO;
import com.example.sep490_mobile.data.dto.booking.MonthlyBookingDTO;
import com.example.sep490_mobile.data.dto.booking.MonthlyBookingReadDTO; // Import nếu cần
import com.example.sep490_mobile.ui.booking.BookingHistoryFragmentDirections;

import java.text.DecimalFormat;
import java.text.ParseException; // Import ParseException
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone; // Import TimeZone
import java.util.stream.Collectors;

public class BookingHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = "BookingHistoryAdapter";
    private static final int TYPE_DAILY = 0;
    private static final int TYPE_MONTHLY = 1;

    private final List<Object> items = new ArrayList<>();
    private final Context context;
    // Format ngày hiển thị dd/MM/yyyy
    private final SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    // Format đọc DateTime từ API (có timezone offset)
    private final SimpleDateFormat apiDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
    private final DecimalFormat priceFormat = new DecimalFormat("#,### 'đ'");

    public BookingHistoryAdapter(Context context) {
        this.context = context;
    }

    /**
     * Xóa dữ liệu cũ và thiết lập danh sách mới.
     * @param data Danh sách item mới
     */
    public void setData(List<?> data) {
        Log.d(TAG, "setData called with " + (data != null ? data.size() : "null") + " items.");
        int previousSize = items.size();
        items.clear();
        if (data != null && !data.isEmpty()) {
            items.addAll(data);
        }
        // Thông báo thay đổi
        if (previousSize == 0 && !items.isEmpty()) {
            notifyItemRangeInserted(0, items.size());
        } else if (previousSize > 0 && items.isEmpty()) {
            notifyItemRangeRemoved(0, previousSize);
        } else if (previousSize != items.size() || previousSize > 0) { // Update if size changed or was not empty
            notifyDataSetChanged(); // Use notifyDataSetChanged as a safe fallback for replacements
        }
        // If both were empty, do nothing
    }


    /**
     * Nối thêm dữ liệu vào cuối danh sách.
     * @param newData Danh sách item mới cần nối vào
     */
    public void appendData(List<?> newData) {
        if (newData == null || newData.isEmpty()) {
            Log.d(TAG, "appendData called with empty or null data.");
            return;
        }
        int startPosition = items.size();
        items.addAll(newData);
        Log.d(TAG, "appendData: Added " + newData.size() + " items. New total size: " + items.size() + ". Notifying range inserted from " + startPosition);
        notifyItemRangeInserted(startPosition, newData.size());
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < 0 || position >= items.size()) return -1;
        Object item = items.get(position);
        if (item instanceof DailyBookingDTO) return TYPE_DAILY;
        if (item instanceof MonthlyBookingDTO) return TYPE_MONTHLY;
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_DAILY) {
            View view = inflater.inflate(R.layout.item_booking_daily, parent, false);
            return new DailyViewHolder(view);
        } else if (viewType == TYPE_MONTHLY) {
            View view = inflater.inflate(R.layout.item_booking_monthly, parent, false);
            return new MonthlyViewHolder(view);
        }
        // Fallback for invalid type
        Log.e(TAG, "onCreateViewHolder invalid viewType: " + viewType);
        View emptyView = new View(parent.getContext());
        emptyView.setLayoutParams(new ViewGroup.LayoutParams(0,0));
        return new RecyclerView.ViewHolder(emptyView) {};
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);
        if (holder instanceof DailyViewHolder && item instanceof DailyBookingDTO) {
            ((DailyViewHolder) holder).bind((DailyBookingDTO) item);
        } else if (holder instanceof MonthlyViewHolder && item instanceof MonthlyBookingDTO) {
            ((MonthlyViewHolder) holder).bind((MonthlyBookingDTO) item);
        } else {
            Log.e(TAG, "Mismatched ViewHolder/Item type at position: " + position);
        }
    }

    // --- ViewHolders ---

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
            // Giữ kiểm tra null cơ bản
            if (dailyBooking == null || dailyBooking.getBooking() == null) {
                Log.w(TAG, "Attempting to bind DailyViewHolder with null data at pos " + getBindingAdapterPosition());
                // Set default texts to avoid showing old data
                tvStadiumName.setText("");
                tvBookingDate.setText("");
                tvSubCourts.setText("");
                tvBookingPrice.setText("");
                tvBookingStatus.setText("");
                itemView.setOnClickListener(null); // Disable click
                return;
            }

            BookingReadDTO booking = dailyBooking.getBooking();

            tvStadiumName.setText(booking.getStadiumName()); // Giả sử tên sân không bao giờ null từ ViewModel

            // Parse và format ngày
            Date bookingDateObj = booking.getDate();
            if (bookingDateObj != null) {
                // Chỉ cần format để hiển thị
                tvBookingDate.setText("Ngày: " + displayDateFormat.format(bookingDateObj));
            } else {
                Log.w(TAG, "Booking date object is null at position " + getBindingAdapterPosition());
                tvBookingDate.setText("Ngày: Không rõ");
            }


            Double price = booking.getTotalPrice();
            tvBookingPrice.setText("Tổng tiền: " + (price != null ? priceFormat.format(price) : "0 đ"));

            // Hiển thị tên sân con
            if (booking.getBookingDetails() != null && !booking.getBookingDetails().isEmpty()) {
                String courtNames = booking.getBookingDetails().stream()
                        .map(detail -> detail.getCourtName() != null ? detail.getCourtName() : ("ID:" + detail.getCourtId())) // Hiển thị ID nếu tên null
                        .filter(name -> name != null && !name.isEmpty())
                        .collect(Collectors.joining(", "));
                if (!courtNames.isEmpty()) {
                    tvSubCourts.setText("Sân con: " + courtNames);
                    tvSubCourts.setVisibility(View.VISIBLE);
                } else {
                    tvSubCourts.setVisibility(View.GONE);
                }
            } else {
                tvSubCourts.setVisibility(View.GONE);
            }

            setStatus(tvBookingStatus, booking.getStatus());

            itemView.setOnClickListener(v -> {
                if (dailyBooking != null) { // Kiểm tra lại trước khi navigate
                    try {
                        NavDirections action = BookingHistoryFragmentDirections.actionBookingHistoryFragmentToBookingHistoryDetailFragment(dailyBooking);
                        Navigation.findNavController(v).navigate(action);
                    } catch (Exception e) { // Bắt lỗi rộng hơn
                        Log.e(TAG, "Navigation failed for Daily item at pos " + getBindingAdapterPosition(), e);
                        Toast.makeText(context, "Không thể mở chi tiết", Toast.LENGTH_SHORT).show();
                    }
                }
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
            if (monthlyBooking == null || monthlyBooking.getMonthlyBooking() == null) {
                Log.w(TAG, "Attempting to bind MonthlyViewHolder with null data at pos " + getBindingAdapterPosition());
                tvStadiumName.setText("");
                tvBookingPeriod.setText("");
                tvTimeFrame.setText("");
                tvBookingPrice.setText("");
                tvBookingStatus.setText("");
                itemView.setOnClickListener(null);
                return;
            }

            MonthlyBookingReadDTO mb = monthlyBooking.getMonthlyBooking();

            tvStadiumName.setText(mb.getStadiumName()); // Giả sử tên sân không null từ ViewModel
            tvBookingPeriod.setText("Gói tháng " + mb.getMonth() + "/" + mb.getYear());

            Double price = mb.getTotalPrice();
            tvBookingPrice.setText("Tổng tiền: " + (price != null ? priceFormat.format(price) : "0 đ"));

            String startTime = parseTime(mb.getStartTime());
            String endTime = parseTime(mb.getEndTime());
            tvTimeFrame.setText("Khung giờ: " + startTime + " - " + endTime);

            setStatus(tvBookingStatus, mb.getStatus());

            itemView.setOnClickListener(v -> {
                if (monthlyBooking != null) { // Kiểm tra lại
                    try {
                        NavDirections action = BookingHistoryFragmentDirections.actionBookingHistoryFragmentToBookingHistoryDetailFragment(monthlyBooking);
                        Navigation.findNavController(v).navigate(action);
                    } catch (Exception e) {
                        Log.e(TAG, "Navigation failed for Monthly item at pos " + getBindingAdapterPosition(), e);
                        Toast.makeText(context, "Không thể mở chi tiết", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // --- Hàm tiện ích ---

    private void setStatus(TextView textView, String status) {
        if (textView == null) return;
        textView.setText(translateStatus(status));
        if (textView.getBackground() instanceof GradientDrawable) {
            GradientDrawable background = (GradientDrawable) textView.getBackground().mutate();
            int colorResId = getStatusColor(status); // Lấy ID màu
            try {
                int color = ContextCompat.getColor(context, colorResId);
                background.setColor(color);
            } catch (Exception e) {
                Log.e(TAG,"Error getting color resource " + colorResId + " for status " + status, e);
                // Set màu mặc định nếu lỗi
                background.setColor(ContextCompat.getColor(context, R.color.background_dark));
            }
        } else {
            Log.w(TAG, "Background is not GradientDrawable for status TextView");
        }
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

    // Trả về Resource ID của màu
    private int getStatusColor(String status) {
        if (status == null) return R.color.background_dark; // Màu mặc định
        switch (status.toLowerCase()) {
            case "accepted": return R.color.status_accepted;
            case "completed": return R.color.status_completed;
            case "cancelled": return R.color.status_cancelled;
            case "payfail": return R.color.status_payfail;
            default: return R.color.background_dark; // Pending, Waiting,... dùng màu này
        }
    }

    private String parseTime(String ptTime) {
        if (ptTime == null || !ptTime.startsWith("PT") || !ptTime.endsWith("H")) {
            return "N/A";
        }
        try {
            String hourString = ptTime.substring(2, ptTime.length() - 1);
            return hourString + ":00";
        } catch (IndexOutOfBoundsException e) {
            Log.e(TAG, "Error parsing PT time: " + ptTime, e);
            return "N/A";
        }
    }
}