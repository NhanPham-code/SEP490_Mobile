package com.example.sep490_mobile.data.repository;

import android.app.DownloadManager;
import android.content.Context;

import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.data.remote.ApiClient;
import com.example.sep490_mobile.data.remote.ApiService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;

public class StadiumRepository {
    private final ApiService apiService;
    private final Context context;

    public StadiumRepository(Context context) {
        this.apiService = ApiClient.getInstance(context).getApiService();
        this.context = context;
    }

    public Call<ODataResponse<StadiumDTO>> getStadiumsOdata(Map<String, String> odataOptions) {
        return apiService.getStadiumsOdata(odataOptions);
    }

    public Call<ODataResponse<StadiumDTO>> getStadiumByListId(String stadiumId){
        Map<String, String> odataOptions = new HashMap<>();
        odataOptions.put("$filter", "Id in ( " + stadiumId + " )");
        odataOptions.put("$expand", "Courts");
        return apiService.getStadiumsOdata(odataOptions);
    }

}
