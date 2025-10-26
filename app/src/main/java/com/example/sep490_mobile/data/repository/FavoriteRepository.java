package com.example.sep490_mobile.data.repository;

import android.content.Context;

import com.example.sep490_mobile.data.dto.favorite.CreateFavoriteDTO;
import com.example.sep490_mobile.data.dto.favorite.ReadFavoriteDTO;
import com.example.sep490_mobile.data.remote.ApiClient;
import com.example.sep490_mobile.data.remote.ApiService;
import java.util.List;
import retrofit2.Call;

public class FavoriteRepository {
    private final ApiService apiService;

    public FavoriteRepository(Context context) {
        // Ensure ApiClient is properly initialized to get the ApiService instance
        this.apiService = ApiClient.getInstance(context).getApiService();
    }

    /**
     * Fetches the list of favorite stadium IDs for the current user.
     * Assumes the ApiService has a method getMyFavoriteStadiums()
     * that corresponds to the backend endpoint (e.g., /myFavoriteStadium).
     * @return A Call object for the network request.
     */
    public Call<List<ReadFavoriteDTO>> getMyFavoriteStadiums() {
        // Make sure the ApiService interface has the getMyFavoriteStadiums method defined
        return apiService.getMyFavoriteStadiums();
    }

    public Call<Void> addFavoriteStadium(CreateFavoriteDTO createFavoriteDTO) {
        return apiService.addFavorite(createFavoriteDTO);
    }

    public Call<Void> removeFavoriteStadium(int userId, int stadiumId) {
        return apiService.removeFavorite(userId, stadiumId);
    }



}