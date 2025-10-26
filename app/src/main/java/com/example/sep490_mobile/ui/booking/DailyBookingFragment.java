package com.example.sep490_mobile.ui.booking;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.adapter.DailyCourtSelectionAdapter;
import com.example.sep490_mobile.adapter.TimeZoneAdapter;
import com.example.sep490_mobile.model.TimeZone;
import com.example.sep490_mobile.viewmodel.DailyBookingViewModel;
import com.google.android.material.button.MaterialButton;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Set;

public class DailyBookingFragment extends Fragment implements
        TimeZoneAdapter.OnTimeZoneClickListener,
        DailyCourtSelectionAdapter.OnCourtClickListener {

    private DailyBookingViewModel viewModel;

    private RecyclerView timeZoneRecyclerView, courtSelectionRecyclerView;
    private TextView tvDatePicker;
    private TimeZoneAdapter timeZoneAdapter;
    private DailyCourtSelectionAdapter courtSelectionAdapter;
    private MaterialButton btnBack, btnContinue;
    private TextView tvSummary;
    private int stadiumId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            DailyBookingFragmentArgs args = DailyBookingFragmentArgs.fromBundle(getArguments());
            stadiumId = args.getStadiumId();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_booking, container, false);
        tvDatePicker = view.findViewById(R.id.tv_date_picker);
        timeZoneRecyclerView = view.findViewById(R.id.timeZoneRecyclerView);
        courtSelectionRecyclerView = view.findViewById(R.id.court_selection_recyclerview);
        btnBack = view.findViewById(R.id.btn_back);
        btnContinue = view.findViewById(R.id.btn_continue);
        tvSummary = view.findViewById(R.id.tv_booking_summary);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DailyBookingViewModel.class);

        setupRecyclerViews();
        setupClickListeners();
        observeViewModel();

        viewModel.onDayCellClicked(LocalDate.now());

        if (stadiumId > 0) {
            viewModel.fetchStadiumData(String.valueOf(stadiumId));
        } else {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID của sân.", Toast.LENGTH_LONG).show();
        }
    }

    private void setupRecyclerViews() {
        timeZoneAdapter = new TimeZoneAdapter(requireContext(), this);
        timeZoneRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        timeZoneRecyclerView.setAdapter(timeZoneAdapter);

        courtSelectionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupClickListeners() {
        tvDatePicker.setOnClickListener(v -> showDatePickerDialog());
        btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
        btnContinue.setOnClickListener(v -> viewModel.onContinueClicked());
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        LocalDate selectedDate = viewModel.selectedDate.getValue();
        if (selectedDate != null) {
            c.set(selectedDate.getYear(), selectedDate.getMonthValue() - 1, selectedDate.getDayOfMonth());
        }

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (datePickerView, year1, monthOfYear, dayOfMonth) -> {
                    LocalDate date = LocalDate.of(year1, monthOfYear + 1, dayOfMonth);
                    viewModel.onDayCellClicked(date);
                },
                year, month, day);

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }


    private void observeViewModel() {
        viewModel.selectedDate.observe(getViewLifecycleOwner(), date -> {
            if (date != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                tvDatePicker.setText(date.format(formatter));
            } else {
                tvDatePicker.setText("Chọn ngày");
            }
        });

        viewModel.timeZones.observe(getViewLifecycleOwner(), zones -> {
            if (zones != null) {
                timeZoneAdapter.submitList(zones);
            }
        });

        viewModel.courtList.observe(getViewLifecycleOwner(), courtDisplayItems -> {
            if (courtDisplayItems != null && courtSelectionAdapter == null) {
                Log.d("ADAPTER_LIFECYCLE", "Tạo mới DailyCourtSelectionAdapter.");
                courtSelectionAdapter = new DailyCourtSelectionAdapter(getContext(), courtDisplayItems, this);
                courtSelectionRecyclerView.setAdapter(courtSelectionAdapter);
            }
        });

        viewModel.bookedCourtIds.observe(getViewLifecycleOwner(), bookedIds -> {
            if (courtSelectionAdapter != null) {
                Log.d("ADAPTER_UPDATE", "Cập nhật bookedCourtIds: " + bookedIds);
                courtSelectionAdapter.setBookedCourtIds(bookedIds);
            } else {
                Log.w("ADAPTER_UPDATE", "Bỏ lỡ cập nhật bookedCourtIds vì adapter là null.");
            }
        });

        viewModel.selectedCourtIds.observe(getViewLifecycleOwner(), selectedIds -> {
            if (courtSelectionAdapter != null) {
                Log.d("ADAPTER_UPDATE", "Cập nhật selectedCourtIds: " + selectedIds);
                courtSelectionAdapter.setSelectedCourtIds(selectedIds);
            }
        });

        viewModel.bookedCourtRelationIds.observe(getViewLifecycleOwner(), relationIds -> {
            if (courtSelectionAdapter != null) {
                Log.d("ADAPTER_UPDATE", "Cập nhật bookedCourtRelationIds: " + relationIds);
                courtSelectionAdapter.setBookedRelationCourtIds(relationIds);
            }
        });

        viewModel.selectedCourtRelationIds.observe(getViewLifecycleOwner(), relationIds -> {
            if (courtSelectionAdapter != null) {
                Log.d("ADAPTER_UPDATE", "Cập nhật selectedCourtRelationIds: " + relationIds);
                courtSelectionAdapter.setSelectedRelationCourtIds(relationIds);
            }
        });


        viewModel.isCourtSelectionEnabled.observe(getViewLifecycleOwner(), isEnabled -> {
            courtSelectionRecyclerView.setAlpha(isEnabled ? 1.0f : 0.5f);
            courtSelectionRecyclerView.setEnabled(isEnabled);
            courtSelectionRecyclerView.setClickable(isEnabled);
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

        viewModel.navigateToCheckout.observe(getViewLifecycleOwner(), bundle -> {
            if (bundle != null) {
                NavHostFragment.findNavController(this).navigate(
                        R.id.action_dailyBookingFragment_to_checkoutTimeZoneFragment,
                        bundle
                );
                viewModel.onCheckoutNavigated();
            }
        });

        // THÊM MỚI: Lắng nghe sự kiện điều hướng Login
        viewModel.navigateToLogin.observe(getViewLifecycleOwner(), shouldNavigate -> {
            if (shouldNavigate != null && shouldNavigate) {
                // Điều hướng đến fragment/màn hình Đăng nhập.
                // Tôi dùng 'navigation_account' dựa trên file mobile_navigation.xml
                NavHostFragment.findNavController(this).navigate(R.id.navigation_account);
                viewModel.onLoginNavigated(); // Reset trigger
            }
        });
    }

    @Override
    public void onTimeZoneClick(TimeZone timeZone) {
        viewModel.onTimeZoneClicked(timeZone);
    }

    @Override
    public void onCourtClick(int courtId) {
        if (Boolean.TRUE.equals(viewModel.isCourtSelectionEnabled.getValue())) {
            viewModel.onCourtClicked(courtId);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Đây là dòng quan trọng nhất:
        // Reset adapter về null khi view bị hủy
        // để nó được tạo lại khi user quay lại.
        courtSelectionAdapter = null;

        // Đây cũng là thực hành tốt để tránh memory leak
        if (courtSelectionRecyclerView != null) {
            courtSelectionRecyclerView.setAdapter(null);
        }
    }
}