package com.example.sep490_mobile.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.sep490_mobile.MainActivity;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.notification.NotificationDTO;

import androidx.core.content.ContextCompat;

public class NotificationHelper {
    private static final String CHANNEL_ID = "sportivey_notifications";
    private static final String CHANNEL_NAME = "Sportivey Notifications";
    private final Context context;

    public NotificationHelper(Context context) {
        this.context = context;
    }

    // Tạo Notification Channel (bắt buộc cho Android 8.0+)
    public void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Kênh thông báo cho ứng dụng Sportivey");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    // Hiển thị thông báo
    public void showNotification(NotificationDTO notification) {
        // === BƯỚC KIỂM TRA QUYỀN BẮT ĐẦU TỪ ĐÂY ===
        // Chỉ kiểm tra trên Android 13 (TIRAMISU) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                // Nếu chưa có quyền, không làm gì cả và ghi log
                Log.w("NotificationHelper", "POST_NOTIFICATIONS permission not granted. Cannot show notification.");
                return;
            }
        }
        // === KẾT THÚC BƯỚC KIỂM TRA QUYỀN ===

        // Tạo intent để mở MainActivity khi người dùng nhấn vào thông báo
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notification.getId(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Xây dựng thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_bell)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getMessage())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Hiển thị thông báo
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notification.getId(), builder.build());
    }
}
