package com.example.sep490_mobile.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.repository.UserRepository;

public class CheckEmailViewModel extends AndroidViewModel {

    private final String TAG = "CheckEmailVM";
    private UserRepository userRepository;
    private final MutableLiveData<Boolean> emailExists = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public CheckEmailViewModel(@NonNull Application application) {
        super(application);
        // Khởi tạo UserRepository, truyền vào Application context
        userRepository = new UserRepository(application);
    }

    // call API kiểm tra email tồn tại
    public void checkEmailExists(String email) {
        // 1. Bắt đầu quá trình, báo cho UI biết là đang tải
        isLoading.setValue(true);

        // 2. Gọi phương thức từ repository
        userRepository.checkEmailExists(email).enqueue(new retrofit2.Callback<Boolean>() {
            @Override
            public void onResponse(retrofit2.Call<Boolean> call, retrofit2.Response<Boolean> response) {
                // Dừng trạng thái đang tải
                isLoading.postValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    // 4a. Xử lý khi API trả về thành công
                    emailExists.postValue(response.body());
                } else {
                    // 4b. Xử lý khi API trả về lỗi (không thành công)
                    errorMessage.postValue("Error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Boolean> call, Throwable t) {
                // Dừng trạng thái đang tải
                isLoading.postValue(false);
                // 5. Xử lý khi gọi API thất bại (ví dụ: lỗi mạng)
                errorMessage.postValue(t.getMessage());
            }
        });
    }

    public LiveData<Boolean> getEmailExists() {
        return emailExists;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}
