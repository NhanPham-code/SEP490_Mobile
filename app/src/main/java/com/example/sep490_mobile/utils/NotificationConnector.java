package com.example.sep490_mobile.utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.notification.NotificationDTO;
import com.example.sep490_mobile.data.dto.notification.NotificationSignalRDTO;
import com.google.gson.Gson;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.reactivex.rxjava3.core.Single;


public class NotificationConnector {
    private static final String TAG = "NotificationConnector";
    // Ví dụ: thay 10.0.2.2
    private static final String HUB_URL_BASE = "https://localhost:7072/notificationHub";

    private static volatile NotificationConnector instance;
    private HubConnection hubConnection;

    // 2. Thay đổi kiểu dữ liệu của LiveData
    private final MutableLiveData<NotificationSignalRDTO> _newNotificationReceived = new MutableLiveData<>();
    public final LiveData<NotificationSignalRDTO> newNotificationReceived = _newNotificationReceived;

    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

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

    @SuppressLint("CheckResult")
    public void startConnection(String accessToken, int userId) {
        if (hubConnection != null && hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
            Log.d(TAG, "Connection already established.");
            return;
        }

        if (userId <= 0) {
            Log.e(TAG, "Cannot start connection with invalid userId: " + userId);
            return;
        }

        String dynamicHubUrl = HUB_URL_BASE + "?userId=" + userId;
        Log.d(TAG, "Starting SignalR connection to: " + dynamicHubUrl);


        hubConnection = HubConnectionBuilder.create(dynamicHubUrl)
                .withAccessTokenProvider(Single.fromCallable(() -> accessToken))
                .setHttpClientBuilderCallback(builder -> {
                    try {
                        final TrustManager[] trustAllCerts = new TrustManager[]{
                                new X509TrustManager() {
                                    @SuppressLint("TrustAllX509TrustManager")
                                    @Override public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                                    @SuppressLint("TrustAllX509TrustManager")
                                    @Override public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {}
                                    @Override public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new java.security.cert.X509Certificate[]{}; }
                                }
                        };
                        final SSLContext sslContext = SSLContext.getInstance("SSL");
                        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                        builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
                        builder.hostnameVerifier((hostname, session) -> true);

                    } catch (Exception e) {
                        Log.e(TAG, "Error setting up unsafe SSL", e);
                    }
                })
                .build();

        // 3. Thay đổi kiểu dữ liệu trong hubConnection.on()
        hubConnection.on("ReceiveNotification", (notification) -> { // 'notification' giờ là một object NotificationSignalRDTO
            Log.d(TAG, "[BACKGROUND THREAD] DTO received directly: " + notification);
            try {
                if (notification != null && notification.getTitle() != null) {
                    mainThreadHandler.post(() -> {
                        Log.i(TAG, "[MAIN THREAD] Updating LiveData with notification: " + notification.getTitle());
                        _newNotificationReceived.setValue(notification);
                    });
                } else {
                    Log.e(TAG, "Received notification is null or has no title.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error processing received DTO", e);
            }
        }, NotificationSignalRDTO.class); // <-- THAY ĐỔI QUAN TRỌNG

        // Bắt đầu kết nối
        hubConnection.start().subscribe(
                () -> Log.i(TAG, "SignalR Connection started successfully. State: " + hubConnection.getConnectionState()),
                error -> Log.e(TAG, "SignalR Connection failed: " + error.getMessage())
        );
    }

    public void stopConnection() {
        if (hubConnection != null && hubConnection.getConnectionState() == HubConnectionState.CONNECTED) {
            hubConnection.stop();
            hubConnection = null;
            Log.i(TAG, "SignalR Connection stopped.");
        }
    }
}
