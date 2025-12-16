package com.example.sep490_mobile.ui.booking;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Cần cho include layout
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.sep490_mobile.R;
// THÊM: Import Adapter
import com.example.sep490_mobile.adapter.DiscountDialogAdapter;
// THÊM: Import DTO
import com.example.sep490_mobile.data.dto.BookingReadDto;
import com.example.sep490_mobile.data.dto.discount.ReadDiscountDTO;
// THÊM: Import ViewBinding
import com.example.sep490_mobile.databinding.MonthlyFragmentCheckoutBinding;
// THÊM: Import Utils
import com.example.sep490_mobile.utils.VnPayUtils;
import com.example.sep490_mobile.viewmodel.BookingViewModel;

// THÊM: Import Java utils
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class MonthlyCheckoutFragment extends Fragment {

    private static final String TAG = "MonthlyCheckout_Log";

    private MonthlyFragmentCheckoutBinding binding; // Sửa: Dùng ViewBinding
    private BookingViewModel bookingViewModel;
    private Bundle bookingArgs; // Lưu lại arguments để dùng cho nút "Hoàn tất"

    // THÊM: State cho giá và discount
    private int stadiumId;
    private double originalTotalPrice;
    private double finalTotalPrice;
    private ReadDiscountDTO selectedDiscount = null;

    // THÊM: Formatters
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    // private final DecimalFormat percentFormat = new DecimalFormat("#.#"); // <<< SỬA: XÓA BIẾN KHÔNG DÙNG

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy args sớm để lấy stadiumId
        if (getArguments() != null) {
            bookingArgs = getArguments();
            stadiumId = MonthlyCheckoutFragmentArgs.fromBundle(bookingArgs).getStadiumId();
            originalTotalPrice = bookingArgs.getFloat("TOTAL_PRICE", 0f);
            finalTotalPrice = originalTotalPrice; // Khởi tạo
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Sửa: Dùng ViewBinding
        binding = MonthlyFragmentCheckoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        if (bookingArgs != null) {
            populateData(bookingArgs); // Tải data vào UI
        }

        bookingViewModel.fetchUserProfile(); // Tải thông tin user
        observeViewModel(); // Cài đặt các observer
        setupClickListeners(); // Cài đặt các nút bấm
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        // THÊM: Click listener cho nút "Chọn mã"
        binding.btnSelectDiscount.setOnClickListener(v -> {
            Log.d(TAG, "'Select Discount' button clicked.");
            binding.btnSelectDiscount.setEnabled(false); // Disable tạm thời
            bookingViewModel.fetchApplicableDiscounts(stadiumId); // Trigger fetch
        });

        // SỬA: Click listener cho nút "Hoàn tất"
        binding.btnCompleteBooking.setOnClickListener(v -> {
            if (bookingArgs != null) {
                // Cập nhật lại bookingArgs với giá và discount
                bookingArgs.putFloat("ORIGINAL_PRICE", (float) originalTotalPrice);
                bookingArgs.putFloat("FINAL_PRICE", (float) finalTotalPrice);
                if (selectedDiscount != null) {
                    bookingArgs.putInt("DISCOUNT_ID", selectedDiscount.getId());
                }

                Log.d(TAG, "Calling createMonthlyBooking in ViewModel...");
                bookingViewModel.createMonthlyBooking(bookingArgs);
            } else {
                Toast.makeText(getContext(), "Lỗi: Không có dữ liệu đặt sân.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeViewModel() {
        // Observe User Profile
        bookingViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            binding.userInfoProgressBar.setVisibility(View.GONE);
            binding.userInfoLayout.setVisibility(View.VISIBLE);
            if (userProfile != null) {
                binding.etFullName.setText(userProfile.getFullName());
                binding.etPhoneNumber.setText(userProfile.getPhoneNumber());
                binding.etEmail.setText(userProfile.getEmail());
            }
        });

        // Observe Loading (cho nút "Hoàn tất")
        bookingViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBarBooking.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnCompleteBooking.setEnabled(!isLoading);
            binding.btnBack.setEnabled(!isLoading);
        });

        // Observe Error
        bookingViewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "ViewModel Error: " + error);
                // Bật lại các nút nếu có lỗi
                binding.progressBarBooking.setVisibility(View.GONE);
                binding.btnCompleteBooking.setEnabled(true);
                binding.btnBack.setEnabled(true);
                binding.btnSelectDiscount.setEnabled(true);
            }
        });

        // Observe Discount
        bookingViewModel.applicableDiscounts.observe(getViewLifecycleOwner(), discounts -> {
            if (discounts == null && binding.btnSelectDiscount.isEnabled()) {
                return; // Bỏ qua state null ban đầu
            }
            binding.btnSelectDiscount.setEnabled(true);

            if (discounts == null) {
                Log.w(TAG, "Discount list is null after fetch.");
            } else if (discounts.isEmpty()) {
                Toast.makeText(getContext(), "Không có mã giảm giá nào phù hợp.", Toast.LENGTH_SHORT).show();
                if (selectedDiscount != null) { removeDiscount(); }
                binding.tvSelectedDiscountCode.setText("Không có mã phù hợp");
                binding.tvSelectedDiscountCode.setTextColor(getResources().getColor(R.color.gray_600));
            } else {
                showDiscountSelectionDialog(discounts);
            }
        });

        // Observe Booking Result (thay cho bookingSuccess) để mở VNPay
        bookingViewModel.bookingResult.observe(getViewLifecycleOwner(), bookingReadDto -> {
            if (bookingReadDto != null) {
                // 1. TẠO MONTHLY BOOKING 'WAITING' THÀNH CÔNG
                Log.d(TAG, "MonthlyBooking (waiting) created successfully with ID: " + bookingReadDto.getId());
                Toast.makeText(getContext(), "Đang chuyển tới VNPay...", Toast.LENGTH_SHORT).show();

                // 2. Tạo URL VNPay
                String monthlyBookingIdStr = String.valueOf(bookingReadDto.getId());
                long amount = (long) finalTotalPrice;

                // === LOGIC QUAN TRỌNG: Đặt type là "MonthlyBooking" ===
                String orderInfo = "MonthlyBooking:" + monthlyBookingIdStr;

                String paymentUrl = VnPayUtils.createPaymentUrl(monthlyBookingIdStr, amount, orderInfo);
                Log.d(TAG, "Generated VNPay URL: " + paymentUrl);

                // 3. Mở Trình duyệt
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Log.e(TAG, "Could not launch browser for VNPay", e);
                    Toast.makeText(getContext(), "Không thể mở cổng thanh toán.", Toast.LENGTH_LONG).show();
                    binding.progressBarBooking.setVisibility(View.GONE);
                    binding.btnCompleteBooking.setEnabled(true);
                    binding.btnBack.setEnabled(true);
                }

                // 4. Reset LiveData và Thoát
                bookingViewModel.onMonthlyBookingNavigated(); // Reset cờ
                NavHostFragment.findNavController(this).popBackStack(R.id.navigation_home, false);
            }
        });
    }


    private void populateData(Bundle args) {
        // Truy cập trực tiếp vào tvLabel và tvValue của binding đã được include

        binding.rowStadiumName.tvLabel.setText("Sân vận động:");
        binding.rowStadiumName.tvValue.setText(args.getString("STADIUM_NAME", "N/A"));

        binding.rowCourtNames.tvLabel.setText("Các sân đã chọn:");
        String[] courtNames = args.getStringArray("COURT_NAMES");
        if (courtNames != null) {
            binding.rowCourtNames.tvValue.setText(String.join(", ", courtNames));
        }

        binding.rowTimeRange.tvLabel.setText("Khung giờ mỗi ngày:");
        binding.rowTimeRange.tvValue.setText(String.format(Locale.US, "%02d:00 - %02d:00", args.getInt("START_TIME"), args.getInt("END_TIME")));

        binding.rowMonthYear.tvLabel.setText("Thời gian:");
        binding.rowMonthYear.tvValue.setText(String.format(Locale.US, "Tháng %d / %d", args.getInt("MONTH"), args.getInt("YEAR")));

        binding.rowSelectedDays.tvLabel.setText("Các ngày đã chọn:");
        String[] bookableDateStrings = args.getStringArray("BOOKABLE_DATES");
        if (bookableDateStrings != null) {
            ArrayList<Integer> days = new ArrayList<>();
            for(String dateStr : bookableDateStrings) {
                days.add(LocalDate.parse(dateStr).getDayOfMonth());
            }
            Collections.sort(days);
            binding.rowSelectedDays.tvValue.setText(days.stream().map(String::valueOf).collect(Collectors.joining(", ")));
        }

        binding.rowOriginalPrice.tvLabel.setText("Giá gốc:");
        binding.rowOriginalPrice.tvValue.setText(currencyFormatter.format(originalTotalPrice));

        binding.tvTotalPrice.setText(currencyFormatter.format(finalTotalPrice));
    }




    private void showDiscountSelectionDialog(List<ReadDiscountDTO> discountsToShow) {
        List<ReadDiscountDTO> itemsForAdapter = new ArrayList<>();
        itemsForAdapter.add(null); // "Không sử dụng"
        itemsForAdapter.addAll(discountsToShow);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chọn mã giảm giá");

        DiscountDialogAdapter adapter = new DiscountDialogAdapter(requireContext(), itemsForAdapter);

        builder.setAdapter(adapter, (dialog, which) -> {
            ReadDiscountDTO selectedItem = adapter.getItem(which);
            if (selectedItem == null) {
                removeDiscount();
            } else {
                applyDiscount(selectedItem);
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    private void applyDiscount(ReadDiscountDTO discount) {
        if (discount == null) {
            removeDiscount();
            return;
        }

        // 1. Kiểm tra đơn tối thiểu
        if (originalTotalPrice < discount.getMinOrderAmount()) {
            Toast.makeText(getContext(), String.format("Đơn tối thiểu %s", currencyFormatter.format(discount.getMinOrderAmount())), Toast.LENGTH_LONG).show();
            removeDiscount();
            return;
        }

        // 2. Tính số tiền giảm theo phần trăm
        double discountValue = originalTotalPrice * (discount.getPercentValue() / 100.0);

        // 3. Logic giới hạn số tiền giảm (Max Amount)
        double maxDiscount = discount.getMaxDiscountAmount();

        // Nếu maxDiscount > 0: Có giới hạn trần -> Cần kiểm tra để cắt giảm
        // Nếu maxDiscount == 0: Không giới hạn -> Bỏ qua if này (giữ nguyên discountValue tính theo %)
        if (maxDiscount > 0) {
            if (discountValue > maxDiscount) {
                discountValue = maxDiscount;
            }
        }
        // Nếu muốn chắc chắn hơn, bạn có thể thêm else if (maxDiscount == 0) để log lại,
        // nhưng về mặt logic thì để trống như trên là đã đúng ý bạn rồi.

        // 4. Tính tổng tiền cuối cùng
        finalTotalPrice = originalTotalPrice - discountValue;

        // Cập nhật UI
        selectedDiscount = discount;
        binding.tvSelectedDiscountCode.setText(discount.getCode());
        binding.tvSelectedDiscountCode.setTextColor(getResources().getColor(R.color.ocean_blue));

        binding.layoutDiscountAmount.setVisibility(View.VISIBLE);
        binding.tvDiscountAmount.setText(String.format("- %s", currencyFormatter.format(discountValue)));

        binding.tvTotalPrice.setText(currencyFormatter.format(finalTotalPrice));

        Log.d(TAG, "Applied discount: " + discount.getCode() + ", Value: " + discountValue + ", MaxCap: " + maxDiscount);
    }

    private void removeDiscount() {
        selectedDiscount = null;
        finalTotalPrice = originalTotalPrice;
        binding.tvSelectedDiscountCode.setText("Chưa chọn mã"); // Reset text
        binding.tvSelectedDiscountCode.setTextColor(getResources().getColor(R.color.gray_600)); // Reset color
        binding.layoutDiscountAmount.setVisibility(View.GONE);
        binding.tvDiscountAmount.setText("");
        binding.tvTotalPrice.setText(currencyFormatter.format(finalTotalPrice));
        Log.d(TAG, "Removed discount. Price reset to: " + finalTotalPrice);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Release ViewBinding
    }
}