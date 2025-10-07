package com.example.sep490_mobile.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.ResetPasswordRequestDTO;
import com.example.sep490_mobile.data.dto.ResetPasswordResponseDTO;
import com.example.sep490_mobile.data.repository.UserRepository;

import retrofit2.Callback;

public class ResetPasswordViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> resetSuccess = new MutableLiveData<>();

    public ResetPasswordViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application);
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getResetSuccess() {
        return resetSuccess;
    }

    public void resetPassword(String email, String newPassword) {
        isLoading.setValue(true);
        ResetPasswordRequestDTO request = new ResetPasswordRequestDTO(email, newPassword);

        userRepository.forgotPassword(request).enqueue(new Callback<ResetPasswordResponseDTO>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<ResetPasswordResponseDTO> call, @NonNull retrofit2.Response<ResetPasswordResponseDTO> response) {
                isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    resetSuccess.postValue(true);
                } else {
                    errorMessage.postValue("Lỗi khi đặt lại mật khẩu.");
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<ResetPasswordResponseDTO> call, @NonNull Throwable t) {
                isLoading.postValue(false);
                errorMessage.postValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }
}
