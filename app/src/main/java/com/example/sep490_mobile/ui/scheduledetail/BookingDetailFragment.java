package com.example.sep490_mobile.ui.scheduledetail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.PrivateUserProfileDTO;
import com.example.sep490_mobile.data.dto.ScheduleBookingDTO;
import com.example.sep490_mobile.data.dto.ScheduleBookingDetailDTO;
import com.example.sep490_mobile.ui.scheduledetail.BookingDetailViewModel;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BookingDetailFragment extends Fragment {

    private BookingDetailViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Khởi tạo ViewModel
        viewModel = new ViewModelProvider(this).get(BookingDetailViewModel.class);
        // Inflate layout
        return inflater.inflate(R.layout.fragment_show_booking_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Lấy dữ liệu booking đã được truyền qua
        if (getArguments() == null) return;
        ScheduleBookingDTO booking = BookingDetailFragmentArgs.fromBundle(getArguments()).getBooking();

        // 1. Hiển thị thông tin hóa đơn từ dữ liệu đã có
        populateInvoiceDetails(view, booking);

        // 2. Thiết lập observers và bắt đầu tải thông tin người dùng
        setupUserObservers(view);
        viewModel.loadUserInfo();
    }

    // Trong file BookingDetailFragment.java

    private void setupUserObservers(View view) {
        TextView tvUserName = view.findViewById(R.id.tv_detail_user_name);
        TextView tvUserPhone = view.findViewById(R.id.tv_detail_user_phone);
        TextView tvUserEmail = view.findViewById(R.id.tv_detail_user_email);

        viewModel.getUserProfile().observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null) {
                // Chỉ gán giá trị, không gán lại mô tả
                tvUserName.setText(userProfile.getFullName());

                // Kiểm tra null cho số điện thoại
                String phoneNumber = userProfile.getPhoneNumber();
                tvUserPhone.setText(phoneNumber != null ? phoneNumber : "Chưa cập nhật");

                tvUserEmail.setText(userProfile.getEmail());
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateInvoiceDetails(View view, ScheduleBookingDTO booking) {
        // Ánh xạ các View cho hóa đơn
        TextView tvStadiumName = view.findViewById(R.id.tv_detail_stadium_name);
        LinearLayout containerDetailRows = view.findViewById(R.id.container_detail_rows);
        Button btnBack = view.findViewById(R.id.btn_back_to_schedule);

        TextView tvOriginalPrice = view.findViewById(R.id.tv_detail_original_price);
        TextView tvTotalPrice = view.findViewById(R.id.tv_detail_total_price);
        RelativeLayout layoutDiscount = view.findViewById(R.id.layout_discount);
        TextView tvDiscount = view.findViewById(R.id.tv_detail_discount);

        // Gán dữ liệu hóa đơn
        tvStadiumName.setText("Sân vận động: " + booking.getStadiumName());
        tvOriginalPrice.setText(formatCurrency(booking.getOriginalPrice()));
        tvTotalPrice.setText(formatCurrency(booking.getTotalPrice()));

        if (booking.getOriginalPrice() > booking.getTotalPrice()) {
            double discountAmount = booking.getOriginalPrice() - booking.getTotalPrice();
            tvDiscount.setText("- " + formatCurrency(discountAmount));
            layoutDiscount.setVisibility(View.VISIBLE);
        } else {
            layoutDiscount.setVisibility(View.GONE);
        }

        btnBack.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.popBackStack();
        });

        if (booking.getBookingDetails() != null) {
            LayoutInflater inflater = LayoutInflater.from(getContext());
            for (ScheduleBookingDetailDTO detail : booking.getBookingDetails()) {
                View rowView = inflater.inflate(R.layout.item_show_booking_detail_row, containerDetailRows, false);

                TextView tvCourtName = rowView.findViewById(R.id.tv_court_name_detail);
                TextView tvTimeRange = rowView.findViewById(R.id.tv_time_range_detail);
                TextView tvPricePerHour = rowView.findViewById(R.id.tv_price_per_hour_detail);
                TextView tvLineTotal = rowView.findViewById(R.id.tv_line_total_detail);

                long durationHours = calculateDurationInHours(detail.getStartTime(), detail.getEndTime());
                double lineTotal = durationHours * detail.getPricePerHour();

                tvCourtName.setText(detail.getCourtName());
                tvTimeRange.setText(String.format("%s - %s", formatTimeString(detail.getStartTime()), formatTimeString(detail.getEndTime())));
                tvPricePerHour.setText(formatCurrency(detail.getPricePerHour()));
                tvLineTotal.setText(formatCurrency(lineTotal));

                containerDetailRows.addView(rowView);
            }
        }
    }

    // --- CÁC HÀM TIỆN ÍCH ---

    private long calculateDurationInHours(String startTimeStr, String endTimeStr) {
        if (startTimeStr == null || endTimeStr == null) return 0;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            Date startTime = format.parse(startTimeStr);
            Date endTime = format.parse(endTimeStr);
            long diffInMillis = endTime.getTime() - startTime.getTime();
            long hours = TimeUnit.MILLISECONDS.toHours(diffInMillis);
            return hours == 0 ? 1 : hours; // Luôn tính ít nhất 1 giờ
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private String formatCurrency(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount);
    }

    private String formatTimeString(String dateString) {
        if (dateString == null) return "";
        try {
            // Thử parse định dạng có mili giây
            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS", Locale.US);
            Date date = sourceFormat.parse(dateString);
            SimpleDateFormat targetFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return targetFormat.format(date);
        } catch (ParseException e) {
            try {
                // Nếu thất bại, thử parse định dạng không có mili giây
                SimpleDateFormat sourceFormat2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                Date date = sourceFormat2.parse(dateString);
                SimpleDateFormat targetFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                return targetFormat.format(date);
            } catch (ParseException e2) {
                e2.printStackTrace();
                return "";
            }
        }
    }
}