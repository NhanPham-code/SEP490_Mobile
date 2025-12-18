package com.example.sep490_mobile.ui.feedback;

import static com.google.android.exoplayer2.mediacodec.MediaCodecInfo.TAG;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.FeedbackDto;
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

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<List<Feedback>> _feedbacks = new MutableLiveData<>();
    public final LiveData<List<Feedback>> feedbacks = _feedbacks;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    // LiveData cho các SỰ KIỆN
    private final MutableLiveData<Boolean> _createSuccessEvent = new MutableLiveData<>();
    public final LiveData<Boolean> createSuccessEvent = _createSuccessEvent;
    private final MutableLiveData<Boolean> _deleteSuccessEvent = new MutableLiveData<>();
    public final LiveData<Boolean> deleteSuccessEvent = _deleteSuccessEvent;
    private final MutableLiveData<Boolean> _updateSuccessEvent = new MutableLiveData<>();
    public final LiveData<Boolean> updateSuccessEvent = _updateSuccessEvent;
    private final MutableLiveData<Boolean> _forceRefreshEvent = new MutableLiveData<>();
    public final LiveData<Boolean> forceRefreshEvent = _forceRefreshEvent;

    // LiveData để giữ feedback của user hiện tại
    private final MutableLiveData<Feedback> _userFeedback = new MutableLiveData<>();
    public final LiveData<Feedback> userFeedback = _userFeedback;

    // Các biến quản lý phân trang
    private int stadiumId;
    public static final int PAGE_SIZE = 5;
    private final MutableLiveData<Integer> _currentPage = new MutableLiveData<>(1);
    public LiveData<Integer> currentPage = _currentPage;
    private final MutableLiveData<Integer> _totalPages = new MutableLiveData<>(0);
    public LiveData<Integer> totalPages = _totalPages;
    private final MutableLiveData<Boolean> _canFeedback = new MutableLiveData<>(false);
    public final LiveData<Boolean> canFeedback = _canFeedback;

    public FeedbackViewModel(@NonNull Application application) {
        super(application);
        this.feedbackRepository = new FeedbackRepository(application.getApplicationContext());
    }

    public void setStadiumId(int stadiumId) {
        this.stadiumId = stadiumId;
    }

    public void loadFeedbacksForPage(int page) {
        if (_isLoading.getValue() != null && _isLoading.getValue()) return;
        _isLoading.setValue(true);
        _currentPage.setValue(page);

        int skip = (page - 1) * PAGE_SIZE;

        Map<String, String> options = new HashMap<>();
        options.put("$filter", "StadiumId eq " + stadiumId);
        options.put("$orderby", "CreatedAt desc");
        options.put("$count", "true");
        options.put("$top", String.valueOf(PAGE_SIZE));
        options.put("$skip", String.valueOf(skip));

        feedbackRepository.getFeedbacks(options).enqueue(new Callback<ODataResponse<FeedbackDto>>() {
            @Override
            public void onResponse(@NonNull Call<ODataResponse<FeedbackDto>> call, @NonNull Response<ODataResponse<FeedbackDto>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ODataResponse<FeedbackDto> odataResponse = response.body();
                    int totalItems = odataResponse.getCount().intValue();
                    _totalPages.setValue((int) Math.ceil((double) totalItems / PAGE_SIZE));

                    List<Feedback> newFeedbacks = FeedbackMapper.toDomain(odataResponse.getItems());
                    _feedbacks.setValue(newFeedbacks);

                } else {
                    _error.setValue("Lỗi tải đánh giá: " + response.code());
                }
                _isLoading.setValue(false);
            }

            @Override
            public void onFailure(@NonNull Call<ODataResponse<FeedbackDto>> call, @NonNull Throwable t) {
                _isLoading.setValue(false);
                _error.setValue(t.getMessage());
            }
        });
    }

    // Hàm tìm feedback của user, bất kể trang nào
    public void findUserFeedback(int stadiumId, int userId) {
        if (userId == -1) {
            _userFeedback.setValue(null);
            return;
        }
        Map<String, String> options = new HashMap<>();
        options.put("$filter", "StadiumId eq " + stadiumId + " and UserId eq " + userId);
        options.put("$top", "1");

        feedbackRepository.getFeedbacks(options).enqueue(new Callback<ODataResponse<FeedbackDto>>() {
            @Override
            public void onResponse(@NonNull Call<ODataResponse<FeedbackDto>> call, @NonNull Response<ODataResponse<FeedbackDto>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getItems().isEmpty()) {
                    Feedback feedback = FeedbackMapper.toDomain(response.body().getItems().get(0));
                    _userFeedback.setValue(feedback);
                } else {
                    _userFeedback.setValue(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ODataResponse<FeedbackDto>> call, @NonNull Throwable t) {
                _userFeedback.setValue(null);
            }
        });
    }

    private void triggerRefresh() {
        _forceRefreshEvent.setValue(true);
        _forceRefreshEvent.setValue(false);
    }

    public void createFeedback(int userId, int stadiumId, int rating, String comment, @Nullable File imageFile) {
        _isLoading.setValue(true);
        feedbackRepository.createFeedbackMultipart(userId, stadiumId, rating, comment, imageFile)
                .enqueue(createUpdateCallback(true));
    }

    public void updateFeedback(int feedbackId, int userId, int stadiumId, int rating, String comment, @Nullable File imageFile) {
        _isLoading.setValue(true);
        feedbackRepository.updateFeedbackMultipart(feedbackId, userId, stadiumId, rating, comment, imageFile)
                .enqueue(createUpdateCallback(false));
    }

    private Callback<FeedbackDto> createUpdateCallback(boolean isCreate) {
        return new Callback<FeedbackDto>() {
            @Override
            public void onResponse(@NonNull Call<FeedbackDto> call, @NonNull Response<FeedbackDto> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful()) {
                    if (isCreate) _createSuccessEvent.setValue(true);
                    else _updateSuccessEvent.setValue(true);
                    triggerRefresh();
                } else {
                    String errorMsg = (isCreate ? "Lỗi tạo đánh giá: " : "Lỗi cập nhật đánh giá: ") + response.code();
                    _error.setValue(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<FeedbackDto> call, @NonNull Throwable t) {
                _isLoading.setValue(false);
                _error.setValue(t.getMessage());
            }
        };
    }

    public void deleteFeedback(int feedbackId) {
        _isLoading.setValue(true);
        feedbackRepository.deleteFeedback(feedbackId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful()) {
                    _deleteSuccessEvent.setValue(true);
                    triggerRefresh();
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

    public void checkUserCanFeedback(int stadiumId) {
        // Đặt tên TAG là "FEEDBACK_CHECK" cho dễ tìm
        String TAG = "FEEDBACK_CHECK";

        feedbackRepository.checkBookingCompleted(stadiumId).enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(@NonNull Call<Boolean> call, @NonNull Response<Boolean> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean result = response.body();
                    _canFeedback.setValue(result);

                    // 👇 LOG KẾT QUẢ THÀNH CÔNG
                    Log.d(TAG, "Kiểm tra quyền đánh giá: " + result + " (StadiumId: " + stadiumId + ")");
                } else {
                    _canFeedback.setValue(false);

                    // 👇 LOG KHI LỖI HOẶC BODY NULL
                    Log.e(TAG, "Lỗi API: Code " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Boolean> call, @NonNull Throwable t) {
                _canFeedback.setValue(false);

                // 👇 LOG KHI MẤT MẠNG / LỖI SERVER
                Log.e(TAG, "Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}