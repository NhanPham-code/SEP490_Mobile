package com.example.sep490_mobile.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.BookingCreateDto;
import com.example.sep490_mobile.data.dto.BookingReadDto;
import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.PrivateUserProfileDTO;
import com.example.sep490_mobile.data.remote.ApiClient;
import com.example.sep490_mobile.data.repository.UserRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingViewModel extends AndroidViewModel {

    // --- LiveData cho dữ liệu Booking ---
    private final MutableLiveData<List<BookingReadDto>> _bookingsForDay = new MutableLiveData<>();
    public final LiveData<List<BookingReadDto>> bookingsForDay = _bookingsForDay;

    // --- LiveData cho dữ liệu User Profile ---
    private final UserRepository userRepository;
    private final MutableLiveData<PrivateUserProfileDTO> _userProfile = new MutableLiveData<>();
    public final LiveData<PrivateUserProfileDTO> userProfile = _userProfile;

    // --- LiveData chung cho các lỗi ---
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    private final MutableLiveData<BookingReadDto> _bookingResult = new MutableLiveData<>();
    public final LiveData<BookingReadDto> bookingResult = _bookingResult;

    private final MutableLiveData<Boolean> _isLoadingBooking = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoadingBooking = _isLoadingBooking;

    public BookingViewModel(@NonNull Application application) {
        super(application);
        // Khởi tạo UserRepository để có thể gọi API user
        this.userRepository = new UserRepository(application);
    }

    /**
     * Lấy danh sách các sân đã được đặt cho một sân vận động và một ngày cụ thể.
     */
    public void fetchBookingsForDay(int stadiumId, Calendar date) {
        TimeZone vietnamTimeZone = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        Calendar startTime = (Calendar) date.clone();
        startTime.set(Calendar.HOUR_OF_DAY, 0);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.SECOND, 0);

        Calendar endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.DAY_OF_MONTH, 1);

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
        isoFormat.setTimeZone(vietnamTimeZone);

        String startIso = isoFormat.format(startTime.getTime());
        String endIso = isoFormat.format(endTime.getTime());

        String statusFilter = "(Status eq 'waiting' or Status eq 'completed' or Status eq 'accepted')";
        String filter = String.format(Locale.US,
                "StadiumId eq %d and Date ge %s and Date lt %s and %s",
                stadiumId, startIso, endIso, statusFilter
        );

        String expand = "BookingDetails";

        ApiClient.getInstance(getApplication()).getApiService().getBookedCourtsByDay(filter, expand)
                .enqueue(new Callback<ODataResponse<BookingReadDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<ODataResponse<BookingReadDto>> call, @NonNull Response<ODataResponse<BookingReadDto>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            _bookingsForDay.postValue(response.body().getItems());
                        } else {
                            _error.postValue("Lỗi tải lịch đặt: " + response.code());
                            _bookingsForDay.postValue(new ArrayList<>()); // Trả về danh sách rỗng khi lỗi
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ODataResponse<BookingReadDto>> call, @NonNull Throwable t) {
                        _error.postValue("Lỗi mạng khi tải lịch đặt: " + t.getMessage());
                        _bookingsForDay.postValue(new ArrayList<>());
                    }
                });
    }

    /**
     * Lấy thông tin cá nhân của người dùng đang đăng nhập.
     */
    public void fetchUserProfile() {
        userRepository.getUserInfo().enqueue(new Callback<PrivateUserProfileDTO>() {
            @Override
            public void onResponse(@NonNull Call<PrivateUserProfileDTO> call, @NonNull Response<PrivateUserProfileDTO> response) {
                if (response.isSuccessful()) {
                    _userProfile.postValue(response.body());
                } else {
                    _error.postValue("Không thể tải thông tin người dùng.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PrivateUserProfileDTO> call, @NonNull Throwable t) {
                _error.postValue("Lỗi mạng khi tải thông tin người dùng: " + t.getMessage());
            }
        });
    }

    public void createBooking(BookingCreateDto bookingRequestDto) {
        _isLoadingBooking.setValue(true);
        _error.setValue(null); // Clear previous errors

        ApiClient.getInstance(getApplication()).getApiService().createBooking(bookingRequestDto)
                .enqueue(new Callback<BookingReadDto>() {
                    @Override
                    public void onResponse(@NonNull Call<BookingReadDto> call, @NonNull Response<BookingReadDto> response) {
                        _isLoadingBooking.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("API_CALL", "✅ createBooking SUCCESS");
                            _bookingResult.setValue(response.body());
                        } else {
                            String errorMsg = "Lỗi tạo booking: " + response.code();
                            try {
                                errorMsg += " - " + response.errorBody().string();
                            } catch (Exception e) { /* Ignore */ }
                            Log.e("API_CALL", "❌ createBooking FAILED: " + errorMsg);
                            _error.setValue(errorMsg);
                            _bookingResult.setValue(null); // Explicitly set to null on failure
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<BookingReadDto> call, @NonNull Throwable t) {
                        _isLoadingBooking.setValue(false);
                        Log.e("API_CALL", "❌ createBooking NETWORK ERROR: " + t.getMessage());
                        _error.setValue("Lỗi mạng khi tạo booking: " + t.getMessage());
                        _bookingResult.setValue(null); // Explicitly set to null on failure
                    }
                });

    }
}