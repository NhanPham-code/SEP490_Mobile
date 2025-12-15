package com.example.sep490_mobile.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sep490_mobile.adapter.StadiumAdapter;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.FeedbackDto;
import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.data.mapper.FeedbackMapper;
import com.example.sep490_mobile.data.repository.FeedbackRepository;
import com.example.sep490_mobile.interfaces.OnItemClickListener;
import com.example.sep490_mobile.databinding.FragmentHomeBinding;
import com.example.sep490_mobile.interfaces.OnFavoriteClickListener;
import com.example.sep490_mobile.model.Feedback;
import com.example.sep490_mobile.ui.stadiumDetail.StadiumDetailFragment;
import com.example.sep490_mobile.utils.removeVietnameseSigns;
import com.example.sep490_mobile.viewmodel.FavoriteViewModel;
import com.example.sep490_mobile.viewmodel.StadiumViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import androidx.annotation.Nullable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements OnItemClickListener, OnFavoriteClickListener {

    private FragmentHomeBinding binding;


    private StadiumAdapter adapter;
    private StadiumViewModel stadiumViewModel;
    private FavoriteViewModel favoriteViewModel;

    // Data sources
    private List<StadiumDTO> fullStadiumList = new ArrayList<>();
    private Set<Integer> favoriteIdsSet = new HashSet<>();

    // State management
    private boolean isFavoriteMode = false;

    // Pagination and Search
    private Map<String, String> odataUrl = new HashMap<>();
    private int skip = 0;
    private int count = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private final int PAGE_SIZE = 10;

    private FeedbackRepository feedbackRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViewModels();
        initRecyclerView();
        initListeners();
        feedbackRepository = new FeedbackRepository(requireContext()); // <- Khởi tạo repo

        // ... các gọi dữ liệu khác ...

        // ⭐ LẤY TOÀN BỘ FEEDBACK VÀ TÍNH SỐ SAO TRUNG BÌNH
        Map<String, String> odataOptions = new HashMap<>(); // Không filter
        feedbackRepository.getFeedbacks(odataOptions).enqueue(new Callback<ODataResponse<FeedbackDto>>() {
            @Override
            public void onResponse(Call<ODataResponse<FeedbackDto>> call, Response<ODataResponse<FeedbackDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FeedbackDto> feedbackDtoList = response.body().getItems();
                    // Group và tính average
                    Map<Integer, Float> averageRatingMap = FeedbackMapper.calculateAverageRatingByStadium(feedbackDtoList);
                    adapter.setAverageRatings(averageRatingMap);
                }
            }
            @Override
            public void onFailure(Call<ODataResponse<FeedbackDto>> call, Throwable t) {
                // Xử lý lỗi nếu muốn
            }
        });
        showLoading();
        setupInitialOdataUrl();
        stadiumViewModel.fetchStadium(odataUrl); // Tải dữ liệu ban đầu
        favoriteViewModel.fetchFavoriteStadiums(); // Tải danh sách yêu thích ban đầu


        setupObservers();
        setupPagination();
        filterStadiums();
    }

    private void filterStadiums() {
        SharedViewModel model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        model.getSelected().observe(getViewLifecycleOwner(), stringStringMap -> {
            if (stringStringMap != null) {
                odataUrl.put("$filter", stringStringMap.get("$filter"));
                performSearch();
            }
        });
    }

    private void initViewModels() {
        stadiumViewModel = new ViewModelProvider(this).get(StadiumViewModel.class);
        favoriteViewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);
    }

    private void initRecyclerView() {
        // Khởi tạo Adapter và truyền các listener cần thiết
        adapter = new StadiumAdapter(getContext(), this, this);
        binding.myRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.myRecyclerView.setAdapter(adapter);
    }

    private void initListeners() {
        binding.filterButton.setOnClickListener(v -> navigateToFilterFragment());
        binding.favoriteListButton.setOnClickListener(v -> toggleFavoriteMode());
        setupSearchListener();
    }

    private void setupInitialOdataUrl() {
        odataUrl.put("$expand", "Courts,StadiumImages,StadiumVideos");
        odataUrl.put("$count", "true");
        odataUrl.put("$top", String.valueOf(PAGE_SIZE));
        odataUrl.put("$skip", "0");
    }

    private void setupObservers() {
        // 1. Lắng nghe danh sách sân vận động từ server
        stadiumViewModel.stadiums.observe(getViewLifecycleOwner(), response -> {
            hideLoading();
            if (response != null && response.getItems() != null) {
                count = Integer.parseInt(response.getCount() + "");
                fullStadiumList.clear(); // Xóa dữ liệu cũ khi có tìm kiếm hoặc filter mới
                fullStadiumList.addAll(response.getItems());
                updateAdapterData();
            }
        });

        // 2. Lắng nghe danh sách sân được load thêm (phân trang)
        stadiumViewModel.newStadiums.observe(getViewLifecycleOwner(), newItemsResponse -> {
            isLoading = false;
            if (newItemsResponse != null && newItemsResponse.getItems() != null) {
                fullStadiumList.addAll(newItemsResponse.getItems());
                updateAdapterData();
            }
        });

        // 3. Lắng nghe danh sách ID sân yêu thích
        favoriteViewModel.favoriteStadiumIds.observe(getViewLifecycleOwner(), favoriteIds -> {
            if (favoriteIds != null) {
                this.favoriteIdsSet = favoriteIds;
                // Cập nhật lại adapter để vẽ lại icon trái tim và lọc danh sách nếu cần
                updateAdapterData();
            }
        });

        // 4. Lắng nghe thông báo (Toast) từ FavoriteViewModel
        favoriteViewModel.toastMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                favoriteViewModel.onToastMessageShown(); // Reset để không hiển thị lại
            }
        });

        // 5. Lắng nghe trạng thái loading từ cả hai viewmodel
        stadiumViewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            if(loading) showLoading(); else hideLoading();
        });
        favoriteViewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            if(loading) showLoading(); else hideLoading();
        });
    }

    /**
     * Chuyển đổi giữa chế độ xem tất cả sân và chỉ xem sân yêu thích.
     */
    private void toggleFavoriteMode() {
        if(requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).getInt("user_id", -1) == -1) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem sân yêu thích", Toast.LENGTH_SHORT).show();
            return;
        }

        isFavoriteMode = !isFavoriteMode;
        if (isFavoriteMode) {
            binding.favoriteListButton.setImageResource(R.drawable.ic_favorite_filled);
            Toast.makeText(getContext(), "Các sân yêu thích", Toast.LENGTH_SHORT).show();
        } else {
            binding.favoriteListButton.setImageResource(R.drawable.ic_favorite_border);
            Toast.makeText(getContext(), "Tất cả sân", Toast.LENGTH_SHORT).show();
        }
        // Cập nhật lại dữ liệu hiển thị trên RecyclerView
        updateAdapterData();
    }

    /**
     * Phương thức trung tâm: quyết định danh sách nào sẽ được hiển thị và cập nhật adapter.
     */
    private void updateAdapterData() {
        if (fullStadiumList == null) return;

        List<StadiumDTO> listToShow;
        if (isFavoriteMode) {
            // Chế độ yêu thích: Lọc danh sách đầy đủ để chỉ lấy các sân có ID trong favoriteIdsSet
            listToShow = fullStadiumList.stream()
                    .filter(stadium -> favoriteIdsSet.contains(stadium.getId()))
                    .collect(Collectors.toList());
        } else {
            // Chế độ bình thường: Hiển thị toàn bộ danh sách đã tải
            listToShow = fullStadiumList;
        }
        // Cập nhật cả danh sách sân và danh sách ID yêu thích cho adapter
        adapter.setData(listToShow, favoriteIdsSet);
    }

    /**
     * Được gọi khi người dùng click vào icon trái tim trên một sân.
     * @param stadiumId ID của sân được click.
     */
    @Override
    public void onFavoriteClick(int stadiumId) {
        // Lấy userId hiện tại từ lớp quản lý đăng nhập của bạn
        int userId = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).getInt("user_id", -1);
        if (userId != -1) {
            favoriteViewModel.toggleFavoriteStatus(stadiumId, userId);
        } else {
            // Có thể điều hướng đến màn hình đăng nhập nếu người dùng chưa đăng nhập
            Toast.makeText(getContext(), "Vui lòng đăng nhập để sử dụng chức năng này", Toast.LENGTH_SHORT).show();
        }
    }

    // --- CÁC PHƯƠNG THỨC CHO TÌM KIẾM, PHÂN TRANG, ĐIỀU HƯỚNG ---
    @Override
    public void onItemClick(int item) {
        // Không dùng trong HomeFragment, để trống cũng được
    }
    private void setupSearchListener() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchQuery = s.toString();
                resetPagination(); // Reset phân trang khi có tìm kiếm mới
                odataUrl.put("$top", String.valueOf(PAGE_SIZE));
                odataUrl.put("$skip", "0");

                if (searchQuery.isEmpty()) {
                    odataUrl.remove("$filter");
                } else {
                    String unsignedQuery = removeVietnameseSigns.removeVietnameseSigns(searchQuery);
                    odataUrl.put("$filter", "contains(NameUnsigned, '" + unsignedQuery + "')");
                }
                performSearch();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch() {
        showLoading();
        odataUrl.replace("$skip","0");
        odataUrl.replace("$top","10");
        skip=0;
        stadiumViewModel.fetchStadium(odataUrl);
    }

    private void resetPagination() {
        skip = 0;
        isLastPage = false;
        isLoading = false;
    }

    private void setupPagination() {
        // Giả sử binding.recyclerView là RecyclerView của bạn
        LinearLayoutManager layoutManager = (LinearLayoutManager) binding.myRecyclerView.getLayoutManager();

        binding.myRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (layoutManager == null) return;

                // Số lượng mục hiện tại
                int visibleItemCount = layoutManager.getChildCount();
                // Số lượng mục đã tải
                int totalItemCount = layoutManager.getItemCount();
                // Vị trí của mục đầu tiên đang thấy
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                System.out.println("test visibleItemCount: " + visibleItemCount);
                System.out.println("test totalItemCount: " + totalItemCount);
                System.out.println("test firstVisibleItemPosition: " + firstVisibleItemPosition);
                System.out.println("test skip: " + skip);


                // Kiểm tra điều kiện để tải thêm:
                // 1. Không đang tải (isLoading == false)
                // 2. Chưa phải trang cuối (isLastPage == false)
                // 3. Đã cuộn gần đến cuối (firstVisibleItemPosition + visibleItemCount >= totalItemCount - THRESHOLD)
                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount < count) { // Đảm bảo đã tải ít nhất 1 trang đầy đủ

                        loadMoreItems();
                    }
                }
            }
        });
    }

    private void loadMoreItems() {
        isLoading = true;


        if(count - (skip + 10) <= 0){
            if(count - (skip + 10) <= -10){
                isLastPage = true;
            }else{
                skip += 10;
                isLastPage = true;

                int take = count - skip;
                odataUrl.replace("$top", take + "");
                odataUrl.replace("$skip", skip + "");
                callApiForNextPage();

            }
        }else{


            isLastPage = false;
            skip += 10;
            odataUrl.replace("$skip", skip + "");
            callApiForNextPage();
        }

        // 1. Hiển thị ProgressBar (tùy chọn)
        // binding.progressBar.setVisibility(View.VISIBLE);

        // 2. Gọi API để tải trang mới

    }


    private void callApiForNextPage() {
        isLoading = true; // Đặt ở đây để đảm bảo

        stadiumViewModel.loadMore(odataUrl);
        isLoading = false;
        if (count < PAGE_SIZE) {
            isLastPage = true;
        }
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
    }

    private void navigateToFilterFragment() {
        FilterFragment filterFragment = new FilterFragment();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        fragmentTransaction.replace(R.id.nav_host_fragment_activity_main, filterFragment);
        fragmentTransaction.addToBackStack("HomeFragment");
        fragmentTransaction.commit();
    }
    private void navigateToDetailFragment(int stadiumId, String stadiumName, int createBy) {
        StadiumDetailFragment stadiumDetailFragment = StadiumDetailFragment.newInstance(stadiumId, stadiumName, createBy);
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        fragmentTransaction.replace(R.id.nav_host_fragment_activity_main, stadiumDetailFragment);
        fragmentTransaction.addToBackStack("HomeFragment");
        fragmentTransaction.commit();
    }
    // Trong com.example.sep490_mobile.interfaces.OnItemClickListener
    @Override
    public void onItemClick(int stadiumId, String stadiumName, int createBy) {
        navigateToDetailFragment(stadiumId, stadiumName, createBy);
    }

    @Override
    public void onBookButtonClick(int stadiumId) {
        HomeFragmentDirections.ActionNavigationHomeToVisuallyBookingFragment action =
                HomeFragmentDirections.actionNavigationHomeToVisuallyBookingFragment(stadiumId);
        NavHostFragment.findNavController(this).navigate(action);
    }

    // Các interface không dùng đến
    @Override public void onItemClick(int item, String type) {}
    @Override public void onItemClickRemoveMember(int id, int memberUserId, int postId, String type) {}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh memory leak
    }
    @Override
    public void onChatClick(int postId, int creatorId, String creatorName) {
        // Nếu không dùng, có thể để trống hoặc log lại cho debug
        // Ví dụ:
        // Log.d("SelectBookingFragment", "onChatClick - Không xử lý ở đây");
    }

    private void onFeedbackLoaded(List<Feedback> feedbackList) {
        // feedbackList là List<Feedback>, nên vòng lặp phải là Feedback
        Map<Integer, Integer> totalRating = new HashMap<>();
        Map<Integer, Integer> countRating = new HashMap<>();
        for (Feedback feedback : feedbackList) {
            int stadiumId = feedback.getStadiumId();
            int rating = feedback.getRating();
            totalRating.put(stadiumId, totalRating.getOrDefault(stadiumId, 0) + rating);
            countRating.put(stadiumId, countRating.getOrDefault(stadiumId, 0) + 1);
        }
        Map<Integer, Float> averageRatingMap = new HashMap<>();
        for (int stadiumId : totalRating.keySet()) {
            float avg = (float) totalRating.get(stadiumId) / countRating.get(stadiumId);
            averageRatingMap.put(stadiumId, avg);
        }
        adapter.setAverageRatings(averageRatingMap); // <-- Gọi sau khi tính xong
    }
    @Override
    public void onDailyBookButtonClick(int stadiumId) {
        // Lấy NavController
        NavController navController = NavHostFragment.findNavController(HomeFragment.this);

        // Tạo Bundle để truyền stadiumId
        Bundle bundle = new Bundle();
        bundle.putInt("stadiumId", stadiumId);

        // Điều hướng đến dailyBookingFragment
        navController.navigate(R.id.action_navigation_home_to_dailyBookingFragment, bundle);
    }
}