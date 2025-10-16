package com.example.sep490_mobile.viewmodel;

import static android.content.ContentValues.TAG;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.data.repository.StadiumRepository;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StadiumViewModel extends AndroidViewModel {
    // Repository instance (handles data logic and network calls)
    private static String TAG = "StadiumViewModel";
    private final StadiumRepository stadiumRepository;

    // Private MutableLiveData (dùng để thay đổi dữ liệu bên trong ViewModel)
    private final MutableLiveData<ODataResponse<StadiumDTO>> _stadiums = new MutableLiveData<>();
    private final MutableLiveData<ODataResponse<StadiumDTO>> _newStadium = new MutableLiveData<>();
    private final MutableLiveData<Long> _totalCount = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();

    // Public LiveData (dùng để View quan sát - chỉ đọc)
    public final LiveData<ODataResponse<StadiumDTO>> stadiums = _stadiums;
    public final LiveData<ODataResponse<StadiumDTO>> newStadiums = _newStadium;
    public final LiveData<Long> totalCount = _totalCount;
    public final LiveData<Boolean> isLoading = _isLoading;
    public final LiveData<String> errorMessage = _errorMessage;

    // Constructor: Khởi tạo Repository và có thể gọi load dữ liệu ban đầu
    public StadiumViewModel(@NonNull Application application) {
        super(application);
        // Khởi tạo Repository, truyền Application Context nếu cần thiết cho Repository
        stadiumRepository = new StadiumRepository(application);

        // Load dữ liệu ban đầu khi ViewModel được tạo

    }

    public void fetchStadium(Map<String, String> odataUrl){
        _isLoading.setValue(true);

        // Gọi phương thức từ Repository để tải dữ liệu
        stadiumRepository.getStadiumsOdata(odataUrl).enqueue(new Callback<ODataResponse<StadiumDTO>>() {

            @Override
            public void onResponse(Call<ODataResponse<StadiumDTO>> call, Response<ODataResponse<StadiumDTO>> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null){
                    _stadiums.setValue(response.body());
                } else {
                    // Log mã lỗi và body lỗi
                    Log.e(TAG, "HTTP Code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Error Body: " + response.errorBody().string());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    _errorMessage.setValue("Lỗi tải dữ liệu. Mã: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ODataResponse<StadiumDTO>> call, Throwable t) {
                _isLoading.setValue(false);
                // Log exception chi tiết
                _errorMessage.setValue("Lỗi mạng: " + t.getMessage());
                Log.e(TAG, "fetchStadium failure", t); // Đây là log quan trọng nhất
            }
        });
    }

    public void loadMore(Map<String, String> odataUrl){
        _isLoading.setValue(true);

        // Gọi phương thức từ Repository để tải dữ liệu
        stadiumRepository.getStadiumsOdata(odataUrl).enqueue(new Callback<ODataResponse<StadiumDTO>>() {

            @Override
            public void onResponse(Call<ODataResponse<StadiumDTO>> call, Response<ODataResponse<StadiumDTO>> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null){
                    ODataResponse<StadiumDTO> newStadium = _stadiums.getValue();
                    newStadium.getItems().addAll(response.body().getItems());
                    _stadiums.setValue(newStadium);
                } else {
                    // Log mã lỗi và body lỗi
                    Log.e(TAG, "HTTP Code: " + response.code());
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "Error Body: " + response.errorBody().string());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    _errorMessage.setValue("Lỗi tải dữ liệu. Mã: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ODataResponse<StadiumDTO>> call, Throwable t) {
                _isLoading.setValue(false);
                // Log exception chi tiết
                _errorMessage.setValue("Lỗi mạng: " + t.getMessage());
                Log.e(TAG, "fetchStadium failure", t); // Đây là log quan trọng nhất
            }
        });
    }
}
