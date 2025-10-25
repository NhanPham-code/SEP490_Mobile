package com.example.sep490_mobile.ui.booking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.adapter.CalendarAdapter;
import com.example.sep490_mobile.adapter.CalendarClickListener;
import com.example.sep490_mobile.adapter.CourtSelectionAdapter;
import com.example.sep490_mobile.adapter.TimeSlotAdapter;
import com.example.sep490_mobile.viewmodel.BookingCalendarViewModel;
import com.google.android.material.button.MaterialButton;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public class MonthlyBookingFragment extends Fragment implements CalendarClickListener,
        TimeSlotAdapter.OnTimeSlotClickListener,
        CourtSelectionAdapter.OnCourtClickListener {

    private BookingCalendarViewModel viewModel;

    // Views
    private CalendarAdapter calendarAdapter;
    private RecyclerView calendarRecyclerView, timeSlotRecyclerView, courtSelectionRecyclerView;
    private TextView monthYearDisplay;
    private ImageButton prevMonthButton, nextMonthButton;
    private TimeSlotAdapter timeSlotAdapter;
    private CourtSelectionAdapter courtSelectionAdapter;
    private MaterialButton btnBack, btnContinue, btnClearAll, btnFilter;
    private TextView tvSummary;
    private int stadiumId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            MonthlyBookingFragmentArgs args = MonthlyBookingFragmentArgs.fromBundle(getArguments());
            stadiumId = args.getStadiumId();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monthly_booking, container, false);
        // Init Views
        calendarRecyclerView = view.findViewById(R.id.calendarRecyclerView);
        monthYearDisplay = view.findViewById(R.id.monthYearDisplay);
        prevMonthButton = view.findViewById(R.id.prevMonth);
        nextMonthButton = view.findViewById(R.id.nextMonth);
        timeSlotRecyclerView = view.findViewById(R.id.timeSlotRecyclerView);
        courtSelectionRecyclerView = view.findViewById(R.id.court_selection_recyclerview);
        btnBack = view.findViewById(R.id.btn_back);
        btnContinue = view.findViewById(R.id.btn_continue);
        btnClearAll = view.findViewById(R.id.btnClearAll);
        btnFilter = view.findViewById(R.id.btnFilter);
        tvSummary = view.findViewById(R.id.tv_booking_summary);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(BookingCalendarViewModel.class);

        setupRecyclerViews();
        setupClickListeners();
        observeViewModel();

        if (stadiumId > 0) {
            viewModel.fetchStadiumData(String.valueOf(stadiumId));
        } else {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID của sân.", Toast.LENGTH_LONG).show();
        }
    }

    private void setupRecyclerViews() {
        calendarAdapter = new CalendarAdapter(getContext(), this);
        calendarRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 7));
        calendarRecyclerView.setAdapter(calendarAdapter);

        timeSlotAdapter = new TimeSlotAdapter(this);
        timeSlotRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        timeSlotRecyclerView.setAdapter(timeSlotAdapter);

        courtSelectionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupClickListeners() {
        prevMonthButton.setOnClickListener(v -> viewModel.goToPreviousMonth());
        nextMonthButton.setOnClickListener(v -> viewModel.goToNextMonth());
        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        btnContinue.setOnClickListener(v -> viewModel.onContinueClicked());
        btnClearAll.setOnClickListener(v -> viewModel.clearAllSelections());

        btnFilter.setOnClickListener(v -> {
            if (viewModel.startTime.getValue() == null || viewModel.endTime.getValue() == null) {
                Toast.makeText(getContext(), "Vui lòng chọn khung giờ trước khi lọc", Toast.LENGTH_SHORT).show();
                return;
            }
            if (viewModel.selectedDates.getValue() == null || viewModel.selectedDates.getValue().isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn ngày trước khi lọc", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.filterBookedCourts();
        });
    }

    private void observeViewModel() {
        viewModel.monthYearText.observe(getViewLifecycleOwner(), text -> monthYearDisplay.setText(text));
        viewModel.calendarCells.observe(getViewLifecycleOwner(), cells -> calendarAdapter.submitList(cells));
        viewModel.timeSlots.observe(getViewLifecycleOwner(), timeSlots -> timeSlotAdapter.submitList(timeSlots));

        viewModel.courtList.observe(getViewLifecycleOwner(), courtDisplayItems -> {
            if (courtDisplayItems != null) {
                courtSelectionAdapter = new CourtSelectionAdapter(getContext(), courtDisplayItems, this);

                Set<Integer> currentBookedIds = viewModel.bookedCourtIds.getValue();
                if (currentBookedIds != null) courtSelectionAdapter.setBookedCourtIds(currentBookedIds);

                Set<Integer> currentBookedRelIds = viewModel.bookedCourtRelationIds.getValue();
                if (currentBookedRelIds != null) courtSelectionAdapter.setBookedCourtRelationIds(currentBookedRelIds);

                Set<Integer> currentSelectedIds = viewModel.selectedCourtIds.getValue();
                if (currentSelectedIds != null) courtSelectionAdapter.setSelectedCourtIds(currentSelectedIds);

                Set<Integer> currentSelectedRelIds = viewModel.selectedCourtRelationIds.getValue();
                if (currentSelectedRelIds != null) courtSelectionAdapter.setSelectedCourtRelationIds(currentSelectedRelIds);

                courtSelectionRecyclerView.setAdapter(courtSelectionAdapter);
            }
        });

        viewModel.bookedCourtIds.observe(getViewLifecycleOwner(), bookedIds -> {
            if (courtSelectionAdapter != null) courtSelectionAdapter.setBookedCourtIds(bookedIds);
        });
        viewModel.bookedCourtRelationIds.observe(getViewLifecycleOwner(), relationIds -> {
            if (courtSelectionAdapter != null) courtSelectionAdapter.setBookedCourtRelationIds(relationIds);
        });
        viewModel.selectedCourtIds.observe(getViewLifecycleOwner(), selectedIds -> {
            if (courtSelectionAdapter != null) courtSelectionAdapter.setSelectedCourtIds(selectedIds);
        });
        viewModel.selectedCourtRelationIds.observe(getViewLifecycleOwner(), relationIds -> {
            if (courtSelectionAdapter != null) courtSelectionAdapter.setSelectedCourtRelationIds(relationIds);
        });

        viewModel.toastMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.onToastShown();
            }
        });

        viewModel.bookingSummary.observe(getViewLifecycleOwner(), summary -> {
            if (summary != null) {
                tvSummary.setText(summary.getSummaryText());
                btnContinue.setEnabled(summary.isButtonEnabled());
                if (getContext() != null) {
                    int colorRes = summary.isButtonEnabled() ? R.color.ocean_blue : R.color.calendar_disabled_text;
                    btnContinue.setBackgroundColor(ContextCompat.getColor(getContext(), colorRes));
                }
            }
        });

        // *** SỬA LỖI: Chỉ giữ lại MỘT observer cho navigateToCheckout ***
        viewModel.navigateToCheckout.observe(getViewLifecycleOwner(), bundle -> {
            if (bundle != null) {
                // *** DÒNG SỬA LỖI QUAN TRỌNG ***
                // Thêm `stadiumId` của Fragment vào bundle trước khi điều hướng.
                bundle.putInt("stadiumId", this.stadiumId);

                // Toast để debug (an toàn)
                int[] courtIds = bundle.getIntArray("COURT_IDS");
                if (courtIds != null) {
                    Toast.makeText(getContext(), "Điều hướng với stadiumId: " + this.stadiumId, Toast.LENGTH_SHORT).show();
                }

                // Thực hiện điều hướng với bundle đã được bổ sung stadiumId
                NavHostFragment.findNavController(this).navigate(R.id.action_monthlyBookingFragment_to_monthlyCheckoutFragment, bundle);
                viewModel.onCheckoutNavigated(); // Reset trạng thái để không bị navigate lại
            }
        });
    }

    @Override
    public void onDayCellClicked(LocalDate date) {
        viewModel.onDayCellClicked(date);
    }
    @Override
    public void onDayHeaderClicked(DayOfWeek dayOfWeek) {
        viewModel.onDayHeaderClicked(dayOfWeek);
    }

    @Override
    public void onTimeSlotClick(int hour) {
        viewModel.onTimeSlotClicked(hour);
    }

    @Override
    public void onCourtClick(int courtId) {
        viewModel.onCourtClicked(courtId);
    }
}