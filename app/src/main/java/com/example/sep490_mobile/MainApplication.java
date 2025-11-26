package com.example.sep490_mobile;

import android.app.Application;

import com.example.sep490_mobile.utils.NotificationConnector;
import com.example.sep490_mobile.utils.NotificationHelper;

public class MainApplication  extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Khởi tạo NotificationHelper
        NotificationHelper notificationHelper = new NotificationHelper(this);

        // 1. Tạo Notification Channel ngay khi ứng dụng khởi động
        notificationHelper.createNotificationChannel();

        // 2. Thiết lập một listener toàn cục để hiển thị thông báo hệ thống
        //    ngay khi nhận được tín hiệu từ SignalR
        NotificationConnector.getInstance().newNotificationReceived.observeForever(notification -> {
            if (notification != null) {
                notificationHelper.showNotification(notification);
            }
        });
    }
}