package com.example.sep490_mobile.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.BookingCreateDto;
import com.example.sep490_mobile.data.dto.BookingDetailCreateDto;
import com.example.sep490_mobile.data.dto.BookingDetailDTO;
import com.example.sep490_mobile.data.dto.BookingReadDto;
import com.example.sep490_mobile.data.dto.BookingSlotRequest;
import com.example.sep490_mobile.data.dto.MonthlyBookingCreateDto;
import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.PrivateUserProfileDTO;
import com.example.sep490_mobile.data.dto.ReadCourtRelationDTO;
import com.example.sep490_mobile.data.remote.ApiClient;
import com.example.sep490_mobile.data.remote.ApiService;
import com.example.sep490_mobile.data.repository.UserRepository;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;
    private final MutableLiveData<Boolean> _bookingSuccess = new MutableLiveData<>(false);
    public LiveData<Boolean> bookingSuccess = _bookingSuccess;
    private final ApiService apiService;

    public BookingViewModel(@NonNull Application application) {
        super(application);
        // Khởi tạo UserRepository để có thể gọi API user
        this.userRepository = new UserRepository(application);
        this.apiService = ApiClient.getInstance(application.getApplicationContext()).getApiService();
    }

    /**
     * SỬA ĐỔI: Lấy danh sách các sân đã đặt cho một sân vận động và một khoảng thời gian cụ thể.
     */
    public void fetchBookingsForDay(int stadiumId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        // 1. Kết hợp ngày và giờ thành đối tượng LocalDateTime
        LocalDateTime myStartTime = date.atTime(startTime);
        LocalDateTime myEndTime = date.atTime(endTime);

        // 2. Chuẩn hóa thời gian sang UTC và định dạng theo chuẩn OData (yyyy-MM-ddTHH:mm:ssZ)
        DateTimeFormatter odataFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String startOdata = myStartTime.atZone(TimeZone.getDefault().toZoneId()).withZoneSameInstant(ZoneOffset.UTC).format(odataFormatter);
        String endOdata = myEndTime.atZone(TimeZone.getDefault().toZoneId()).withZoneSameInstant(ZoneOffset.UTC).format(odataFormatter);

        // 3. Xây dựng các thành phần của câu truy vấn
        String statusFilter = "(Status eq 'waiting' or Status eq 'completed' or Status eq 'accepted')";

        // Điều kiện lọc cho BookingDetails (tìm các khoảng thời gian chồng chéo)
        String detailFilter = String.format(Locale.US, "d/StartTime lt %s and d/EndTime gt %s", endOdata, startOdata);

        // Câu truy vấn $filter chính
        String filterQuery = String.format(Locale.US,
                "StadiumId eq %d and BookingDetails/any(d: %s) and %s",
                stadiumId,
                detailFilter,
                statusFilter
        );

        // Câu truy vấn $expand với bộ lọc lồng bên trong
        String expandQuery = String.format(Locale.US,
                "BookingDetails($filter=%s)",
                // Bỏ tiền tố 'd/' vì context đã là BookingDetails
                detailFilter.replace("d/", "")
        );

        // 4. Gọi API với các tham số đã xây dựng
        apiService.getBookedCourtsByDay(filterQuery, expandQuery)
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
        _isLoading.setValue(true);
        userRepository.getUserInfo().enqueue(new Callback<PrivateUserProfileDTO>() {
            @Override
            public void onResponse(@NonNull Call<PrivateUserProfileDTO> call, @NonNull Response<PrivateUserProfileDTO> response) {
                _isLoading.postValue(false);
                if (response.isSuccessful()) {
                    _userProfile.postValue(response.body());
                } else {
                    _error.postValue("Không thể tải thông tin người dùng.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<PrivateUserProfileDTO> call, @NonNull Throwable t) {
                _isLoading.postValue(false);
                _error.postValue("Lỗi mạng khi tải thông tin người dùng: " + t.getMessage());
            }
        });
    }

    public void createMonthlyBooking(Bundle args) {
        if (args == null) {
            _error.postValue("Lỗi: Không có dữ liệu đặt sân.");
            return;
        }
        _isLoading.setValue(true);

        // 1. Lấy tất cả các sân liên quan
        int[] selectedCourtIds = args.getIntArray("COURT_IDS");
        if (selectedCourtIds == null || selectedCourtIds.length == 0) {
            _error.postValue("Lỗi: Không có sân nào được chọn.");
            _isLoading.postValue(false);
            return;
        }

        getAllInvolvedCourts(Arrays.stream(selectedCourtIds).boxed().collect(Collectors.toList()), allInvolvedIds -> {
            // 2. Sau khi có tất cả sân liên quan, tạo danh sách slot để kiểm tra
            List<BookingSlotRequest> slotsToCheck = buildSlotRequests(args, allInvolvedIds);
            if (slotsToCheck.isEmpty()) {
                _error.postValue("Không có ngày hợp lệ để kiểm tra.");
                _isLoading.postValue(false);
                return;
            }

            // 3. Gọi API checkAvailability
            checkAvailabilityAndProceed(slotsToCheck, args);
        });
    }

    public void onBookingSuccessNavigated() {
        _bookingSuccess.setValue(false);
    }

    private void getAllInvolvedCourts(List<Integer> selectedCourtIds, final OnInvolvedCourtsReady callback) {
        Set<Integer> allInvolvedIds = new HashSet<>(selectedCourtIds);
        AtomicInteger pendingCalls = new AtomicInteger(selectedCourtIds.size() * 2);

        if (pendingCalls.get() == 0) {
            callback.onReady(new ArrayList<>(allInvolvedIds));
            return;
        }

        for (int courtId : selectedCourtIds) {
            // Lấy sân cha
            apiService.getAllCourtRelationByChildId(courtId).enqueue(new Callback<List<ReadCourtRelationDTO>>() {
                @Override
                public void onResponse(Call<List<ReadCourtRelationDTO>> call, Response<List<ReadCourtRelationDTO>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        for (ReadCourtRelationDTO relation : response.body()) {
                            allInvolvedIds.add(relation.getParentCourtId());
                        }
                    }
                    if (pendingCalls.decrementAndGet() == 0) {
                        callback.onReady(new ArrayList<>(allInvolvedIds));
                    }
                }
                @Override
                public void onFailure(Call<List<ReadCourtRelationDTO>> call, Throwable t) {
                    if (pendingCalls.decrementAndGet() == 0) {
                        callback.onReady(new ArrayList<>(allInvolvedIds));
                    }
                }
            });

            // Lấy sân con
            apiService.getAllCourtRelationByParentId(courtId).enqueue(new Callback<List<ReadCourtRelationDTO>>() {
                @Override
                public void onResponse(Call<List<ReadCourtRelationDTO>> call, Response<List<ReadCourtRelationDTO>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        for (ReadCourtRelationDTO relation : response.body()) {
                            allInvolvedIds.add(relation.getChildCourtId());
                        }
                    }
                    if (pendingCalls.decrementAndGet() == 0) {
                        callback.onReady(new ArrayList<>(allInvolvedIds));
                    }
                }
                @Override
                public void onFailure(Call<List<ReadCourtRelationDTO>> call, Throwable t) {
                    if (pendingCalls.decrementAndGet() == 0) {
                        callback.onReady(new ArrayList<>(allInvolvedIds));
                    }
                }
            });
        }
    }

    private List<BookingSlotRequest> buildSlotRequests(Bundle args, List<Integer> allInvolvedIds) {
        List<BookingSlotRequest> slots = new ArrayList<>();
        int year = args.getInt("YEAR");
        int month = args.getInt("MONTH");
        int startHour = args.getInt("START_TIME");
        int endHour = args.getInt("END_TIME");

        String[] dateStrings = args.getStringArray("BOOKABLE_DATES");
        if (dateStrings == null) return slots;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        for (String dateStr : dateStrings) {
            LocalDate date = LocalDate.parse(dateStr);
            LocalDateTime startTime = date.atTime(startHour, 0);
            LocalDateTime endTime = date.atTime(endHour, 0);

            for (int courtId : allInvolvedIds) {
                slots.add(new BookingSlotRequest(courtId, startTime.format(formatter), endTime.format(formatter)));
            }
        }
        return slots;
    }

    private void checkAvailabilityAndProceed(List<BookingSlotRequest> slotsToCheck, Bundle args) {
        apiService.checkSlotsAvailability(slotsToCheck).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Sân trống, tiến hành tạo booking
                    proceedToCreateBooking(args);
                } else {
                    _isLoading.postValue(false);
                    _error.postValue("Rất tiếc, một trong các ngày bạn chọn đã có người khác đặt.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                _isLoading.postValue(false);
                _error.postValue("Lỗi mạng khi kiểm tra lịch: " + t.getMessage());
            }
        });
    }

    private void proceedToCreateBooking(Bundle args) {
        int stadiumId = args.getInt("stadiumId", 0);
        float totalPrice = args.getFloat("TOTAL_PRICE", 0f);
        String startTimeStr = String.format(Locale.US, "%02d:00", args.getInt("START_TIME"));
        String endTimeStr = String.format(Locale.US, "%02d:00", args.getInt("END_TIME"));
        int month = args.getInt("MONTH");
        int year = args.getInt("YEAR");

        int[] courtIds = args.getIntArray("COURT_IDS");
        List<Integer> courtIdList = (courtIds != null) ? Arrays.stream(courtIds).boxed().collect(Collectors.toList()) : new ArrayList<>();

        String[] dateStrings = args.getStringArray("BOOKABLE_DATES");
        List<Integer> dayList = new ArrayList<>();
        if (dateStrings != null) {
            for (String dateStr : dateStrings) {
                dayList.add(LocalDate.parse(dateStr).getDayOfMonth());
            }
        }

        MonthlyBookingCreateDto dto = new MonthlyBookingCreateDto(
                stadiumId, totalPrice, totalPrice, "cod",
                startTimeStr, endTimeStr, month, year, dayList, courtIdList
        );

        apiService.createMonthlyBooking(dto).enqueue(new Callback<BookingReadDto>() {
            @Override
            public void onResponse(Call<BookingReadDto> call, Response<BookingReadDto> response) {
                _isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    _error.postValue("Đặt sân hàng tháng thành công!");
                    _bookingSuccess.postValue(true);
                } else {
                    _error.postValue("Tạo đơn đặt sân thất bại. Vui lòng thử lại.");
                }
            }
            @Override
            public void onFailure(Call<BookingReadDto> call, Throwable t) {
                _isLoading.postValue(false);
                _error.postValue("Lỗi mạng khi tạo đơn: " + t.getMessage());
            }
        });
    }

    // Interface để xử lý callback bất đồng bộ
    interface OnInvolvedCourtsReady {
        void onReady(List<Integer> allInvolvedIds);
    }

    public void createDailyBooking(Bundle args) {
        if (args == null) {
            _error.postValue("Lỗi: Không có dữ liệu đặt sân.");
            return;
        }
        _isLoadingBooking.setValue(true);
        _bookingResult.setValue(null); // Reset kết quả cũ
        _error.setValue(null); // Xóa lỗi cũ

        int[] selectedCourtIds = args.getIntArray("courtIds");
        if (selectedCourtIds == null || selectedCourtIds.length == 0) {
            _error.postValue("Lỗi: Không có sân nào được chọn.");
            _isLoadingBooking.postValue(false);
            return;
        }

        // 1. Lấy tất cả các sân liên quan (giống hệt monthly booking)
        getAllInvolvedCourts(Arrays.stream(selectedCourtIds).boxed().collect(Collectors.toList()), allInvolvedIds -> {

            // 2. Tạo danh sách slot để kiểm tra (chỉ 1 ngày)
            List<BookingSlotRequest> slotsToCheck = buildSlotRequestsForDaily(args, allInvolvedIds);
            if (slotsToCheck.isEmpty()) {
                _error.postValue("Dữ liệu ngày giờ không hợp lệ.");
                _isLoadingBooking.postValue(false);
                return;
            }

            // 3. Gọi API checkAvailability
            checkAvailabilityAndProceedDaily(slotsToCheck, args);
        });
    }

    private void checkAvailabilityAndProceedDaily(List<BookingSlotRequest> slotsToCheck, Bundle args) {
        apiService.checkSlotsAvailability(slotsToCheck).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Sân trống, tiến hành tạo booking
                    proceedToCreateDailyBooking(args);
                } else {
                    _isLoadingBooking.postValue(false);
                    _error.postValue("Rất tiếc, một trong các khung giờ (hoặc sân liên quan) đã có người khác đặt.");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                _isLoadingBooking.postValue(false);
                _error.postValue("Lỗi mạng khi kiểm tra lịch: " + t.getMessage());
            }
        });
    }

    private void proceedToCreateDailyBooking(Bundle args) {
        // --- SỬA LỖI 1: Lấy thông tin User ---
        PrivateUserProfileDTO currentUser = _userProfile.getValue();
        if (currentUser == null) {
            _error.postValue("Lỗi: Không lấy được thông tin người dùng. Vui lòng thử lại.");
            _isLoadingBooking.postValue(false);
            return;
        }
        int userId = Integer.parseInt(currentUser.getUserId()); // Giả sử DTO của bạn có hàm getId()

        // Lấy dữ liệu từ bundle
        int stadiumId = args.getInt("stadiumId");
        double totalPrice = (double) args.getFloat("totalPrice"); // Chuyển sang Double
        String dateString = args.getString("date");
        int startHour = args.getInt("startTime");
        int endHour = args.getInt("endTime");
        int[] courtIds = args.getIntArray("courtIds");

        // Định dạng thời gian
        String startTimeIso = String.format(Locale.US, "%sT%02d:00:00", dateString, startHour);
        String endTimeIso = String.format(Locale.US, "%sT%02d:00:00", dateString, endHour);

        // --- SỬA LỖI 2: Dùng BookingDetailCreateDto ---
        // (Giả sử bạn có file BookingDetailCreateDto.java với constructor
        // public BookingDetailCreateDto(int courtId, String startTime, String endTime) )
        List<BookingDetailCreateDto> details = new ArrayList<>();
        if (courtIds != null) {
            for (int courtId : courtIds) {
                details.add(new BookingDetailCreateDto(courtId, startTimeIso, endTimeIso));
            }
        }

        // --- SỬA LỖI 3: Dùng constructor của BookingCreateDto ---
        BookingCreateDto bookingDto = new BookingCreateDto(
                userId,
                "waiting",
                startTimeIso, // Dùng ngày + giờ bắt đầu làm ngày chính
                totalPrice,
                totalPrice,   // OriginalPrice (chưa có logic giảm giá)
                "vnpay_100",  // PaymentMethod (Hardcode)
                null,         // DiscountId (Chưa có logic)
                stadiumId,
                details
        );

        // Gọi hàm createBooking đã có sẵn (hàm này đã xử lý _isLoadingBooking, _bookingResult, _error)
        createBooking(bookingDto);
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

    private List<BookingSlotRequest> buildSlotRequestsForDaily(Bundle args, List<Integer> allInvolvedIds) {
        List<BookingSlotRequest> slots = new ArrayList<>();
        String dateString = args.getString("date"); // "2025-10-26"
        int startHour = args.getInt("startTime"); // 16
        int endHour = args.getInt("endTime");     // 18

        if (dateString == null) return slots;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDate date = LocalDate.parse(dateString);

        LocalDateTime startTime = date.atTime(startHour, 0);
        LocalDateTime endTime = date.atTime(endHour, 0);

        String startTimeStr = startTime.format(formatter);
        String endTimeStr = endTime.format(formatter);

        for (int courtId : allInvolvedIds) {
            slots.add(new BookingSlotRequest(courtId, startTimeStr, endTimeStr));
        }
        return slots;
    }

    public boolean isLoggedIn() {
        SharedPreferences prefs = getApplication().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        int currentUserId = prefs.getInt("user_id", -1);
        return currentUserId != -1;
    }
}