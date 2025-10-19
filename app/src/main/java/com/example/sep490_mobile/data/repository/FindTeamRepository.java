package com.example.sep490_mobile.data.repository;

import android.content.Context;

import com.example.sep490_mobile.data.dto.CreateTeamMemberDTO;
import com.example.sep490_mobile.data.dto.CreateTeamPostDTO;
import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.ReadTeamMemberDTO;
import com.example.sep490_mobile.data.dto.ReadTeamPostDTO;
import com.example.sep490_mobile.data.remote.ApiClient;
import com.example.sep490_mobile.data.remote.ApiService;

import java.util.Map;

import retrofit2.Call;

public class FindTeamRepository {
    private final ApiService apiService;
    private final Context context;


    public FindTeamRepository(Context context) {
        this.apiService = ApiClient.getInstance(context).getApiService();
        this.context = context;
    }

    public Call<ODataResponse<ReadTeamPostDTO>> getTeamPost(Map<String, String> odataOptions){
        return apiService.getTeamPost(odataOptions);
    }

    public Call<ReadTeamPostDTO> createNewPost(CreateTeamPostDTO body){
        return apiService.createTeamPost(body);
    }

    public Call<ReadTeamMemberDTO> addNewMember(CreateTeamMemberDTO body){
        return apiService.createTeamMember(body);
    }
}
