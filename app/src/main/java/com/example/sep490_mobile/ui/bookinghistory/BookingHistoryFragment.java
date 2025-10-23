package com.example.sep490_mobile.ui.bookinghistory;

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
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.adapter.BookingHistoryAdapter;
import com.example.sep490_mobile.databinding.FragmentBookingHistoryBinding;
import com.google.android.material.tabs.TabLayout;
import com.example.sep490_mobile.data.dto.booking.DailyBookingDTO; // Import if needed
import com.example.sep490_mobile.data.dto.booking.MonthlyBookingDTO; // Import if needed

import java.util.Collections;
import java.util.List; // Import List

public class BookingHistoryFragment extends Fragment {

    private static final String TAG = "BookingHistoryFrag_Log";

    private BookingHistoryViewModel viewModel;
    private BookingHistoryAdapter adapter;
    private FragmentBookingHistoryBinding binding;
    private BookingHistoryViewModel.BookingType currentType = BookingHistoryViewModel.BookingType.DAILY;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        binding = FragmentBookingHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");
        viewModel = new ViewModelProvider(this).get(BookingHistoryViewModel.class);

        setupRecyclerView();
        setupTabLayout();
        setupClickListeners();
        observeViewModel();

        setLoadingState(); // Set initial loading state
        viewModel.fetchInitialBookings(currentType); // Fetch first page of default tab
    }

    // Sets the initial loading state (main progress bar visible)
    private void setLoadingState() {
        Log.d(TAG, "Setting initial loading state");
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.progressBarLoadMore.setVisibility(View.GONE);
        binding.recyclerViewBookingHistory.setVisibility(View.GONE);
        binding.tvEmptyState.setVisibility(View.GONE);
        binding.btnLoadMoreContainer.setVisibility(View.GONE);
    }

    // Sets the loading state for "load more" (small progress bar visible)
    private void setLoadingMoreState(boolean isLoadingMore) {
        Log.d(TAG, "Setting load more state: " + isLoadingMore);
        binding.progressBarLoadMore.setVisibility(isLoadingMore ? View.VISIBLE : View.GONE);
        // Hide "Load More" button while loading more
        if (isLoadingMore) {
            binding.btnLoadMoreContainer.setVisibility(View.GONE);
        }
        // Don't hide RecyclerView while loading more
        binding.progressBar.setVisibility(View.GONE); // Ensure main progress bar is hidden
    }


    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView");
        adapter = new BookingHistoryAdapter(requireContext());
        binding.recyclerViewBookingHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewBookingHistory.setAdapter(adapter);
    }

    private void setupTabLayout() {
        binding.tabLayoutBookingType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.i(TAG, "Tab selected: " + tab.getPosition());
                currentType = (tab.getPosition() == 0) ?
                        BookingHistoryViewModel.BookingType.DAILY :
                        BookingHistoryViewModel.BookingType.MONTHLY;

                adapter.setData(Collections.emptyList()); // Clear adapter immediately
                setLoadingState(); // Show initial loading for the new tab
                viewModel.fetchInitialBookings(currentType); // Fetch data for the selected tab
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupClickListeners() {
        binding.btnBackToAccount.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            NavHostFragment.findNavController(BookingHistoryFragment.this).popBackStack();
        });

        binding.btnLoadMore.setOnClickListener(v -> {
            Log.i(TAG, "Load More button clicked for type: " + currentType);
            setLoadingMoreState(true); // Show small progress, hide button
            viewModel.fetchMoreBookings(currentType);
        });
    }

    private void observeViewModel() {
        Log.d(TAG, "setupObservers");

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG, "isLoading Observer: " + isLoading);
            boolean isLoadingMore = binding.progressBarLoadMore.getVisibility() == View.VISIBLE;

            // Quản lý ProgressBar
            if (isLoading && !isLoadingMore && adapter.getItemCount() == 0) {
                binding.progressBar.setVisibility(View.VISIBLE); // Chỉ hiện bar chính khi load ban đầu
                // Ẩn nội dung khi bắt đầu load ban đầu
                binding.recyclerViewBookingHistory.setVisibility(View.GONE);
                binding.tvEmptyState.setVisibility(View.GONE);
                binding.btnLoadMoreContainer.setVisibility(View.GONE);
            } else if (!isLoading) {
                binding.progressBar.setVisibility(View.GONE);
                setLoadingMoreState(false); // Ẩn cả bar nhỏ
                // Khi loading KẾT THÚC, gọi hàm cập nhật UI cuối cùng
                updateUiBasedOnData();
            }
            // Không làm gì nếu isLoading=true và đang load more (bar nhỏ đã hiện)
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            // ... (Xử lý lỗi như cũ, đảm bảo tắt hết loading và gọi updateUiBasedOnData nếu list rỗng) ...
            Log.d(TAG, "errorMessage Observer: " + error);
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                binding.progressBar.setVisibility(View.GONE);
                setLoadingMoreState(false);
                binding.btnLoadMoreContainer.setVisibility(View.GONE);

                if (adapter.getItemCount() == 0) {
                    binding.tvEmptyState.setText("Lỗi: " + error);
                    updateUiBasedOnData(); // Cập nhật để hiện empty state lỗi
                }
            }
        });

        // isLastPage chỉ lưu trạng thái, không trực tiếp đổi UI nút ở đây
        viewModel.getIsLastPage().observe(getViewLifecycleOwner(), isLast -> {
            Log.d(TAG, "isLastPage Observer: " + isLast + " (Will update button via updateUiBasedOnData)");
            // Gọi updateUiBasedOnData để cập nhật nút nếu không còn loading
            Boolean loading = viewModel.getIsLoading().getValue();
            if (loading != null && !loading) {
                updateUiBasedOnData();
            }
        });

        // Data Observers chỉ cập nhật Adapter và gọi updateUiBasedOnData
        viewModel.getDailyBookings().observe(getViewLifecycleOwner(), dailyBookings -> {
            Log.d(TAG, "dailyBookings Observer received: " + (dailyBookings != null ? dailyBookings.size() : "null"));
            if (currentType == BookingHistoryViewModel.BookingType.DAILY) {
                List<DailyBookingDTO> newlyFetched = viewModel.getLastFetchedDaily();
                boolean isInitialLoadOrTabSwitch = (adapter.getItemCount() == 0 && dailyBookings != null && !dailyBookings.isEmpty());
                boolean isLoadingMoreState = binding.progressBarLoadMore.getVisibility() == View.VISIBLE;

                if (isInitialLoadOrTabSwitch) {
                    adapter.setData(dailyBookings);
                } else if (newlyFetched != null && !newlyFetched.isEmpty() && isLoadingMoreState) {
                    adapter.appendData(newlyFetched);
                } else if (dailyBookings != null && !isLoadingMoreState){ // Cập nhật lại list nếu không phải load more (VD: refresh)
                    adapter.setData(dailyBookings);
                }
                // Luôn gọi updateUiBasedOnData sau khi adapter có thể đã thay đổi
                updateUiBasedOnData();
            }
        });

        viewModel.getMonthlyBookings().observe(getViewLifecycleOwner(), monthlyBookings -> {
            Log.d(TAG, "monthlyBookings Observer received: " + (monthlyBookings != null ? monthlyBookings.size() : "null"));
            if (currentType == BookingHistoryViewModel.BookingType.MONTHLY) {
                List<MonthlyBookingDTO> newlyFetched = viewModel.getLastFetchedMonthly();
                boolean isInitialLoadOrTabSwitch = (adapter.getItemCount() == 0 && monthlyBookings != null && !monthlyBookings.isEmpty());
                boolean isLoadingMoreState = binding.progressBarLoadMore.getVisibility() == View.VISIBLE;

                if (isInitialLoadOrTabSwitch) {
                    adapter.setData(monthlyBookings);
                } else if (newlyFetched != null && !newlyFetched.isEmpty() && isLoadingMoreState) {
                    adapter.appendData(newlyFetched);
                } else if (monthlyBookings != null && !isLoadingMoreState){
                    adapter.setData(monthlyBookings);
                }
                updateUiBasedOnData();
            }
        });
    }

    // *** THÊM HÀM MỚI NÀY ***
    /**
     * Hàm trung tâm để cập nhật trạng thái hiển thị cuối cùng của List/Empty/Button
     * Được gọi sau khi loading kết thúc HOẶC sau khi adapter được cập nhật.
     */
    private void updateUiBasedOnData() {
        // Chỉ thực hiện nếu không còn loading
        Boolean loading = viewModel.getIsLoading().getValue();
        if (loading != null && loading) {
            Log.d(TAG, "updateUiBasedOnData: Still loading, skipping UI update.");
            return; // Nếu vẫn đang loading thì chưa cập nhật UI cuối
        }
        Log.d(TAG, "updateUiBasedOnData: Updating final UI state.");

        boolean isEmpty = adapter.getItemCount() == 0;
        Log.d(TAG, "updateUiBasedOnData: isEmpty=" + isEmpty);

        // Cập nhật text empty state
        if (isEmpty) {
            // Kiểm tra xem có lỗi không
            String error = viewModel.getErrorMessage().getValue();
            if (error != null && !error.isEmpty()) {
                binding.tvEmptyState.setText("Lỗi: " + error);
            } else {
                binding.tvEmptyState.setText(currentType == BookingHistoryViewModel.BookingType.DAILY ?
                        R.string.no_daily_booking_history :
                        R.string.no_monthly_booking_history);
            }
        }

        // Cập nhật hiển thị List/Empty
        binding.recyclerViewBookingHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        // Cập nhật nút "Xem thêm"
        Boolean isLast = viewModel.getIsLastPage().getValue();
        boolean isLoadingMore = binding.progressBarLoadMore.getVisibility() == View.VISIBLE;
        // Hiện nút nếu isLastPage=false, list không rỗng, và không đang load more
        boolean shouldShowButton = (isLast != null && !isLast) && !isEmpty && !isLoadingMore;
        Log.d(TAG, "updateUiBasedOnData: Updating Load More Button -> isLast=" + isLast + ", isEmpty=" + isEmpty + ", isLoadingMore=" + isLoadingMore + ", shouldShow=" + shouldShowButton);
        binding.btnLoadMoreContainer.setVisibility(shouldShowButton ? View.VISIBLE : View.GONE);
    }

    // Updates visibility of RecyclerView vs Empty State TextView
    private void updateUiVisibility(boolean isEmpty) {
        Log.d(TAG, "updateUiVisibility: isEmpty=" + isEmpty);
        binding.tvEmptyState.setText(currentType == BookingHistoryViewModel.BookingType.DAILY ?
                R.string.no_daily_booking_history :
                R.string.no_monthly_booking_history);

        binding.recyclerViewBookingHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        // Update load more button state AFTER showing/hiding the main content
        Boolean isLast = viewModel.getIsLastPage().getValue();
        if(isLast != null) {
            updateLoadMoreButtonVisibility(isLast);
        }
    }

    // Updates visibility of the "Load More" button container
    private void updateLoadMoreButtonVisibility(boolean isLastPage) {
        // Show button only if NOT last page AND list is NOT empty AND NOT currently loading more
        boolean isLoadingMore = binding.progressBarLoadMore.getVisibility() == View.VISIBLE;
        boolean shouldShow = !isLastPage && adapter.getItemCount() > 0 && !isLoadingMore;
        Log.d(TAG, "updateLoadMoreButtonVisibility: isLastPage=" + isLastPage + ", itemCount=" + adapter.getItemCount() + ", isLoadingMore="+ isLoadingMore + ", shouldShow=" + shouldShow);
        binding.btnLoadMoreContainer.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        binding = null;
    }
}