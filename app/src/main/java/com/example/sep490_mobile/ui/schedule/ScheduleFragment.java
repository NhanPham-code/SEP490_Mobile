package com.example.sep490_mobile.ui.schedule;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.ScheduleDisplayItem;
import com.example.sep490_mobile.databinding.FragmentScheduleBinding;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ScheduleFragment extends Fragment {

    private ScheduleViewModel scheduleViewModel;
    private FragmentScheduleBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        scheduleViewModel = new ViewModelProvider(this).get(ScheduleViewModel.class);
        binding = FragmentScheduleBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setupObservers();
        setupClickListeners();
        return root;
    }

    private void setupClickListeners() {
        binding.btnNextWeek.setOnClickListener(v -> scheduleViewModel.nextWeek());
        binding.btnPreviousWeek.setOnClickListener(v -> scheduleViewModel.previousWeek());
        binding.tvCurrentWeekRange.setOnClickListener(v -> showDatePicker());
    }

    private void setupObservers() {
        scheduleViewModel.getWeekRangeText().observe(getViewLifecycleOwner(), text -> {
            binding.tvCurrentWeekRange.setText(text);
        });
        scheduleViewModel.getDaysOfWeek().observe(getViewLifecycleOwner(), this::updateDaysUI);
        scheduleViewModel.getGroupedAndSortedBookings().observe(getViewLifecycleOwner(), this::updateBookingsUI);
    }

    private void updateBookingsUI(Map<String, List<ScheduleDisplayItem>> groupedBookings) {
        binding.scheduleDetailsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        List<Calendar> daysInWeek = scheduleViewModel.getDaysOfWeek().getValue();
        if (getContext() == null || daysInWeek == null) return;

        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat dayOfMonthFormat = new SimpleDateFormat("dd/MM", new Locale("vi", "VN"));
        SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEE", new Locale("vi", "VN"));

        for (Calendar day : daysInWeek) {
            View scheduleRowView = inflater.inflate(R.layout.item_schedule_row, binding.scheduleDetailsContainer, false);
            TextView tvDayOfMonth = scheduleRowView.findViewById(R.id.tv_day_of_month);
            TextView tvDayOfWeek = scheduleRowView.findViewById(R.id.tv_day_of_week);
            LinearLayout dayBookingsContainer = scheduleRowView.findViewById(R.id.day_bookings_container);

            tvDayOfMonth.setText(dayOfMonthFormat.format(day.getTime()));
            tvDayOfWeek.setText(dayOfWeekFormat.format(day.getTime()));

            String dateKey = keyFormat.format(day.getTime());
            List<ScheduleDisplayItem> itemsForDay = groupedBookings.get(dateKey);

            if (itemsForDay != null && !itemsForDay.isEmpty()) {
                for (ScheduleDisplayItem item : itemsForDay) {
                    View bookingCardView = inflater.inflate(R.layout.item_booking_detail, dayBookingsContainer, false);

                    TextView tvStadiumName = bookingCardView.findViewById(R.id.tv_stadium_name);
                    TextView tvBookingTime = bookingCardView.findViewById(R.id.tv_booking_time);
                    TextView tvStatus = bookingCardView.findViewById(R.id.tv_booking_status);
                    LinearLayout courtBadgeContainer = bookingCardView.findViewById(R.id.court_badge_container);
                    TextView tvDetailsButton = bookingCardView.findViewById(R.id.tv_details_button);

                    tvStadiumName.setText(item.getStadiumName());
                    tvStatus.setText(translateStatus(item.getStatus()));

                    String startTime = formatTimeString(item.getStartTime());
                    String endTime = formatTimeString(item.getEndTime());
                    tvBookingTime.setText(String.format(Locale.US, "%s - %s", startTime, endTime));

                    courtBadgeContainer.removeAllViews();
                    List<String> courtNames = item.getCourtNames();
                    if (courtNames != null && !courtNames.isEmpty()) {
                        String firstCourtName = courtNames.get(0);
                        String fullCourtText = firstCourtName;

                        if (courtNames.size() > 1) {
                            fullCourtText += "<br><small>(+" + (courtNames.size() - 1) + " sân khác)</small>";
                        }

                        TextView badgeView = (TextView) inflater.inflate(R.layout.item_court_badge, courtBadgeContainer, false);
                        badgeView.setText(Html.fromHtml(fullCourtText, Html.FROM_HTML_MODE_LEGACY));
                        courtBadgeContainer.addView(badgeView);
                    }

                    // Xử lý sự kiện click cho nút "Chi tiết"
                    tvDetailsButton.setOnClickListener(v -> {
                        if (item.getOriginalBooking() != null) {
                            ScheduleFragmentDirections.ActionNavigationScheduleToBookingDetailFragment action =
                                    ScheduleFragmentDirections.actionNavigationScheduleToBookingDetailFragment(item.getOriginalBooking());

                            NavController navController = Navigation.findNavController(v);
                            navController.navigate(action);
                        }
                    });

                    dayBookingsContainer.addView(bookingCardView);
                }
            } else {
                View noBookingInDayView = inflater.inflate(R.layout.item_no_booking, dayBookingsContainer, false);
                dayBookingsContainer.addView(noBookingInDayView);
            }
            binding.scheduleDetailsContainer.addView(scheduleRowView);
        }
    }

    private void showDatePicker() {
        if (getContext() == null) return;
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                getContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selectedDate = Calendar.getInstance();
                    selectedDate.set(year, month, dayOfMonth);
                    scheduleViewModel.setDate(selectedDate);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private String translateStatus(String status) {
        if (status == null) return "N/A";
        switch (status.toLowerCase()) {
            case "completed": return "Hoàn thành";
            case "accepted": return "Đã nhận";
            case "pending":
            case "waiting": return "Đang chờ";
            case "cancelled": return "Đã hủy";
            default: return status.toUpperCase();
        }
    }

    private String formatTimeString(String dateString) {
        if (dateString == null) return "";
        try {
            SimpleDateFormat sourceFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
            SimpleDateFormat targetFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = sourceFormat.parse(dateString);
            return targetFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void updateDaysUI(List<Calendar> days) {
        if (getContext() == null) return;
        binding.layoutDaysHeader.removeAllViews();
        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", new Locale("vi", "VN"));
        SimpleDateFormat dayNumberFormat = new SimpleDateFormat("d", Locale.getDefault());
        Calendar today = Calendar.getInstance();

        for (Calendar day : days) {
            View dayView = LayoutInflater.from(getContext()).inflate(R.layout.item_day_of_week, binding.layoutDaysHeader, false);
            TextView tvDayName = dayView.findViewById(R.id.tv_day_name);
            TextView tvDayNumber = dayView.findViewById(R.id.tv_day_number);
            tvDayName.setText(dayNameFormat.format(day.getTime()));
            tvDayNumber.setText(dayNumberFormat.format(day.getTime()));

            if (today.get(Calendar.YEAR) == day.get(Calendar.YEAR) &&
                    today.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR)) {
                tvDayNumber.setSelected(true);
            } else {
                tvDayNumber.setSelected(false);
            }
            binding.layoutDaysHeader.addView(dayView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}