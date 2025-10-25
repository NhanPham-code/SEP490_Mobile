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
import com.example.sep490_mobile.viewmodel.BookingViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Collectors;

public class MonthlyCheckoutFragment extends Fragment {

    private BookingViewModel bookingViewModel;
    private Bundle bookingArgs; // Lưu lại arguments để dùng cho nút "Hoàn tất"

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookingArgs = getArguments();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.monthly_fragment_checkout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        if (bookingArgs != null) {
            populateData(view, bookingArgs);
        }

        bookingViewModel.fetchUserProfile();
        observeViewModel(view);
        setupClickListeners(view);
    }

    private void setupClickListeners(View view) {
        MaterialButton btnBack = view.findViewById(R.id.btn_back);
        MaterialButton btnCompleteBooking = view.findViewById(R.id.btn_complete_booking);

        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        btnCompleteBooking.setOnClickListener(v -> {
            if (bookingArgs != null) {
                // Lấy stadiumId từ arguments và truyền cho ViewModel
                // Lưu ý: Tên argument phải khớp với file mobile_navigation.xml
                int stadiumIdFromNav = MonthlyCheckoutFragmentArgs.fromBundle(getArguments()).getStadiumId();
                bookingArgs.putInt("stadiumId", stadiumIdFromNav);

                bookingViewModel.createMonthlyBooking(bookingArgs);
            } else {
                Toast.makeText(getContext(), "Lỗi: Không có dữ liệu đặt sân.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeViewModel(View view) {
        final TextInputEditText etFullName = view.findViewById(R.id.et_full_name);
        final TextInputEditText etPhoneNumber = view.findViewById(R.id.et_phone_number);
        final TextInputEditText etEmail = view.findViewById(R.id.et_email);
        final ProgressBar progressBar = view.findViewById(R.id.user_info_progress_bar);
        final MaterialButton btnCompleteBooking = view.findViewById(R.id.btn_complete_booking);

        bookingViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null) {
                etFullName.setText(userProfile.getFullName());
                etPhoneNumber.setText(userProfile.getPhoneNumber());
                etEmail.setText(userProfile.getEmail());
            }
        });

        bookingViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnCompleteBooking.setEnabled(!isLoading);
        });

        bookingViewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        bookingViewModel.bookingSuccess.observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess != null && isSuccess) {
                NavHostFragment.findNavController(this).navigate(R.id.navigation_home);
                bookingViewModel.onBookingSuccessNavigated();
            }
        });
    }

    private void populateData(View view, Bundle args) {
        // (Hàm này giữ nguyên như bạn đã viết)
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        View rowStadium = view.findViewById(R.id.row_stadium_name);
        ((TextView) rowStadium.findViewById(R.id.tv_label)).setText("Sân vận động:");
        ((TextView) rowStadium.findViewById(R.id.tv_value)).setText(args.getString("STADIUM_NAME", "N/A"));

        View rowCourts = view.findViewById(R.id.row_court_names);
        ((TextView) rowCourts.findViewById(R.id.tv_label)).setText("Các sân đã chọn:");
        String[] courtNames = args.getStringArray("COURT_NAMES");
        if (courtNames != null) {
            ((TextView) rowCourts.findViewById(R.id.tv_value)).setText(String.join(", ", courtNames));
        }

        View rowTime = view.findViewById(R.id.row_time_range);
        ((TextView) rowTime.findViewById(R.id.tv_label)).setText("Khung giờ mỗi ngày:");
        ((TextView) rowTime.findViewById(R.id.tv_value)).setText(String.format(Locale.US, "%02d:00 - %02d:00", args.getInt("START_TIME"), args.getInt("END_TIME")));

        View rowMonthYear = view.findViewById(R.id.row_month_year);
        ((TextView) rowMonthYear.findViewById(R.id.tv_label)).setText("Thời gian:");
        ((TextView) rowMonthYear.findViewById(R.id.tv_value)).setText(String.format(Locale.US, "Tháng %d / %d", args.getInt("MONTH"), args.getInt("YEAR")));

        View rowDays = view.findViewById(R.id.row_selected_days);
        ((TextView) rowDays.findViewById(R.id.tv_label)).setText("Các ngày đã chọn:");
        String[] bookableDateStrings = args.getStringArray("BOOKABLE_DATES");
        if (bookableDateStrings != null) {
            ArrayList<Integer> days = new ArrayList<>();
            for(String dateStr : bookableDateStrings) {
                days.add(LocalDate.parse(dateStr).getDayOfMonth());
            }
            Collections.sort(days);
            ((TextView) rowDays.findViewById(R.id.tv_value)).setText(days.stream().map(String::valueOf).collect(Collectors.joining(", ")));
        }

        float totalPrice = args.getFloat("TOTAL_PRICE", 0f);
        View rowPrice = view.findViewById(R.id.row_original_price);
        ((TextView) rowPrice.findViewById(R.id.tv_label)).setText("Giá gốc:");
        ((TextView) rowPrice.findViewById(R.id.tv_value)).setText(currencyFormat.format(totalPrice));

        ((TextView) view.findViewById(R.id.tv_total_price_value)).setText(currencyFormat.format(totalPrice));
    }
}