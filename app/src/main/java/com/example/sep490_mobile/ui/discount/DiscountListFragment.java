package com.example.sep490_mobile.ui.discount;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Import Button
import android.widget.ImageButton; // Import ImageButton
import android.widget.LinearLayout; // Import LinearLayout
import android.widget.ProgressBar; // Import ProgressBar
import android.widget.TextView; // Import TextView
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView; // Import RecyclerView

import com.example.sep490_mobile.R; // Đảm bảo import R đúng
import com.example.sep490_mobile.adapter.DiscountAdapter;
import com.example.sep490_mobile.data.dto.discount.ReadDiscountDTO;
import com.example.sep490_mobile.viewmodel.DiscountViewModel;
import com.google.android.material.tabs.TabLayout; // Import TabLayout

import java.util.Collections;
import java.util.List;

public class DiscountListFragment extends Fragment {

    // Giữ nguyên TAG Log
    private static final String TAG = "DiscountListFrag_Log";

    // --- Các biến ---
    private DiscountViewModel viewModel;
    private DiscountAdapter adapter;
    private DiscountViewModel.DiscountType currentType = DiscountViewModel.DiscountType.PERSONAL;

    // --- Khai báo các View components ---
    private RecyclerView recyclerViewDiscounts;
    private ProgressBar progressBar;
    private ProgressBar progressBarLoadMore;
    private TextView tvEmptyState;
    private TabLayout tabLayoutDiscountType;
    private LinearLayout btnLoadMoreDiscountContainer;
    private Button btnLoadMore; // Sửa kiểu thành Button (hoặc MaterialButton)
    private ImageButton btnBackToAccount;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate layout gốc
        View view = inflater.inflate(R.layout.fragment_discount_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");

        // --- Ánh xạ Views bằng findViewById ---
        recyclerViewDiscounts = view.findViewById(R.id.recycler_view_discounts);
        progressBar = view.findViewById(R.id.progress_bar);
        progressBarLoadMore = view.findViewById(R.id.progress_bar_load_more);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        tabLayoutDiscountType = view.findViewById(R.id.tab_layout_discount_type);
        btnLoadMoreDiscountContainer = view.findViewById(R.id.btn_load_more_discount_container);
        // ID btn_load_more là của MaterialButton bên trong LinearLayout
        btnLoadMore = view.findViewById(R.id.btn_load_more);
        btnBackToAccount = view.findViewById(R.id.btn_back_to_account);


        // Lấy ViewModel
        viewModel = new ViewModelProvider(this).get(DiscountViewModel.class);

        // Thiết lập UI và logic
        setupRecyclerView();
        setupTabLayout();
        setupClickListeners();
        observeViewModel();

        // Tải dữ liệu ban đầu
        setLoadingState();
        viewModel.fetchInitialDiscounts(currentType);
    }

    // --- Hàm thiết lập RecyclerView (Dùng findViewById) ---
    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView");
        adapter = new DiscountAdapter(requireContext());
        recyclerViewDiscounts.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewDiscounts.setAdapter(adapter);
    }

    // --- Hàm thiết lập TabLayout (Dùng findViewById) ---
    private void setupTabLayout() {
        tabLayoutDiscountType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.i(TAG, "Tab selected: " + tab.getPosition());
                currentType = (tab.getPosition() == 0) ?
                        DiscountViewModel.DiscountType.PERSONAL :
                        DiscountViewModel.DiscountType.FAVORITE;

                adapter.setData(Collections.emptyList());
                setLoadingState();
                viewModel.fetchInitialDiscounts(currentType);
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // --- Hàm thiết lập Click Listeners (Dùng findViewById) ---
    private void setupClickListeners() {
        btnBackToAccount.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            NavHostFragment.findNavController(DiscountListFragment.this).popBackStack();
        });

        // Listener cho nút Load More bên trong LinearLayout
        btnLoadMore.setOnClickListener(v -> {
            Log.i(TAG, "Load More button clicked for type: " + currentType);
            setLoadingMoreState(true);
            viewModel.fetchMoreDiscounts(currentType);
        });
    }

    // --- Các hàm quản lý trạng thái UI (Dùng findViewById) ---
    private void setLoadingState() {
        Log.d(TAG, "Setting initial loading state");
        progressBar.setVisibility(View.VISIBLE);
        progressBarLoadMore.setVisibility(View.GONE);
        recyclerViewDiscounts.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.GONE);
        btnLoadMoreDiscountContainer.setVisibility(View.GONE);
    }

    private void setLoadingMoreState(boolean isLoadingMore) {
        Log.d(TAG, "Setting load more state: " + isLoadingMore);
        progressBarLoadMore.setVisibility(isLoadingMore ? View.VISIBLE : View.GONE);
        if (isLoadingMore) {
            btnLoadMoreDiscountContainer.setVisibility(View.GONE);
        }
        progressBar.setVisibility(View.GONE);
    }

    // --- Hàm thiết lập Observers (Logic giữ nguyên, chỉ thay đổi cách truy cập View) ---
    private void observeViewModel() {
        Log.d(TAG, "setupObservers");

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG, "isLoading Observer: " + isLoading);
            boolean isLoadingMore = progressBarLoadMore.getVisibility() == View.VISIBLE;

            if (isLoading && !isLoadingMore && adapter.getItemCount() == 0) {
                // Đang load ban đầu -> gọi setLoadingState() cho chắc
                setLoadingState();
            } else if (!isLoading) {
                // Load xong (bất kể là ban đầu hay load more)
                progressBar.setVisibility(View.GONE);
                setLoadingMoreState(false); // Ẩn bar nhỏ
                // Cập nhật UI cuối cùng
                updateUiBasedOnData();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            Log.d(TAG, "errorMessage Observer: " + error);
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                setLoadingMoreState(false);
                btnLoadMoreDiscountContainer.setVisibility(View.GONE);

                if (adapter.getItemCount() == 0) {
                    tvEmptyState.setText("Lỗi: " + error);
                    // Gọi updateUiBasedOnData để hiện Empty State lỗi
                    updateUiBasedOnData();
                }
            }
        });

        viewModel.getIsLastPage().observe(getViewLifecycleOwner(), isLast -> {
            Log.d(TAG, "isLastPage Observer: " + isLast);
            // Gọi hàm cập nhật UI để nó kiểm tra lại nút "Xem thêm"
            updateUiBasedOnData();
        });

        viewModel.getPersonalDiscounts().observe(getViewLifecycleOwner(), personalList -> {
            Log.d(TAG, "personalDiscounts Observer received: " + (personalList != null ? personalList.size() : "null"));
            if (currentType == DiscountViewModel.DiscountType.PERSONAL) {
                List<ReadDiscountDTO> newlyFetched = viewModel.getLastFetchedPersonal();
                boolean isLoadingMoreState = progressBarLoadMore.getVisibility() == View.VISIBLE;
                boolean isInitialLoad = (adapter.getItemCount() == 0 && personalList != null && !personalList.isEmpty());

                if (isInitialLoad) {
                    adapter.setData(personalList);
                } else if (newlyFetched != null && !newlyFetched.isEmpty() && isLoadingMoreState) {
                    adapter.appendData(newlyFetched);
                } else if (personalList != null && !isLoadingMoreState){
                    adapter.setData(personalList);
                } else if (personalList == null || personalList.isEmpty()) {
                    adapter.setData(Collections.emptyList());
                }
                updateUiBasedOnData(); // Cập nhật UI sau khi adapter thay đổi
            }
        });

        viewModel.getFavoriteStadiumDiscounts().observe(getViewLifecycleOwner(), favoriteList -> {
            Log.d(TAG, "favoriteDiscounts Observer received: " + (favoriteList != null ? favoriteList.size() : "null"));
            if (currentType == DiscountViewModel.DiscountType.FAVORITE) {
                List<ReadDiscountDTO> newlyFetched = viewModel.getLastFetchedFavorite();
                boolean isLoadingMoreState = progressBarLoadMore.getVisibility() == View.VISIBLE;
                boolean isInitialLoad = (adapter.getItemCount() == 0 && favoriteList != null && !favoriteList.isEmpty());

                if (isInitialLoad) {
                    adapter.setData(favoriteList);
                } else if (newlyFetched != null && !newlyFetched.isEmpty() && isLoadingMoreState) {
                    adapter.appendData(newlyFetched);
                } else if (favoriteList != null && !isLoadingMoreState){
                    adapter.setData(favoriteList);
                } else if (favoriteList == null || favoriteList.isEmpty()){
                    adapter.setData(Collections.emptyList());
                }
                updateUiBasedOnData();
            }
        });
    }

    // --- Hàm cập nhật UI cuối cùng (Dùng findViewById) ---
    private void updateUiBasedOnData() {
        Boolean loading = viewModel.getIsLoading().getValue();
        boolean isLoadingMore = progressBarLoadMore.getVisibility() == View.VISIBLE;
        if ((loading != null && loading) || isLoadingMore) {
            Log.d(TAG, "updateUiBasedOnData: Still loading, skipping UI update.");
            return;
        }
        Log.d(TAG, "updateUiBasedOnData: Updating final UI state.");

        boolean isEmpty = adapter.getItemCount() == 0;
        Log.d(TAG, "updateUiBasedOnData: isEmpty=" + isEmpty);

        if (isEmpty) {
            String error = viewModel.getErrorMessage().getValue();
            if (error != null && !error.isEmpty()) {
                tvEmptyState.setText("Lỗi: " + error);
            } else {
                tvEmptyState.setText(currentType == DiscountViewModel.DiscountType.PERSONAL ?
                        R.string.no_personal_discounts :
                        R.string.no_favorite_discounts);
            }
        }

        recyclerViewDiscounts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        Boolean isLast = viewModel.getIsLastPage().getValue();
        boolean shouldShowButton = (isLast != null && !isLast) && !isEmpty;
        Log.d(TAG, "updateUiBasedOnData: Updating Load More Button -> isLast=" + isLast + ", isEmpty=" + isEmpty + ", shouldShow=" + shouldShowButton);
        btnLoadMoreDiscountContainer.setVisibility(shouldShowButton ? View.VISIBLE : View.GONE);
    }

    // --- onDestroyView (Không dùng binding) ---
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        // Không cần gán binding = null
        // Đảm bảo các listener được gỡ bỏ nếu cần (RecyclerView tự xử lý adapter)
        recyclerViewDiscounts.setAdapter(null); // Giúp giải phóng tham chiếu
    }
}