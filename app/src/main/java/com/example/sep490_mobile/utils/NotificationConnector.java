package com.example.sep490_mobile.utils;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.notification.NotificationDTO;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

import io.reactivex.rxjava3.core.Single;


public class NotificationConnector {
    private static final String TAG = "NotificationConnector";
    // Vui lòng thay đổi URL này
    private static final String HUB_URL = "http://10.0.2.2:7136/notificationHub"; // https://localhost:7072 https://localhost:7136

    private static volatile NotificationConnector instance;
    private HubConnection hubConnection;

    private final MutableLiveData<NotificationDTO> _newNotificationReceived = new MutableLiveData<>();
    public final LiveData<NotificationDTO> newNotificationReceived = _newNotificationReceived;

    private NotificationConnector() {}

    public static NotificationConnector getInstance() {
        if (instance == null) {
            synchronized (NotificationConnector.class) {
                if (instance == null) {
                    instance = new NotificationConnector();
                }
            }
        }
        return instance;
    }

    public void startConnection(String accessToken) {
        if (hubConnection != null && hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
            Log.d(TAG, "Connection already established.");
            return;
        }

        Log.d(TAG, "Starting SignalR connection...");
        hubConnection = HubConnectionBuilder.create(HUB_URL)
                .withAccessTokenProvider(Single.defer(() -> Single.just(accessToken)))
                .build();

        // Đăng ký lắng nghe sự kiện
        hubConnection.on("ReceiveNotification", (notification) -> {
            Log.i(TAG, "New notification received from hub: " + notification.getTitle());
            _newNotificationReceived.postValue(notification);
        }, NotificationDTO.class);

        // Bắt đầu kết nối
        hubConnection.start().doOnComplete(() -> {
            Log.i(TAG, "SignalR Connection started successfully. State: " + hubConnection.getConnectionState());
        }).doOnError(error -> {
            Log.e(TAG, "SignalR Connection failed: " + error.getMessage());
        }).subscribe();
    }

    public void stopConnection() {
        if (hubConnection != null && hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
            hubConnection.stop();
            hubConnection = null;
            Log.i(TAG, "SignalR Connection stopped.");
        }
    }
}
