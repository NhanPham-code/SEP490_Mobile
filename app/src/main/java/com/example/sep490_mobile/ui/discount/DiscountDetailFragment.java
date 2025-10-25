package com.example.sep490_mobile.ui.discount;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.discount.ReadDiscountDTO; // Điều chỉnh đường dẫn nếu cần
import com.example.sep490_mobile.databinding.FragmentDiscountDetailBinding; // Import lớp Binding
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class DiscountDetailFragment extends Fragment {

    // Tag để ghi log
    private static final String TAG = "DiscountDetailFrag_Log";

    // Biến binding để truy cập view
    private FragmentDiscountDetailBinding binding;
    // Biến lưu dữ liệu discount nhận được
    private ReadDiscountDTO discountData;
    // Các đối tượng format ngày tháng và tiền tệ
    private final SimpleDateFormat inputDateFormat = createInputDateFormat();
    private final SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final DecimalFormat currencyFormat = new DecimalFormat("#,### 'đ'");

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        binding = FragmentDiscountDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");

        // Lấy dữ liệu discount được truyền qua Arguments
        if (getArguments() != null) {
            discountData = DiscountDetailFragmentArgs.fromBundle(getArguments()).getDiscount();
            Log.d(TAG, "Đã nhận dữ liệu discount: " + (discountData != null ? discountData.getCode() : "null"));
            populateUI(); // Hiển thị dữ liệu lên giao diện
        } else {
            Log.w(TAG, "Không nhận được Arguments!");
        }

        // Xử lý sự kiện click nút quay lại
        binding.btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Nhấn nút Quay lại");
            NavHostFragment.findNavController(this).popBackStack();
        });
    }

    // Hàm gán dữ liệu từ `discountData` vào các View trên giao diện
    private void populateUI() {
        if (discountData == null || getContext() == null) {
            Log.e(TAG, "populateUI: Dữ liệu discount hoặc Context bị null!");
            return; // Không làm gì nếu không có dữ liệu hoặc context
        }

        Log.d(TAG, "Bắt đầu gán dữ liệu vào UI...");
        binding.tvDiscountCode.setText(discountData.getCode());
        binding.tvDiscountDescription.setText(discountData.getDescription());

        // Hiển thị loại mã
        String codeTypeDisplay = "unique".equalsIgnoreCase(discountData.getCodeType()) ? "(Mã cá nhân)" : "(Mã theo sân)";
        binding.tvCodeType.setText(codeTypeDisplay);

        // Hiển thị giá trị giảm %
        String valueText = String.format(Locale.US, "%.0f%%", discountData.getPercentValue());
        binding.tvValue.setText(valueText);

        // Hiển thị số tiền giảm tối đa
        binding.tvMaxAmount.setText(currencyFormat.format(discountData.getMaxDiscountAmount()));

        // Hiển thị giá trị đơn hàng tối thiểu
        binding.tvMinAmount.setText(currencyFormat.format(discountData.getMinOrderAmount()));

        // Hiển thị khoảng thời gian áp dụng
        try {
            Date startDate = inputDateFormat.parse(discountData.getStartDate());
            Date endDate = inputDateFormat.parse(discountData.getEndDate());
            binding.tvDateRange.setText(outputDateFormat.format(startDate) + " - " + outputDateFormat.format(endDate));
        } catch (ParseException | NullPointerException e) {
            Log.e(TAG, "Lỗi parse ngày bắt đầu/kết thúc", e);
            binding.tvDateRange.setText("Không xác định");
        }

        // Hiển thị tình trạng (còn/hết hiệu lực) và đổi màu chữ
        if (discountData.isActive()) {
            binding.tvStatus.setText("Còn hiệu lực");
            binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.success_green));
        } else {
            binding.tvStatus.setText("Hết hiệu lực");
            binding.tvStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.error_red));
        }

        // Hiển thị danh sách sân áp dụng
        List<String> stadiumNames = discountData.getStadiumNames();
        Log.d(TAG, "Tên sân áp dụng: " + stadiumNames);
        if (stadiumNames != null && !stadiumNames.isEmpty()) {
            binding.tvStadiumsLabel.setVisibility(View.VISIBLE);
            binding.tvApplicableStadiums.setVisibility(View.VISIBLE);
            // Tạo chuỗi danh sách sân, mỗi sân một dòng có gạch đầu dòng
            String stadiumsText = stadiumNames.stream()
                    .map(name -> "- " + name)
                    .collect(Collectors.joining("\n"));
            binding.tvApplicableStadiums.setText(stadiumsText);
        } else {
            // Ẩn nếu không có sân nào
            binding.tvStadiumsLabel.setVisibility(View.GONE);
            binding.tvApplicableStadiums.setVisibility(View.GONE);
        }
        Log.d(TAG, "Gán dữ liệu vào UI hoàn tất.");
    }

    // Hàm tiện ích để tạo đối tượng SimpleDateFormat đọc ngày từ API
    private SimpleDateFormat createInputDateFormat() {
        // Định dạng ngày API trả về (Ví dụ: 2025-10-31T23:59:59+07:00)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
        // Quan trọng: Nếu API trả về múi giờ khác, cần setTimeZone phù hợp ở đây
        // Ví dụ: sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        binding = null; // Giải phóng binding để tránh rò rỉ bộ nhớ
    }
}