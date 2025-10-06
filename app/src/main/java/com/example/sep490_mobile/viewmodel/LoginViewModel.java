package com.example.sep490_mobile.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.GoogleApiLoginRequestDTO;
import com.example.sep490_mobile.data.dto.LoginRequestDTO;
import com.example.sep490_mobile.data.dto.LoginResponseDTO;
import com.example.sep490_mobile.data.repository.UserRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    // LiveData để gửi trạng thái kết quả Login (Success/Error) về Activity
    private final MutableLiveData<LoginResponseDTO> loginResult = new MutableLiveData<>();
    // LiveData để thông báo trạng thái đang tải
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application);
    }

    public LiveData<LoginResponseDTO> getLoginResult() {
        return loginResult;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loginWithBiometric(String biometricToken) {
        isLoading.setValue(true);
        userRepository.loginWithBiometricToken(biometricToken).enqueue(new Callback<LoginResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponseDTO> call, @NonNull Response<LoginResponseDTO> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponseDTO loginResponse = response.body();
                    if (loginResponse.isValid()) {
                        saveLoginData(loginResponse);
                        loginResult.setValue(loginResponse);
                    } else {
                        loginResult.setValue(loginResponse); // Gửi lỗi về để Activity hiển thị
                    }
                } else {
                    errorMessage.setValue("Đăng nhập sinh trắc học thất bại.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponseDTO> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    public void loginWithGoogle(String idToken) {
        isLoading.setValue(true);
        GoogleApiLoginRequestDTO request = new GoogleApiLoginRequestDTO(idToken);

        userRepository.loginWithGoogle(request).enqueue(new Callback<LoginResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponseDTO> call, @NonNull Response<LoginResponseDTO> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponseDTO loginResponse = response.body();
                    if (loginResponse.isValid()) {
                        saveLoginData(loginResponse);
                        loginResult.setValue(loginResponse);
                    } else {
                        loginResult.setValue(loginResponse);
                    }
                } else {
                    errorMessage.setValue("Đăng nhập Google thất bại: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponseDTO> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    public void login(String email, String password) {
        isLoading.setValue(true);
        LoginRequestDTO request = new LoginRequestDTO(email, password);

        userRepository.login(request).enqueue(new Callback<LoginResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponseDTO> call, @NonNull Response<LoginResponseDTO> response) {
                isLoading.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponseDTO loginResponse = response.body();

                    if (loginResponse.isValid()) {
                        // LƯU DỮ LIỆU VÀO SharedPreferences (Logic nghiệp vụ)
                        saveLoginData(loginResponse);
                        loginResult.setValue(loginResponse);
                    } else {
                        // Logic lỗi server báo (email/password không đúng)
                        loginResult.setValue(loginResponse);
                    }
                } else {
                    // Logic lỗi phản hồi không thành công từ server
                    errorMessage.setValue("Đăng nhập thất bại: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponseDTO> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void saveLoginData(LoginResponseDTO response) {
        Context context = getApplication().getApplicationContext();
        context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                .edit()
                .putString("access_token", response.getAccessToken())
                .putString("refresh_token", response.getRefreshToken())
                .putInt("user_id", response.getUserId())
                .putString("full_name", response.getFullName())
                .putString("avatar_url", response.getAvatarUrl())
                .apply();
    }
}
