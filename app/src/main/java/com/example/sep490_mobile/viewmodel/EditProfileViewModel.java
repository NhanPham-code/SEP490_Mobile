package com.example.sep490_mobile.viewmodel;

import android.app.Application;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.ApiErrorResponseDTO;
import com.example.sep490_mobile.data.dto.PrivateUserProfileDTO;
import com.example.sep490_mobile.data.dto.UpdateUserProfileDTO;
import com.example.sep490_mobile.data.repository.UserRepository;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileViewModel extends AndroidViewModel {
    private static final String TAG = "EditProfileViewModel";
    private final UserRepository userRepository;

    private final MutableLiveData<PrivateUserProfileDTO> _userProfile = new MutableLiveData<>();
    public LiveData<PrivateUserProfileDTO> userProfile = _userProfile;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<Boolean> _updateSuccess = new MutableLiveData<>();
    public LiveData<Boolean> updateSuccess = _updateSuccess;

    public EditProfileViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application);
    }

    public void fetchUserProfile() {
        _isLoading.setValue(true);
        userRepository.getUserInfo().enqueue(new Callback<PrivateUserProfileDTO>() {
            @Override
            public void onResponse(@NonNull Call<PrivateUserProfileDTO> call, @NonNull Response<PrivateUserProfileDTO> response) {
                _isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    _userProfile.postValue(response.body());
                } else {
                    _errorMessage.postValue("Không thể tải thông tin người dùng.");
                    Log.e(TAG, "fetchUserProfile error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PrivateUserProfileDTO> call, @NonNull Throwable t) {
                _isLoading.postValue(false);
                _errorMessage.postValue("Lỗi mạng: " + t.getMessage());
                Log.e(TAG, "fetchUserProfile failure", t);
            }
        });
    }

    public void updateUserProfile(String fullName, String address, String phoneNumber, String gender, String dateOfBirth) {
        PrivateUserProfileDTO currentUser = _userProfile.getValue();
        if (currentUser == null || TextUtils.isEmpty(currentUser.getUserId())) {
            _errorMessage.setValue("Không tìm thấy thông tin người dùng hiện tại.");
            return;
        }

        _isLoading.setValue(true);

        // Chuyển đổi userId từ String sang int
        int userId = Integer.parseInt(currentUser.getUserId());

        UpdateUserProfileDTO dto = new UpdateUserProfileDTO(
                userId, fullName, currentUser.getEmail(), address, phoneNumber, gender, dateOfBirth
        );

        userRepository.updateUserProfile(dto).enqueue(new Callback<PrivateUserProfileDTO>() {
            @Override
            public void onResponse(@NonNull Call<PrivateUserProfileDTO> call, @NonNull Response<PrivateUserProfileDTO> response) {
                _isLoading.postValue(false);
                if (response.isSuccessful()) {
                    _userProfile.postValue(response.body()); // Cập nhật lại profile với data mới
                    _updateSuccess.postValue(true);
                    // Cập nhật thông tin mới vào SharedPreferences
                    updateUserProfileToSharedPrefs(response.body());
                } else {
                    String errorMessage = "Cập nhật thất bại."; // Mặc định
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyString = response.errorBody().string();
                            // Dùng Gson để parse chuỗi JSON lỗi
                            ApiErrorResponseDTO errorResponse = new Gson().fromJson(errorBodyString, ApiErrorResponseDTO.class);
                            String specificError = errorResponse.getFirstErrorMessage();
                            if (specificError != null) {
                                errorMessage = specificError; // Lấy lỗi cụ thể
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                    }
                    _errorMessage.postValue(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PrivateUserProfileDTO> call, @NonNull Throwable t) {
                _isLoading.postValue(false);
                _errorMessage.postValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    public void updateAvatar(Uri avatarUri) {
        PrivateUserProfileDTO currentUser = _userProfile.getValue();
        if (currentUser == null || TextUtils.isEmpty(currentUser.getUserId())) {
            _errorMessage.setValue("Không tìm thấy thông tin người dùng hiện tại.");
            return;
        }

        _isLoading.setValue(true);

        int userId = Integer.parseInt(currentUser.getUserId());

        userRepository.updateAvatar(userId, avatarUri).enqueue(new Callback<PrivateUserProfileDTO>() {
            @Override
            public void onResponse(@NonNull Call<PrivateUserProfileDTO> call, @NonNull Response<PrivateUserProfileDTO> response) {
                _isLoading.postValue(false);
                if (response.isSuccessful()) {
                    _userProfile.postValue(response.body());
                    _updateSuccess.postValue(true);
                    // Cập nhật thông tin mới vào SharedPreferences
                    updateUserProfileToSharedPrefs(response.body());
                } else {
                    _errorMessage.postValue("Cập nhật ảnh đại diện thất bại. Mã lỗi: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PrivateUserProfileDTO> call, @NonNull Throwable t) {
                _isLoading.postValue(false);
                _errorMessage.postValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void updateUserProfileToSharedPrefs(PrivateUserProfileDTO profile) {
        getApplication();
        if (profile == null) return;

        // Lưu thông tin mới vào SharedPreferences
        getApplication().getSharedPreferences("MyAppPrefs", Application.MODE_PRIVATE)
                .edit()
                .putString("full_name", profile.getFullName())
                .putString("avatar_url", profile.getAvatarUrl())
                .apply();
    }
}
