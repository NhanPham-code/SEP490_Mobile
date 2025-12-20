package com.example.sep490_mobile.ui.home;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
// import android.widget.CheckBox; // <- Đã xóa
import android.widget.ImageButton;
// import android.widget.SeekBar; // <- Đã xóa
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.utils.removeVietnameseSigns;
import com.google.android.material.button.MaterialButton; // <- Đã thêm
import com.google.android.material.chip.Chip; // <- Đã thêm
import com.google.android.material.chip.ChipGroup; // <- Đã thêm
import com.google.android.material.slider.Slider; // <- Đã thêm
import com.google.android.material.textfield.TextInputEditText; // <- Đã thêm

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FilterFragment extends Fragment {
    // --- Đã cập nhật các kiểu dữ liệu ---
    private TextInputEditText address;
    private Slider priceSlider; // Thay thế cho SeekBar
    private TextView tvCurrentPrice;
    private TextInputEditText etStartTime; // Thay thế cho TextView
    private TextInputEditText etEndTime;   // Thay thế cho TextView
    private MaterialButton btnApplyFilters, btnClearAll, btnResetFilters;
    private ChipGroup sportChipGroup; // Thay thế cho CheckBox[]
    private boolean filterPrice = true;
    // ---

    private int price;
    private Map<String, String> odata = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter, container, false);
        SharedViewModel model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Ánh xạ View chính (đã cập nhật)
        address = view.findViewById(R.id.et_location_search);
        priceSlider = view.findViewById(R.id.price_slider); // Cập nhật ID
        tvCurrentPrice = view.findViewById(R.id.tv_current_price);
        btnApplyFilters = view.findViewById(R.id.btn_apply_filters);
        btnClearAll = view.findViewById(R.id.btn_clear_all); // Nút "Xóa Tất Cả" (trên)
        btnResetFilters = view.findViewById(R.id.btn_reset_filters); // Nút "ĐẶT LẠI" (dưới)
        ImageButton btnCloseFilter = view.findViewById(R.id.btn_close_filter);

        // Ánh xạ View thời gian (đã cập nhật)
        etStartTime = view.findViewById(R.id.et_start_time); // Cập nhật ID
        etEndTime = view.findViewById(R.id.et_end_time);   // Cập nhật ID

        etStartTime.setText(model.getStartTime().getValue());
        etEndTime.setText(model.getEndTime().getValue());

        // Thiết lập Listener cho ô chọn thời gian
        etStartTime.setOnClickListener(v -> showTimePickerDialog(etStartTime));
        etEndTime.setOnClickListener(v -> showTimePickerDialog(etEndTime));

        // Ánh xạ ChipGroup (đã cập nhật)
        sportChipGroup = view.findViewById(R.id.cg_sport_filter); // Cập nhật ID

        // Xử lý sự kiện nhấn nút Đóng
        if (btnCloseFilter != null) {
            btnCloseFilter.setOnClickListener(v -> {
                FragmentManager fragmentManager = getParentFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    if (getParentFragmentManager() != null) {
                        getParentFragmentManager().popBackStack("HomeFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }
                } else {
                    Toast.makeText(getContext(), "Không thể đóng Fragment", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Tải các lựa chọn đã có
        loadPreviousSportSelections(model.getSportType().getValue());
        setupPriceSlider(); // Cập nhật tên hàm
        setupActionButtons();
        setOldValue();

        return view;
    }

    // Đã cập nhật để dùng ChipGroup
    public void loadPreviousSportSelections(List<String> previouslySelectedIds) {
        if (previouslySelectedIds == null || previouslySelectedIds.isEmpty()) {
            return;
        }

        // Duyệt qua tất cả Chip con trong ChipGroup
        for (int i = 0; i < sportChipGroup.getChildCount(); i++) {
            View child = sportChipGroup.getChildAt(i);
            if (child instanceof Chip) {
                Chip chip = (Chip) child;
                String chipText = chip.getText().toString();

                // Kiểm tra xem text của Chip có trong danh sách đã lưu không
                boolean isChecked = previouslySelectedIds.contains(chipText);
                chip.setChecked(isChecked);
            }
        }
    }

    // Đã cập nhật để dùng Slider và TextInputEditText
    private void setOldValue() {
        SharedViewModel model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        etStartTime.setText(model.getStartTime().getValue());
        etEndTime.setText(model.getEndTime().getValue());
        address.setText(model.getAddress().getValue());

        try {
            // Đặt giá trị cho Slider
            int priceValue = Integer.parseInt(model.getPrice().getValue());
            int priceProgress = priceValue / 1000;
            priceSlider.setValue(priceProgress); // Dùng setValue cho Slider

            // Cập nhật text hiển thị giá
            DecimalFormat formatter = new DecimalFormat("#,###");
            tvCurrentPrice.setText("Giá tối đa: " + formatter.format(priceValue) + "đ/giờ");
            this.price = priceValue; // Lưu lại giá trị
        } catch (NumberFormatException e) {
            // Nếu model chưa có giá, đặt giá trị mặc định
            priceSlider.setValue(250f);
            tvCurrentPrice.setText("Giá tối đa: 250.000đ/giờ");
            this.price = 250000;
        }
    }

    /**
     * Hàm hiển thị TimePickerDialog (Đã cập nhật để dùng TextInputEditText)
     * @param targetEditText EditText cần cập nhật kết quả
     */
    private void showTimePickerDialog(final TextInputEditText targetEditText) {
        final Calendar c = Calendar.getInstance();
        int initialHour = c.get(Calendar.HOUR_OF_DAY);
        int initialMinute = c.get(Calendar.MINUTE);

        String currentText = (targetEditText.getText() != null) ? targetEditText.getText().toString() : "";

        if (currentText.matches("\\d{2}:\\d{2}")) {
            try {
                String[] parts = currentText.split(":");
                initialHour = Integer.parseInt(parts[0]);
                initialMinute = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, hourOfDay, minute) -> {
                    String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    targetEditText.setText(selectedTime);
                },
                initialHour,
                initialMinute,
                true // 24-hour format
        );
        timePickerDialog.show();
    }

    // Đã cập nhật để dùng ChipGroup và TextInputEditText
    private void setBtnApplyFilters() {
        SharedViewModel model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        FragmentManager fragmentManager = getParentFragmentManager();
        String filter = "";
        String conjunction = "";

        // 1. Address Filter (Dùng address.getText())
        if (address.getText() != null && address.getText().length() > 0) {
            filter += "contains(AddressUnsigned, '" + removeVietnameseSigns.removeVietnameseSigns(address.getText().toString()) + "')";
            conjunction = " and ";
            model.setAddress(address.getText().toString());
        } else {
            model.setAddress("");
        }

        // 2. Sport Type Filter(s) (Dùng ChipGroup)
        List<String> sportTypesOData = new ArrayList<>(); // Cho OData query
        List<String> sportTypesModel = new ArrayList<>(); // Cho ViewModel

        List<Integer> checkedChipIds = sportChipGroup.getCheckedChipIds();

        for (int chipId : checkedChipIds) {
            Chip chip = sportChipGroup.findViewById(chipId);
            if (chip != null) {
                String sportName = chip.getText().toString();
                sportTypesModel.add(sportName); // Thêm tên vào list cho ViewModel
                String condition = String.format("c/SportType eq '%s'", sportName);
                sportTypesOData.add(condition); // Thêm điều kiện vào list cho OData
            }
        }

        // Cập nhật ViewModel (1 lần duy nhất sau vòng lặp)
        model.setSportType(sportTypesModel);

        if (!sportTypesOData.isEmpty()) {
            String typeFilter = String.join(" or ", sportTypesOData);
            filter += conjunction + "Courts/any(c: (" + typeFilter + "))";
            conjunction = " and ";
        }
        // Kết thúc Sport Type Filter

        // 3. Price Filter (biến 'price' đã được cập nhật bởi Slider)
        if (price >= 0 && filterPrice) {
            filter += conjunction + "Courts/any(c: c/PricePerHour le " + price + ")";
            conjunction = " and ";
            model.setPrice(price + "");
        } else {
            model.setPrice("250000");
        }

        // 4. Time Filter (Dùng etStartTime và etEndTime)
        String startTime = (etStartTime.getText() != null) ? etStartTime.getText().toString() : "";
        String endTime = (etEndTime.getText() != null) ? etEndTime.getText().toString() : "";

        if (!startTime.isEmpty() && !endTime.isEmpty()) {
            model.setStartTime(startTime);
            model.setEndTime(endTime);

            String[] startParts = startTime.split(":");
            int startHour = Integer.parseInt(startParts[0]);
            int startMinute = Integer.parseInt(startParts[1]);

            String[] endParts = endTime.split(":");
            int endHour = Integer.parseInt(endParts[0]);
            int endMinute = Integer.parseInt(endParts[1]);

            String startDuration = String.format(Locale.ROOT, "duration'PT%dH%dM'", startHour, startMinute);
            String endDuration = String.format(Locale.ROOT, "duration'PT%dH%dM'", endHour, endMinute);

            filter += conjunction + "OpenTime le " + startDuration + " and CloseTime ge " + endDuration;
        } else {
            model.setStartTime("");
            model.setEndTime("");
        }

        odata.put("$filter", filter);
        System.out.println("filter: " + filter);
        if (fragmentManager.getBackStackEntryCount() > 0) {
            model.select(odata);
            Bundle result = new Bundle();
            result.putBoolean("refresh", true);

            // 2. GỬI TÍN HIỆU
            requireActivity().getSupportFragmentManager().setFragmentResult("HOME_FILTER_REQUEST_KEY", result);

            // 3. ĐÓNG FRAGMENT HIỆN TẠI
            requireActivity().getSupportFragmentManager().popBackStack("HomeFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);

        } else {
            Toast.makeText(getContext(), "Không thể đóng Fragment", Toast.LENGTH_SHORT).show();
        }
    }

    // Thiết lập Slider (Khoảng Giá) - Đã cập nhật
    private void setupPriceSlider() {
        // Listener cho Slider (thay thế cho OnSeekBarChangeListener)
        priceSlider.addOnChangeListener((slider, value, fromUser) -> {
            // 'value' là giá trị float (ví dụ: 250.0)
            int progress = (int) value;
            int priceValue = progress * 1000;
            this.price = priceValue; // Cập nhật biến 'price' của class

            DecimalFormat formatter = new DecimalFormat("#,###");
            tvCurrentPrice.setText("Giá tối đa: " + formatter.format(priceValue) + "đ/giờ");
        });

        // Thiết lập giá trị ban đầu (Slider dùng float)
        priceSlider.setValue(250f);
        this.price = 250 * 1000; // Đồng bộ giá trị ban đầu
    }

    // Thiết lập các nút hành động (Đã cập nhật)
    private void setupActionButtons() {
        btnApplyFilters.setOnClickListener(v -> {
            // Hàm collectFilterData bây giờ chỉ gọi setBtnApplyFilters
            collectFilterData();
        });

        // Tạo một hàm reset chung
        View.OnClickListener resetListener = v -> {
            // Đặt lại Slider về giá trị tối đa (ví dụ: 500)
            priceSlider.setValue(priceSlider.getValueTo() / 2);
            // Cập nhật Text
            int maxPriceValue = ((int) priceSlider.getValueTo() / 2) * 1000;
            this.price = maxPriceValue;
            DecimalFormat formatter = new DecimalFormat("#,###");
            tvCurrentPrice.setText("Giá tối đa: " + formatter.format(maxPriceValue) + "đ/giờ");

            // Đặt lại ChipGroup (cách đơn giản hơn)
            sportChipGroup.clearCheck();

            // Đặt lại thời gian
            etStartTime.setText("");
            etEndTime.setText("");

            // Đặt lại địa điểm
            address.setText(" ");
            filterPrice = false;

            Toast.makeText(getContext(), "Đã xóa tất cả bộ lọc", Toast.LENGTH_SHORT).show();
        };

        // Gán listener cho cả hai nút "Xóa" và "Đặt lại"
        btnClearAll.setOnClickListener(resetListener);
        btnResetFilters.setOnClickListener(resetListener);
    }

    // Hàm thu thập dữ liệu bộ lọc (Không cần thay đổi nhiều)
    private void collectFilterData() {
        // Hàm này giờ chỉ còn nhiệm vụ gọi hàm xử lý chính
        setBtnApplyFilters();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}