package com.example.sep490_mobile.data.remote;

import com.example.sep490_mobile.data.dto.BiometricTokenResponseDTO;
import com.example.sep490_mobile.data.dto.BookingCreateDto;
import com.example.sep490_mobile.data.dto.BookingReadDto;
import com.example.sep490_mobile.data.dto.BookingSlotRequest;
import com.example.sep490_mobile.data.dto.CreateTeamMemberDTO;
import com.example.sep490_mobile.data.dto.CreateTeamPostDTO;
import com.example.sep490_mobile.data.dto.MonthlyBookingCreateDto;
import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.ReadCourtRelationDTO;
import com.example.sep490_mobile.data.dto.PublicProfileDTO;
import com.example.sep490_mobile.data.dto.ReadTeamMemberDTO;
import com.example.sep490_mobile.data.dto.ReadTeamMemberForDetailDTO;
import com.example.sep490_mobile.data.dto.ReadTeamPostDTO;
import com.example.sep490_mobile.data.dto.ReadTeamPostResponse;
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
import com.example.sep490_mobile.data.dto.UpdateTeamMemberDTO;
import com.example.sep490_mobile.data.dto.UpdateTeamPostDTO;
import com.example.sep490_mobile.data.dto.UpdateUserProfileDTO;
import com.example.sep490_mobile.data.dto.VerifyOtpRequestDTO;
import com.example.sep490_mobile.data.dto.VerifyOtpResponseDTO;
import com.example.sep490_mobile.data.dto.booking.response.BookingHistoryODataResponse;
import com.example.sep490_mobile.data.dto.booking.response.MonthlyBookingODataResponse;
import com.example.sep490_mobile.data.dto.discount.ReadDiscountDTO;

import java.util.List;
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

    @GET("bookings/booked")
    Call<ODataResponse<BookingReadDto>> getBookedCourtsByDay(
            @Query("$filter") String filter,
            @Query("$expand") String expand
    );

    @GET("GetAllCourtRelationParent")
    Call<List<ReadCourtRelationDTO>> getAllCourtRelationByParentId(@Query("parentId") int parentId);

    @GET("GetAllCourtRelationChild")
    Call<List<ReadCourtRelationDTO>> getAllCourtRelationByChildId(@Query("childId") int childId);

    @POST("Bookings/add") // Match your API endpoint path
    Call<BookingReadDto> createBooking(@Body BookingCreateDto bookingRequest);

    // API get other profile
    @GET("users/get")
    Call<ODataResponse<PublicProfileDTO>> getPublicProfileByListId(@Query("$filter=UserId in ") String userId);

    // API get Team post
    @GET("odata/TeamPost")
    Call<ODataResponse<ReadTeamPostDTO>> getTeamPost(@QueryMap Map<String, String> odataOptions);

    @POST("CreateTeamPost")
    Call<ReadTeamPostResponse> createTeamPost(@Body CreateTeamPostDTO createTeamPostDTO);

    @POST("AddNewTeamMember")
    Call<ReadTeamMemberDTO> createTeamMember(@Body CreateTeamMemberDTO createTeamMemberDTO);

    @GET("bookings/history?$expand=BookingDetails")
    Call<BookingHistoryODataResponse> getBookingsHistory(@Query("$filter") String filter);

    // API lấy các gói đặt tháng
    @GET("monthlyBooking")
    Call<MonthlyBookingODataResponse> getMonthlyBookings(@Query("$filter") String filter);

    @GET("discounts/{id}")
    Call<ReadDiscountDTO> getDiscountById(@Path("id") int discountId);

    @PUT("UpdateTeamPost")
    Call<ReadTeamPostDTO> updateTeamPost(@Body UpdateTeamPostDTO updateTeamPostDTO);

    @PUT("UpdateTeamMember")
    Call<ReadTeamMemberForDetailDTO> updateTeamMember(@Body UpdateTeamMemberDTO updateTeamMemberDTO);

    @GET("GetAllTeamMember")
    Call<List<ReadTeamMemberForDetailDTO>> getTeamMember(@Query("postId") int postId);

    @GET("GetTeamMemberByPostIdAndId")
    Call<ReadTeamMemberForDetailDTO> getTeamMemberByPostIdAndId(@Query("teamId") int teamId, @Query("postId") int postId);

    @DELETE("DeleteTeamPost")
    Call<Boolean> deleteTeamPost(@Query("postId") int postId);

    @DELETE("DeleteTeamMember")
    Call<Boolean> deleteTeamMember(@Query("teamMemberId") int teamMemberId, @Query("postId") int postId);

    @GET("Booking/FilterByDateAndHour")
    Call<List<BookingReadDto>> filterByDateAndHour(
            @Query("year") int year,
            @Query("month") int month,
            @Query("days") List<Integer> days,
            @Query("startTime") String startTime, // Đổi sang String
            @Query("endTime") String endTime,     // Đổi sang String
            @Query("stadiumId") int stadiumId
    );

    @GET("Booking/FilterByCourtAndHour")
    Call<List<BookingReadDto>> filterByCourtAndHourForCalendar(
            @Query("courtIds") List<Integer> courtIds,
            @Query("year") int year,
            @Query("month") int month,
            @Query("startTime") String startTime, // <-- Đổi thành String
            @Query("endTime") String endTime     // <-- Đổi thành String
    );

    @POST("booking/monthly")
    Call<BookingReadDto> createMonthlyBooking(@Body MonthlyBookingCreateDto bookingDto);

    @POST("bookings/checkAvailability")
    Call<Void> checkSlotsAvailability(@Body List<BookingSlotRequest> requestedSlots);
}
