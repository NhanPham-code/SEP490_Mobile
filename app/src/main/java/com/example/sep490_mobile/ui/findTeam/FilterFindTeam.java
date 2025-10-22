package com.example.sep490_mobile.ui.findTeam;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.sep490_mobile.R; // Đảm bảo package này đúng
import com.example.sep490_mobile.databinding.FragmentFilterFindTeamBinding;
import com.example.sep490_mobile.ui.home.SharedViewModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class FilterFindTeam extends Fragment {

    // Khai báo các View cần thiết
    private ImageButton closeFilterBtn;
    private Button clearFiltersBtn;
    private Button applyFiltersBtn;
    private Button resetFiltersBtn;
    private FragmentFilterFindTeamBinding binding;
    private List<String> sportTypes = new ArrayList<>();

    // Các phần tử Toggle
    private RelativeLayout sportFilterHeader, dateFilterHeader, timeFilterHeader, playersFilterHeader, locationFilterHeader;
    private LinearLayout sportFilterContent, dateFilterContent, timeFilterContent, playersFilterContent, locationFilterContent;

    // Input fields
    private EditText playDateFilter, playTimeFilter, minPlayersInput, maxPlayersInput, locationSearchInput;

    // VÌ XML TRÊN CHƯA CÓ KHOẢNG GIÁ, tôi sẽ bổ sung giả định cho Khoảng Giá
    // private SeekBar priceRangeSlider;
    // private TextView currentPrice;


    // newInstance factory method (giữ nguyên theo mẫu của bạn)
    public static FilterFindTeam newInstance(String param1, String param2) {
        FilterFindTeam fragment = new FilterFindTeam();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout
        binding = FragmentFilterFindTeamBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ View
        mapViews(view);

        // Thiết lập sự kiện
        setupEventListeners();

        // Thiết lập Date/Time Picker cho EditText
        setupDateAndTimePickers();
        setSelectedFilters();
        // Thiết lập trạng thái ban đầu (nếu cần)
        // Ví dụ: playersFilterContent.setVisibility(View.GONE);
    }

    private void mapViews(View view) {
        // Header actions
        closeFilterBtn = view.findViewById(R.id.close_filter_btn);
        clearFiltersBtn = view.findViewById(R.id.clear_filters_btn);
        applyFiltersBtn = view.findViewById(R.id.apply_filters_btn);
        resetFiltersBtn = view.findViewById(R.id.reset_filters_btn);

        // Toggle Headers
        sportFilterHeader = view.findViewById(R.id.sport_filter_header);
        dateFilterHeader = view.findViewById(R.id.date_filter_header);
        timeFilterHeader = view.findViewById(R.id.time_filter_header);
        playersFilterHeader = view.findViewById(R.id.players_filter_header);
        locationFilterHeader = view.findViewById(R.id.location_filter_header);

        // Toggle Contents
        sportFilterContent = view.findViewById(R.id.sport_filter_content);
        dateFilterContent = view.findViewById(R.id.date_filter_content);
        timeFilterContent = view.findViewById(R.id.time_filter_content);
        playersFilterContent = view.findViewById(R.id.players_filter_content);
        locationFilterContent = view.findViewById(R.id.location_filter_content);

        // Input fields
        playDateFilter = view.findViewById(R.id.play_date_filter);
        playTimeFilter = view.findViewById(R.id.play_time_filter);
        minPlayersInput = view.findViewById(R.id.min_players_input);
        maxPlayersInput = view.findViewById(R.id.max_players_input);
        locationSearchInput = view.findViewById(R.id.location_search_input);

        // *LƯU Ý: Phần Khoảng Giá không có trong XML bạn cung cấp,
        // nếu bạn thêm vào, hãy ánh xạ ở đây*
        // priceRangeSlider = view.findViewById(R.id.price_range_slider);
        // currentPrice = view.findViewById(R.id.current_price);
    }

    private void setupEventListeners() {
        // 1. Logic Đóng Fragment
        closeFilterBtn.setOnClickListener(v -> closeFragment());

        // 2. Logic Áp Dụng và Đặt Lại
        applyFiltersBtn.setOnClickListener(v -> applyFilters());
        resetFiltersBtn.setOnClickListener(v -> resetFilters());
        clearFiltersBtn.setOnClickListener(v -> resetFilters()); // Giống chức năng Đặt Lại

        // 3. Logic Mở Rộng/Thu Gọn (Toggle)


        // 4. Logic Xử lý các button nhanh (Hôm nay, Mai,...)
        // LƯU Ý: Bạn cần gán ID cho các button này trong XML để sử dụng findViewById ở đây
        // Ví dụ: view.findViewById(R.id.today_btn).setOnClickListener(v -> setQuickDate(0));
    }


    private void closeFragment() {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().popBackStack("FindTeamFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    // set lại những trường đã chọn
    private void setSelectedFilters() {
        ShareFilterFindTeamViewModel model = new ViewModelProvider(requireActivity()).get(ShareFilterFindTeamViewModel.class);

        if(model.getSportType().hasObservers()){
            List<String> selectedSports = model.getSportType().getValue();
            if (selectedSports != null) {
                // Đặt lại tất cả CheckBox về unchecked trước khi thiết lập
                resetCheckboxes(sportFilterContent);

                for (String sport : selectedSports) {
                    // Đảm bảo sử dụng binding và kiểm tra null để tránh lỗi nếu không tìm thấy view
                    if (sport.equalsIgnoreCase("Bóng đá sân 11") && binding.bg11 != null) {
                        binding.bg11.setChecked(true);
                    } else if (sport.equalsIgnoreCase("Bóng đá sân 5") && binding.bg5 != null) {
                        binding.bg5.setChecked(true);
                    } else if (sport.equalsIgnoreCase("Bóng đá sân 7") && binding.bg7 != null) {
                        binding.bg7.setChecked(true);
                    } else if (sport.equalsIgnoreCase("Bóng Chuyền") && binding.bgC != null) {
                        binding.bgC.setChecked(true);
                    } else if (sport.equalsIgnoreCase("Bóng Rổ") && binding.bgR != null) {
                        binding.bgR.setChecked(true);
                    } else if (sport.equalsIgnoreCase("Cầu Lông") && binding.bgCl != null) {
                        binding.bgCl.setChecked(true);
                    } else if (sport.equalsIgnoreCase("Tennis") && binding.bgTn != null) {
                        binding.bgTn.setChecked(true);
                    }
                }
            }
        }
        if(model.getPlayDate().hasObservers()){
            playDateFilter.setText(model.getPlayDate().getValue());
        }
        if(model.getPlayTime().hasObservers()){
            playTimeFilter.setText(model.getPlayTime().getValue());
        }
        if(model.getMinPlayerr().hasObservers()){
            minPlayersInput.setText(model.getMinPlayerr().getValue().toString());

        }
        if(model.getMaxPlayer().hasObservers()){
            maxPlayersInput.setText(model.getMaxPlayer().getValue().toString());
        }
        if(model.getAddress().hasObservers()){
            locationSearchInput.setText(model.getAddress().getValue());
        }

    }

    private void applyFilters() {
        // TODO: Thu thập dữ liệu từ các trường và gửi đến Activity/ViewModel
        // Ví dụ:
        ShareFilterFindTeamViewModel model = new ViewModelProvider(requireActivity()).get(ShareFilterFindTeamViewModel.class);
        getSelectedCheckboxes(sportFilterContent);
        List<String> odata = new ArrayList<>();

        model.setSportType(sportTypes);
        String date = playDateFilter.getText().toString();
        String time = playTimeFilter.getText().toString();
        int minPlayers = Integer.parseInt(minPlayersInput.getText().toString());
        int maxPlayers = Integer.parseInt(maxPlayersInput.getText().toString());
        String location = locationSearchInput.getText().toString();

        if(sportTypes.size() > 0){
            String sport = sportTypes.stream().collect(Collectors.joining("or"));
            odata.add("SportType eq '" + sport + "'");
        }
        if (date.length() > 0){
            odata.add("PlayDate eq " + date);
            model.setPlayDate(date);
        }
        if(time.length() > 0){
            odata.add("PlayTime eq " + time);
            model.setPlayTime(time);
        }
        if(minPlayers > 0){
            odata.add("NeededPlayers ge " + minPlayers);
            model.setMinPlayer(minPlayers);
        }
        if(maxPlayers > 0){
            odata.add("NeededPlayers le " + maxPlayers);
            model.setMaxPlayer(maxPlayers);
        }
        if(location.length() > 0){
            odata.add("contains(Location, '" + location + "')");
            model.setAddress(location);
        }

        String filter = odata.stream().collect(Collectors.joining(" and "));
        Map<String, String> url = new HashMap<>();
        url.put("$filter", filter);
        model.setSelected(url);

        if (getParentFragmentManager() != null) {
            getParentFragmentManager().popBackStack("FindTeamFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    private void resetFilters() {
        // 1. Đặt lại CheckBox (Loại Thể Thao)
        resetCheckboxes(sportFilterContent);

        // 2. Đặt lại trường nhập liệu
        playDateFilter.setText("");
        playTimeFilter.setText("");
        minPlayersInput.setText("1");
        maxPlayersInput.setText("10");
        locationSearchInput.setText("");

        // 3. Đặt lại SeekBar (Nếu có)
        // if (priceRangeSlider != null) priceRangeSlider.setProgress(25);
        // if (currentPrice != null) currentPrice.setText("250.000đ/giờ");

        // Đảm bảo các filter ẩn ban đầu được ẩn lại (Ví dụ: Players, Location)
        playersFilterContent.setVisibility(View.VISIBLE); // Giả sử ban đầu chúng ta muốn hiển thị
        locationFilterContent.setVisibility(View.VISIBLE);
    }

    private void resetCheckboxes(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof CheckBox) {
                ((CheckBox) child).setChecked(false);
            }
        }
    }

    private void getSelectedCheckboxes(LinearLayout container) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox cb = (CheckBox) child;
                if (cb.isChecked()) {
                    sportTypes.add(cb.getText().toString()); // Thêm tên của loại thể thao)
                }
            }
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2); // Loại bỏ dấu phẩy và khoảng trắng cuối cùng
        }
    }

    // ===================================================
    // Xử lý DatePicker và TimePicker
    // ===================================================

    private void setupDateAndTimePickers() {
        playDateFilter.setOnClickListener(v -> showDatePicker());
        playTimeFilter.setOnClickListener(v -> showTimePicker());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Định dạng lại thành DD/MM/YYYY hoặc format mong muốn
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    playDateFilter.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, selectedHour, selectedMinute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    playTimeFilter.setText(time);
                }, hour, minute, true); // true cho định dạng 24 giờ
        timePickerDialog.show();
    }
}