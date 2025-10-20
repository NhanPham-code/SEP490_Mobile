package com.example.sep490_mobile.ui.bookingdetail;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.sep490_mobile.data.dto.discount.ReadDiscountDTO;
import com.example.sep490_mobile.data.remote.ApiClient;
import com.example.sep490_mobile.data.remote.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingHistoryDetailViewModel extends AndroidViewModel {

    private final ApiService apiService;
    private final MutableLiveData<ReadDiscountDTO> discountDetails = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public BookingHistoryDetailViewModel(@NonNull Application application) {
        super(application);
        apiService = ApiClient.getInstance(application).getApiService();
    }

    public LiveData<ReadDiscountDTO> getDiscountDetails() { return discountDetails; }
    public LiveData<String> getError() { return error; }

    public void loadDiscountDetails(Integer discountId) {
        // Nếu không có discountId, set giá trị là null để UI biết và ẩn đi.
        if (discountId == null || discountId <= 0) {
            discountDetails.setValue(null);
            return;
        }

        apiService.getDiscountById(discountId).enqueue(new Callback<ReadDiscountDTO>() {
            @Override
            public void onResponse(@NonNull Call<ReadDiscountDTO> call, @NonNull Response<ReadDiscountDTO> response) {
                if (response.isSuccessful()) {
                    discountDetails.postValue(response.body());
                } else {
                    error.postValue("Không thể tải thông tin giảm giá.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReadDiscountDTO> call, @NonNull Throwable t) {
                error.postValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }
}