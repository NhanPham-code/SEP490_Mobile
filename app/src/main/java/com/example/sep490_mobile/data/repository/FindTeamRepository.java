package com.example.sep490_mobile.data.repository;

import android.content.Context;

import com.example.sep490_mobile.data.dto.CreateTeamMemberDTO;
import com.example.sep490_mobile.data.dto.CreateTeamPostDTO;
import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.ReadTeamMemberDTO;
import com.example.sep490_mobile.data.dto.ReadTeamMemberForDetailDTO;
import com.example.sep490_mobile.data.dto.ReadTeamPostDTO;
import com.example.sep490_mobile.data.dto.ReadTeamPostResponse;
import com.example.sep490_mobile.data.dto.UpdateTeamMemberDTO;
import com.example.sep490_mobile.data.dto.UpdateTeamPostDTO;
import com.example.sep490_mobile.data.dto.booking.response.BookingHistoryODataResponse;
import com.example.sep490_mobile.data.remote.ApiClient;
import com.example.sep490_mobile.data.remote.ApiService;

import java.util.List;
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

    public Call<ReadTeamPostResponse> createNewPost(CreateTeamPostDTO body){
        return apiService.createTeamPost(body);
    }

    public Call<ReadTeamMemberDTO> addNewMember(CreateTeamMemberDTO body){
        return apiService.createTeamMember(body);
    }

    public Call<BookingHistoryODataResponse> getBookingsHistoryFindTeam(String odata) {
        return apiService.getBookingsHistory(odata);
    }

    public Call<ReadTeamPostDTO> updateTeamPost(UpdateTeamPostDTO updateTeamPostDTO){
        return apiService.updateTeamPost(updateTeamPostDTO);
    }

    public Call<ReadTeamMemberForDetailDTO> updateTeamMember(UpdateTeamMemberDTO updateTeamMemberDTO){
        return apiService.updateTeamMember(updateTeamMemberDTO);
    }

    public Call<List<ReadTeamMemberForDetailDTO>> getTeamMember(int postId) {
        return apiService.getTeamMember(postId);
    }

    public Call<ReadTeamMemberForDetailDTO> getTeamMemberByPostIdAndId(int teamId, int postId) {
        return apiService.getTeamMemberByPostIdAndId(teamId, postId);
    }

    public Call<Boolean> deleteTeamPost(int postId) {
        return apiService.deleteTeamPost(postId);
    }

    public Call<Boolean> deleteTeamMember(int teamMemberId, int postId) {
        return apiService.deleteTeamMember(teamMemberId, postId);
    }

}
