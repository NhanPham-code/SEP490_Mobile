package com.example.sep490_mobile.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.sep490_mobile.data.dto.notification.NotificationDTO;
import com.example.sep490_mobile.data.repository.NotificationRepository;
import com.example.sep490_mobile.utils.NotificationConnector;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationCountViewModel extends AndroidViewModel {

    private final NotificationRepository notificationRepository;
    private final MutableLiveData<Integer> _unreadCount = new MutableLiveData<>(0);
    public final LiveData<Integer> unreadCount = _unreadCount;

    private final Observer<NotificationDTO> newNotificationObserver;

    public NotificationCountViewModel(@NonNull Application application) {
        super(application);
        this.notificationRepository = new NotificationRepository(application);

        // Lắng nghe sự kiện thông báo mới từ SignalR Connector
        newNotificationObserver = notification -> {
            if (notification != null) {
                incrementUnreadCount();
            }
        };
        NotificationConnector.getInstance().newNotificationReceived.observeForever(newNotificationObserver);
    }

    public void fetchUnreadCount() {
        notificationRepository.getUnreadCount().enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(@NonNull Call<Integer> call, @NonNull Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    _unreadCount.setValue(response.body());
                } else {
                    _unreadCount.setValue(0);
                }
            }
            @Override
            public void onFailure(@NonNull Call<Integer> call, @NonNull Throwable t) {
                _unreadCount.setValue(0);
            }
        });
    }

    public void resetCount() {
        _unreadCount.setValue(0);
    }

    private void incrementUnreadCount() {
        Integer currentCount = _unreadCount.getValue();
        if (currentCount != null) {
            _unreadCount.postValue(currentCount + 1);
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Rất quan trọng: Gỡ bỏ observer để tránh memory leak
        NotificationConnector.getInstance().newNotificationReceived.removeObserver(newNotificationObserver);
    }
}
