package com.example.sep490_mobile.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.ApiErrorResponseDTO;
import com.example.sep490_mobile.data.dto.VerifyOtpResponseDTO;
import com.example.sep490_mobile.data.repository.UserRepository;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpViewModel extends AndroidViewModel {

    private final String TAG = "OtpViewModel";
    private final UserRepository userRepository;

    // LiveData cho trạng thái UI
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // LiveData cho kết quả của các hành động
    private final MutableLiveData<Boolean> otpSent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> otpVerified = new MutableLiveData<>();

    public OtpViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application);
    }

    // --- GETTERS ĐỂ UI OBSERVE ---

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getOtpSent() {
        return otpSent;
    }

    public LiveData<Boolean> getOtpVerified() {
        return otpVerified;
    }


    // --- CÁC HÀNH ĐỘNG GỌI API ---

    /**
     * Yêu cầu server gửi mã OTP đến email được chỉ định.
     * @param email Email của người dùng.
     */
    public void sendOtp(String email) {
        isLoading.setValue(true);
        otpSent.setValue(false); // Reset trạng thái

        // Giả sử userRepository.sendOtp trả về Call<Void>
        userRepository.sendOTP(email).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                isLoading.postValue(false);
                if (response.isSuccessful()) {
                    Log.i(TAG, "OTP sent successfully to " + email);
                    otpSent.postValue(true);
                } else {
                    Log.e(TAG, "Failed to send OTP. Code: " + response.code());
                    errorMessage.postValue("Không thể gửi mã OTP. Vui lòng thử lại.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                isLoading.postValue(false);
                Log.e(TAG, "Network error while sending OTP", t);
                errorMessage.postValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    /**
     * Gửi mã OTP người dùng nhập để xác thực với server.
     * @param email Email của người dùng.
     * @param code Mã OTP người dùng đã nhập.
     */
    public void verifyOtp(String email, String code) {
        isLoading.setValue(true);
        otpVerified.setValue(false);

        // userRepository.verifyOTP bây giờ trả về Call<VerifyOtpResponseDTO>
        userRepository.verifyOTP(email, code).enqueue(new Callback<VerifyOtpResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<VerifyOtpResponseDTO> call, @NonNull Response<VerifyOtpResponseDTO> response) {
                isLoading.postValue(false);
                // Kiểm tra response.body() không null
                if (response.isSuccessful() && response.body() != null) {
                    // Lấy giá trị boolean từ đối tượng DTO
                    boolean isVerified = response.body().isVerified();
                    if (isVerified) {
                        Log.i(TAG, "OTP verified successfully for " + email);
                        otpVerified.postValue(true);
                    } else {
                        Log.e(TAG, "OTP verification failed for " + email);
                        errorMessage.postValue(response.body().getMessage() != null
                                ? response.body().getMessage() : "Mã OTP không chính xác hoặc đã hết hạn.");
                    }
                } else {
                    // Xử lý lỗi từ server (vd: 400, 404)
                    String errorMsg = "Yêu cầu xác thực thất bại.";
                    Log.e(TAG, "Failed to verify OTP. " + errorMsg);
                    errorMessage.postValue(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<VerifyOtpResponseDTO> call, @NonNull Throwable t) {
                isLoading.postValue(false);
                Log.e(TAG, "Network error while verifying OTP", t);
                // Lỗi IllegalStateException sẽ xảy ra ở đây nếu kiểu dữ liệu không khớp
                errorMessage.postValue("Lỗi xử lý dữ liệu: " + t.getMessage());
            }
        });
    }
}
