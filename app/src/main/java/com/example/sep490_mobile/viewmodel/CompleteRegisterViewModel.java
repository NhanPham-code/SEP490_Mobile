package com.example.sep490_mobile.viewmodel;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.RegisterResponseDTO;
import com.example.sep490_mobile.data.repository.UserRepository;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CompleteRegisterViewModel extends AndroidViewModel {

    private final String TAG = "CompleteRegisterVM";
    private UserRepository userRepository;
    private final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public CompleteRegisterViewModel(@NonNull Application application) {
        super(application);
        // Khởi tạo UserRepository, truyền vào Application context
        userRepository = new UserRepository(application);
    }

    // Phương thức công khai để UI gọi và bắt đầu quá trình đăng ký
    public void registerUser(
            String fullName, String email, String password, String address, String phoneNumber,
            String gender, String dateOfBirth, Uri avatarUri, List<Uri> faceImageUris) {

        isLoading.setValue(true);

        // Bây giờ call là Call<RegisterResponseDTO>
        Call<RegisterResponseDTO> call = userRepository.customerRegister(
                fullName, email, password, address, phoneNumber,
                gender, dateOfBirth, avatarUri
        );

        // Sửa Callback để nhận RegisterResponseDTO
        call.enqueue(new Callback<RegisterResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<RegisterResponseDTO> call, @NonNull Response<RegisterResponseDTO> response) {
                isLoading.postValue(false);

                // CHỈ CẦN KIỂM TRA isSuccessful là đủ để biết thành công
                if (response.isSuccessful() && response.body() != null) {
                    // Đăng ký thành công!
                    Log.i(TAG, "Registration successful: " + response.body().getMessage());
                    registerSuccess.postValue(true);
                    // Bạn không cần làm gì với response.body().getUser() nếu không muốn
                } else {
                    // Xử lý lỗi
                    String errorMsg = "Đăng ký thất bại.";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg = response.errorBody().string();
                            Log.e(TAG, "Registration error: " + errorMsg);
                        } catch (IOException e) {
                            Log.e(TAG, "Error parsing error body", e);
                        }
                    }
                    errorMessage.postValue(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<RegisterResponseDTO> call, @NonNull Throwable t) {
                isLoading.postValue(false);
                Log.e(TAG, "Registration failure: " + t.getMessage(), t);
                errorMessage.postValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    // Các getter để UI có thể observe (theo dõi) sự thay đổi của LiveData
    // Trả về LiveData thay vì MutableLiveData để đảm bảo dữ liệu chỉ được thay đổi từ bên trong ViewModel
    public LiveData<Boolean> getRegisterSuccess() {
        return registerSuccess;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
}
