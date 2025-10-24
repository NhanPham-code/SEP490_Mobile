package com.example.sep490_mobile.ui.booking;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.adapter.CheckoutAdapter;
import com.example.sep490_mobile.data.dto.BookingCreateDto;
import com.example.sep490_mobile.data.dto.BookingDetailCreateDto;
import com.example.sep490_mobile.data.dto.CourtsDTO;
import com.example.sep490_mobile.data.dto.PrivateUserProfileDTO;
import com.example.sep490_mobile.data.dto.SelectedCourtInfo;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.databinding.FragmentCheckoutBinding;
import com.example.sep490_mobile.ui.booking.CheckoutFragmentArgs;
import com.example.sep490_mobile.viewmodel.BookingViewModel;
import com.example.sep490_mobile.viewmodel.StadiumViewModel;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class CheckoutFragment extends Fragment {

    private FragmentCheckoutBinding binding;
    private StadiumViewModel stadiumViewModel;
    private CheckoutAdapter checkoutAdapter;
    private BookingViewModel bookingViewModel;

    private int stadiumId;
    private String date;
    private float totalPrice;
    private SelectedCourtInfo[] selectedCourts;
    private ProgressBar progressBarBooking;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        stadiumViewModel = new ViewModelProvider(this).get(StadiumViewModel.class);
        // KHỞI TẠO BOOKINGVIEWMODEL
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBarBooking = view.findViewById(R.id.progressBarBooking);
        retrieveArguments();
        setupObservers();
        observeBookingResult();
        setupClickListeners();

        // Lấy dữ liệu phụ
        fetchStadiumDetails();
        fetchUserProfile();
    }

    private void retrieveArguments() {
        if (getArguments() != null) {
            stadiumId = CheckoutFragmentArgs.fromBundle(getArguments()).getStadiumId();
            date = CheckoutFragmentArgs.fromBundle(getArguments()).getDate();
            totalPrice = CheckoutFragmentArgs.fromBundle(getArguments()).getTotalPrice();
            selectedCourts = CheckoutFragmentArgs.fromBundle(getArguments()).getSelectedCourts();
        }
    }

    private void setupObservers() {
        // Lắng nghe dữ liệu sân (giữ nguyên)
        stadiumViewModel.stadiums.observe(getViewLifecycleOwner(), response -> {
            if (response != null && !response.getItems().isEmpty()) {
                StadiumDTO stadium = response.getItems().get(0);
                binding.tvStadiumName.setText(stadium.getName());
                if (stadium.courts != null) {
                    setupRecyclerView(new ArrayList<>(stadium.courts));
                }
            }
        });

        // THAY ĐỔI: Lắng nghe dữ liệu người dùng TỪ BOOKINGVIEWMODEL
        bookingViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null) {
                populateUserInfo(userProfile);
            }
        });

        // (Tùy chọn) Lắng nghe lỗi
        bookingViewModel.error.observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchStadiumDetails() {
        Map<String, String> odataUrl = new HashMap<>();
        odataUrl.put("$expand", "Courts");
        odataUrl.put("$filter", "Id eq " + stadiumId);
        stadiumViewModel.fetchStadium(odataUrl);
    }

    private void fetchUserProfile() {
        bookingViewModel.fetchUserProfile();
    }

    private void setupRecyclerView(List<CourtsDTO> allCourtsInStadium) {
        if (selectedCourts == null) return;

        binding.rvCheckoutDetails.setLayoutManager(new LinearLayoutManager(getContext()));
        checkoutAdapter = new CheckoutAdapter(Arrays.asList(selectedCourts), allCourtsInStadium);
        binding.rvCheckoutDetails.setAdapter(checkoutAdapter);

        // Cập nhật giá tiền
        NumberFormat currencyFormatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        binding.tvOriginalPrice.setText(currencyFormatter.format(totalPrice));
        binding.tvTotalPrice.setText(currencyFormatter.format(totalPrice));
    }

    private void populateUserInfo(PrivateUserProfileDTO user) {
        binding.etFullName.setText(user.getFullName());
        binding.etPhoneNumber.setText(user.getPhoneNumber());
        binding.etEmail.setText(user.getEmail());
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        binding.btnCompleteBooking.setOnClickListener(v -> {
            // Check if user profile is loaded
            PrivateUserProfileDTO currentUser = bookingViewModel.userProfile.getValue();
            if (currentUser == null) {
                Toast.makeText(getContext(), "Chưa tải được thông tin người dùng, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
                return;
            }

            // 1. Prepare BookingDetails
            List<BookingDetailCreateDto> details = new ArrayList<>();
            if (selectedCourts != null) {
                for (SelectedCourtInfo courtInfo : selectedCourts) {
                    for (String timeRange : courtInfo.getTimes()) {
                        try {
                            // Example timeRange: "08:00-10:00"
                            String[] times = timeRange.split("-");
                            String startTimeStr = date + "T" + times[0] + ":00"; // Combine date and time
                            String endTimeStr = date + "T" + times[1] + ":00";   // Combine date and time

                            // You might need more robust date/time parsing here if format varies
                            details.add(new BookingDetailCreateDto(
                                    courtInfo.getCourtId(),
                                    startTimeStr,
                                    endTimeStr
                            ));
                        } catch (Exception e) {
                            Log.e("Checkout", "Error parsing time range: " + timeRange, e);
                            Toast.makeText(getContext(), "Lỗi định dạng thời gian: " + timeRange, Toast.LENGTH_SHORT).show();
                            return; // Stop processing if time format is wrong
                        }
                    }
                }
            }

            if (details.isEmpty()) {
                Toast.makeText(getContext(), "Không có chi tiết đặt sân nào.", Toast.LENGTH_SHORT).show();
                return;
            }

            int userId = Integer.parseInt(currentUser.getUserId());

            // 2. Create BookingCreateDto object
            BookingCreateDto bookingRequest = new BookingCreateDto(
                    userId,      // Get user ID from profile
                    "waiting",                    // Initial status
                    date,                         // Date string "yyyy-MM-dd"
                    (double) totalPrice,          // Total price (cast to double)
                    (double) totalPrice,          // Original price (same for now)
                    "vnpay_100",                  // Payment method
                    null,                         // Discount ID (null for now)
                    stadiumId,                    // Stadium ID
                    details                       // List of details created above
            );

            // 3. Call ViewModel to create booking
            Log.d("API_CALL", "Attempting to create booking...");
            bookingViewModel.createBooking(bookingRequest);
        });
    }

    private void observeBookingResult() {
        bookingViewModel.isLoadingBooking.observe(getViewLifecycleOwner(), isLoading -> {
            // Show/hide progress bar and disable/enable button
            if (progressBarBooking != null) { // Check if progress bar exists
                progressBarBooking.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
            binding.btnCompleteBooking.setEnabled(!isLoading);
            binding.btnBack.setEnabled(!isLoading); // Disable back button too
        });

        bookingViewModel.bookingResult.observe(getViewLifecycleOwner(), bookingReadDto -> {
            if (bookingReadDto != null) {
                // Booking SUCCESS!
                Toast.makeText(getContext(), "Đặt sân thành công! ID: " + bookingReadDto.getId(), Toast.LENGTH_LONG).show();
                // TODO: Navigate to a success screen or back to history
                NavHostFragment.findNavController(this).popBackStack(R.id.navigation_home, false); // Example: Go back to home
            }
            // No need for 'else' here, error is handled by the _error LiveData
        });

        // Error observer is already in setupObservers, it will show booking errors too
    }
}