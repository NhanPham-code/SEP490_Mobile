package com.example.sep490_mobile.ui.findTeam;

import static com.google.gson.reflect.TypeToken.get;

import android.os.Bundle;

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
import com.example.sep490_mobile.data.dto.FindTeamDTO;
import com.example.sep490_mobile.data.dto.ScheduleBookingDTO;
import com.example.sep490_mobile.databinding.FragmentFindTeamBinding;
import com.example.sep490_mobile.ui.home.FilterFragment;
import com.example.sep490_mobile.ui.home.OnItemClickListener;
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
        showLoading();
        recyclerView = root.findViewById(R.id.my_recycler_view);
        odataUrl.put("$expand", "TeamMembers");

        findTeamViewModel = new ViewModelProvider(this).get(FindTeamViewModel.class);
        findTeamViewModel.fetchFindTeamList(odataUrl);
        observeFindTeamListResponse();

        ShareFilterFindTeamViewModel model = new ViewModelProvider(requireActivity()).get(ShareFilterFindTeamViewModel.class);
        model.getSelected().observe(getViewLifecycleOwner(), item -> {
            // Cập nhật UI với `item`
            odataUrl.put("$filter", item.get("$filter"));

            findTeamViewModel.fetchFindTeamList(odataUrl);
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

            }
        });
        root.findViewById(R.id.btn_joined_posts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        root.findViewById(R.id.filter_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToFilterFragment();
            }
        });

        return root;
    }
    private void navigateToCreatePostFragment(){
        SelectBookingFragment filterFragment = new SelectBookingFragment();

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
        fragmentTransaction.addToBackStack("FindTeamFragment");

        // 6. Hoàn tất giao dịch
        fragmentTransaction.commit();
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
        fragmentTransaction.replace(R.id.nav_host_fragment_activity_main, filterFragment);

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

        // 3. Quan sát lỗi (Rất cần thiết)
        findTeamViewModel.errorMessage.observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                // Hiển thị lỗi cho người dùng (ví dụ: dùng Toast)
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                hideLoading(); // Tắt loading ngay cả khi có lỗi
            }
        });
    }

    @Override
    public void onItemClick(int item) {

    }
}