package com.example.sep490_mobile.data.remote;

import com.example.sep490_mobile.data.dto.BiometricTokenResponseDTO;
import com.example.sep490_mobile.data.dto.GoogleApiLoginRequestDTO;
import com.example.sep490_mobile.data.dto.LoginRequestDTO;
import com.example.sep490_mobile.data.dto.LoginResponseDTO;
import com.example.sep490_mobile.data.dto.LogoutRequestDTO;
import com.example.sep490_mobile.data.dto.LogoutResponseDTO;
import com.example.sep490_mobile.data.dto.PrivateUserProfileDTO;
import com.example.sep490_mobile.data.dto.RefreshTokenRequestDTO;
import com.example.sep490_mobile.data.dto.RegisterResponseDTO;
import com.example.sep490_mobile.data.dto.SendOtpRequestDTO;
import com.example.sep490_mobile.data.dto.UpdateUserProfileDTO;
import com.example.sep490_mobile.data.dto.VerifyOtpRequestDTO;
import com.example.sep490_mobile.data.dto.VerifyOtpResponseDTO;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface ApiService {

    // API Login
    @POST("users/login")
    Call<LoginResponseDTO> login(@Body LoginRequestDTO body);

    // API Logout
    @POST("users/logout")
    Call<LogoutResponseDTO> logout(@Body LogoutRequestDTO body);

    // API Refresh Token
    @POST("users/refreshToken")
    Call<LoginResponseDTO> refreshToken(@Body RefreshTokenRequestDTO request);

    // API Check if email exists
    @POST("users/checkEmail")
    Call<Boolean> checkEmailExists(@Body  String email);

    // API send OTP to email
    @POST("users/send-otp")
    Call<Void> sendOTP(@Body SendOtpRequestDTO body);

    // API verify OTP
    @POST("users/verify-otp")
    Call<VerifyOtpResponseDTO> verifyOTP(@Body VerifyOtpRequestDTO body);

    // Register API
    @Multipart
    @POST("users/CustomerRegister")
    Call<RegisterResponseDTO> customerRegister(
            // Các trường dữ liệu dạng text
            @Part("FullName") RequestBody fullName,
            @Part("Email") RequestBody email,
            @Part("Password") RequestBody password,
            @Part("Address") RequestBody address,
            @Part("PhoneNumber") RequestBody phoneNumber,
            @Part("Gender") RequestBody gender,
            @Part("DateOfBirth") RequestBody dateOfBirth,

            // File ảnh đại diện (IFormFile)
            @Part MultipartBody.Part avatar
    );

    // API login with Google
    @POST("users/google-auth")
    Call<LoginResponseDTO> loginWithGoogle(@Body GoogleApiLoginRequestDTO request);

    // Api get user info
    @GET("users/me")
    Call<PrivateUserProfileDTO> getUserInfo();

    // API update user profile
    @PUT("users/update-profile")
    Call<PrivateUserProfileDTO> updateUserProfile(@Body UpdateUserProfileDTO body);

    // API update avatar
    @Multipart
    @PUT("users/update-avatar")
    Call<PrivateUserProfileDTO> updateAvatar(@Part("UserId") RequestBody userId, @Part MultipartBody.Part avatar);

    // API get biometric tokens
    @GET("users/biometric-token")
    Call<BiometricTokenResponseDTO> getBiometricToken();

    // API login with biometric token
    @POST("users/biometric-login")
    Call<LoginResponseDTO> loginWithBiometricToken(@Body String biometricToken);

    // API delete biometric token
    @DELETE("users/biometric-delete")
    Call<Void> deleteBiometricToken();
}
