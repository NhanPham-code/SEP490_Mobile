package com.example.sep490_mobile.ui.findTeam;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
// XÓA DÒNG NÀY: import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.adapter.SelectBookingAdapter;
import com.example.sep490_mobile.data.dto.SelectBookingDTO;
import com.example.sep490_mobile.databinding.FragmentSelectBookingBinding;
import com.example.sep490_mobile.interfaces.OnItemClickListener;
import com.example.sep490_mobile.viewmodel.FindTeamViewModel;


public class SelectBookingFragment extends Fragment implements OnItemClickListener{

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FindTeamViewModel model;
    private SelectBookingDTO selectBookingDTO = new SelectBookingDTO(null, null);
    // private int count; // Biến này không được sử dụng, có thể xóa
    private FragmentSelectBookingBinding binding;

    // XÓA BIẾN NÀY: private RecyclerView recyclerView; // Bị null và gây crash

    private SelectBookingAdapter adapter;

    public static SelectBookingFragment newInstance(String param1, String param2) {
        SelectBookingFragment fragment = new SelectBookingFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Trong onCreateView, CHỈ inflate và return view
        binding = FragmentSelectBookingBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        view.findViewById(R.id.buttonClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack(mParam1, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
        });

        return view;
    }

    /**
     * Phương thức này được gọi ngay sau khi onCreateView hoàn tất.
     * Đây là nơi an toàn để thiết lập (setup) mọi thứ liên quan đến View.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Hiển thị loading
        showLoading();

        // 2. Khởi tạo ViewModel
        model = new ViewModelProvider(this).get(FindTeamViewModel.class);

        // 3. Thiết lập RecyclerView (Adapter và LayoutManager)
        setupRecyclerView();
        // 5. Gọi API để lấy dữ liệu
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            model.fetchBooking(this.getContext());
        }
        // 4. Lắng nghe (observe) dữ liệu từ ViewModel
        observeBookingListResponse();


    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerViewBookings.setVisibility(View.GONE);
        binding.layoutNoBookings.setVisibility(View.GONE); // Ẩn luôn layout "không có"
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        // Không set GONE/VISIBLE cho RecyclerView ở đây,
        // Hãy để logic trong observer quyết định
    }

    /**
     * Thiết lập các thành phần tĩnh của RecyclerView
     */
    private void setupRecyclerView() {
        adapter = new SelectBookingAdapter(this.getContext());

        // SỬA LỖI: Sử dụng binding.recyclerViewBookings thay vì 'recyclerView' (bị null)
        binding.recyclerViewBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewBookings.setAdapter(adapter);
    }


    /**
     * Lắng nghe LiveData từ ViewModel
     */
    private void observeBookingListResponse() {
        model.selectBooking.observe(getViewLifecycleOwner(), response -> {

            // Tắt loading bất kể kết quả thế nào
            hideLoading();

            // Kiểm tra kỹ dữ liệu trả về
            if (response != null && response.getBookingReadDTOS() != null && !response.getBookingReadDTOS().isEmpty()) {

                // CÓ DỮ LIỆU
                selectBookingDTO = response;
                Toast.makeText(this.getContext(), "Tổng Booking: "+ response.getBookingReadDTOS().size(), Toast.LENGTH_LONG).show();
                adapter.setSelectBookingList(selectBookingDTO, this);
                // adapter.notifyDataSetChanged(); // setSelectBookingList đã làm việc này
                adapter.notifyDataSetChanged();
                binding.recyclerViewBookings.setVisibility(View.VISIBLE); // Hiển thị list
                binding.layoutNoBookings.setVisibility(View.GONE); // Ẩn "không có"

            } else {

                // KHÔNG CÓ DỮ LIỆU (list rỗng hoặc null)
                binding.recyclerViewBookings.setVisibility(View.GONE); // Ẩn list
                binding.layoutNoBookings.setVisibility(View.VISIBLE); // Hiển thị "không có"
            }
        });
    }

    private void navigateToCreatePostFragment(){
        CreatePostFragment createPostFragment = new CreatePostFragment();

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
        fragmentTransaction.replace(R.id.select_booking_fragment_constraint_layout, createPostFragment);

        // 5. Thêm vào back stack
        fragmentTransaction.addToBackStack("SelectBookingFragment");

        // 6. Hoàn tất giao dịch
        fragmentTransaction.commit();
    }

    @Override
    public void onItemClick(int item) {
        // Xử lý khi click vào item
        ShareFilterFindTeamViewModel model1 = new ViewModelProvider(requireActivity()).get(ShareFilterFindTeamViewModel.class);

        model1.setBooking(selectBookingDTO.getBookingReadDTOS().get(item));
        System.out.println("bookingaaaa: " + model1.getBooking().getValue().getId());
        model1.setStadium(selectBookingDTO.getStadiums().get(selectBookingDTO.getBookingReadDTOS().get(item).getStadiumId()));
        navigateToCreatePostFragment();
    }

    @Override
    public void onItemClick(int item, String type) {

    }

    @Override
    public void onItemClickRemoveMember(int id, int postId, String type) {

    }

    @Override
    public void onBookButtonClick(int stadiumId) {

    }
}