package com.example.sep490_mobile.ui.scheduledetail;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.sep490_mobile.data.dto.PrivateUserProfileDTO;
import com.example.sep490_mobile.data.repository.UserRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingDetailViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<PrivateUserProfileDTO> userProfile = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public BookingDetailViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public LiveData<PrivateUserProfileDTO> getUserProfile() {
        return userProfile;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadUserInfo() {
        userRepository.getUserInfo().enqueue(new Callback<PrivateUserProfileDTO>() {
            @Override
            public void onResponse(@NonNull Call<PrivateUserProfileDTO> call, @NonNull Response<PrivateUserProfileDTO> response) {
                if (response.isSuccessful()) {
                    userProfile.setValue(response.body());
                } else {
                    error.setValue("Không thể tải thông tin người dùng.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PrivateUserProfileDTO> call, @NonNull Throwable t) {
                error.setValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }
}