package com.example.sep490_mobile.ui.findTeam;

import static com.google.gson.reflect.TypeToken.get;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.adapter.FindTeamAdapter;
import com.example.sep490_mobile.data.dto.CreateTeamMemberDTO;
import com.example.sep490_mobile.data.dto.FindTeamDTO;
import com.example.sep490_mobile.data.dto.notification.CreateNotificationDTO;
import com.example.sep490_mobile.databinding.FragmentFindTeamBinding;
import com.example.sep490_mobile.interfaces.OnItemClickListener;
import com.example.sep490_mobile.model.ChatRoomInfo;
import com.example.sep490_mobile.utils.DurationConverter;
import com.example.sep490_mobile.viewmodel.FindTeamViewModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    // ⭐ BƯỚC 1: ĐỊNH NGHĨA KEY CHO FRAGMENT RESULT
    public static final String POST_CREATED_REQUEST_KEY = "POST_CREATED_REQUEST_KEY";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // QUAN TRỌNG: Đăng ký listener tại đây
        requireActivity().getSupportFragmentManager().setFragmentResultListener(POST_CREATED_REQUEST_KEY, this, (requestKey, bundle) -> {
            boolean shouldRefresh = bundle.getBoolean("refresh", false);
            if (shouldRefresh) {
                Log.d("FindTeamFragment", ">>> ĐÃ NHẬN TÍN HIỆU! BẮT ĐẦU TẢI LẠI DỮ LIỆU... <<<");
                refreshData(); // Gọi hàm tải lại dữ liệu của bạn
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentFindTeamBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        SharedPreferences sharedPreferences = this.getContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("user_id", 0);
        if(currentUserId <= 0){
            Toast.makeText(getContext(), "Bạn cần đăng nhập để sử dụng chức năng này", Toast.LENGTH_LONG).show();
            return root;
        }

        // Thay vì khởi tạo ViewModel ở đây, hãy khởi tạo nó với scope Activity
        // để nó được chia sẻ giữa các fragment.
        findTeamViewModel = new ViewModelProvider(requireActivity()).get(FindTeamViewModel.class);

        showLoading();
        recyclerView = root.findViewById(R.id.my_recycler_view);
        setupInitialODataParams();
        findTeamViewModel.fetchFindTeamList(odataUrl);
        observeFindTeamListResponse();

        ShareFilterFindTeamViewModel model = new ViewModelProvider(requireActivity()).get(ShareFilterFindTeamViewModel.class);
        model.getSelected().observe(getViewLifecycleOwner(), item -> {
            if(item != null){
                String baseFilter = "PlayDate ge " + DurationConverter.createCurrentISOStringToSearch();
                odataUrl.put("$filter", baseFilter + " and " + item.get("$filter"));
                Log.d("FindTeamFilter", "Applying filter: " + odataUrl.get("$filter"));
                refreshData();
            }else{
                // Khi bộ lọc bị xóa, quay về trạng thái ban đầu
                setupInitialODataParams();
                refreshData();
            }
        });

        binding.swipeRefreshLayout.setOnRefreshListener(this::refreshData);

        root.findViewById(R.id.btn_create_post).setOnClickListener(v -> navigateToCreatePostFragment());
        root.findViewById(R.id.btn_manage_posts).setOnClickListener(v -> navigateToMyPostFragment());
        root.findViewById(R.id.btn_joined_posts).setOnClickListener(v -> navigateToJoinedPostFragment());
        root.findViewById(R.id.filter_button).setOnClickListener(v -> navigateToFilterFragment());

        setupPagination();
        return root;
    }

    private void setupInitialODataParams() {
        odataUrl.put("$expand", "TeamMembers");
        odataUrl.put("$top", String.valueOf(PAGE_SIZE));
        odataUrl.put("$skip", "0");
        odataUrl.put("$orderby", "CreatedAt desc");
        String iso = DurationConverter.createCurrentISOStringToSearch();
        String filter = "PlayDate gt " + iso;
        odataUrl.put("$filter", filter);
        odataUrl.put("$count", "true");
    }

    private void refreshData() {
        Log.d("FindTeamFragment", "Refreshing data...");
        skip = 0;
        isLastPage = false;
        isLoading = false; // Đảm bảo có thể tải lại
        odataUrl.put("$skip", "0");
        odataUrl.put("$top", String.valueOf(PAGE_SIZE));
        binding.swipeRefreshLayout.setRefreshing(true);
        findTeamViewModel.fetchFindTeamList(odataUrl);
    }

    // ... (Toàn bộ code còn lại của bạn từ onChatClick đến onResume giữ nguyên không đổi)
    @Override
    public void onChatClick(int postId, int creatorId, String creatorName) {
        // Lấy userId người đăng nhập từ SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập để chat!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userId == creatorId) {
            Toast.makeText(getContext(), "Bạn không thể chat với chính mình!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gọi hàm tạo phòng chat
        createChatRoomIfNeeded(userId, creatorId, creatorName, new ChatRoomCreationCallback() {
            @Override
            public void onComplete(boolean success) {
                if (success) {
                    Bundle chatBundle = new Bundle();
                    chatBundle.putBoolean("start_chat", true);
                    chatBundle.putString("SENDER_ID", String.valueOf(userId));
                    chatBundle.putString("RECEIVER_ID", String.valueOf(creatorId));
                    chatBundle.putString("RECEIVER_NAME", creatorName);

                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
                    navController.navigate(R.id.navigation_chat, chatBundle);
                }
            }
        });
    }

    // 2. Thêm interface này vào FindTeamFragment (nếu chưa có)
    public interface ChatRoomCreationCallback {
        void onComplete(boolean success);
    }

    // 3. Thêm hàm này vào FindTeamFragment (nếu chưa có)
    private void createChatRoomIfNeeded(int userId, int ownerId, String ownerName, ChatRoomCreationCallback callback) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        long timestamp = System.currentTimeMillis();

        Map<String, Object> updates = new HashMap<>();
        updates.put("userChats/" + userId + "/" + ownerId, new ChatRoomInfo(ownerName, timestamp, ""));
        updates.put("userChats/" + ownerId + "/" + userId, new ChatRoomInfo("Người dùng", timestamp, ""));

        dbRef.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onComplete(true);
                    } else {
                        Toast.makeText(getContext(), "Tạo phòng chat thất bại!", Toast.LENGTH_SHORT).show();
                        callback.onComplete(false);
                    }
                });
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

                // Kiểm tra điều kiện để tải thêm:
                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount < count) {

                        loadMoreItems();
                    }
                }
            }
        });
    }
    private void loadMoreItems() {
        isLoading = true;

        skip += PAGE_SIZE;
        odataUrl.put("$skip", String.valueOf(skip));

        callApiForNextPage();
    }


    private void callApiForNextPage() {
        isLoading = true; // Đặt ở đây để đảm bảo

        findTeamViewModel.loadMore(odataUrl, findTeamDTO);
        // Cập nhật trạng thái isLoading và isLastPage trong observer của ViewModel
        // thay vì đặt cứng ở đây.
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

        findTeamViewModel.isLoading.observe(getViewLifecycleOwner(), loading -> {
            isLoading = loading; // Cập nhật trạng thái loading chung
            if (loading && skip == 0) { // Chỉ show full-screen loading cho lần tải đầu
                showLoading();
            } else {
                hideLoading();
            }
            binding.swipeRefreshLayout.setRefreshing(loading);
        });

        findTeamViewModel.findTeam.observe(getViewLifecycleOwner(), response -> {
            if (response != null) {

                findTeamDTO = response;
                adapter.setFindTeamDTO(findTeamDTO, this);
                if (adapter.getItemCount() >= count) {
                    isLastPage = true;
                }
            }
        });

        findTeamViewModel.totalCount.observe(getViewLifecycleOwner(), integer -> {
            if(integer != null && integer > 0){
                count = integer;
                binding.emptyView.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setVisibility(View.VISIBLE);
            }else{
                binding.swipeRefreshLayout.setVisibility(View.GONE);
                binding.emptyView.setVisibility(View.VISIBLE);
            }
        });

        findTeamViewModel.errorMessage.observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }


    private void joinTeam(int postId, int creatorId){
        SharedPreferences sharedPreferences = this.getContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
        int currentUserId = sharedPreferences.getInt("user_id", 0);

        CreateTeamMemberDTO createTeamMemberDTO = new CreateTeamMemberDTO();
        createTeamMemberDTO.setTeamPostId(postId);
        createTeamMemberDTO.setUserId(currentUserId);
        createTeamMemberDTO.setRole("Waiting");
        createTeamMemberDTO.setJoinedAt(DurationConverter.createCurrentISOString());

        findTeamViewModel.createMember(createTeamMemberDTO);
        findTeamViewModel.notifyToMember(new CreateNotificationDTO(
                creatorId,
                "Recruitment.JoinRequest",
                "Đã nhận được yêu cầu tham gia",
                "Vừa có một thành viên tham gia vào đội nhóm của bạn",
                "{\"title\": FindTeam, \"content\": \"/FindTeam/FindTeam\"}"
        ));
        findTeamViewModel.created.observe(getViewLifecycleOwner(), isCreated -> {
            if(isCreated){
                Toast.makeText(this.getContext(), "Đã gửi yêu cầu tham gia, vui lòng chờ duyệt", Toast.LENGTH_LONG).show();
                refreshData(); // Tải lại toàn bộ để cập nhật trạng thái nút
            }
        });
    }

    private void goToDetail(int postId){
        PostDetailFragment postDetailFragment = new PostDetailFragment().newInstance(postId + "", "FindTeamFragment");

        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.setCustomAnimations(
                R.anim.slide_in_right, // enter
                R.anim.slide_out_left,  // exit
                R.anim.slide_in_left,  // popEnter
                R.anim.slide_out_right // popExit
        );

        fragmentTransaction.replace(R.id.find_team_fragment_constraint_layout, postDetailFragment, "FindTeamFragment_TAG");
        fragmentTransaction.addToBackStack("FindTeamFragment");
        fragmentTransaction.commit();
    }

    @Override
    public void onItemClick(int item) {

    }
    @Override
    public void onItemClick(int stadiumId, String stadiumName, int createBy) {

    }
    @Override
    public void onItemClick(int item, String type) {

    }

    @Override
    public void onItemClickRemoveMember(int id, int memberUserId, int postId, String type) {
        if("join".equalsIgnoreCase(type)){
            joinTeam(id, memberUserId);
        }else if ("detail".equalsIgnoreCase(type)){
            goToDetail(id);
        }
    }

    @Override
    public void onBookButtonClick(int stadiumId) {

    }

    @Override
    public void onDailyBookButtonClick(int stadiumId) {

    }

    @Override
    public void onStop(){
        super.onStop();
        ShareFilterFindTeamViewModel model = new ViewModelProvider(requireActivity()).get(ShareFilterFindTeamViewModel.class);
        model.setSelected(null);
        // Không reset filter ở đây để giữ trạng thái khi quay lại
    }

    // ⭐ XÓA BỎ onResume()
    // Logic tải lại dữ liệu đã được xử lý bằng FragmentResultListener và Swipe-to-refresh
    // nên không cần tải lại mỗi khi onResume nữa để tránh gọi API không cần thiết.
    /*
    @Override
    public void onResume() {
        super.onResume();
        findTeamViewModel.fetchFindTeamList(odataUrl);
    }
    */
}