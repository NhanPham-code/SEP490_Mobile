package com.example.sep490_mobile.ui.bookingdetail;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.discount.ReadDiscountDTO;
import com.example.sep490_mobile.data.dto.booking.BookingDetailViewModelDTO;
import com.example.sep490_mobile.data.dto.booking.BookingReadDTO;
import com.example.sep490_mobile.data.dto.booking.IBookingHistoryItem;
import com.example.sep490_mobile.data.dto.booking.MonthlyBookingDTO;
import com.example.sep490_mobile.databinding.FragmentBookingHistoryDetailBinding;
import com.example.sep490_mobile.ui.bookingdetail.BookingHistoryDetailViewModel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class BookingHistoryDetailFragment extends Fragment {

    private FragmentBookingHistoryDetailBinding binding;
    private BookingHistoryDetailViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookingHistoryDetailBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(BookingHistoryDetailViewModel.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupObservers(); // BẮT ĐẦU LẮNG NGHE

        if (getArguments() != null) {
            IBookingHistoryItem bookingItem = BookingHistoryDetailFragmentArgs.fromBundle(getArguments()).getBookingItem();
            populateInitialUI(bookingItem); // 1. HIỂN THỊ DỮ LIỆU CÓ SẴN
            viewModel.loadDiscountDetails(bookingItem.getDiscountId()); // 2. YÊU CẦU TẢI THÊM DỮ LIỆU
        }

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

    // Hiển thị ngay lập tức các thông tin đã được truyền qua
    private void populateInitialUI(IBookingHistoryItem item) {
        if (item == null || getContext() == null) return;

        binding.tvStadiumName.setText(item.getStadiumName());
        setStatus(binding.tvStatus, item.getStatus(), requireContext());

        DecimalFormat formatter = new DecimalFormat("#,### 'đ'");
        binding.tvOriginalPrice.setText(formatter.format(item.getOriginalPrice()));
        binding.tvTotalPrice.setText(formatter.format(item.getTotalPrice()));

        if (item instanceof MonthlyBookingDTO) {
            // ... code xử lý hiển thị cho lịch tháng ...
        } else {
            binding.cardMonthlyInfo.setVisibility(View.GONE);
        }

        // Hiển thị danh sách chi tiết các buổi chơi
        binding.containerBookingItems.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        for (BookingReadDTO bookingDetail : item.getBookingItems()) {
            for (BookingDetailViewModelDTO detail : bookingDetail.getBookingDetails()) {
                View itemView = inflater.inflate(R.layout.item_booking_history_detail_row, binding.containerBookingItems, false);
                TextView tvCourtName = itemView.findViewById(R.id.tv_item_court_name);
                TextView tvDateTime = itemView.findViewById(R.id.tv_item_date_time);

                tvCourtName.setText(detail.getCourtName());
                String time = timeFormat.format(detail.getStartTime()) + " - " + timeFormat.format(detail.getEndTime());
                String date = dateFormat.format(bookingDetail.getDate());
                tvDateTime.setText(time + ", " + date);

                binding.containerBookingItems.addView(itemView);
            }
        }
    }

    // Lắng nghe các thay đổi từ ViewModel
    private void setupObservers() {
        viewModel.getDiscountDetails().observe(getViewLifecycleOwner(), discount -> {
            // Khi có dữ liệu discount trả về (hoặc là null), cập nhật UI
            updateDiscountUI(discount);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Cập nhật phần giao diện giảm giá
    private void updateDiscountUI(ReadDiscountDTO discount) {
        if (discount != null) {
            binding.layoutDiscount.setVisibility(View.VISIBLE);
            binding.tvDiscountInfo.setText("Giảm giá (" + discount.getCode() + ")");

            double savedAmount = getOriginalPriceValue() - getTotalPriceValue();
            DecimalFormat formatter = new DecimalFormat("#,### 'đ'");
            binding.tvDiscountAmount.setText("- " + formatter.format(savedAmount));
        } else {
            binding.layoutDiscount.setVisibility(View.GONE);
        }
    }

    // --- Các hàm tiện ích ---
    private double getOriginalPriceValue() {
        try { return Double.parseDouble(binding.tvOriginalPrice.getText().toString().replaceAll("[^\\d]", "")); } catch (Exception e) { return 0; }
    }
    private double getTotalPriceValue() {
        try { return Double.parseDouble(binding.tvTotalPrice.getText().toString().replaceAll("[^\\d]", "")); } catch (Exception e) { return 0; }
    }

    private void setStatus(TextView textView, String status, Context context) {
        textView.setText(translateStatus(status));
        int colorResId;
        switch (status.toLowerCase()) {
            case "accepted": colorResId = R.color.status_accepted; break;
            case "completed": colorResId = R.color.status_completed; break;
            case "cancelled": colorResId = R.color.status_cancelled; break;
            case "payfail": colorResId = R.color.status_payfail; break;
            default: colorResId = R.color.background_dark; break;
        }
        textView.setBackgroundColor(ContextCompat.getColor(context, colorResId));
    }

    private String translateStatus(String status) {
        if (status == null) return "Không rõ";
        switch (status.toLowerCase()) {
            case "accepted": return "ĐÃ NHẬN";
            case "completed": return "HOÀN THÀNH";
            case "cancelled": return "ĐÃ HỦY";
            case "payfail": return "THANH TOÁN LỖI";
            case "pending": return "CHỜ XỬ LÝ";
            case "waiting": return "CHỜ THANH TOÁN";
            default: return status.toUpperCase();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}