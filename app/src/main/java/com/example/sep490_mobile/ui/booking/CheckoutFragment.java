package com.example.sep490_mobile.ui.booking;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter; // Keep for the dialog structure
import android.widget.ProgressBar;
import android.widget.TextView; // Keep for custom adapter override
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.adapter.CheckoutAdapter;
// Import Custom Discount Adapter
import com.example.sep490_mobile.adapter.DiscountDialogAdapter; // <<< THÊM IMPORT
// Import DTOs
import com.example.sep490_mobile.data.dto.BookingCreateDto;
import com.example.sep490_mobile.data.dto.BookingDetailCreateDto;
import com.example.sep490_mobile.data.dto.CourtsDTO;
import com.example.sep490_mobile.data.dto.PrivateUserProfileDTO;
import com.example.sep490_mobile.data.dto.SelectedCourtInfo;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.data.dto.discount.ReadDiscountDTO;
// ViewBinding
import com.example.sep490_mobile.databinding.FragmentCheckoutBinding;
// ViewModels
import com.example.sep490_mobile.viewmodel.BookingViewModel;
import com.example.sep490_mobile.viewmodel.StadiumViewModel;

// Java Utilities
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import android.content.Intent;
import android.net.Uri;
import com.example.sep490_mobile.utils.VnPayUtils;

public class CheckoutFragment extends Fragment {

    private static final String TAG = "CheckoutFragment_Log";

    private FragmentCheckoutBinding binding;
    private StadiumViewModel stadiumViewModel;
    private CheckoutAdapter checkoutAdapter;
    private BookingViewModel bookingViewModel;

    // Arguments
    private int stadiumId;
    private String date;
    private float originalTotalPrice;
    private SelectedCourtInfo[] selectedCourts;

    // Discount State
    private ReadDiscountDTO selectedDiscount = null;
    private double finalTotalPrice;

    // Formatters
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final DecimalFormat percentFormat = new DecimalFormat("#.#"); // For percentages

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCheckoutBinding.inflate(inflater, container, false);
        stadiumViewModel = new ViewModelProvider(this).get(StadiumViewModel.class);
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");

        retrieveArguments();
        setupObservers();
        setupClickListeners();

        // Fetch initial data
        fetchStadiumDetails();
        fetchUserProfile();
        // Discounts fetched on click
    }

    private void retrieveArguments() {
        if (getArguments() != null) {
            stadiumId = CheckoutFragmentArgs.fromBundle(getArguments()).getStadiumId();
            date = CheckoutFragmentArgs.fromBundle(getArguments()).getDate();
            originalTotalPrice = CheckoutFragmentArgs.fromBundle(getArguments()).getTotalPrice();
            selectedCourts = CheckoutFragmentArgs.fromBundle(getArguments()).getSelectedCourts();
            finalTotalPrice = originalTotalPrice;
            Log.d(TAG, "Arguments retrieved: stadiumId=" + stadiumId + ", date=" + date + ", originalPrice=" + originalTotalPrice);
        } else {
            Log.e(TAG, "Arguments are null!");
            Toast.makeText(getContext(), "Lỗi: Thiếu dữ liệu đặt sân.", Toast.LENGTH_LONG).show();
            NavHostFragment.findNavController(this).popBackStack();
        }
    }

    private void setupObservers() {
        // Observer for stadium details
        stadiumViewModel.stadiums.observe(getViewLifecycleOwner(), response -> {
            if (response != null && !response.getItems().isEmpty()) {
                StadiumDTO stadium = response.getItems().get(0);
                binding.tvStadiumName.setText(stadium.getName());
                setupRecyclerView(stadium.courts != null ? new ArrayList<>(stadium.courts) : new ArrayList<>());
            } else {
                Log.w(TAG, "Stadium details response empty/null.");
                binding.tvStadiumName.setText("Không rõ tên sân");
                setupRecyclerView(new ArrayList<>());
            }
        });

        // Observer for user profile
        bookingViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null) {
                populateUserInfo(userProfile);
            } else {
                Log.w(TAG, "User profile is null.");
            }
        });

        // Observer for general errors (Handles fetch errors too)
        bookingViewModel.error.observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show(); // <<< Lỗi thực sự sẽ hiện ở đây
                Log.e(TAG, "ViewModel Error: " + errorMsg);
                binding.progressBarBooking.setVisibility(View.GONE); // Ẩn loading chính nếu có lỗi
                binding.btnCompleteBooking.setEnabled(true);
                binding.btnBack.setEnabled(true);
                binding.btnSelectDiscount.setEnabled(true); // Bật lại nút chọn mã khi có lỗi
            }
        });

        // Observer for booking creation loading state
        bookingViewModel.isLoadingBooking.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBarBooking.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnCompleteBooking.setEnabled(!isLoading);
            binding.btnBack.setEnabled(!isLoading);
        });

        // ================================================================
        // === BẮT ĐẦU THAY ĐỔI: Observer for booking success result ===
        // ================================================================
        bookingViewModel.bookingResult.observe(getViewLifecycleOwner(), bookingReadDto -> {
            if (bookingReadDto != null) {
                // 1. ĐẶT SÂN VỚI STATUS 'WAITING' THÀNH CÔNG
                Log.d(TAG, "Booking (waiting) created successfully with ID: " + bookingReadDto.getId());
                Toast.makeText(getContext(), "Đang chuyển tới VNPay...", Toast.LENGTH_SHORT).show();

                // 2. Tạo URL thanh toán VNPay
                String bookingIdStr = bookingReadDto.getId() + "";
                // VNPay yêu cầu số tiền là số nguyên (long), không nhận số thập phân
                long amount = (long) finalTotalPrice;
                String orderInfo = "Booking:" + bookingIdStr; // Gửi kèm type
                String paymentUrl = VnPayUtils.createPaymentUrl(bookingIdStr, amount, orderInfo);
                Log.d(TAG, "Generated VNPay URL: " + paymentUrl);

                // 3. Mở URL bằng trình duyệt
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Log.e(TAG, "Could not launch browser for VNPay", e);
                    Toast.makeText(getContext(), "Không thể mở cổng thanh toán. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                    // Nếu lỗi, bật lại nút
                    binding.progressBarBooking.setVisibility(View.GONE);
                    binding.btnCompleteBooking.setEnabled(true);
                    binding.btnBack.setEnabled(true);
                    // (Cân nhắc gọi API xóa/hủy booking 'waiting' ở đây nếu cần)
                }

                // 4. Xóa kết quả để không trigger lại
                bookingViewModel.clearBookingResult();

                // 5. Thoát khỏi màn Checkout sau khi đã mở trình duyệt thanh toán
                // User sẽ quay lại trang Home
                NavHostFragment.findNavController(this).popBackStack(R.id.navigation_home, false);
            }
        });
        // ================================================================
        // === KẾT THÚC THAY ĐỔI ===
        // ================================================================


        // Observer for applicable discounts (triggered after button click)
        bookingViewModel.applicableDiscounts.observe(getViewLifecycleOwner(), discounts -> {
            // Bỏ qua giá trị null ban đầu nếu nút vẫn đang enable (trạng thái chờ trước khi fetch)
            if (discounts == null && binding.btnSelectDiscount.isEnabled()) {
                Log.d(TAG, "Applicable discounts observer received null, likely initial state or waiting for fetch.");
                // Không làm gì cả, đợi kết quả thực sự hoặc lỗi từ _error LiveData
                return;
            }

            Log.d(TAG, "Applicable discounts received: " + (discounts != null ? discounts.size() : "null"));

            // Luôn bật lại nút chọn mã sau khi có kết quả (null, rỗng hoặc có list)
            binding.btnSelectDiscount.setEnabled(true);

            // Xử lý kết quả thực tế
            if (discounts == null) {
                // Trường hợp này ít xảy ra nếu _error hoạt động tốt, nhưng để phòng ngừa
                Log.w(TAG, "Discount list is null after fetch completed without error signal.");
            } else if (discounts.isEmpty()) {
                // Không tìm thấy mã
                Toast.makeText(getContext(), "Không có mã giảm giá nào phù hợp.", Toast.LENGTH_SHORT).show();
                if (selectedDiscount != null) { removeDiscount(); } // Xóa mã đang chọn nếu có
                binding.tvSelectedDiscountCode.setText("Không có mã phù hợp");
                binding.tvSelectedDiscountCode.setTextColor(getResources().getColor(R.color.gray_600));
            } else {
                // Tìm thấy mã -> Hiển thị dialog chọn
                showDiscountSelectionDialog(discounts);
            }
        });
    }

    private void fetchStadiumDetails() {
        Log.d(TAG, "Fetching stadium details for ID: " + stadiumId);
        Map<String, String> odataUrl = new HashMap<>();
        odataUrl.put("$expand", "Courts");
        odataUrl.put("$filter", "Id eq " + stadiumId);
        stadiumViewModel.fetchStadium(odataUrl);
    }

    private void fetchUserProfile() {
        Log.d(TAG, "Fetching user profile");
        bookingViewModel.fetchUserProfile();
    }

    private void setupRecyclerView(List<CourtsDTO> allCourtsInStadium) {
        if (selectedCourts == null) {
            Log.e(TAG, "selectedCourts is null in setupRecyclerView");
            binding.tvOriginalPrice.setText(currencyFormatter.format(0));
            binding.tvTotalPrice.setText(currencyFormatter.format(0));
            return;
        }
        binding.rvCheckoutDetails.setLayoutManager(new LinearLayoutManager(getContext()));
        checkoutAdapter = new CheckoutAdapter(Arrays.asList(selectedCourts), allCourtsInStadium);
        binding.rvCheckoutDetails.setAdapter(checkoutAdapter);
        binding.tvOriginalPrice.setText(currencyFormatter.format(originalTotalPrice));
        binding.tvTotalPrice.setText(currencyFormatter.format(originalTotalPrice));
    }

    private void populateUserInfo(PrivateUserProfileDTO user) {
        binding.etFullName.setText(user.getFullName());
        binding.etPhoneNumber.setText(user.getPhoneNumber());
        binding.etEmail.setText(user.getEmail());
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        // Listener for select discount button
        binding.btnSelectDiscount.setOnClickListener(v -> {
            Log.d(TAG, "'Select Discount' button clicked.");
            binding.btnSelectDiscount.setEnabled(false); // Disable temporarily
            bookingViewModel.fetchApplicableDiscounts(stadiumId); // Trigger fetch
        });

        // Listener for complete booking button
        binding.btnCompleteBooking.setOnClickListener(v -> {
            Log.d(TAG, "Complete Booking button clicked.");
            PrivateUserProfileDTO currentUser = bookingViewModel.userProfile.getValue();
            if (currentUser == null || currentUser.getUserId() == null) { Toast.makeText(getContext(), "Chưa tải được thông tin người dùng.", Toast.LENGTH_SHORT).show(); Log.w(TAG, "User profile/ID null."); return; }
            if (selectedCourts == null || selectedCourts.length == 0) { Toast.makeText(getContext(), "Lỗi: Không có sân/giờ.", Toast.LENGTH_SHORT).show(); Log.e(TAG, "selectedCourts null/empty."); return; }

            List<BookingDetailCreateDto> details = new ArrayList<>();
            String firstStartTimeStr = null; // To use for main booking time
            for (SelectedCourtInfo courtInfo : selectedCourts) {
                for (String timeRange : courtInfo.getTimes()) {
                    try {
                        String[] times = timeRange.split("-");
                        // Construct ISO-like string (ensure consistent format)
                        String startTimeStr = date + "T" + times[0] + ":00";
                        String endTimeStr = date + "T" + times[1] + ":00";
                        details.add(new BookingDetailCreateDto(courtInfo.getCourtId(), startTimeStr, endTimeStr));
                        if (firstStartTimeStr == null) firstStartTimeStr = startTimeStr; // Get the first start time
                    } catch (Exception e) { Log.e(TAG, "Error parsing time: " + timeRange, e); Toast.makeText(getContext(), "Lỗi định dạng giờ: " + timeRange, Toast.LENGTH_SHORT).show(); return; }
                }
            }
            if (details.isEmpty() || firstStartTimeStr == null) { Toast.makeText(getContext(), "Lỗi chi tiết đặt sân.", Toast.LENGTH_SHORT).show(); Log.e(TAG, "Details empty or firstStartTimeStr null."); return; }

            int userId; try { userId = Integer.parseInt(currentUser.getUserId()); } catch (NumberFormatException e) { Toast.makeText(getContext(), "Lỗi User ID.", Toast.LENGTH_SHORT).show(); Log.e(TAG, "Invalid User ID: " + currentUser.getUserId()); return; }
            Integer selectedDiscountId = (selectedDiscount != null) ? selectedDiscount.getId() : null;

            BookingCreateDto bookingRequest = new BookingCreateDto(
                    userId, "waiting", firstStartTimeStr, // Use first start time
                    finalTotalPrice, (double) originalTotalPrice,
                    "vnpay_100", selectedDiscountId, stadiumId, details
            );
            Log.d(TAG, "Calling createBooking in ViewModel...");
            bookingViewModel.createBooking(bookingRequest);
        });
    }


    // --- Show Discount Selection Dialog (Using Custom Adapter) ---
    private void showDiscountSelectionDialog(List<ReadDiscountDTO> discountsToShow) {

        // 1. Tạo danh sách DTO bao gồm cả lựa chọn "Không sử dụng" (dùng null)
        List<ReadDiscountDTO> itemsForAdapter = new ArrayList<>();
        itemsForAdapter.add(null); // Add null to represent "No discount"
        itemsForAdapter.addAll(discountsToShow);

        // 2. Hiển thị Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chọn mã giảm giá");

        // <<< SỬ DỤNG CUSTOM ADAPTER MÀ KHÔNG CẦN OVERRIDE getView >>>
        // Create adapter with the combined list
        DiscountDialogAdapter adapter = new DiscountDialogAdapter(requireContext(), itemsForAdapter);

        // Set adapter và listener như bình thường
        builder.setAdapter(adapter, (dialog, which) -> {
            ReadDiscountDTO selectedItem = adapter.getItem(which); // Get the DTO (or null)
            if (selectedItem == null) { // Position 0 ("Không sử dụng mã") was clicked
                removeDiscount();
            } else {
                applyDiscount(selectedItem);
            }
        });
        // <<< KẾT THÚC SỬA >>>

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }


    // --- Apply Discount Logic ---
    private void applyDiscount(ReadDiscountDTO discount) {
        if (discount == null) { removeDiscount(); return; }
        if (originalTotalPrice < discount.getMinOrderAmount()) { Toast.makeText(getContext(), String.format("Đơn tối thiểu %s", currencyFormatter.format(discount.getMinOrderAmount())), Toast.LENGTH_LONG).show(); removeDiscount(); return; }
        double discountValue = originalTotalPrice * (discount.getPercentValue() / 100.0);
        if (discount.getMaxDiscountAmount() > 0 && discountValue > discount.getMaxDiscountAmount()) {
            discountValue = discount.getMaxDiscountAmount();
        }
        finalTotalPrice = originalTotalPrice - discountValue;
        selectedDiscount = discount;
        binding.tvSelectedDiscountCode.setText(discount.getCode());
        binding.tvSelectedDiscountCode.setTextColor(getResources().getColor(R.color.ocean_blue));
        binding.layoutDiscountAmount.setVisibility(View.VISIBLE);
        binding.tvDiscountAmount.setText(String.format("- %s", currencyFormatter.format(discountValue)));
        binding.tvTotalPrice.setText(currencyFormatter.format(finalTotalPrice));
        Log.d(TAG, "Applied discount: " + discount.getCode() + ", Value: " + discountValue + ", Final Price: " + finalTotalPrice);
    }


    // --- Remove Discount Logic ---
    private void removeDiscount() {
        selectedDiscount = null;
        finalTotalPrice = originalTotalPrice;
        binding.tvSelectedDiscountCode.setText(""); // Clear selected code text
        binding.tvSelectedDiscountCode.setTextColor(getResources().getColor(R.color.ocean_blue));
        binding.layoutDiscountAmount.setVisibility(View.GONE);
        binding.tvDiscountAmount.setText("");
        binding.tvTotalPrice.setText(currencyFormatter.format(finalTotalPrice));
        Log.d(TAG, "Removed discount. Price reset to: " + finalTotalPrice);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Release ViewBinding reference
        Log.d(TAG, "onDestroyView");
    }
}