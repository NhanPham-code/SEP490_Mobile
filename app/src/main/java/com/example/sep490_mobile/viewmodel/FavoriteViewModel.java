package com.example.sep490_mobile.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.favorite.CreateFavoriteDTO;
import com.example.sep490_mobile.data.dto.favorite.ReadFavoriteDTO;
import com.example.sep490_mobile.data.repository.FavoriteRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteViewModel extends AndroidViewModel {

    private static final String TAG = "FavoriteViewModel";
    private final FavoriteRepository favoriteRepository;

    // LiveData chứa một Set các ID của sân vận động yêu thích.
    // Dùng Set để kiểm tra sự tồn tại (isFavorite) nhanh hơn.
    private final MutableLiveData<Set<Integer>> _favoriteStadiumIds = new MutableLiveData<>(new HashSet<>());
    public final LiveData<Set<Integer>> favoriteStadiumIds = _favoriteStadiumIds;

    // LiveData cho trạng thái loading và thông báo lỗi.
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _toastMessage = new MutableLiveData<>();
    public final LiveData<String> toastMessage = _toastMessage;


    public FavoriteViewModel(@NonNull Application application) {
        super(application);
        favoriteRepository = new FavoriteRepository(application);
        fetchFavoriteStadiums(); // Tải danh sách yêu thích khi ViewModel được tạo
    }

    public void fetchFavoriteStadiums() {
        _isLoading.setValue(true);
        favoriteRepository.getMyFavoriteStadiums().enqueue(new Callback<List<ReadFavoriteDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReadFavoriteDTO>> call, @NonNull Response<List<ReadFavoriteDTO>> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    Set<Integer> ids = new HashSet<>();
                    for (ReadFavoriteDTO favorite : response.body()) {
                        ids.add(favorite.getStadiumId());
                    }
                    _favoriteStadiumIds.setValue(ids);
                } else {
                    Log.e(TAG, "fetchFavoriteStadiums failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ReadFavoriteDTO>> call, @NonNull Throwable t) {
                _isLoading.setValue(false);
                Log.e(TAG, "fetchFavoriteStadiums failure", t);
            }
        });
    }

    public void toggleFavoriteStatus(int stadiumId, int userId) {
        Set<Integer> currentIds = _favoriteStadiumIds.getValue();
        if (currentIds == null) {
            currentIds = new HashSet<>();
        }

        boolean isCurrentlyFavorite = currentIds.contains(stadiumId);

        if (isCurrentlyFavorite) {
            // Nếu đang là yêu thích -> Xóa
            removeFavorite(userId, stadiumId);
        } else {
            // Nếu chưa phải yêu thích -> Thêm
            addFavorite(userId, stadiumId);
        }
    }


    private void addFavorite(int userId, int stadiumId) {
        _isLoading.setValue(true);
        CreateFavoriteDTO dto = new CreateFavoriteDTO(userId, stadiumId);
        favoriteRepository.addFavoriteStadium(dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful()) {
                    // Cập nhật lại Set và thông báo cho LiveData
                    Set<Integer> currentIds = new HashSet<>(_favoriteStadiumIds.getValue());
                    currentIds.add(stadiumId);
                    _favoriteStadiumIds.setValue(currentIds);
                    _toastMessage.setValue("Đã thêm vào danh sách yêu thích");
                } else {
                    _toastMessage.setValue("Lỗi: Không thể thêm vào yêu thích");
                    Log.e(TAG, "addFavorite failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                _isLoading.setValue(false);
                _toastMessage.setValue("Lỗi mạng: " + t.getMessage());
                Log.e(TAG, "addFavorite failure", t);
            }
        });
    }

    private void removeFavorite(int userId, int stadiumId) {
        _isLoading.setValue(true);
        favoriteRepository.removeFavoriteStadium(userId, stadiumId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful()) {
                    // Cập nhật lại Set và thông báo cho LiveData
                    Set<Integer> currentIds = new HashSet<>(_favoriteStadiumIds.getValue());
                    currentIds.remove(stadiumId);
                    _favoriteStadiumIds.setValue(currentIds);
                    _toastMessage.setValue("Đã xóa khỏi danh sách yêu thích");
                } else {
                    _toastMessage.setValue("Lỗi: Không thể xóa khỏi yêu thích");
                    Log.e(TAG, "removeFavorite failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                _isLoading.setValue(false);
                _toastMessage.setValue("Lỗi mạng: " + t.getMessage());
                Log.e(TAG, "removeFavorite failure", t);
            }
        });
    }

    // Helper để reset message sau khi đã hiển thị
    public void onToastMessageShown() {
        _toastMessage.setValue(null);
    }
}
