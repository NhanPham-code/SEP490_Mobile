package com.example.sep490_mobile.ui.booking;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sep490_mobile.adapter.CellAdapter;
import com.example.sep490_mobile.adapter.CourtHeaderAdapter;
import com.example.sep490_mobile.adapter.ScheduleRowAdapter;
import com.example.sep490_mobile.adapter.SportTypeHeaderAdapter;
import com.example.sep490_mobile.adapter.TimeHeaderAdapter;
import com.example.sep490_mobile.data.dto.BookingReadDto;
import com.example.sep490_mobile.data.dto.CourtHeaderItem;
import com.example.sep490_mobile.data.dto.CourtsDTO;
import com.example.sep490_mobile.data.dto.SelectedCourtInfo;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.databinding.FragmentVisuallyBookingBinding;
import com.example.sep490_mobile.utils.DurationConverter;
import com.example.sep490_mobile.viewmodel.BookingViewModel;
import com.example.sep490_mobile.viewmodel.StadiumViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VisuallyBookingFragment extends Fragment implements CellAdapter.OnCellInteractionListener {

    private static final String ARG_STADIUM_ID = "stadiumId";
    private int stadiumId;

    private StadiumViewModel stadiumViewModel;
    private BookingViewModel bookingViewModel;

    private FragmentVisuallyBookingBinding binding;

    private boolean isSyncingHorizontal = false;
    private boolean isSyncingVertical = false;

    private final Calendar selectedDate = Calendar.getInstance();
    private ScheduleRowAdapter scheduleRowAdapter;
    private final List<BookingReadDto> bookingsForDay = new ArrayList<>();

    private final List<RecyclerView> horizontalRecyclerViews = new ArrayList<>();
    private Map<Integer, List<Integer>> courtRelations = new HashMap<>();

    private boolean isInteractionAllowed = false;
    private final Map<Integer, List<Integer>> selectedSlots = new HashMap<>();

    public static VisuallyBookingFragment newInstance(int stadiumId) {
        VisuallyBookingFragment fragment = new VisuallyBookingFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_STADIUM_ID, stadiumId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stadiumId = getArguments().getInt(ARG_STADIUM_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentVisuallyBookingBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        stadiumViewModel = new ViewModelProvider(this).get(StadiumViewModel.class);
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        // Thiết lập các thành phần và listener
        setupDatePicker();
        fetchStadiumData();
        observeBookings();
        observeCourtRelations();
        binding.btnContinue.setOnClickListener(v -> navigateToCheckout());
        binding.btnBack.setOnClickListener(v -> NavHostFragment.findNavController(this).popBackStack());
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.scheduleGridRecyclerview.setAlpha(0.5f);
            isInteractionAllowed = false;
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.scheduleGridRecyclerview.setAlpha(1.0f);
            isInteractionAllowed = true;
        }
    }

    private void fetchStadiumData() {
        showLoading(true);
        Map<String, String> odataUrl = new HashMap<>();
        odataUrl.put("$expand", "Courts");
        odataUrl.put("$filter", "Id eq " + stadiumId);

        stadiumViewModel.stadiums.observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getItems() != null && !response.getItems().isEmpty()) {
                StadiumDTO stadium = response.getItems().get(0);
                setupScheduleUI(stadium);
                bookingViewModel.fetchBookingsForDay(stadiumId, selectedDate);
                if (stadium.courts != null && !stadium.courts.isEmpty()) {
                    stadiumViewModel.fetchCourtRelations(new ArrayList<>(stadium.courts));
                }
            } else {
                Toast.makeText(getContext(), "Could not load stadium data.", Toast.LENGTH_SHORT).show();
                showLoading(false);
            }
        });
        stadiumViewModel.fetchStadium(odataUrl);
    }

    private void setupDatePicker() {
        updateDateButtonText(selectedDate);
        binding.btnDatePicker.setOnClickListener(v -> {
            DatePickerDialog.OnDateSetListener dateSetListener = (datePicker, year, month, dayOfMonth) -> {
                selectedDate.set(Calendar.YEAR, year);
                selectedDate.set(Calendar.MONTH, month);
                selectedDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateButtonText(selectedDate);

                selectedSlots.clear();
                updateStats();

                showLoading(true);
                bookingViewModel.fetchBookingsForDay(stadiumId, selectedDate);

                if (scheduleRowAdapter != null) {
                    scheduleRowAdapter.setSelectedDate(selectedDate);
                    scheduleRowAdapter.updateSelections(selectedSlots);
                }

                for (RecyclerView rv : horizontalRecyclerViews) {
                    rv.scrollToPosition(0);
                }
            };
            DatePickerDialog dialog = new DatePickerDialog(requireContext(), dateSetListener, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));
            dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dialog.show();
        });
    }

    private void observeBookings() {
        bookingViewModel.bookingsForDay.observe(getViewLifecycleOwner(), bookings -> {
            if (bookings != null && scheduleRowAdapter != null) {
                scheduleRowAdapter.setBookingsForDay(bookings);
                showLoading(false);
            }
        });
    }

    private void observeCourtRelations() {
        stadiumViewModel.courtRelations.observe(getViewLifecycleOwner(), relationsMap -> {
            if (relationsMap != null) {
                this.courtRelations = relationsMap;
                Log.d("CourtRelations", "Court relations map updated: " + relationsMap.toString());
                if (scheduleRowAdapter != null) {
                    scheduleRowAdapter.setCourtRelations(relationsMap);
                }
            }
        });
    }

    private void setupScheduleUI(StadiumDTO stadium) {
        int openHour = DurationConverter.parseHour(stadium.openTime);
        int closeHour = DurationConverter.parseHour(stadium.closeTime);

        List<String> timeLabels = new ArrayList<>();
        for (int i = openHour; i <= closeHour; i++) {
            timeLabels.add(String.format(Locale.getDefault(),"%02d:00", i));
        }

        List<String> hourBlocks = new ArrayList<>();
        for (int i = openHour; i < closeHour; i++) {
            hourBlocks.add(String.format(Locale.getDefault(),"%02d:00", i));
        }

        horizontalRecyclerViews.clear();

        final RecyclerView.OnScrollListener horizontalListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isSyncingHorizontal) {
                    isSyncingHorizontal = true;
                    for (RecyclerView rv : horizontalRecyclerViews) {
                        if (rv != recyclerView) {
                            rv.scrollBy(dx, 0);
                        }
                    }
                    isSyncingHorizontal = false;
                }
            }
        };

        TimeHeaderAdapter timeHeaderAdapter = new TimeHeaderAdapter(timeLabels);
        binding.timeHeaderRecyclerview.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.timeHeaderRecyclerview.setAdapter(timeHeaderAdapter);
        horizontalRecyclerViews.add(binding.timeHeaderRecyclerview);
        binding.timeHeaderRecyclerview.addOnScrollListener(horizontalListener);

        List<CourtHeaderItem> courtHeaderItems = new ArrayList<>();
        if (stadium.courts != null && !stadium.courts.isEmpty()) {
            List<CourtsDTO> courts = new ArrayList<>(stadium.courts);
            int i = 0;
            while (i < courts.size()) {
                String currentSportType = courts.get(i).getSportType();
                int groupStartIndex = i;
                int groupEndIndex = i;
                while (groupEndIndex + 1 < courts.size() && courts.get(groupEndIndex + 1).getSportType().equals(currentSportType)) {
                    groupEndIndex++;
                }
                int middleIndex = groupStartIndex + (groupEndIndex - groupStartIndex) / 2;
                for (int j = groupStartIndex; j <= groupEndIndex; j++) {
                    boolean showSportType = (j == groupStartIndex);
                    boolean isCenter = (j == middleIndex);
                    courtHeaderItems.add(new CourtHeaderItem(courts.get(j).getName(), courts.get(j).getSportType(), showSportType, isCenter));
                }
                i = groupEndIndex + 1;
            }
        }

        binding.sportTypeHeaderRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        SportTypeHeaderAdapter sportTypeHeaderAdapter = new SportTypeHeaderAdapter(courtHeaderItems);
        binding.sportTypeHeaderRecyclerview.setAdapter(sportTypeHeaderAdapter);

        binding.courtHeaderRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));
        CourtHeaderAdapter courtHeaderAdapter = new CourtHeaderAdapter(courtHeaderItems);
        binding.courtHeaderRecyclerview.setAdapter(courtHeaderAdapter);

        binding.scheduleGridRecyclerview.setLayoutManager(new LinearLayoutManager(getContext()));

        scheduleRowAdapter = new ScheduleRowAdapter(
                new ArrayList<>(stadium.courts),
                hourBlocks,
                horizontalRecyclerViews,
                horizontalListener,
                selectedDate,
                bookingsForDay,
                null,
                this
        );
        binding.scheduleGridRecyclerview.setAdapter(scheduleRowAdapter);

        syncVerticalScrolling();
    }

    private void updateDateButtonText(Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        binding.btnDatePicker.setText(sdf.format(calendar.getTime()));
    }

    private void syncVerticalScrolling() {
        final RecyclerView.OnScrollListener verticalListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isSyncingVertical) {
                    isSyncingVertical = true;
                    if (recyclerView == binding.sportTypeHeaderRecyclerview) {
                        binding.courtHeaderRecyclerview.scrollBy(0, dy);
                        binding.scheduleGridRecyclerview.scrollBy(0, dy);
                    } else if (recyclerView == binding.courtHeaderRecyclerview) {
                        binding.sportTypeHeaderRecyclerview.scrollBy(0, dy);
                        binding.scheduleGridRecyclerview.scrollBy(0, dy);
                    } else {
                        binding.sportTypeHeaderRecyclerview.scrollBy(0, dy);
                        binding.courtHeaderRecyclerview.scrollBy(0, dy);
                    }
                    isSyncingVertical = false;
                }
            }
        };
        binding.sportTypeHeaderRecyclerview.addOnScrollListener(verticalListener);
        binding.courtHeaderRecyclerview.addOnScrollListener(verticalListener);
        binding.scheduleGridRecyclerview.addOnScrollListener(verticalListener);
    }

    @Override
    public void onCellClick(int courtId, int hour, boolean isSelected) {
        if (isSelected) {
            List<Integer> courtsInHour = selectedSlots.getOrDefault(hour, new ArrayList<>());
            if (!courtsInHour.contains(courtId)) {
                courtsInHour.add(courtId);
            }
            selectedSlots.put(hour, courtsInHour);
        } else {
            if (selectedSlots.containsKey(hour)) {
                selectedSlots.get(hour).remove(Integer.valueOf(courtId));
                if (selectedSlots.get(hour).isEmpty()) {
                    selectedSlots.remove(hour);
                }
            }
        }

        if (scheduleRowAdapter != null) {
            scheduleRowAdapter.updateSelections(selectedSlots);
        }

        updateStats();
    }

    @Override
    public boolean isInteractionAllowed() {
        return isInteractionAllowed;
    }

    private void updateStats() {
        int totalHours = 0;
        double totalPrice = 0;
        if (stadiumViewModel.stadiums.getValue() == null || stadiumViewModel.stadiums.getValue().getItems().isEmpty()) {
            return;
        }
        List<CourtsDTO> allCourts = new ArrayList<>(stadiumViewModel.stadiums.getValue().getItems().get(0).courts);

        for (Map.Entry<Integer, List<Integer>> entry : selectedSlots.entrySet()) {
            totalHours += entry.getValue().size();
            for (Integer courtId : entry.getValue()) {
                for (CourtsDTO court : allCourts) {
                    if (court.getId() == courtId) {
                        totalPrice += court.getPricePerHour();
                        break;
                    }
                }
            }
        }
        binding.tvTotalHours.setText(String.valueOf(totalHours));
        binding.tvTotalPrice.setText(String.format(Locale.getDefault(), "%,.0f VNĐ", totalPrice));
    }

    private ArrayList<SelectedCourtInfo> getSelectedCourtsGrouped() {
        ArrayList<SelectedCourtInfo> result = new ArrayList<>();
        Map<Integer, List<Integer>> courtsWithHours = new HashMap<>();
        for (Map.Entry<Integer, List<Integer>> entry : selectedSlots.entrySet()) {
            int hour = entry.getKey();
            for (int courtId : entry.getValue()) {
                courtsWithHours.computeIfAbsent(courtId, k -> new ArrayList<>()).add(hour);
            }
        }

        for (Map.Entry<Integer, List<Integer>> entry : courtsWithHours.entrySet()) {
            int courtId = entry.getKey();
            List<Integer> hours = entry.getValue();
            if (hours.isEmpty()) continue;

            Collections.sort(hours);

            List<String> timeRanges = new ArrayList<>();
            int startHour = hours.get(0);
            int endHour = startHour + 1;

            for (int i = 1; i < hours.size(); i++) {
                if (hours.get(i) == endHour) {
                    endHour = hours.get(i) + 1;
                } else {
                    timeRanges.add(String.format(Locale.US, "%02d:00-%02d:00", startHour, endHour));
                    startHour = hours.get(i);
                    endHour = startHour + 1;
                }
            }
            timeRanges.add(String.format(Locale.US, "%02d:00-%02d:00", startHour, endHour));

            result.add(new SelectedCourtInfo(courtId, timeRanges));
        }

        Log.d("CheckoutData", "Grouped data: " + result.toString());
        return result;
    }

    private void navigateToCheckout() {
        ArrayList<SelectedCourtInfo> groupedData = getSelectedCourtsGrouped();
        if (groupedData.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng chọn ít nhất một khung giờ", Toast.LENGTH_SHORT).show();
            return;
        }

        String dateString = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(selectedDate.getTime());
        float totalPrice = 0;
        try {
            totalPrice = Float.parseFloat(binding.tvTotalPrice.getText().toString().replaceAll("[^\\d]", ""));
        } catch (NumberFormatException e) {
            // Xử lý lỗi nếu không parse được
        }

        VisuallyBookingFragmentDirections.ActionVisuallyBookingFragmentToCheckoutFragment action =
                VisuallyBookingFragmentDirections.actionVisuallyBookingFragmentToCheckoutFragment(
                        stadiumId,
                        dateString,
                        totalPrice,
                        groupedData.toArray(new SelectedCourtInfo[0])
                );

        NavHostFragment.findNavController(this).navigate(action);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}