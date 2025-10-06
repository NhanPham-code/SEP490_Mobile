package com.example.sep490_mobile.data.remote;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sep490_mobile.data.dto.LoginResponseDTO;
import com.example.sep490_mobile.data.dto.RefreshTokenRequestDTO;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;

public class TokenAuthenticator implements Authenticator {
    private final Context context;
    private final ApiService authApiService; // Cần một instance ApiService KHÔNG CÓ AuthInterceptor/Authenticator

    public TokenAuthenticator(Context context, ApiService authApiService) {
        this.context = context;
        this.authApiService = authApiService;
    }

    @Override
    public Request authenticate(@NonNull Route route, @NonNull Response response) throws IOException {
        // 1. Lấy token hiện tại từ SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String oldAccessToken = prefs.getString("access_token", null);
        String refreshToken = prefs.getString("refresh_token", null);

        // Kiểm tra xem yêu cầu gốc có phải là yêu cầu refresh token không
        // (Để tránh lặp vô hạn nếu refresh token cũng thất bại)
        if (response.request().header("Authorization") == null || response.request().url().pathSegments().contains("refresh-token")) {
            return null; // Không thử lại hoặc yêu cầu refresh token thất bại
        }

        // 2. Nếu không có refresh token, không thể làm mới, buộc người dùng đăng nhập lại
        if (refreshToken == null || oldAccessToken == null) {
            // TODO: Chuyển hướng người dùng về màn hình đăng nhập (Logout logic)
            prefs.edit().remove("access_token").remove("refresh_token").apply();
            return null;
        }

        // 3. Gọi API làm mới token một cách ĐỒNG BỘ
        RefreshTokenRequestDTO requestDTO = new RefreshTokenRequestDTO(oldAccessToken, refreshToken);
        Call<LoginResponseDTO> call = authApiService.refreshToken(requestDTO);

        // Lưu ý: Phải gọi call.execute() (Đồng bộ) trong Authenticator, KHÔNG phải enqueue() (Bất đồng bộ)
        retrofit2.Response<LoginResponseDTO> refreshResponse = call.execute();

        if (refreshResponse.isSuccessful() && refreshResponse.body() != null) {
            LoginResponseDTO newTokens = refreshResponse.body();

            // 4. LƯU TOKEN MỚI
            prefs.edit()
                    .putString("access_token", newTokens.getAccessToken())
                    .putString("refresh_token", newTokens.getRefreshToken())
                    .apply();

            // 5. Thử lại yêu cầu gốc với Access Token MỚI
            return response.request().newBuilder()
                    .header("Authorization", "Bearer " + newTokens.getAccessToken())
                    .build();
        } else {
            // Refresh token thất bại (refresh token hết hạn, bị thu hồi,...)
            // TODO: Xóa token và chuyển hướng người dùng về màn hình đăng nhập (Logout logic)
            prefs.edit().remove("access_token").remove("refresh_token").apply();
            return null;
        }
    }
}
