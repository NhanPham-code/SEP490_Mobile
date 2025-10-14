package com.example.sep490_mobile.ui.home;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.SeekBar;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FilterFragment extends Fragment {
    private TextView address;
    private SeekBar priceSeekBar;
    private TextView tvCurrentPrice;
    private TextView tvStartTime;
    private TextView tvEndTime;
    private Button btnApplyFilters, btnClearAll;
    private int price;

    private Map<String, String> odata = new HashMap<>();
    // Danh sách CheckBox để dễ dàng quản lý
    private CheckBox[] sportCheckBoxes;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Ánh xạ layout fragment_filter.xml
        View view = inflater.inflate(R.layout.fragment_filter, container, false);
        SharedViewModel model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Ánh xạ View chính
        address = view.findViewById(R.id.et_location_search);
        priceSeekBar = view.findViewById(R.id.price_seekbar);
        tvCurrentPrice = view.findViewById(R.id.tv_current_price);
        btnApplyFilters = view.findViewById(R.id.btn_apply_filters);
        btnClearAll = view.findViewById(R.id.btn_clear_all);
        ImageButton btnCloseFilter = view.findViewById(R.id.btn_close_filter);
// 1. Ánh xạ View từ XML
        tvStartTime = view.findViewById(R.id.tv_start_time);
        tvEndTime = view.findViewById(R.id.tv_end_time);

        tvStartTime.setText(model.getStartTime().getValue());
        tvEndTime.setText(model.getEndTime().getValue());

        // 2. Thiết lập Listener cho cả hai TextView
        tvStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(tvStartTime, tvStartTime.getText().toString());
            }
        });

        tvEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog(tvEndTime, tvEndTime.getText().toString());
            }
        });
        // Khởi tạo danh sách CheckBox (cho mục đích quản lý/reset)
        sportCheckBoxes = new CheckBox[]{
                view.findViewById(R.id.cb_bongda5),
                view.findViewById(R.id.cb_bongda7),
                view.findViewById(R.id.cb_bongda11),
                view.findViewById(R.id.cb_bongro),
                view.findViewById(R.id.cb_tennis),
                view.findViewById(R.id.cb_bongchuyen)
                // ... thêm các CheckBox khác ở đây
        };


        // 2. Xử lý sự kiện nhấn nút Đóng
        if (btnCloseFilter != null) {
            btnCloseFilter.setOnClickListener(v -> {
                // Lấy FragmentManager và quay lại Fragment trước đó trong Back Stack
                FragmentManager fragmentManager = getParentFragmentManager();
                if (fragmentManager.getBackStackEntryCount() > 0) {
                    fragmentManager.popBackStack();

                } else {
                    // Xử lý trường hợp không có gì trong back stack (hiếm)
                    Toast.makeText(getContext(), "Không thể đóng Fragment", Toast.LENGTH_SHORT).show();
                }
            });
        }
        loadPreviousSportSelections(model.getSportType().getValue());
        setupPriceSeekBar();
        setupActionButtons();
        setOldValue();

        return view;
    }

    public void loadPreviousSportSelections(List<String> previouslySelectedIds) {

        if (previouslySelectedIds == null || previouslySelectedIds.isEmpty()) {
            // Không có dữ liệu cũ, không cần làm gì
            return;
        }

        for (CheckBox checkBox : sportCheckBoxes) {
            // 1. Lấy ID của CheckBox hiện tại
            String idName = getResources().getResourceEntryName(checkBox.getId());

            // 2. Kiểm tra xem ID này có trong danh sách đã chọn trước đó không
            boolean isChecked = previouslySelectedIds.contains(checkBox.getText().toString());

            // 3. Thiết lập trạng thái
            checkBox.setChecked(isChecked);
        }
    }



    private void setOldValue(){
        SharedViewModel model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        tvStartTime.setText(model.getStartTime().getValue());
        tvEndTime.setText(model.getEndTime().getValue());
        address.setText(model.getAddress().getValue());
        Toast.makeText(this.getContext(), model.getPrice().getValue(), Toast.LENGTH_SHORT).show();
        int price = Integer.parseInt(model.getPrice().getValue()) / 1000;

        priceSeekBar.setProgress(price);

    }

    /**
     * Hàm hiển thị TimePickerDialog
     * @param targetTextView TextView cần cập nhật kết quả
     */
    private void showTimePickerDialog(final TextView targetTextView, String time) {
        System.out.println("showTimePickerDialog");

        // --- KHỞI TẠO GIÁ TRỊ MẶC ĐỊNH ---

        // Mặc định ban đầu là giờ hiện tại của hệ thống (phòng trường hợp parsing lỗi)
        final Calendar c = Calendar.getInstance();
        int initialHour = c.get(Calendar.HOUR_OF_DAY);
        int initialMinute = c.get(Calendar.MINUTE);

        // 1. Lấy giá trị hiện tại của TextView (ví dụ: "09:00" hoặc "15:00")
        String currentText = targetTextView.getText().toString();

        // 2. Kiểm tra nếu chuỗi có định dạng HH:mm hợp lệ, thì phân tích và sử dụng
        if (currentText.matches("\\d{2}:\\d{2}")) {
            try {
                String[] parts = currentText.split(":");
                // Cập nhật giá trị mặc định bằng giờ đã chọn trước đó
                initialHour = Integer.parseInt(parts[0]);
                initialMinute = Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                // Nếu có lỗi phân tích cú pháp, giữ nguyên giờ hệ thống mặc định
                e.printStackTrace();
            }
        }


        // Khởi tạo TimePickerDialog
        // LƯU Ý: Nếu code này nằm trong Fragment, hãy thay 'this.getContext()' bằng 'requireContext()'
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this.getContext(), // Context
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Xử lý khi người dùng chọn giờ xong
                        // Định dạng giờ: "HH:mm" (ví dụ: 08:05)
                        String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);

                        // Cập nhật TextView với giờ đã chọn
                        targetTextView.setText(selectedTime);
                    }
                },
                initialHour,   // Giờ mặc định (giờ cũ)
                initialMinute, // Phút mặc định (phút cũ)
                true           // Định dạng 24 giờ
        );

        Toast.makeText(this.getContext(), "Giờ đã chọn: " + targetTextView.getText(), Toast.LENGTH_SHORT).show();
        // Hiển thị hộp thoại
        timePickerDialog.show();
    }
    private void setBtnApplyFilters(){
        SharedViewModel model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
            FragmentManager fragmentManager = getParentFragmentManager();
        String filter = "";
        String conjunction = ""; // 👈 Start with an empty conjunction

// 1. Address Filter
        if(address.length() > 0){
            filter += "contains(AddressUnsigned, '" + removeVietnameseSigns.removeVietnameseSigns(address.getText().toString()) + "')";
            conjunction = " and "; // 👈 Set conjunction only after the first condition is added
            model.setAddress(address.getText().toString());
        }else{
            model.setAddress("");
        }

// 2. Sport Type Filter(s)
        if(sportCheckBoxes.length > 0){
            List<String> sportTypes = new ArrayList<>();

            for (CheckBox cb : sportCheckBoxes) {
                if (cb.isChecked()) {
                    String sportName = cb.getText().toString();

                    // 1. Xây dựng điều kiện OData chính xác và thêm vào List
                    // Cần có dấu nháy đơn '...' cho giá trị chuỗi trong OData
                    String condition = String.format("c/SportType eq '%s'", sportName);
                    sportTypes.add(condition);

                    // Cập nhật Model (Giữ nguyên logic của bạn)
                    // Lưu ý: List.of() chỉ có từ Java 9 trở lên
                     model.setSportType(List.of(sportName));
                }
            }

            if (!sportTypes.isEmpty()) {
                // 2. Sử dụng String.join() để nối các điều kiện bằng " or "
                String typeFilter = String.join(" or ", sportTypes);

                // 3. Xây dựng chuỗi filter hoàn chỉnh và thêm vào 'filter'
                // Cần bọc điều kiện bằng dấu ngoặc đơn để đảm bảo logic OData đúng
                filter += conjunction + "Courts/any(c: (" + typeFilter + "))";

                // 4. Cập nhật conjunction cho các filter tiếp theo
                conjunction = " and ";
            }
        }else{
            model.setSportType(List.of());
        }

// 3. Price Filter
        if(price > 0){
            // Use the conjunction BEFORE adding the new part
            filter += conjunction + "Courts/any(c: c/PricePerHour le " + price + ")";
            conjunction = " and ";

            model.setPrice(price + "");
        }
        if(tvStartTime.getText().toString().isEmpty() == false && tvEndTime.getText().toString().isEmpty() == false){
            // 4. Time Filter
            //format time

            String startTime = tvStartTime.getText().toString(); // Ví dụ: "09:00"
            String endTime = tvEndTime.getText().toString();     // Ví dụ: "17:30"
            model.setStartTime(startTime);
            model.setEndTime(endTime);

// --- BƯỚC 1: TÁCH GIỜ VÀ PHÚT ---

// Tách Giờ Bắt đầu
            String[] startParts = startTime.split(":");
            int startHour = Integer.parseInt(startParts[0]); // sh
            int startMinute = Integer.parseInt(startParts[1]); // sm

// Tách Giờ Kết thúc
            String[] endParts = endTime.split(":");
            int endHour = Integer.parseInt(endParts[0]); // eh
            int endMinute = Integer.parseInt(endParts[1]); // em
// --- BƯỚC 2: ĐỊNH DẠNG CHUỖI DURATION CHUẨN ODATA ---

            String startDuration = String.format(Locale.ROOT, "duration'PT%dH%dM'", startHour, startMinute);
// Định dạng cho Giờ Kết thúc
            String endDuration = String.format(Locale.ROOT, "duration'PT%dH%dM'", endHour, endMinute);
// Kết quả (cho 17:30): duration'PT17H30M'

            filter += conjunction + "OpenTime le " + startDuration + " and CloseTime ge " + endDuration ;
        }else{
            model.setStartTime("");
            model.setEndTime("");
        }

        odata.put("$filter", filter);



            if (fragmentManager.getBackStackEntryCount() > 0) {
                // 2. Tạo Bundle để đóng gói dữ liệu


                    model.select(odata);


                fragmentManager.popBackStack();

            } else {
                // Xử lý trường hợp không có gì trong back stack (hiếm)
                Toast.makeText(getContext(), "Không thể đóng Fragment", Toast.LENGTH_SHORT).show();
            }

    }



    // Thiết lập SeekBar (Khoảng Giá)
    private void setupPriceSeekBar() {
        // Max là 500, đại diện cho 500.000 VNĐ

        priceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Giá trị thực tế: progress * 1000 VNĐ. Ví dụ: 250 * 1000 = 250.000
                int priceValue = progress * 1000;
                DecimalFormat formatter = new DecimalFormat("#,###");
                // Chuyển đổi sang định dạng tiền tệ

                if (progress == seekBar.getMax()) {
                    price = priceValue;
                    tvCurrentPrice.setText("Giá tối đa: " + formatter.format(priceValue) + "đ/giờ");
                } else {
                    price = priceValue;
                    tvCurrentPrice.setText("Giá tối đa: " + formatter.format(priceValue) + "đ/giờ");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Không cần làm gì
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Có thể áp dụng logic tìm kiếm tự động ở đây
            }
        });

        // Thiết lập giá trị ban đầu
        priceSeekBar.setProgress(250); // Mặc định 250, tương ứng 250.000đ
    }

    // Thiết lập các nút hành động
    private void setupActionButtons() {
        btnApplyFilters.setOnClickListener(v -> {
            // TODO: Thu thập dữ liệu và áp dụng bộ lọc
            collectFilterData();

        });

        btnClearAll.setOnClickListener(v -> {

            // Đặt lại SeekBar
            priceSeekBar.setProgress(priceSeekBar.getMax()); // Đặt lại mức tối đa
            tvCurrentPrice.setText("Giá tối đa: 500.000đ/giờ");
            // Đặt lại CheckBox
            for (CheckBox cb : sportCheckBoxes) {
                cb.setChecked(false);
            }
            Toast.makeText(getContext(), "Đã xóa tất cả bộ lọc", Toast.LENGTH_SHORT).show();
        });
    }

    // Hàm thu thập dữ liệu bộ lọc
    private void collectFilterData() {
        // Thu thập loại thể thao đã chọn
        StringBuilder sportSelected = new StringBuilder();
        for (CheckBox cb : sportCheckBoxes) {
            if (cb.isChecked()) {
                sportSelected.append(cb.getText().toString()).append(", ");
            }
        }


        int maxPrice = priceSeekBar.getProgress() * 1000;
        setBtnApplyFilters();
        // In ra Console/Logcat để kiểm tra

        // Bạn sẽ truyền dữ liệu này cho Activity/ViewModel để thực hiện tìm kiếm
    }
}
