package com.example.sep490_mobile.ui.notifications;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.notification.CreateNotificationDTO;
import com.example.sep490_mobile.data.dto.notification.NotificationDTO;
import com.example.sep490_mobile.data.repository.NotificationRepository;
import com.example.sep490_mobile.viewmodel.NotificationCountViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsViewModel extends AndroidViewModel {

    private final NotificationRepository notificationRepository;
    private final int userId;

    // LiveData cho danh sách thông báo để UI observe
    private final MutableLiveData<List<NotificationDTO>> _notifications = new MutableLiveData<>(new ArrayList<>());
    public final LiveData<List<NotificationDTO>> notifications = _notifications;

    // LiveData cho các trạng thái UI
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<Boolean> _isRefreshing = new MutableLiveData<>(false);
    public final LiveData<Boolean> isRefreshing = _isRefreshing;

    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public final LiveData<String> toastMessage = _toastMessage;

    // Logic phân trang (Pagination)
    private int skip = 0;
    private final int PAGE_SIZE = 20; // Số lượng item mỗi lần tải
    private boolean isLastPage = false;
    private boolean isLoadingMore = false;

    public NotificationsViewModel(@NonNull Application application) {
        super(application);
        notificationRepository = new NotificationRepository(application);
        // Lấy userId từ SharedPreferences khi ViewModel được tạo
        SharedPreferences prefs = application.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);
    }

    public void fetchInitialNotifications() {
        if (userId == -1) {
            _toastMessage.setValue("Vui lòng đăng nhập để xem thông báo.");
            return;
        }
        // Chỉ hiển thị loading toàn màn hình ở lần tải đầu tiên
        if (_notifications.getValue() == null || _notifications.getValue().isEmpty()) {
            _isLoading.setValue(true);
        }
        loadNotifications(0, false);
    }

    public void refreshNotifications() {
        if (isLoadingMore || (_isRefreshing.getValue() != null && _isRefreshing.getValue())) return;
        _isRefreshing.setValue(true);
        isLastPage = false;
        loadNotifications(0, true);
    }

    public void loadMoreNotifications() {
        if (isLoadingMore || isLastPage || (_isRefreshing.getValue() != null && _isRefreshing.getValue())) {
            return;
        }
        isLoadingMore = true;
        // Tải trang tiếp theo
        loadNotifications(skip, false);
    }

    private void loadNotifications(int currentSkip, boolean isRefresh) {
        notificationRepository.getMyNotifications(userId, currentSkip, PAGE_SIZE).enqueue(new Callback<ODataResponse<NotificationDTO>>() {
            @Override
            public void onResponse(@NonNull Call<ODataResponse<NotificationDTO>> call, @NonNull Response<ODataResponse<NotificationDTO>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<NotificationDTO> newItems = response.body().getItems();

                    List<NotificationDTO> updatedList;
                    if (isRefresh || currentSkip == 0) {
                        // Nếu là refresh hoặc tải lần đầu, tạo một danh sách hoàn toàn mới
                        updatedList = new ArrayList<>(newItems);
                    } else {
                        // Nếu là tải thêm, lấy danh sách cũ và thêm vào
                        updatedList = new ArrayList<>(_notifications.getValue() != null ? _notifications.getValue() : new ArrayList<>());
                        updatedList.addAll(newItems);
                    }

                    // Đặt giá trị bằng một đối tượng List mới
                    _notifications.setValue(updatedList);

                    // Cập nhật trạng thái phân trang
                    if (newItems.size() < PAGE_SIZE) {
                        isLastPage = true;
                    }
                    skip = updatedList.size();
                } else {
                    _toastMessage.setValue("Không thể tải thông báo.");
                }
                resetLoadingStates();
            }

            @Override
            public void onFailure(@NonNull Call<ODataResponse<NotificationDTO>> call, @NonNull Throwable t) {
                _toastMessage.setValue("Lỗi mạng: " + t.getMessage());
                resetLoadingStates();
            }
        });
    }

    public void markAllAsRead() {
        _isLoading.setValue(true); // Hiển thị loading khi thực hiện hành động
        notificationRepository.markAllAsRead().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    _toastMessage.setValue("Đã đánh dấu tất cả là đã đọc.");
                    // Tải lại danh sách để cập nhật UI
                    refreshNotifications();
                } else {
                    _toastMessage.setValue("Thao tác thất bại.");
                    _isLoading.setValue(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                _toastMessage.setValue("Lỗi mạng: " + t.getMessage());
                _isLoading.setValue(false);
            }
        });
    }

    // Hàm ví dụ để tạo thông báo (bạn có thể gọi từ nơi khác trong app)
    public void sendTestNotification() {
        if (userId == -1) return;

        CreateNotificationDTO newNotif = new CreateNotificationDTO(
                userId,
                "Test.Event",
                "Thông báo Test",
                "Đây là nội dung của thông báo test được gửi từ app.",
                "{\"testId\":123}"
        );

        notificationRepository.createNotification(newNotif).enqueue(new Callback<NotificationDTO>() {
            @Override
            public void onResponse(@NonNull Call<NotificationDTO> call, @NonNull Response<NotificationDTO> response) {
                if (response.isSuccessful()) {
                    Log.i("NotificationViewModel", "Test notification sent successfully.");
                    // Thông báo sẽ được nhận qua SignalR, không cần xử lý thêm ở đây.
                } else {
                    Log.e("NotificationViewModel", "Failed to send test notification.");
                }
            }

            @Override
            public void onFailure(Call<NotificationDTO> call, Throwable t) {
                Log.e("NotificationViewModel", "Error sending test notification: " + t.getMessage());
            }
        });
    }


    private void resetLoadingStates() {
        _isLoading.setValue(false);
        _isRefreshing.setValue(false);
        isLoadingMore = false;
    }

    // Hàm này được gọi khi LiveData của toast đã được hiển thị, để tránh hiển thị lại
    public void onToastMessageShown() {
        _toastMessage.setValue(null);
    }
}