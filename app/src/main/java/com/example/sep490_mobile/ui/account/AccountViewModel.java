package com.example.sep490_mobile.ui.account;

import static androidx.lifecycle.AndroidViewModel_androidKt.getApplication;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sep490_mobile.data.dto.BiometricTokenResponseDTO;
import com.example.sep490_mobile.data.dto.LoginResponseDTO;
import com.example.sep490_mobile.data.dto.LogoutRequestDTO;
import com.example.sep490_mobile.data.dto.LogoutResponseDTO;
import com.example.sep490_mobile.data.repository.UserRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountViewModel extends AndroidViewModel {

    private static final String TAG = "AccountViewModel";

    // LiveData để thông báo cho Fragment khi Logout thành công
    private final MutableLiveData<Boolean> logoutSuccess = new MutableLiveData<>();

    private final MutableLiveData<String> biometricToken = new MutableLiveData<>();
    private final MutableLiveData<String> biometricError = new MutableLiveData<>();

    // Có thể thêm UserRepository nếu cần gọi API Logout
    private final UserRepository userRepository;

    // Constructor cần Application để kế thừa AndroidViewModel
    public AccountViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public MutableLiveData<Boolean> getLogoutSuccess() {
        return logoutSuccess;
    }
    public LiveData<String> getBiometricToken() {
        return biometricToken;
    }

    public LiveData<String> getBiometricError() {
        return biometricError;
    }

    /**
     * Gọi API server để xóa Biometric Token tương ứng với thiết bị này.
     */
    public void deleteBiometricToken() {
        userRepository.deleteBiometricToken().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Biometric token on server deleted successfully.");
                } else {
                    // Ghi log lỗi, không cần thông báo cho người dùng vì tính năng đã bị tắt ở client
                    Log.w(TAG, "Failed to delete biometric token on server. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                // Ghi log lỗi mạng
                Log.e(TAG, "Network error while deleting biometric token.", t);
            }
        });
    }

    /**
     * Gọi API server để tạo một Biometric Token mới.
     */
    public void generateBiometricToken() {
        userRepository.getBiometricToken().enqueue(new Callback<BiometricTokenResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<BiometricTokenResponseDTO> call, @NonNull Response<BiometricTokenResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // <-- LẤY TOKEN TỪ BÊN TRONG DTO -->
                    String token = response.body().getBiometricToken();
                    biometricToken.postValue(token);
                } else {
                    biometricError.postValue("Lỗi khi tạo mã sinh trắc học.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<BiometricTokenResponseDTO> call, @NonNull Throwable t) {
                biometricError.postValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    public void logout() {
        Context context = getApplication().getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        // GỌI API LOGOUT (Nếu cần)
        String accessToken = prefs.getString("access_token", null);
        String refreshToken = prefs.getString("refresh_token", null);
        LogoutRequestDTO request = new LogoutRequestDTO(accessToken, refreshToken);

        userRepository.logout(request).enqueue(new retrofit2.Callback<LogoutResponseDTO>() {
            @Override
            public void onResponse(Call<LogoutResponseDTO> call, Response<LogoutResponseDTO> response) {
                // Dù API logout thành công hay thất bại, ta vẫn xóa token cục bộ
                clearLocalData(prefs);
            }

            @Override
            public void onFailure(Call<LogoutResponseDTO> call, Throwable t) {
                // Trong trường hợp lỗi mạng, ta vẫn xóa token cục bộ
                clearLocalData(prefs);
            }
        });

        //THÔNG BÁO CHO FRAGMENT: Logout đã thành công
        logoutSuccess.postValue(true);
    }

    private void clearLocalData(SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("access_token");
        editor.remove("refresh_token");
        editor.remove("user_id");
        editor.remove("full_name");
        editor.remove("avatar_url");
        editor.apply();
    }
}