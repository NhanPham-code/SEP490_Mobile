package com.example.sep490_mobile.ui.feedback;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.FeedbackDto;
import com.example.sep490_mobile.data.dto.FeedbackRequestDto;
import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.mapper.FeedbackMapper;
import com.example.sep490_mobile.data.repository.FeedbackRepository;
import com.example.sep490_mobile.model.Feedback;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackViewModel extends AndroidViewModel {

    private final FeedbackRepository feedbackRepository;

    // LiveData cho trạng thái ĐANG TẢI
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    // LiveData cho DỮ LIỆU (danh sách feedback)
    private final MutableLiveData<List<Feedback>> _feedbacks = new MutableLiveData<>();
    public final LiveData<List<Feedback>> feedbacks = _feedbacks;

    // LiveData cho THÔNG BÁO LỖI
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    // LiveData cho các SỰ KIỆN (tạo, xóa, cập nhật thành công)
    private final MutableLiveData<Boolean> _createSuccessEvent = new MutableLiveData<>();
    public final LiveData<Boolean> createSuccessEvent = _createSuccessEvent;

    private final MutableLiveData<Boolean> _deleteSuccessEvent = new MutableLiveData<>();
    public final LiveData<Boolean> deleteSuccessEvent = _deleteSuccessEvent;

    // NEW: update success event
    private final MutableLiveData<Boolean> _updateSuccessEvent = new MutableLiveData<>();
    public final LiveData<Boolean> updateSuccessEvent = _updateSuccessEvent;

    public FeedbackViewModel(@NonNull Application application) {
        super(application);
        this.feedbackRepository = new FeedbackRepository(application.getApplicationContext());
    }

    public void loadFeedbacks(int stadiumId) {
        _isLoading.setValue(true);

        Map<String, String> options = new HashMap<>();
        options.put("$filter", "StadiumId eq " + stadiumId);
        options.put("$orderby", "CreatedAt desc");

        feedbackRepository.getFeedbacks(options).enqueue(new Callback<ODataResponse<FeedbackDto>>() {
            @Override
            public void onResponse(@NonNull Call<ODataResponse<FeedbackDto>> call, @NonNull Response<ODataResponse<FeedbackDto>> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Feedback> domainList = FeedbackMapper.toDomain(response.body().getItems());
                    _feedbacks.setValue(domainList);
                } else {
                    _error.setValue("Lỗi tải đánh giá: " + response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call<ODataResponse<FeedbackDto>> call, @NonNull Throwable t) {
                _isLoading.setValue(false);
                _error.setValue(t.getMessage());
            }
        });
    }

    public void createFeedback(int userId, int stadiumId, int rating, String comment) {
        _isLoading.setValue(true);

        feedbackRepository.createFeedbackMultipart(userId, stadiumId, rating, comment, null)
                .enqueue(new Callback<FeedbackDto>() {
                    @Override
                    public void onResponse(@NonNull Call<FeedbackDto> call, @NonNull Response<FeedbackDto> response) {
                        _isLoading.setValue(false);
                        if (response.isSuccessful()) {
                            _createSuccessEvent.setValue(true);
                        } else {
                            String err = "Lỗi tạo đánh giá: " + response.code();
                            _error.setValue(err);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<FeedbackDto> call, @NonNull Throwable t) {
                        _isLoading.setValue(false);
                        _error.setValue(t.getMessage());
                    }
                });
    }

    // NEW: update feedback
    public void updateFeedback(int feedbackId, int userId, int stadiumId, int rating, String comment) {
        _isLoading.setValue(true);

        // nếu muốn upload ảnh, truyền File thay null
        feedbackRepository.updateFeedbackMultipart(feedbackId, userId, stadiumId, rating, comment, null)
                .enqueue(new Callback<FeedbackDto>() {
                    @Override
                    public void onResponse(@NonNull Call<FeedbackDto> call, @NonNull Response<FeedbackDto> response) {
                        _isLoading.setValue(false);
                        if (response.isSuccessful()) {
                            _updateSuccessEvent.setValue(true);
                        } else {
                            _error.setValue("Lỗi cập nhật đánh giá: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<FeedbackDto> call, @NonNull Throwable t) {
                        _isLoading.setValue(false);
                        _error.setValue(t.getMessage());
                    }
                });
    }

    public void deleteFeedback(int feedbackId) {
        _isLoading.setValue(true);
        feedbackRepository.deleteFeedback(feedbackId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful()) {
                    _deleteSuccessEvent.setValue(true);
                } else {
                    _error.setValue("Lỗi xóa đánh giá: " + response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                _isLoading.setValue(false);
                _error.setValue(t.getMessage());
            }
        });
    }
}