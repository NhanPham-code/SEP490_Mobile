package com.example.sep490_mobile.data.repository;

import android.content.Context;

import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.notification.CreateNotificationDTO;
import com.example.sep490_mobile.data.dto.notification.NotificationDTO;
import com.example.sep490_mobile.data.remote.ApiClient;
import com.example.sep490_mobile.data.remote.ApiService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

public class NotificationRepository {
    private final ApiService apiService;

    public NotificationRepository(Context context) {
        this.apiService =  ApiClient.getInstance(context).getApiService();
    }

    /**
     * Lấy danh sách thông báo cho người dùng, có phân trang và sắp xếp.
     * @param userId ID của người dùng để lọc.
     * @param skip Số lượng bản ghi bỏ qua (để phân trang).
     * @param top Số lượng bản ghi muốn lấy.
     * @return Call object để thực thi.
     */
    public Call<ODataResponse<NotificationDTO>> getMyNotifications(int userId, int skip, int top) {
        Map<String, String> options = new HashMap<>();

        // Logic lọc: lấy các thông báo có UserId = 0 (cho tất cả) HOẶC UserId = userId hiện tại
        String filter = "UserId eq 0 or UserId eq " + userId;
        options.put("$filter", filter);

        // Sắp xếp theo ngày tạo, mới nhất lên đầu
        options.put("$orderby", "CreatedAt desc");

        // Phân trang
        options.put("$count", "true");
        options.put("$skip", String.valueOf(skip));
        options.put("$top", String.valueOf(top));

        return apiService.getMyNotifications(options);
    }

    /**
     * Lấy số lượng thông báo chưa đọc.
     */
    public Call<Integer> getUnreadCount() {
        return apiService.getUnreadNotificationCount();
    }

    /**
     * Đánh dấu tất cả là đã đọc.
     */
    public Call<Void> markAllAsRead() {
        return apiService.markAllAsRead();
    }

    /**
     * Tạo thông báo mới cho 1 người dùng.
     */
    public Call<NotificationDTO> createNotification(CreateNotificationDTO notification) {
        return apiService.createNotification(notification);
    }

    /**
     * Tạo thông báo mới cho 1 nhóm người dùng.
     */
    public Call<Void> createNotificationForGroup(List<CreateNotificationDTO> notifications) {
        return apiService.createNotificationsBatch(notifications);
    }

    /**
     * Tạo thông báo mới cho tất cả người dùng.
     */
    public Call<Void> createNotificationForAllUsers(CreateNotificationDTO notification) {
        return apiService.createNotificationForAll(notification);
    }

}
