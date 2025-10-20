package com.example.sep490_mobile.data.remote;

import com.example.sep490_mobile.data.dto.BiometricTokenResponseDTO;
import com.example.sep490_mobile.data.dto.FeedbackDto;
import com.example.sep490_mobile.data.dto.FeedbackRequestDto;
import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.ScheduleBookingODataResponseDTO;
import com.example.sep490_mobile.data.dto.GoogleApiLoginRequestDTO;
import com.example.sep490_mobile.data.dto.LoginRequestDTO;
import com.example.sep490_mobile.data.dto.LoginResponseDTO;
import com.example.sep490_mobile.data.dto.LogoutRequestDTO;
import com.example.sep490_mobile.data.dto.LogoutResponseDTO;
import com.example.sep490_mobile.data.dto.PrivateUserProfileDTO;
import com.example.sep490_mobile.data.dto.RefreshTokenRequestDTO;
import com.example.sep490_mobile.data.dto.RegisterResponseDTO;
import com.example.sep490_mobile.data.dto.ResetPasswordRequestDTO;
import com.example.sep490_mobile.data.dto.ResetPasswordResponseDTO;
import com.example.sep490_mobile.data.dto.ScheduleODataStadiumResponseDTO;
import com.example.sep490_mobile.data.dto.SendOtpRequestDTO;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.data.dto.UpdateUserProfileDTO;
import com.example.sep490_mobile.data.dto.VerifyOtpRequestDTO;
import com.example.sep490_mobile.data.dto.VerifyOtpResponseDTO;
import com.example.sep490_mobile.data.dto.booking.MonthlyBookingReadDTO;
import com.example.sep490_mobile.data.dto.booking.response.BookingHistoryODataResponse;
import com.example.sep490_mobile.data.dto.booking.response.MonthlyBookingODataResponse;
import com.example.sep490_mobile.data.dto.discount.ReadDiscountDTO;

import java.util.Map;

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
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

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

    // API get stadium
    @GET("odata/Stadium")
    Call<ODataResponse<StadiumDTO>> getStadiumsOdata(
            @QueryMap Map<String, String> odataOptions
    );

    // API reset password
    @PUT("users/forgot-password")
    Call<ResetPasswordResponseDTO> resetPassword(@Body ResetPasswordRequestDTO body);

    @GET("bookings/history?$expand=BookingDetails")
    Call<ScheduleBookingODataResponseDTO> getBookings(@Query("$filter") String filter);

    @GET("odata/Stadium")
    Call<ScheduleODataStadiumResponseDTO> getStadiums(
            @Query("$filter") String filter,
            @Query("$expand") String expand
    );

    @GET("odata/FeedbackOData")
    Call<ODataResponse<FeedbackDto>> getFeedbacksOdata(
            @QueryMap Map<String, String> odataOptions
    );


    @Multipart
    @POST("feedback")
    Call<FeedbackDto> createFeedbackMultipart(
            @Part("UserId") RequestBody userId,
            @Part("StadiumId") RequestBody stadiumId,
            @Part("Rating") RequestBody rating,
            @Part("Comment") RequestBody comment,
            @Part MultipartBody.Part image // pass null nếu không có ảnh
    );

    @Multipart
    @PUT("feedback/{id}")
    Call<FeedbackDto> updateFeedbackMultipart(
            @Path("id") int id,
            @Part("UserId") RequestBody userId,
            @Part("StadiumId") RequestBody stadiumId,
            @Part("Rating") RequestBody rating,
            @Part("Comment") RequestBody comment,
            @Part MultipartBody.Part image
    );

    @PUT("feedback/{id}")
    Call<FeedbackDto> updateFeedback(@Path("id") int feedbackId, @Body FeedbackRequestDto request);


    @DELETE("feedback/{id}")
    Call<Void> deleteFeedback(@Path("id") int feedbackId);

    @GET("bookings/history?$expand=BookingDetails")
    Call<BookingHistoryODataResponse> getBookingsHistory(@Query("$filter") String filter);

    // API lấy các gói đặt tháng
    @GET("monthlyBooking")
    Call<MonthlyBookingODataResponse> getMonthlyBookings(@Query("$filter") String filter);

    @GET("discounts/{id}")
    Call<ReadDiscountDTO> getDiscountById(@Path("id") int discountId);
}
