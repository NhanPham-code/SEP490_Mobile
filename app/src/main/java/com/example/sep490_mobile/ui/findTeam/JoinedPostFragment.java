package com.example.sep490_mobile.ui.findTeam;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.adapter.PostManagerAdapter;
import com.example.sep490_mobile.data.dto.FindTeamDTO;
import com.example.sep490_mobile.data.dto.ReadTeamMemberDTO;
import com.example.sep490_mobile.data.dto.ReadTeamPostDTO;
import com.example.sep490_mobile.interfaces.OnItemClickListener;
import com.example.sep490_mobile.databinding.FragmentJoinedPostBinding;
import com.example.sep490_mobile.model.ChatRoomInfo;
import com.example.sep490_mobile.viewmodel.FindTeamViewModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JoinedPostFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JoinedPostFragment extends Fragment implements OnItemClickListener {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private FragmentJoinedPostBinding binding;
    private PostManagerAdapter adapter;
    private FindTeamViewModel findTeamViewModel;
    private FindTeamDTO findTeamDTO;
    private RecyclerView recyclerView;
    private int skip = 0;
    private int count = 0;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private final int PAGE_SIZE = 10; // Số lượng mục mỗi trang
    private Map<String, String> odataUrl = new HashMap<>();
    public static final String POST_CREATED_REQUEST_KEY = "POST_UPDATE_REQUEST_KEY";


    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public JoinedPostFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment JoinedPostFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static JoinedPostFragment newInstance(String param1, String param2) {
        JoinedPostFragment fragment = new JoinedPostFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        requireActivity().getSupportFragmentManager().setFragmentResultListener(POST_CREATED_REQUEST_KEY, this, (requestKey, bundle) -> {
            boolean shouldRefresh = bundle.getBoolean("refresh", false);
            if (shouldRefresh) {
                Log.d("FindTeamFragment", ">>> ĐÃ NHẬN TÍN HIỆU! BẮT ĐẦU TẢI LẠI DỮ LIỆU... <<<");
                findTeamViewModel.fetchFindTeamList(odataUrl);
                observePostListResponse();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentJoinedPostBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        recyclerView = view.findViewById(R.id.recycler_view_joined_post);
        findTeamViewModel = new ViewModelProvider(this).get(FindTeamViewModel.class);
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyAppPrefs", getContext().MODE_PRIVATE);
        int myId  = sharedPreferences.getInt("user_id", 0);
        odataUrl.put("$expand", "TeamMembers");
        odataUrl.put("$filter", "TeamMembers/any(x: x/UserId eq " + myId + ")");
        odataUrl.put("$orderby", "CreatedAt desc");
        odataUrl.put("$count", "true");
        odataUrl.put("$top", "10");
        odataUrl.put("$skip", skip + "");
        setupPagination();
        findTeamViewModel.fetchFindTeamList(odataUrl);
        observePostListResponse();

        binding.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getParentFragmentManager() != null){
                    FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                    fragmentTransaction.addToBackStack("FindTeamFragment");
                    Bundle result = new Bundle();
                    result.putBoolean("refresh", true);

                    // 2. GỬI TÍN HIỆU
                    requireActivity().getSupportFragmentManager().setFragmentResult("POST_CREATED_REQUEST_KEY", result);

                    // 3. ĐÓNG FRAGMENT HIỆN TẠI
                    requireActivity().getSupportFragmentManager().popBackStack("FindTeamFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    private void loadData(){
        if(skip >= 10){
            odataUrl.replace("$top", (skip + 10) + "");
        }
        findTeamViewModel.fetchFindTeamList(odataUrl);
        odataUrl.replace("$top", "10");
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

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerViewJoinedPost.setVisibility(View.GONE);
    }
    @Override
    public void onItemClick(int stadiumId, String stadiumName, int createBy) {
        // Không dùng ở FindTeamFragment, để trống hoặc log lại
    }
    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.recyclerViewJoinedPost.setVisibility(View.VISIBLE);
    }
    private void observePostListResponse(){
        adapter = new PostManagerAdapter(this.getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        findTeamViewModel.findTeam.observe(getViewLifecycleOwner(), findTeamDTO1 -> {
            if(findTeamDTO1 != null){
                findTeamDTO = findTeamDTO1;
                adapter.setPostData(findTeamDTO, this);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void goToDetail(int postId){
        PostDetailFragment postDetailFragment = new PostDetailFragment().newInstance(postId + "", "JoinedPostFragment");

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
        fragmentTransaction.replace(R.id.joined_post_fragment_constraint_layout, postDetailFragment, "JoinedPostFragment_TAG");

        // 5. Thêm vào back stack
        fragmentTransaction.addToBackStack("JoinedPostFragment");

        // 6. Hoàn tất giao dịch
        fragmentTransaction.commit();
    }
    private void editPost(int id){
        UpdatePostFragment updatePostFragment = new UpdatePostFragment().newInstance(id + "", "JoinedPostFragment");

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
        fragmentTransaction.replace(R.id.joined_post_fragment_constraint_layout, updatePostFragment, "JoinedPostFragment_TAG");

        // 5. Thêm vào back stack
        fragmentTransaction.addToBackStack("JoinedPostFragment");

        // 6. Hoàn tất giao dịch
        fragmentTransaction.commit();
    }
    private void detelePost(int position){
        ReadTeamPostDTO readTeamPostDTO = findTeamDTO.getTeamPostDTOS().get(position);
        for (ReadTeamMemberDTO teamMember : readTeamPostDTO.getTeamMembers()){
            findTeamViewModel.deleteMember(teamMember.getId(), teamMember.getTeamPostId(), readTeamPostDTO, "Waiting");
        }
        findTeamViewModel.deleteTeamPost(readTeamPostDTO.getId());
        findTeamViewModel.success.observe(getViewLifecycleOwner(), aBoolean -> {
            if(aBoolean){
                loadData();
            }
        });
    }

    @Override
    public void onItemClick(int item) {

    }

    @Override
    public void onItemClick(int item, String type) {
        if(type.equalsIgnoreCase("delete")){
            detelePost(item);
        }else if (type.equalsIgnoreCase("detail")){
            goToDetail(item);
        }else if(type.equalsIgnoreCase("edit")){
            editPost(item);
        }
    }
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

    @Override
    public void onItemClickRemoveMember(int id, int memberUserId, int postId, String type) {

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
        isLastPage = false;
        skip = 0;
        odataUrl.replace("$skip", "0");
        odataUrl.replace("$top", "10");
        odataUrl.remove("$filter");
    }
}