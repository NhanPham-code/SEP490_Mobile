package com.example.sep490_mobile.ui.findTeam;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Build;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.databinding.FragmentFilterFindTeamBinding;
import com.example.sep490_mobile.utils.DurationConverter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class FilterFindTeam extends Fragment {

    private FragmentFilterFindTeamBinding binding;

    // newInstance factory method (giữ nguyên theo mẫu của bạn)
    public static FilterFindTeam newInstance() {
        return new FilterFindTeam();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFilterFindTeamBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Thiết lập sự kiện
        setupEventListeners();

        // Thiết lập Date/Time Picker cho EditText
        setupDateAndTimePickers();

        // Lấy và hiển thị lại các filter đã chọn từ ViewModel
        setSelectedFilters();
    }

    private void setupEventListeners() {
        binding.closeFilterBtn.setOnClickListener(v -> closeFragment());
        binding.applyFiltersBtn.setOnClickListener(v -> applyFilters());
        binding.resetFiltersBtn.setOnClickListener(v -> resetFilters());
        binding.clearFiltersBtn.setOnClickListener(v -> resetFilters()); // Giống chức năng Đặt Lại
    }

    private void closeFragment() {
        if (getParentFragmentManager() != null) {
            getParentFragmentManager().popBackStack("FindTeamFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    /**
     * Lấy dữ liệu từ ViewModel và thiết lập lại trạng thái cho các View.
     * Đã sửa lại để kiểm tra giá trị của LiveData thay vì kiểm tra observers.
     */
    private void setSelectedFilters() {
        ShareFilterFindTeamViewModel model = new ViewModelProvider(requireActivity()).get(ShareFilterFindTeamViewModel.class);

        // 1. Set SportType
        List<String> selectedSports = model.getSportType().getValue();
        if (selectedSports != null && !selectedSports.isEmpty()) {
            resetCheckboxes(binding.sportFilterContent);
            for (String sport : selectedSports) {
                if (sport.equalsIgnoreCase("Bóng đá sân 11")) binding.bg11.setChecked(true);
                else if (sport.equalsIgnoreCase("Bóng đá sân 5")) binding.bg5.setChecked(true);
                else if (sport.equalsIgnoreCase("Bóng đá sân 7")) binding.bg7.setChecked(true);
                else if (sport.equalsIgnoreCase("Bóng chuyền")) binding.bgC.setChecked(true);
                else if (sport.equalsIgnoreCase("Bóng rổ")) binding.bgR.setChecked(true);
                else if (sport.equalsIgnoreCase("Cầu lông")) binding.bgCl.setChecked(true);
                else if (sport.equalsIgnoreCase("Tennis")) binding.bgTn.setChecked(true);
                else if (sport.equalsIgnoreCase("Pickleball")) binding.bgPk.setChecked(true);
            }
        }

        // 2. Set PlayDate
        String playDate = model.getPlayDate().getValue();
        if (playDate != null && !playDate.isEmpty()) {
            binding.playDateFilter.setText(playDate);
        }

        // 3. Set PlayTime
        String playTime = model.getPlayTime().getValue();
        if (playTime != null && !playTime.isEmpty()) {
            binding.playTimeFilter.setText(playTime);
        }

        // 4. Set MinPlayer
        Integer minPlayer = model.getMinPlayerr().getValue();
        if (minPlayer != null) {
            binding.minPlayersInput.setText(String.valueOf(minPlayer));
        }

        // 5. Set MaxPlayer
        Integer maxPlayer = model.getMaxPlayer().getValue();
        if (maxPlayer != null) {
            binding.maxPlayersInput.setText(String.valueOf(maxPlayer));
        }

        // 6. Set Address
        String address = model.getAddress().getValue();
        if (address != null && !address.isEmpty()) {
            binding.locationSearchInput.setText(address);
        }
    }

    /**
     * Thu thập dữ liệu, tạo query và gửi đến ViewModel.
     */
    private void applyFilters() {
        ShareFilterFindTeamViewModel model = new ViewModelProvider(requireActivity()).get(ShareFilterFindTeamViewModel.class);

        List<String> odata = new ArrayList<>();

        // 1. Sport Types
        List<String> sportTypes = getSelectedCheckboxes(binding.sportFilterContent);
        model.setSportType(sportTypes);
        if (!sportTypes.isEmpty()) {
            String sportFilter = sportTypes.stream()
                    .map(s -> "SportType eq '" + s + "'")
                    .collect(Collectors.joining(" or "));
            odata.add("(" + sportFilter + ")");
        }
        String date = binding.playDateFilter.getText().toString(); // e.g., "26-10-2025"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!date.isEmpty()) {
                // 1. Parse the input date string
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate localPlayDate = LocalDate.parse(date, inputFormatter);

                // 2. Define the start and end of the day in UTC
                // Start of the day: 2025-10-26T00:00:00Z
                OffsetDateTime startOfDay = localPlayDate.atStartOfDay().atOffset(ZoneOffset.UTC);
                // End of the day (start of the next day): 2025-10-27T00:00:00Z
                OffsetDateTime startOfNextDay = localPlayDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

                // 3. Create the OData filter clause for the date range
                // The query will be: (PlayDate ge 2025-10-26T00:00:00Z and PlayDate lt 2025-10-27T00:00:00Z)
                // Note that there are no single quotes around the date values.
                String odataFilter = String.format("(PlayDate lt %s)",
                        startOfDay.toString(),
                        startOfNextDay.toString());
                odata.add(odataFilter);
                model.setPlayDate(date);
            } else {
                model.setPlayDate(null);
            }
        }


        // 3. Play Time
        String timeStr = binding.playTimeFilter.getText().toString();
        if (!timeStr.isEmpty()) {
            String time = DurationConverter.convertTimeToIsoDuration(timeStr);
            odata.add("TimePlay eq duration'" + time + "'");
            model.setPlayTime(timeStr);
        } else {
            model.setPlayTime(null);
        }

        // 4. Players
        int minPlayers = 0, maxPlayers = 0;
        try {
            if (!binding.minPlayersInput.getText().toString().isEmpty()) {
                minPlayers = Integer.parseInt(binding.minPlayersInput.getText().toString());
            }
            if (!binding.maxPlayersInput.getText().toString().isEmpty()) {
                maxPlayers = Integer.parseInt(binding.maxPlayersInput.getText().toString());
            }
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Số người chơi không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (minPlayers > 0) {
            odata.add("NeededPlayers ge " + minPlayers);
            model.setMinPlayer(minPlayers);
        } else {
            model.setMinPlayer(1);
        }
        if (maxPlayers > 0) {
            odata.add("NeededPlayers le " + maxPlayers);
            model.setMaxPlayer(maxPlayers);
        } else {
            model.setMaxPlayer(10);
        }

        // 5. Location
        String location = binding.locationSearchInput.getText().toString();
        if (!location.isEmpty()) {
            odata.add("contains(Location, '" + location + "')");
            model.setAddress(location);
        } else {
            model.setAddress(null);
        }

        // Build query and set to ViewModel
        String filter = String.join(" and ", odata);
        Map<String, String> url = new HashMap<>();
        if (!filter.isEmpty()) {
            url.put("$filter", filter);
            model.setSelected(url);
        }else{
            model.setSelected(null);
        }


        closeFragment();
    }

    /**
     * Đặt lại tất cả các trường nhập liệu trên UI và xóa dữ liệu trong ViewModel.
     */
    private void resetFilters() {
        ShareFilterFindTeamViewModel model = new ViewModelProvider(requireActivity()).get(ShareFilterFindTeamViewModel.class);

        // 1. Reset UI
        resetCheckboxes(binding.sportFilterContent);
        binding.playDateFilter.setText("");
        binding.playTimeFilter.setText("");
        binding.minPlayersInput.setText("");
        binding.maxPlayersInput.setText("");
        binding.locationSearchInput.setText("");

//        // 2. Reset ViewModel
//        model.setSportType(new ArrayList<>());
//        model.setPlayDate(null);
//        model.setPlayTime(null);
//        model.setMinPlayer(1);
//        model.setMaxPlayer(10);
//        model.setAddress(null);
//        model.setSelected(new HashMap<>()); // Xóa cả odata query

        Toast.makeText(getContext(), "Đã xóa tất cả bộ lọc", Toast.LENGTH_SHORT).show();
    }

    private void resetCheckboxes(LinearLayout container) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof CheckBox) {
                ((CheckBox) child).setChecked(false);
            }
        }
    }

    private List<String> getSelectedCheckboxes(LinearLayout container) {
        List<String> selectedSports = new ArrayList<>();
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox cb = (CheckBox) child;
                if (cb.isChecked()) {
                    selectedSports.add(cb.getText().toString());
                }
            }
        }
        return selectedSports;
    }

    // ===================================================
    // Xử lý DatePicker và TimePicker
    // ===================================================

    private void setupDateAndTimePickers() {
        binding.playDateFilter.setOnClickListener(v -> showDatePicker());
        binding.playTimeFilter.setOnClickListener(v -> showTimePicker());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear);
                    binding.playDateFilter.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                (view, selectedHour, selectedMinute) -> {
                    String time = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute);
                    binding.playTimeFilter.setText(time);
                }, hour, minute, true); // true cho định dạng 24 giờ
        timePickerDialog.show();
    }
}