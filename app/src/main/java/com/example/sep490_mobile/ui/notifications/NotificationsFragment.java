package com.example.sep490_mobile.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sep490_mobile.adapter.NotificationAdapter;
import com.example.sep490_mobile.databinding.FragmentNotificationsBinding;
import com.example.sep490_mobile.viewmodel.NotificationCountViewModel;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private NotificationsViewModel notificationsViewModel;
    private NotificationAdapter adapter;

    private NotificationCountViewModel sharedViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // Sử dụng View Binding để khởi tạo view
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel
        notificationsViewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(NotificationCountViewModel.class);

        // Thiết lập các thành phần UI
        setupRecyclerView();
        setupListeners();
        setupObservers();

        // Bắt đầu tải dữ liệu lần đầu tiên
        notificationsViewModel.fetchInitialNotifications();
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerViewNotifications.setLayoutManager(layoutManager);
        binding.recyclerViewNotifications.setAdapter(adapter);

        // Thêm listener để xử lý việc tải thêm khi cuộn đến cuối danh sách
        binding.recyclerViewNotifications.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                // Kiểm tra xem có đang loading hay không trước khi gọi tải thêm
                boolean isLoading = notificationsViewModel.isLoading.getValue() != null && notificationsViewModel.isLoading.getValue();
                boolean isRefreshing = notificationsViewModel.isRefreshing.getValue() != null && notificationsViewModel.isRefreshing.getValue();

                if (!isLoading && !isRefreshing) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        notificationsViewModel.loadMoreNotifications();
                    }
                }
            }
        });
    }

    private void setupListeners() {
        // Listener cho việc kéo để làm mới
        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            notificationsViewModel.refreshNotifications();
        });

        // Listener cho nút "Đánh dấu đã đọc"
        binding.btnMarkAllRead.setOnClickListener(v -> {
            sharedViewModel.resetCount();
            notificationsViewModel.markAllAsRead();
        });
    }

    private void setupObservers() {
        // 1. Lắng nghe sự thay đổi của danh sách thông báo
        notificationsViewModel.notifications.observe(getViewLifecycleOwner(), notifications -> {
            if (notifications != null && !notifications.isEmpty()) {
                // Có dữ liệu
                binding.recyclerViewNotifications.setVisibility(View.VISIBLE);
                binding.textEmptyState.setVisibility(View.GONE);
                adapter.submitList(notifications);
            } else {
                // Không có dữ liệu
                binding.recyclerViewNotifications.setVisibility(View.GONE);
                binding.textEmptyState.setVisibility(View.VISIBLE);
                adapter.submitList(null); // Xóa danh sách cũ trong adapter
            }
        });

        // 2. Lắng nghe trạng thái loading ban đầu
        notificationsViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            // Chỉ hiển thị ProgressBar chính khi không phải đang refresh
            boolean isRefreshing = binding.swipeRefreshLayout.isRefreshing();
            if (isLoading && !isRefreshing) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else {
                binding.progressBar.setVisibility(View.GONE);
            }
        });

        // 3. Lắng nghe trạng thái "kéo để làm mới"
        notificationsViewModel.isRefreshing.observe(getViewLifecycleOwner(), isRefreshing -> {
            binding.swipeRefreshLayout.setRefreshing(isRefreshing);
        });

        // 4. Lắng nghe các thông báo (lỗi, thành công,...) để hiển thị Toast
        notificationsViewModel.toastMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                // Reset message để không hiển thị lại khi xoay màn hình
                notificationsViewModel.onToastMessageShown();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Dọn dẹp binding để tránh memory leak
        binding = null;
    }
}