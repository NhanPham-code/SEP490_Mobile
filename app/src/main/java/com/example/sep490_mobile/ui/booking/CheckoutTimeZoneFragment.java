package com.example.sep490_mobile.ui.booking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.viewmodel.BookingViewModel; // <--- Import ViewModel
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CheckoutTimeZoneFragment extends Fragment {

    // Argument variables
    private int stadiumId;
    private String stadiumName;
    private int[] courtIds;
    private String[] courtNames;
    private String dateString;
    private int startTime;
    private int endTime;
    private float totalPrice;

    // View variables
    private MaterialToolbar toolbar;
    private TextView tvStadiumName, tvCourtNames, tvBookingDate, tvTimeRange, tvDuration, tvTotalPrice;
    private TextInputEditText etFullName, etPhoneNumber, etEmail;
    private MaterialButton btnSelectDiscount, btnConfirmBooking, btnBack;
    private ProgressBar progressBar;

    // ViewModel
    private BookingViewModel bookingViewModel; // <--- Thêm ViewModel
    private Bundle fragmentArgs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        if (getArguments() != null) {
            fragmentArgs = getArguments(); // <-- Lưu toàn bộ bundle
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout
        return inflater.inflate(R.layout.fragment_checkout_time_zone, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ views
        setupViews(view);

        // Gán dữ liệu tĩnh (từ bundle) lên Views
        bindDataToViews();

        // Cài đặt Listeners
        setupListeners();

        // Cài đặt Observers để lấy dữ liệu động (từ API)
        observeViewModel();

        // Gọi API để lấy thông tin user
        bookingViewModel.fetchUserProfile();
    }

    private void setupViews(View view) {
        toolbar = view.findViewById(R.id.toolbar);

        // Hàng Sân vận động
        View rowStadiumName = view.findViewById(R.id.row_stadium_name);
        ((TextView) rowStadiumName.findViewById(R.id.tv_label)).setText("Sân vận động:");
        tvStadiumName = rowStadiumName.findViewById(R.id.tv_value);

        // Hàng Sân
        View rowCourtNames = view.findViewById(R.id.row_court_names);
        ((TextView) rowCourtNames.findViewById(R.id.tv_label)).setText("Sân:");
        tvCourtNames = rowCourtNames.findViewById(R.id.tv_value);

        // Hàng Ngày đặt
        View rowBookingDate = view.findViewById(R.id.row_booking_date);
        ((TextView) rowBookingDate.findViewById(R.id.tv_label)).setText("Ngày đặt:");
        tvBookingDate = rowBookingDate.findViewById(R.id.tv_value);

        // Hàng Thời gian
        View rowTimeRange = view.findViewById(R.id.row_time_range);
        ((TextView) rowTimeRange.findViewById(R.id.tv_label)).setText("Thời gian:");
        tvTimeRange = rowTimeRange.findViewById(R.id.tv_value);

        // Hàng Tổng số giờ
        View rowDuration = view.findViewById(R.id.row_duration);
        ((TextView) rowDuration.findViewById(R.id.tv_label)).setText("Tổng số giờ:");
        tvDuration = rowDuration.findViewById(R.id.tv_value);

        // Tổng cộng
        tvTotalPrice = view.findViewById(R.id.tv_total_price);

        // Input fields
        etFullName = view.findViewById(R.id.et_full_name);
        etPhoneNumber = view.findViewById(R.id.et_phone_number);
        etEmail = view.findViewById(R.id.et_email);

        // Buttons
        btnSelectDiscount = view.findViewById(R.id.btn_select_discount);
        btnConfirmBooking = view.findViewById(R.id.btn_confirm_booking);
        btnBack = view.findViewById(R.id.btn_back);

        // Progress Bar
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void bindDataToViews() {
        if (fragmentArgs == null) return;

        // Lấy dữ liệu từ bundle đã lưu
        tvStadiumName.setText(fragmentArgs.getString("stadiumName"));
        tvCourtNames.setText(String.join(", ", fragmentArgs.getStringArray("courtNames")));
        tvBookingDate.setText(formatDate(fragmentArgs.getString("date")));
        int start = fragmentArgs.getInt("startTime");
        int end = fragmentArgs.getInt("endTime");
        tvTimeRange.setText(String.format(Locale.getDefault(), "%02d:00 - %02d:00", start, end));
        tvDuration.setText(String.format(Locale.getDefault(), "%d giờ", (end - start)));
        tvTotalPrice.setText(formatCurrency(fragmentArgs.getFloat("totalPrice")));
    }

    private void observeViewModel() {
        // Lắng nghe trạng thái loading
        bookingViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Lắng nghe dữ liệu user profile
        bookingViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null) {
                etFullName.setText(userProfile.getFullName());
                etPhoneNumber.setText(userProfile.getPhoneNumber());
                etEmail.setText(userProfile.getEmail());
            }
        });

        // Lắng nghe lỗi
        bookingViewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        bookingViewModel.bookingResult.observe(getViewLifecycleOwner(), bookingReadDto -> {
            if (bookingReadDto != null) {
                Toast.makeText(getContext(), "Đặt sân thành công! Mã: " + bookingReadDto.getId(), Toast.LENGTH_LONG).show();
                // (Chuyển sang màn hình VNPay hoặc màn hình thành công ở đây)
                // Tạm thời quay về trang chủ
                NavHostFragment.findNavController(this).navigate(R.id.navigation_home);
            }
        });
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        // Nút quay lại
        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        btnSelectDiscount.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng chọn mã giảm giá", Toast.LENGTH_SHORT).show();
        });

        // Nút xác nhận đặt sân
        btnConfirmBooking.setOnClickListener(v -> {
            if (fragmentArgs != null) {
                // Gọi hàm mới trong ViewModel
                bookingViewModel.createDailyBooking(fragmentArgs);
            } else {
                Toast.makeText(getContext(), "Lỗi: Không có dữ liệu đặt sân", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String formatDate(String isoDate) {
        try {
            LocalDate date = LocalDate.parse(isoDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return date.format(formatter);
        } catch (Exception e) { return isoDate; }
    }

    private String formatCurrency(float amount) {
        Locale vietnameseLocale = new Locale("vi", "VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(vietnameseLocale);
        return currencyFormatter.format(amount);
    }
}