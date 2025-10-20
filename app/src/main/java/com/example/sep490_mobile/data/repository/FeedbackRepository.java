package com.example.sep490_mobile.data.repository;

import android.content.Context;

import com.example.sep490_mobile.data.dto.FeedbackDto;
import com.example.sep490_mobile.data.dto.FeedbackRequestDto;
import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.remote.ApiClient;
import com.example.sep490_mobile.data.remote.ApiService;

import java.io.File;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

public class FeedbackRepository {
    private final ApiService apiService;

    public FeedbackRepository(Context context) {
        this.apiService = ApiClient.getInstance(context).getApiService();
    }

    public Call<ODataResponse<FeedbackDto>> getFeedbacks(Map<String, String> odataOptions) {
        return apiService.getFeedbacksOdata(odataOptions);
    }




    // NEW: multipart createFeedback để khớp controller [FromForm]
    private RequestBody textPart(String value) {
        return RequestBody.create(value == null ? "" : value, MultipartBody.FORM);
    }
    public Call<FeedbackDto> updateFeedbackMultipart(int id, int userId, int stadiumId, int rating, String comment, File imageFile) {
        RequestBody userIdPart = textPart(String.valueOf(userId));
        RequestBody stadiumIdPart = textPart(String.valueOf(stadiumId));
        RequestBody ratingPart = textPart(String.valueOf(rating));
        RequestBody commentPart = textPart(comment == null ? "" : comment);

        MultipartBody.Part imagePart = null;
        if (imageFile != null && imageFile.exists()) {
            MediaType mediaType = MediaType.parse("image/png");
            RequestBody fileReq = RequestBody.create(imageFile, mediaType);
            imagePart = MultipartBody.Part.createFormData("Image", imageFile.getName(), fileReq);
        }

        return apiService.updateFeedbackMultipart(id, userIdPart, stadiumIdPart, ratingPart, commentPart, imagePart);
    }
    public Call<FeedbackDto> createFeedbackMultipart(int userId, int stadiumId, int rating, String comment, File imageFile) {
        RequestBody userIdPart = textPart(String.valueOf(userId));
        RequestBody stadiumIdPart = textPart(String.valueOf(stadiumId));
        RequestBody ratingPart = textPart(String.valueOf(rating));
        RequestBody commentPart = textPart(comment == null ? "" : comment);

        MultipartBody.Part imagePart = null;
        if (imageFile != null && imageFile.exists()) {
            MediaType mediaType = MediaType.parse("image/png"); // or "image/jpeg"
            RequestBody fileReq = RequestBody.create(imageFile, mediaType);
            imagePart = MultipartBody.Part.createFormData("Image", imageFile.getName(), fileReq);
        }

        return apiService.createFeedbackMultipart(userIdPart, stadiumIdPart, ratingPart, commentPart, imagePart);
    }

    public Call<FeedbackDto> updateFeedback(int feedbackId, FeedbackRequestDto request) {
        return apiService.updateFeedback(feedbackId, request);
    }

    public Call<Void> deleteFeedback(int feedbackId) {
        return apiService.deleteFeedback(feedbackId);
    }
}