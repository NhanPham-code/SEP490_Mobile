package com.example.sep490_mobile.ui.booking;

import android.app.AlertDialog; // <<< THÊM
import android.content.Intent; // <<< THÊM
import android.net.Uri; // <<< THÊM
import android.os.Bundle;
import android.util.Log; // <<< THÊM
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// import android.widget.ProgressBar; // <<< BỎ (Dùng binding)
// import android.widget.TextView; // <<< BỎ (Dùng binding)
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.example.sep490_mobile.R;
// <<< THÊM Imports >>>
import com.example.sep490_mobile.adapter.DiscountDialogAdapter;
import com.example.sep490_mobile.data.dto.BookingReadDto;
import com.example.sep490_mobile.data.dto.discount.ReadDiscountDTO;
import com.example.sep490_mobile.databinding.FragmentCheckoutTimeZoneBinding; // <<< THÊM ViewBinding
import com.example.sep490_mobile.utils.VnPayUtils;
// <<< END THÊM Imports >>>
import com.example.sep490_mobile.viewmodel.BookingViewModel;
// import com.google.android.material.appbar.MaterialToolbar; // <<< BỎ (Dùng binding)
// import com.google.android.material.button.MaterialButton; // <<< BỎ (Dùng binding)
// import com.google.android.material.textfield.TextInputEditText; // <<< BỎ (Dùng binding)

// <<< THÊM Imports >>>
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
// <<< END THÊM Imports >>>

public class CheckoutTimeZoneFragment extends Fragment {

    private static final String TAG = "CheckoutTimeZone_Log"; // <<< THÊM TAG

    // <<< THÊM ViewBinding >>>
    private FragmentCheckoutTimeZoneBinding binding;

    // Argument variables (Giữ nguyên)
    private int stadiumId;
    // private String stadiumName; // <<< BỎ (Lấy từ binding)
    // private int[] courtIds; // <<< BỎ (Lấy từ args)
    // private String[] courtNames; // <<< BỎ (Lấy từ args)
    // private String dateString; // <<< BỎ (Lấy từ args)
    // private int startTime; // <<< BỎ (Lấy từ args)
    // private int endTime; // <<< BỎ (Lấy từ args)
    // private float totalPrice; // <<< BỎ (Dùng original/final)

    // View variables (BỎ HẾT, DÙNG BINDING)

    // ViewModel (Giữ nguyên)
    private BookingViewModel bookingViewModel;
    private Bundle fragmentArgs;

    // <<< THÊM State cho giá và discount >>>
    private double originalTotalPrice;
    private double finalTotalPrice;
    private ReadDiscountDTO selectedDiscount = null;

    // <<< THÊM Formatters >>>
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    // private final DecimalFormat percentFormat = new DecimalFormat("#.#"); // <<< BỎ (Không dùng)

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        if (getArguments() != null) {
            fragmentArgs = getArguments(); // <-- Lưu toàn bộ bundle
            // <<< THÊM: Lấy giá và stadiumId sớm >>>
            stadiumId = CheckoutTimeZoneFragmentArgs.fromBundle(fragmentArgs).getStadiumId(); // Lấy từ SafeArgs nếu dùng
            originalTotalPrice = fragmentArgs.getFloat("totalPrice", 0f);
            finalTotalPrice = originalTotalPrice; // Khởi tạo
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // <<< SỬA: Dùng ViewBinding >>>
        binding = FragmentCheckoutTimeZoneBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ views (BỎ, vì dùng binding)
        // setupViews(view); <<< BỎ

        // Gán dữ liệu tĩnh (từ bundle) lên Views
        bindDataToViews(); // Sửa hàm này dùng binding

        // Cài đặt Listeners
        setupListeners(); // Sửa hàm này dùng binding

        // Cài đặt Observers để lấy dữ liệu động (từ API)
        observeViewModel(); // Sửa hàm này dùng binding

        // Gọi API để lấy thông tin user
        bookingViewModel.fetchUserProfile();
    }

    // <<< BỎ hàm setupViews >>>

    // <<< SỬA: Dùng binding và thêm giá gốc/cuối >>>
    private void bindDataToViews() {
        if (fragmentArgs == null) return;

        // Truy cập view bên trong include qua binding.ID_INCLUDE.ID_VIEW
        binding.rowStadiumName.tvLabel.setText("Sân vận động:");
        binding.rowStadiumName.tvValue.setText(fragmentArgs.getString("stadiumName"));

        binding.rowCourtNames.tvLabel.setText("Sân:");
        binding.rowCourtNames.tvValue.setText(String.join(", ", fragmentArgs.getStringArray("courtNames")));

        binding.rowBookingDate.tvLabel.setText("Ngày đặt:");
        binding.rowBookingDate.tvValue.setText(formatDate(fragmentArgs.getString("date")));

        binding.rowTimeRange.tvLabel.setText("Thời gian:");
        int start = fragmentArgs.getInt("startTime");
        int end = fragmentArgs.getInt("endTime");
        binding.rowTimeRange.tvValue.setText(String.format(Locale.getDefault(), "%02d:00 - %02d:00", start, end));

        binding.rowDuration.tvLabel.setText("Tổng số giờ:");
        binding.rowDuration.tvValue.setText(String.format(Locale.getDefault(), "%d giờ", (end - start)));

        // Hiển thị giá gốc và giá cuối
        binding.tvOriginalPrice.setText(currencyFormatter.format(originalTotalPrice));
        binding.tvTotalPrice.setText(currencyFormatter.format(finalTotalPrice));
    }

    // <<< SỬA: Dùng binding và thêm observer discount/bookingResult >>>
    private void observeViewModel() {
        // Lắng nghe trạng thái loading (Sửa dùng binding)
        bookingViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            // <<< THÊM: Disable nút khi loading >>>
            binding.btnConfirmBooking.setEnabled(!isLoading);
            binding.btnBack.setEnabled(!isLoading);
        });

        // Lắng nghe dữ liệu user profile (Sửa dùng binding)
        bookingViewModel.userProfile.observe(getViewLifecycleOwner(), userProfile -> {
            if (userProfile != null) {
                binding.etFullName.setText(userProfile.getFullName());
                binding.etPhoneNumber.setText(userProfile.getPhoneNumber());
                binding.etEmail.setText(userProfile.getEmail());
            }
        });

        // Lắng nghe lỗi
        bookingViewModel.error.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "ViewModel Error: " + error);
                // <<< THÊM: Enable nút khi lỗi >>>
                binding.progressBar.setVisibility(View.GONE);
                binding.btnConfirmBooking.setEnabled(true);
                binding.btnBack.setEnabled(true);
                binding.btnSelectDiscount.setEnabled(true);
            }
        });

        // <<< THÊM: Lắng nghe applicable discounts >>>
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

        // <<< SỬA: Lắng nghe bookingResult để mở VNPay >>>
        bookingViewModel.bookingResult.observe(getViewLifecycleOwner(), bookingReadDto -> {
            if (bookingReadDto != null) {
                // 1. TẠO BOOKING 'WAITING' THÀNH CÔNG
                Log.d(TAG, "Booking (waiting) created successfully with ID: " + bookingReadDto.getId());
                Toast.makeText(getContext(), "Đang chuyển tới VNPay...", Toast.LENGTH_SHORT).show();

                // 2. Tạo URL VNPay
                String bookingIdStr = String.valueOf(bookingReadDto.getId());
                long amount = (long) finalTotalPrice;

                // === LOGIC QUAN TRỌNG: Đặt type là "Booking" ===
                String orderInfo = "Booking:" + bookingIdStr;
                // ===============================================

                String paymentUrl = VnPayUtils.createPaymentUrl(bookingIdStr, amount, orderInfo);
                Log.d(TAG, "Generated VNPay URL: " + paymentUrl);

                // 3. Mở Trình duyệt
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Log.e(TAG, "Could not launch browser for VNPay", e);
                    Toast.makeText(getContext(), "Không thể mở cổng thanh toán.", Toast.LENGTH_LONG).show();
                    binding.progressBar.setVisibility(View.GONE);
                    binding.btnConfirmBooking.setEnabled(true);
                    binding.btnBack.setEnabled(true);
                }

                // 4. Reset LiveData và Thoát
                bookingViewModel.clearBookingResult(); // Reset cờ
                NavHostFragment.findNavController(this).popBackStack(R.id.navigation_home, false);
            }
        });
    }

    // <<< SỬA: Dùng binding và thêm logic discount/VNPay >>>
    private void setupListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        // Nút quay lại
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());

        // <<< SỬA: Nút chọn discount >>>
        binding.btnSelectDiscount.setOnClickListener(v -> {
            Log.d(TAG, "'Select Discount' button clicked.");
            binding.btnSelectDiscount.setEnabled(false); // Disable tạm thời
            bookingViewModel.fetchApplicableDiscounts(stadiumId); // Trigger fetch
        });

        // <<< SỬA: Nút xác nhận đặt sân >>>
        binding.btnConfirmBooking.setOnClickListener(v -> {
            if (fragmentArgs != null) {
                // Cập nhật lại fragmentArgs với giá và discount
                fragmentArgs.putFloat("ORIGINAL_PRICE", (float) originalTotalPrice);
                fragmentArgs.putFloat("FINAL_PRICE", (float) finalTotalPrice);
                if (selectedDiscount != null) {
                    fragmentArgs.putInt("DISCOUNT_ID", selectedDiscount.getId());
                }

                Log.d(TAG, "Calling createDailyBooking in ViewModel...");
                // Gọi hàm createDailyBooking (đã được sửa trong ViewModel)
                bookingViewModel.createDailyBooking(fragmentArgs);
            } else {
                Toast.makeText(getContext(), "Lỗi: Không có dữ liệu đặt sân", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // (Giữ nguyên)
    private String formatDate(String isoDate) {
        try {
            LocalDate date = LocalDate.parse(isoDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return date.format(formatter);
        } catch (Exception e) { return isoDate; }
    }

    // (Bỏ hàm formatCurrency vì dùng NumberFormat trực tiếp)


    // === CÁC HÀM LOGIC DISCOUNT (COPY TỪ CHECKOUTFRAGMENT) ===

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
        binding.tvSelectedDiscountCode.setText("Chưa có mã giảm giá nào được chọn"); // Reset text
        binding.tvSelectedDiscountCode.setTextColor(getResources().getColor(R.color.gray_600)); // Reset color
        binding.layoutDiscountAmount.setVisibility(View.GONE);
        binding.tvDiscountAmount.setText("");
        binding.tvTotalPrice.setText(currencyFormatter.format(finalTotalPrice));
        Log.d(TAG, "Removed discount. Price reset to: " + finalTotalPrice);
    }

    // <<< THÊM: onDestroyView để giải phóng binding >>>
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}