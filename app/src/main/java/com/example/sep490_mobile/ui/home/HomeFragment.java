package com.example.sep490_mobile.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText; // Đã đổi sang EditText để phù hợp với thanh search
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sep490_mobile.Adapter.StadiumAdapter;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.databinding.FragmentHomeBinding;
import com.example.sep490_mobile.ui.stadiumDetail.StadiumDetailFragment;
import com.example.sep490_mobile.utils.removeVietnameseSigns;
import com.example.sep490_mobile.viewmodel.StadiumViewModel;

import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment implements OnItemClickListener{

    private RecyclerView recyclerView;
    // Đã đổi kiểu từ TextView sang EditText, vì nó hoạt động như thanh tìm kiếm
    private EditText searchEditText;
    private Map<String, String> odataUrl = new HashMap<>();
    private ImageButton filterButton; // Thêm ImageButton
    private StadiumAdapter adapter;
    private StadiumViewModel viewModel;
    private ODataResponse<StadiumDTO> stadiumList;
    private FragmentHomeBinding binding;
    private int skip = 0;
    private int count = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private final int PAGE_SIZE = 10; // Số lượng mục mỗi trang
    private final int THRESHOLD = 5; // Số lượng mục còn lại trước khi tải thêm

    // --- Constructor và Lifecycle ---


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        showLoading();
        odataUrl.put("$expand", "Courts,StadiumImages,StadiumVideos");
        odataUrl.put("$count", "true");
        odataUrl.put("$top", "10");
        odataUrl.put("$skip", skip + "");
        viewModel = new ViewModelProvider(this).get(StadiumViewModel.class);

        SharedViewModel model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        model.getSelected().observe(getViewLifecycleOwner(), item -> {
            // Cập nhật UI với `item`
            odataUrl.put("$filter", item.get("$filter"));
            performSearch();

        });



        // 1. Ánh xạ các Views (Đã thêm filterButton)
        recyclerView = root.findViewById(R.id.my_recycler_view);
        // Lưu ý: Đảm bảo layout XML của bạn sử dụng android:id="@+id/search_edit_text"
        searchEditText = root.findViewById(R.id.search_edit_text);
        // Ánh xạ nút lọc
        filterButton = root.findViewById(R.id.filter_button);

        // 2. Thiết lập sự kiện cho Nút Lọc
        setupFilterButton();

        // 3. Logic gọi API (Giữ nguyên)


        performSearch();


        observeStadiumListResponse();

        setupPagination();
        return root;
    }

    // Trong onViewCreated hoặc onCreate:
    private void setupPagination() {
        // Giả sử binding.recyclerView là RecyclerView của bạn
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        viewModel.loadMore(odataUrl);
        isLoading = false;
        if (count < PAGE_SIZE) {
            isLastPage = true;
        }
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.myRecyclerView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.myRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * Phương thức thiết lập sự kiện onClick cho nút Lọc
     */
    private void setupFilterButton() {
        if (filterButton != null) {
            filterButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    navigateToFilterFragment();
                }
            });
        }

        // TODO: Thêm logic xử lý tìm kiếm khi người dùng nhấn "Search" trên bàn phím
        if (searchEditText != null) {
            searchEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Không cần làm gì ở đây
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // HÀNH ĐỘNG XỬ LÝ KHI NỘI DUNG THAY ĐỔI
                    String searchQuery = s.toString();
                    odataUrl.replace("$top", "10");
                    odataUrl.replace("$skip", "0");

                    if (searchQuery.isEmpty()) {
                        // Nếu chuỗi rỗng, xóa filter và gọi tìm kiếm
                        odataUrl.remove("$filter");
                    } else {
                        // Áp dụng filter (sử dụng hàm xóa dấu của bạn)
                        // Lưu ý: Cần đảm bảo 'removeVietnameseSigns' được truy cập tĩnh/hoặc là một đối tượng
                        String unsignedQuery = removeVietnameseSigns.removeVietnameseSigns(searchQuery);
                        odataUrl.put("$filter", "contains(NameUnsigned, '" + unsignedQuery + "')");
                    }

                    // Gọi hàm tìm kiếm mỗi khi nội dung thay đổi
                    performSearch();

//                    InputMethodManager imm = (InputMethodManager) searchEditText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//
//                    if(imm.isAcceptingText()){
//                        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
//                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Không cần làm gì ở đây

                }
            });
        }
    }

    private void performSearch(){
        viewModel.fetchStadium(odataUrl);
    }

    /**
     * Phương thức thực hiện chuyển đổi sang Fragment Lọc
     */
    private void navigateToFilterFragment() {
        // 1. Khởi tạo Fragment lọc
        FilterFragment filterFragment = new FilterFragment();

        // 2. Lấy FragmentManager
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


        // Ví dụ về tên animation:
        // slide_in_right: Fragment mới trượt vào từ bên phải
        // slide_out_left: Fragment hiện tại trượt ra bên trái
        // slide_in_left: Fragment quay lại trượt vào từ bên trái (khi pop)
        // slide_out_right: Fragment hiện tại trượt ra bên phải (khi pop)

        fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_right, // enter
                R.anim.slide_out_left,  // exit
                R.anim.slide_in_left,  // popEnter
                R.anim.slide_out_right // popExit
        );

        // 4. Thay thế Fragment
        // !!! Giữ nguyên R.id.nav_host_fragment_activity_main hoặc kiểm tra lại ID chính xác
        fragmentTransaction.replace(R.id.nav_host_fragment_activity_main, filterFragment);

        // 5. Thêm vào back stack
        fragmentTransaction.addToBackStack("HomeFragment");

        // 6. Hoàn tất giao dịch
        fragmentTransaction.commit();
    }

    private void observeStadiumListResponse() {
        adapter = new StadiumAdapter(this.getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        viewModel.stadiums.observe(getViewLifecycleOwner(), response -> {
            stadiumList = response;
            count = Integer.parseInt(stadiumList.getCount() + "");
            adapter.setStadiumDTOS(stadiumList.getItems(), this);
            adapter.notifyDataSetChanged();
            hideLoading();
        });
    }
    @Override
    public void onItemClick(int stadiumId){
//        Toast.makeText(getContext(), "Đặt sân: " , Toast.LENGTH_SHORT).show();

        // 1. Tạo Fragment Chi Tiết và truyền dữ liệu qua Bundle
        StadiumDetailFragment stadiumDetailFragment = StadiumDetailFragment.newInstance(stadiumId);


        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


        // Ví dụ về tên animation:
        // slide_in_right: Fragment mới trượt vào từ bên phải
        // slide_out_left: Fragment hiện tại trượt ra bên trái
        // slide_in_left: Fragment quay lại trượt vào từ bên trái (khi pop)
        // slide_out_right: Fragment hiện tại trượt ra bên phải (khi pop)

        fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_right, // enter
                R.anim.slide_out_left,  // exit
                R.anim.slide_in_left,  // popEnter
                R.anim.slide_out_right // popExit
        );

        // 4. Thay thế Fragment
        // !!! Giữ nguyên R.id.nav_host_fragment_activity_main hoặc kiểm tra lại ID chính xác
        fragmentTransaction.replace(R.id.nav_host_fragment_activity_main, stadiumDetailFragment);

        // 5. Thêm vào back stack
        fragmentTransaction.addToBackStack("HomeFragment");

        // 6. Hoàn tất giao dịch
        fragmentTransaction.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}