package com.example.sep490_mobile.ui.findTeam;

import static com.google.gson.reflect.TypeToken.get;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.adapter.FindTeamAdapter;
import com.example.sep490_mobile.data.dto.CreateTeamMemberDTO;
import com.example.sep490_mobile.data.dto.FindTeamDTO;
import com.example.sep490_mobile.databinding.FragmentFindTeamBinding;
import com.example.sep490_mobile.interfaces.OnItemClickListener;
import com.example.sep490_mobile.utils.DurationConverter;
import com.example.sep490_mobile.viewmodel.FindTeamViewModel;

import java.util.HashMap;
import java.util.Map;


public class FindTeamFragment extends Fragment implements OnItemClickListener{

    private FindTeamViewModel findTeamViewModel;
    private FindTeamDTO findTeamDTO;
    private FragmentFindTeamBinding binding;
    private Map<String, String> odataUrl = new HashMap<>();
    private FindTeamAdapter adapter;
    private RecyclerView recyclerView;
    private int skip = 0;
    private int count = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private final int PAGE_SIZE = 10; // Số lượng mục mỗi trang


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFindTeamBinding.inflate(inflater, container, false);        // Inflate the layout for this fragment
        View root = binding.getRoot();
        SharedPreferences sharedPreferences = this.getContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("user_id", 0);
        if(currentUserId <= 0){
            Toast.makeText(getContext(), "Bạn cần đăng nhập để sửa dụng chức năng này", Toast.LENGTH_LONG).show();
            return root;
        }
        showLoading();
        recyclerView = root.findViewById(R.id.my_recycler_view);
        odataUrl.put("$expand", "TeamMembers");
        odataUrl.put("$top", "10");
        odataUrl.put("$skip", skip + "");
        odataUrl.put("$orderby", "CreatedAt desc");
        String iso = DurationConverter.createCurrentISOStringToSearch(); // 2025-10-22T14:43:05.472+07:00


// For OData V4 servers (preferred):
        String filter = "PlayDate gt " + iso;
        odataUrl.put("$filter", filter);
        odataUrl.put("$count", "true");
        findTeamViewModel = new ViewModelProvider(this).get(FindTeamViewModel.class);
        findTeamViewModel.fetchFindTeamList(odataUrl);
        observeFindTeamListResponse();
        ShareFilterFindTeamViewModel model = new ViewModelProvider(requireActivity()).get(ShareFilterFindTeamViewModel.class);
        model.getSelected().observe(getViewLifecycleOwner(), item -> {
            // Cập nhật UI với `item`
            if(item != null){
                odataUrl.replace("$filter", filter + " and " + item.get("$filter"));
                System.out.println("filter: " + odataUrl.get("$filter"));
                odataUrl.replace("$top", "10");
                odataUrl.replace("$skip", "0");
                skip = 0;
                findTeamViewModel.fetchFindTeamList(odataUrl);
            }else{
                odataUrl.replace("$filter", filter);
                odataUrl.replace("$top", "10");
                odataUrl.replace("$skip", "0");
                skip = 0;
                findTeamViewModel.fetchFindTeamList(odataUrl);
            }
        });

        root.findViewById(R.id.btn_create_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToCreatePostFragment();
            }
        });
        root.findViewById(R.id.btn_manage_posts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToMyPostFragment();
            }
        });
        root.findViewById(R.id.btn_joined_posts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToJoinedPostFragment();
            }
        });
        root.findViewById(R.id.filter_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToFilterFragment();
            }
        });
        setupPagination();
        return root;
    }

    private void navigateToJoinedPostFragment(){
        JoinedPostFragment joinedPostFragment = new JoinedPostFragment();

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
        fragmentTransaction.replace(R.id.find_team_fragment_constraint_layout, joinedPostFragment, "FindTeamFragment_TAG");

        // 5. Thêm vào back stack
        fragmentTransaction.addToBackStack("FindTeamFragment");

        // 6. Hoàn tất giao dịch
        fragmentTransaction.commit();
    }
    private void navigateToMyPostFragment(){
        MyPostManagerFragment myPostFragment = new MyPostManagerFragment();

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
        fragmentTransaction.replace(R.id.find_team_fragment_constraint_layout, myPostFragment, "FindTeamFragment_TAG");

        // 5. Thêm vào back stack
        fragmentTransaction.addToBackStack("FindTeamFragment");

        // 6. Hoàn tất giao dịch
        fragmentTransaction.commit();
    }
    private void navigateToCreatePostFragment(){
    SelectBookingFragment filterFragment = new SelectBookingFragment().newInstance("FindTeamFragment", "");

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
        fragmentTransaction.replace(R.id.find_team_fragment_constraint_layout, filterFragment, "FindTeamFragment_TAG");

        // 5. Thêm vào back stack
        fragmentTransaction.addToBackStack("FindTeamFragment");

        // 6. Hoàn tất giao dịch
        fragmentTransaction.commit();
    }
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

        findTeamViewModel.loadMore(odataUrl, findTeamDTO);
        isLoading = false;
        if (count < PAGE_SIZE) {
            isLastPage = true;
        }
    }
    private void navigateToFilterFragment() {
        // 1. Khởi tạo Fragment lọc
        FilterFindTeam filterFragment = new FilterFindTeam();

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
        fragmentTransaction.replace(R.id.find_team_fragment_constraint_layout, filterFragment, "FindTeamFragment_TAG");

        // 5. Thêm vào back stack
        fragmentTransaction.addToBackStack("FindTeamFragment");

        // 6. Hoàn tất giao dịch
        fragmentTransaction.commit();
    }
    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.myRecyclerView.setVisibility(View.GONE);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.myRecyclerView.setVisibility(View.VISIBLE);
    }
    private void observeFindTeamListResponse() {
        adapter = new FindTeamAdapter(this.getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 1. Quan sát dữ liệu


        // 2. Quan sát trạng thái loading
        findTeamViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                showLoading();
            } else {
                findTeamViewModel.findTeam.observe(getViewLifecycleOwner(), response -> {
                    findTeamDTO = response;
                    // LƯU Ý: Đảm bảo FindTeamAdapter implements OnItemClickListener
                    adapter.setFindTeamDTO(findTeamDTO, this);
                    adapter.notifyDataSetChanged();
                });
                hideLoading();
            }
        });
        findTeamViewModel.totalCount.observe(getViewLifecycleOwner(), integer -> {
            if(integer > 0){
                count = integer;
            }
        });

        // 3. Quan sát lỗi (Rất cần thiết)
        findTeamViewModel.errorMessage.observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                // Hiển thị lỗi cho người dùng (ví dụ: dùng Toast)
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                hideLoading(); // Tắt loading ngay cả khi có lỗi
            }
        });
    }


    private void joinTeam(int postId){
        SharedPreferences sharedPreferences = this.getContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("user_id", 0);

        CreateTeamMemberDTO createTeamMemberDTO = new CreateTeamMemberDTO();
        createTeamMemberDTO.setTeamPostId(postId);
        createTeamMemberDTO.setUserId(currentUserId);
        createTeamMemberDTO.setRole("Waiting");
        createTeamMemberDTO.setJoinedAt(DurationConverter.createCurrentISOString());

        findTeamViewModel.createMember(createTeamMemberDTO);
        findTeamViewModel.created.observe(getViewLifecycleOwner(), isCreated -> {
           if(isCreated){
               Toast.makeText(this.getContext(), "Đã tham gia, và hãy chờ duyệt", Toast.LENGTH_LONG).show();
               if(skip >= 10){
                   odataUrl.replace("$top", (skip + 10) + "");
               }
               findTeamViewModel.fetchFindTeamList(odataUrl);
               odataUrl.replace("$top", "10");
           }
        });
    }

    private void goToDetail(int postId){
        PostDetailFragment postDetailFragment = new PostDetailFragment().newInstance(postId + "", "FindTeamFragment");

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
        fragmentTransaction.replace(R.id.find_team_fragment_constraint_layout, postDetailFragment, "FindTeamFragment_TAG");

        // 5. Thêm vào back stack
        fragmentTransaction.addToBackStack("FindTeamFragment");

        // 6. Hoàn tất giao dịch
        fragmentTransaction.commit();
    }

    @Override
    public void onItemClick(int item) {

    }

    @Override
    public void onItemClick(int item, String type) {
        if(type.equalsIgnoreCase("join")){
            joinTeam(item);
        }else if (type.equalsIgnoreCase("detail")){
            goToDetail(item);
        }
    }

    @Override
    public void onItemClickRemoveMember(int id, int postId, String type) {

    }

    @Override
    public void onBookButtonClick(int stadiumId) {

    }

    @Override
    public void onStop(){
        super.onStop();
//        ShareFilterFindTeamViewModel model = new ViewModelProvider(requireActivity()).get(ShareFilterFindTeamViewModel.class);
//
//        model.setSelected(null);
        isLastPage = false;
        skip = 0;
        odataUrl.replace("$skip", "0");
        odataUrl.replace("$top", "10");
        odataUrl.remove("$filter");
    }
}