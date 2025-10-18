package com.example.sep490_mobile.ui.booking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.adapter.BookingHistoryAdapter;
import com.example.sep490_mobile.databinding.FragmentBookingHistoryBinding;

public class BookingHistoryFragment extends Fragment {

    private BookingHistoryViewModel viewModel;
    private BookingHistoryAdapter adapter;
    private FragmentBookingHistoryBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBookingHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(BookingHistoryViewModel.class);

        setupRecyclerView();
        setupSpinners();
        observeViewModel();

        // Xử lý sự kiện click cho nút "Quay lại"
        binding.btnBackToAccount.setOnClickListener(v -> {
            NavHostFragment.findNavController(BookingHistoryFragment.this).popBackStack();
        });

        viewModel.fetchAndProcessBookingHistory();
    }

    private void setupRecyclerView() {
        adapter = new BookingHistoryAdapter(requireContext());
        binding.recyclerViewBookingHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewBookingHistory.setAdapter(adapter);
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.booking_types_array, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerBookingType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.booking_status_array, android.R.layout.simple_spinner_item);
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerStatus.setAdapter(statusAdapter);

        AdapterView.OnItemSelectedListener filterListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                applyFilters();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        };

        binding.spinnerBookingType.setOnItemSelectedListener(filterListener);
        binding.spinnerStatus.setOnItemSelectedListener(filterListener);
    }

    private void applyFilters() {
        if (adapter == null) return;
        String typeFilter = binding.spinnerBookingType.getSelectedItem().toString();
        String statusFilter = binding.spinnerStatus.getSelectedItem().toString();
        adapter.filterData(typeFilter, statusFilter);
        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean isEmpty = adapter.getItemCount() == 0;
        binding.tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.recyclerViewBookingHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.recyclerViewBookingHistory.setVisibility(View.GONE);
                binding.tvEmptyState.setVisibility(View.GONE);
            } else {
                binding.progressBar.setVisibility(View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getProcessedBookingHistory().observe(getViewLifecycleOwner(), data -> {
            if (data != null) {
                adapter.setData(data);
                applyFilters();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}