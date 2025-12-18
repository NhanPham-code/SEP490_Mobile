package com.example.sep490_mobile.ui.discount; // Đảm bảo đúng package

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.adapter.DiscountAdapter;
import com.example.sep490_mobile.data.dto.discount.ReadDiscountDTO;
import com.example.sep490_mobile.viewmodel.DiscountViewModel;
import com.google.android.material.tabs.TabLayout;

import java.util.Collections;
import java.util.List;

public class DiscountListFragment extends Fragment {

    private static final String TAG = "DiscountListFrag_Log";

    // --- Các biến ---
    private DiscountViewModel viewModel;
    private DiscountAdapter adapter;
    private DiscountViewModel.DiscountType currentType = DiscountViewModel.DiscountType.PERSONAL;

    // --- Khai báo View components ---
    private RecyclerView recyclerViewDiscounts;
    private ProgressBar progressBar;
    private ProgressBar progressBarLoadMore;
    private TextView tvEmptyState;
    private TabLayout tabLayoutDiscountType;
    private LinearLayout btnLoadMoreDiscountContainer;
    private Button btnLoadMore;
    private ImageButton btnBackToAccount;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_discount_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");

        // --- Ánh xạ Views ---
        recyclerViewDiscounts = view.findViewById(R.id.recycler_view_discounts);
        progressBar = view.findViewById(R.id.progress_bar);
        progressBarLoadMore = view.findViewById(R.id.progress_bar_load_more);
        tvEmptyState = view.findViewById(R.id.tv_empty_state);
        tabLayoutDiscountType = view.findViewById(R.id.tab_layout_discount_type);
        btnLoadMoreDiscountContainer = view.findViewById(R.id.btn_load_more_discount_container);
        btnLoadMore = view.findViewById(R.id.btn_load_more);
        btnBackToAccount = view.findViewById(R.id.btn_back_to_account);

        // Lấy ViewModel
        viewModel = new ViewModelProvider(this).get(DiscountViewModel.class);

        // Thiết lập UI và logic
        setupRecyclerView();
        setupTabLayout();
        setupClickListeners();
        observeViewModel();

        // Kiểm tra xem đã có dữ liệu chưa trước khi gọi API
        boolean hasData = false;
        if (currentType == DiscountViewModel.DiscountType.PERSONAL) {
            List<ReadDiscountDTO> list = viewModel.getPersonalDiscounts().getValue();
            hasData = (list != null && !list.isEmpty());
        } else {
            List<ReadDiscountDTO> list = viewModel.getFavoriteStadiumDiscounts().getValue();
            hasData = (list != null && !list.isEmpty());
        }

        // Nếu chưa có dữ liệu thì mới load và hiện loading
        if (!hasData) {
            setLoadingState();
            viewModel.fetchInitialDiscounts(currentType);
        }
        // Nếu đã có dữ liệu (quay lại từ Detail), Adapter sẽ tự nhận qua Observer, không cần fetch lại

        ensureCorrectTabSelection();
    }

    // --- Hàm thiết lập RecyclerView ---
    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView");
        adapter = new DiscountAdapter(requireContext());
        recyclerViewDiscounts.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewDiscounts.setAdapter(adapter);
    }

    // --- Hàm thiết lập TabLayout ---
    private void setupTabLayout() {
        tabLayoutDiscountType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.i(TAG, "Tab selected: " + tab.getPosition());
                DiscountViewModel.DiscountType selectedType = (tab.getPosition() == 0) ?
                        DiscountViewModel.DiscountType.PERSONAL :
                        DiscountViewModel.DiscountType.FAVORITE;

                // Chỉ thực hiện nếu tab thực sự thay đổi
                if (selectedType != currentType) {
                    currentType = selectedType;
                    setLoadingState();

                    if (adapter != null) {
                        adapter.setData(Collections.emptyList());
                        Log.d(TAG, "Adapter cleared on tab change.");
                    }

                    viewModel.fetchInitialDiscounts(currentType); // Tải dữ liệu trang đầu
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    // --- HÀM MỚI: Đảm bảo tab được chọn đúng ---
    private void ensureCorrectTabSelection() {
        int targetTabIndex = (currentType == DiscountViewModel.DiscountType.PERSONAL) ? 0 : 1;
        if (tabLayoutDiscountType != null && tabLayoutDiscountType.getSelectedTabPosition() != targetTabIndex) {
            TabLayout.Tab tab = tabLayoutDiscountType.getTabAt(targetTabIndex);
            if (tab != null) {
                tab.select();
                Log.d(TAG, "Ensured correct tab selection: " + targetTabIndex);
            }
        }
    }

    // --- Hàm thiết lập Click Listeners ---
    private void setupClickListeners() {
        btnBackToAccount.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            NavHostFragment.findNavController(DiscountListFragment.this).popBackStack();
        });

        btnLoadMore.setOnClickListener(v -> {
            Log.i(TAG, "Load More button clicked for type: " + currentType);
            setLoadingMoreState(true);
            viewModel.fetchMoreDiscounts(currentType);
        });
    }

    // --- Các hàm quản lý trạng thái UI ---
    private void setLoadingState() {
        Log.d(TAG, "Setting initial loading state");
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        if (progressBarLoadMore != null) progressBarLoadMore.setVisibility(View.GONE);
        if (recyclerViewDiscounts != null) recyclerViewDiscounts.setVisibility(View.GONE); // Ẩn list khi load lại từ đầu
        if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
        if (btnLoadMoreDiscountContainer != null) btnLoadMoreDiscountContainer.setVisibility(View.GONE);
    }

    private void setLoadingMoreState(boolean isLoadingMore) {
        Log.d(TAG, "Setting load more state: " + isLoadingMore);
        if (progressBarLoadMore != null) progressBarLoadMore.setVisibility(isLoadingMore ? View.VISIBLE : View.GONE);
        if (isLoadingMore && btnLoadMoreDiscountContainer != null) {
            btnLoadMoreDiscountContainer.setVisibility(View.GONE);
        }
        if (progressBar != null) progressBar.setVisibility(View.GONE); // Luôn ẩn bar chính khi load more
    }

    // --- Hàm thiết lập Observers (Đã sửa) ---
    private void observeViewModel() {
        Log.d(TAG, "setupObservers");

        // Observer isLoading: Chỉ quản lý ProgressBar chính
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            Log.d(TAG, "isLoading Observer: " + isLoading);
            if (progressBar != null) {
                if (isLoading && (adapter == null || adapter.getItemCount() == 0)) {
                    // Hiện bar chính KHI BẮT ĐẦU loading ban đầu
                    progressBar.setVisibility(View.VISIBLE);
                    // Ẩn nội dung khi bắt đầu load
                    if (recyclerViewDiscounts != null) recyclerViewDiscounts.setVisibility(View.GONE);
                    if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
                    if (btnLoadMoreDiscountContainer != null) btnLoadMoreDiscountContainer.setVisibility(View.GONE);
                } else if (!isLoading) {
                    // Ẩn bar chính KHI KẾT THÚC loading
                    progressBar.setVisibility(View.GONE);
                    setLoadingMoreState(false); // Đảm bảo bar nhỏ cũng tắt
                    updateUiBasedOnData();
                }
            }
        });

        // Observer errorMessage: Xử lý lỗi
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            Log.d(TAG, "errorMessage Observer: " + error);
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                setLoadingMoreState(false);
                if (btnLoadMoreDiscountContainer != null) btnLoadMoreDiscountContainer.setVisibility(View.GONE);

                if (adapter != null && adapter.getItemCount() == 0) {
                    if (tvEmptyState != null) tvEmptyState.setText("Lỗi: " + error);
                    updateUiBasedOnData(); // Gọi để hiện Empty State lỗi
                }
            }
        });

        // Observer isLastPage: Chỉ lưu trạng thái, gọi update UI nếu cần
        viewModel.getIsLastPage().observe(getViewLifecycleOwner(), isLast -> {
            Log.d(TAG, "isLastPage Observer: " + isLast);
            Boolean loading = viewModel.getIsLoading().getValue();
            boolean isLoadingMore = progressBarLoadMore != null && progressBarLoadMore.getVisibility() == View.VISIBLE;
            if (loading != null && !loading && !isLoadingMore) {
                updateUiBasedOnData();
            }
        });


        // Observer Dữ liệu Mã Cá Nhân
        viewModel.getPersonalDiscounts().observe(getViewLifecycleOwner(), personalList -> {
            Log.d(TAG, "personalDiscounts Observer received: " + (personalList != null ? personalList.size() : "null"));
            if (currentType == DiscountViewModel.DiscountType.PERSONAL && adapter != null) {
                List<ReadDiscountDTO> newlyFetched = viewModel.getLastFetchedPersonal();
                boolean isInitialUpdate = (adapter.getItemCount() == 0 && personalList != null && !personalList.isEmpty()); // Kiểm tra nếu adapter rỗng

                if (isInitialUpdate) {
                    Log.d(TAG, "Personal Observer: Initial update, using setData.");
                    adapter.setData(personalList);
                } else if (newlyFetched != null && !newlyFetched.isEmpty()) { // Chỉ append nếu có dữ liệu mới
                    Log.d(TAG, "Personal Observer: Appending data (" + newlyFetched.size() + ").");
                    adapter.appendData(newlyFetched);
                } else {
                    Log.d(TAG, "Personal Observer: Not initial and no new items fetched (or list is empty). Setting data.");
                    // Cập nhật lại adapter với list hiện tại (có thể rỗng) nếu không phải lần đầu và không có gì mới
                    adapter.setData(personalList != null ? personalList : Collections.emptyList());
                }
                // <<< Gọi updateUiBasedOnData SAU KHI cập nhật adapter >>>
                updateUiBasedOnData();
            }
        });

        // Observer Dữ liệu Mã Sân Yêu Thích
        viewModel.getFavoriteStadiumDiscounts().observe(getViewLifecycleOwner(), favoriteList -> {
            Log.d(TAG, "favoriteDiscounts Observer received: " + (favoriteList != null ? favoriteList.size() : "null"));
            if (currentType == DiscountViewModel.DiscountType.FAVORITE && adapter != null) {
                List<ReadDiscountDTO> newlyFetched = viewModel.getLastFetchedFavorite();
                boolean isInitialUpdate = (adapter.getItemCount() == 0 && favoriteList != null && !favoriteList.isEmpty());

                if (isInitialUpdate) {
                    Log.d(TAG, "Favorite Observer: Initial update, using setData.");
                    adapter.setData(favoriteList);
                } else if (newlyFetched != null && !newlyFetched.isEmpty()) {
                    Log.d(TAG, "Favorite Observer: Appending data (" + newlyFetched.size() + ").");
                    adapter.appendData(newlyFetched);
                } else {
                    Log.d(TAG, "Favorite Observer: Not initial and no new items fetched (or list is empty). Setting data.");
                    adapter.setData(favoriteList != null ? favoriteList : Collections.emptyList());
                }
                // <<< Gọi updateUiBasedOnData SAU KHI cập nhật adapter >>>
                updateUiBasedOnData();
            }
        });
    }

    // --- Hàm cập nhật UI cuối cùng ---
    private void updateUiBasedOnData() {
        // Thêm kiểm tra null cho các view trước khi sử dụng
        if (viewModel == null || adapter == null || tvEmptyState == null || recyclerViewDiscounts == null || btnLoadMoreDiscountContainer == null || progressBarLoadMore == null) {
            Log.w(TAG, "updateUiBasedOnData: Views not ready, skipping."); return;
        }

        // KHÔNG cần kiểm tra isLoading ở đây nữa

        Log.d(TAG, "updateUiBasedOnData: Updating final UI state.");

        boolean isEmpty = adapter.getItemCount() == 0; // <<< Lấy itemCount MỚI NHẤT
        Log.d(TAG, "updateUiBasedOnData: isEmpty=" + isEmpty);

        // Cập nhật text empty state
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

        // Cập nhật visibility List/Empty
        recyclerViewDiscounts.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        tvEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        // Cập nhật visibility Nút Xem Thêm
        Boolean isLast = viewModel.getIsLastPage().getValue();
        // Kiểm tra isLoadingMore ở đây để ẩn nút khi đang load
        boolean isLoadingMore = progressBarLoadMore.getVisibility() == View.VISIBLE;
        boolean shouldShowButton = (isLast != null && !isLast) && !isEmpty && !isLoadingMore;
        Log.d(TAG, "updateUiBasedOnData: Updating Load More Button -> isLast=" + isLast + ", isEmpty=" + isEmpty + ", isLoadingMore=" + isLoadingMore + ", shouldShow=" + shouldShowButton);
        btnLoadMoreDiscountContainer.setVisibility(shouldShowButton ? View.VISIBLE : View.GONE);
    }

    // --- onDestroyView ---
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        if (recyclerViewDiscounts != null) {
            recyclerViewDiscounts.setAdapter(null); // Giải phóng adapter
        }
        // Gán null cho các view
        recyclerViewDiscounts = null; progressBar = null; progressBarLoadMore = null;
        tvEmptyState = null; tabLayoutDiscountType = null; btnLoadMoreDiscountContainer = null;
        btnLoadMore = null; btnBackToAccount = null;
    }
}