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

import com.example.sep490_mobile.data.dto.notification.NotificationDTO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.example.sep490_mobile.data.dto.OdataHaveCountResponse;
import com.example.sep490_mobile.data.dto.booking.BookingViewDTO;
import com.example.sep490_mobile.data.dto.booking.BookingUpdateDTO;
import com.example.sep490_mobile.data.dto.booking.MonthlyBookingReadDTO;
import com.example.sep490_mobile.data.dto.booking.MonthlyBookingUpdateDTO;
import com.example.sep490_mobile.data.dto.booking.response.BookingHistoryODataResponse;
import com.example.sep490_mobile.data.dto.booking.response.MonthlyBookingODataResponse;
import com.example.sep490_mobile.data.dto.discount.ReadDiscountDTO;
import com.example.sep490_mobile.data.dto.discount.UpdateDiscountDTO;
import com.example.sep490_mobile.data.repository.DiscountRepository;
import com.example.sep490_mobile.data.dto.BookingCreateDto;
import com.example.sep490_mobile.data.dto.BookingDetailCreateDto;
import com.example.sep490_mobile.data.dto.BookingReadDto;
import com.example.sep490_mobile.data.dto.BookingSlotRequest;
import com.example.sep490_mobile.data.dto.MonthlyBookingCreateDto;
import com.example.sep490_mobile.data.dto.ODataResponse;
import com.example.sep490_mobile.data.dto.PrivateUserProfileDTO;
import com.example.sep490_mobile.data.dto.ReadCourtRelationDTO;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.data.repository.StadiumRepository;
import com.example.sep490_mobile.data.dto.notification.CreateNotificationDTO;
import com.example.sep490_mobile.data.repository.NotificationRepository;
import com.example.sep490_mobile.data.repository.BookingRepository;
import com.example.sep490_mobile.data.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingViewModel extends AndroidViewModel {

    private static final String TAG = "BookingViewModel_Log";

    // --- Repositories ---
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final DiscountRepository discountRepository;
    private final NotificationRepository notificationRepository;
    private final StadiumRepository stadiumRepository;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    // --- LiveData for User Profile ---
    private final MutableLiveData<PrivateUserProfileDTO> _userProfile = new MutableLiveData<>();
    public final LiveData<PrivateUserProfileDTO> userProfile = _userProfile;

    // --- LiveData for Bookings For Day ---
    private final MutableLiveData<List<BookingReadDto>> _bookingsForDay = new MutableLiveData<>();
    public final LiveData<List<BookingReadDto>> bookingsForDay = _bookingsForDay;

    // --- General LiveData ---
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    // --- LiveData for Booking Creation Process ---
    private final MutableLiveData<Boolean> _isLoadingBooking = new MutableLiveData<>(false);
    public final LiveData<Boolean> isLoadingBooking = _isLoadingBooking;

    /** LiveData này trả về BookingReadDto sau khi tạo booking thành công (cho cả Daily và Monthly)
     * Fragment sẽ observe cái này để lấy ID và mở VNPay. */
    private final MutableLiveData<BookingReadDto> _bookingResult = new MutableLiveData<>();
    public final LiveData<BookingReadDto> bookingResult = _bookingResult;

    /** LiveData này chỉ dùng cho luồng KHÔNG CÓ VNPay (nếu có) */
    private final MutableLiveData<Boolean> _bookingSuccess = new MutableLiveData<>(false);
    public LiveData<Boolean> bookingSuccess = _bookingSuccess;

    // --- LiveData for Applicable Discounts ---
    private final MutableLiveData<List<ReadDiscountDTO>> _applicableDiscounts = new MutableLiveData<>();
    public final LiveData<List<ReadDiscountDTO>> applicableDiscounts = _applicableDiscounts;


    public BookingViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application);
        this.bookingRepository = new BookingRepository(application);
        this.discountRepository = new DiscountRepository(application);
        this.notificationRepository = new NotificationRepository(application);
        this.stadiumRepository = new StadiumRepository(application);
        this.sharedPreferences = application.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    /**
     * Lấy thông tin cá nhân của người dùng (Họ tên, SĐT, Email...)
     * Dùng ở màn hình Checkout.
     */
    public void fetchUserProfile() {
        Log.d(TAG, "Fetching user profile...");
        _isLoading.setValue(true);
        _error.setValue(null);
        userRepository.getUserInfo().enqueue(new Callback<PrivateUserProfileDTO>() {
            @Override
            public void onResponse(@NonNull Call<PrivateUserProfileDTO> call, @NonNull Response<PrivateUserProfileDTO> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    _userProfile.postValue(response.body());
                    Log.i(TAG, "User profile fetched successfully.");
                } else {
                    _error.postValue("Lỗi tải thông tin người dùng: " + response.code());
                    Log.e(TAG, "Failed to fetch user profile: " + response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call<PrivateUserProfileDTO> call, @NonNull Throwable t) {
                _isLoading.setValue(false);
                _error.postValue("Lỗi mạng khi tải thông tin người dùng: " + t.getMessage());
                Log.e(TAG, "Network error fetching user profile", t);
            }
        });
    }

    /**
     * Lấy danh sách các sân đã bị đặt trong một ngày cụ thể (để hiển thị lịch).
     */
    public void fetchBookingsForDay(int stadiumId, LocalDate date, LocalTime startTime, LocalTime endTime) {
        Log.d(TAG, "Fetching bookings for day: " + date + " from " + startTime + " to " + endTime);
        LocalDateTime myStartTime = date.atTime(startTime);
        LocalDateTime myEndTime = date.atTime(endTime);
        DateTimeFormatter odataFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String startOdata = myStartTime.atZone(TimeZone.getDefault().toZoneId()).withZoneSameInstant(ZoneOffset.UTC).format(odataFormatter);
        String endOdata = myEndTime.atZone(TimeZone.getDefault().toZoneId()).withZoneSameInstant(ZoneOffset.UTC).format(odataFormatter);
        String statusFilter = "(Status eq 'waiting' or Status eq 'completed' or Status eq 'accepted')";
        String detailFilter = String.format(Locale.US, "d/StartTime lt %s and d/EndTime gt %s", endOdata, startOdata);
        String filterQuery = String.format(Locale.US, "StadiumId eq %d and BookingDetails/any(d: %s) and %s", stadiumId, detailFilter, statusFilter);
        String expandQuery = String.format(Locale.US, "BookingDetails($filter=%s)", detailFilter.replace("d/", ""));

        bookingRepository.getBookedCourtsByDay(filterQuery, expandQuery)
                .enqueue(new Callback<ODataResponse<BookingReadDto>>() {
                    @Override
                    public void onResponse(@NonNull Call<ODataResponse<BookingReadDto>> call, @NonNull Response<ODataResponse<BookingReadDto>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            _bookingsForDay.postValue(response.body().getItems());
                        } else {
                            _error.postValue("Lỗi tải lịch đặt: " + response.code());
                            _bookingsForDay.postValue(new ArrayList<>());
                            Log.e(TAG, "Failed to fetch bookings for day: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<ODataResponse<BookingReadDto>> call, @NonNull Throwable t) {
                        _error.postValue("Lỗi mạng khi tải lịch đặt: " + t.getMessage());
                        _bookingsForDay.postValue(new ArrayList<>());
                        Log.e(TAG, "Network error fetching bookings for day", t);
                    }
                });
    }

    /**
     * Lấy danh sách mã giảm giá có thể áp dụng (cả "Unique" và "Stadium").
     * Được gọi khi người dùng bấm nút "Chọn mã" ở màn hình Checkout.
     */
    public void fetchApplicableDiscounts(int stadiumId) {
        _error.setValue(null);
        _applicableDiscounts.setValue(null);
        Log.i(TAG, "Fetching applicable discounts for stadiumId: " + stadiumId);

        String userId = String.valueOf(sharedPreferences.getInt("user_id", -1));
        if (userId.equals("-1")) {
            _error.postValue("User ID không hợp lệ.");
            return;
        }

        final List<ReadDiscountDTO> combinedList = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger pendingCalls = new AtomicInteger(2); // 2 cuộc gọi API (unique, stadium)

        Runnable checkCompletion = () -> {
            if (pendingCalls.decrementAndGet() == 0) {
                // Lọc trùng lặp và post kết quả
                List<ReadDiscountDTO> distinctList = combinedList.stream()
                        .collect(Collectors.collectingAndThen(
                                Collectors.toMap(ReadDiscountDTO::getId, d -> d, (d1, d2) -> d1),
                                map -> new ArrayList<>(map.values())
                        ));
                Log.i(TAG, "Posting " + distinctList.size() + " distinct applicable discounts.");
                _applicableDiscounts.postValue(distinctList);
            }
        };

        // 1. Lấy mã cá nhân ("unique")
        discountRepository.getDiscountsByType(userId, "unique", 1, 100)
                .enqueue(new Callback<OdataHaveCountResponse<ReadDiscountDTO>>() {
                    @Override
                    public void onResponse(@NonNull Call<OdataHaveCountResponse<ReadDiscountDTO>> call, @NonNull Response<OdataHaveCountResponse<ReadDiscountDTO>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                            List<ReadDiscountDTO> personalDiscounts = response.body().getValue();
                            List<ReadDiscountDTO> applicablePersonal = personalDiscounts.stream()
                                    .filter(d -> d.getStadiumIds() == null || d.getStadiumIds().isEmpty() || d.getStadiumIds().contains(stadiumId))
                                    .collect(Collectors.toList());
                            combinedList.addAll(applicablePersonal);
                        }
                        checkCompletion.run();
                    }
                    @Override
                    public void onFailure(@NonNull Call<OdataHaveCountResponse<ReadDiscountDTO>> call, @NonNull Throwable t) {
                        Log.e(TAG, "Network error fetching personal discounts", t);
                        checkCompletion.run();
                    }
                });

        // 2. Lấy mã của sân ("stadium")
        discountRepository.getDiscountsByType("0", "stadium", 1, 100)
                .enqueue(new Callback<OdataHaveCountResponse<ReadDiscountDTO>>() {
                    @Override
                    public void onResponse(@NonNull Call<OdataHaveCountResponse<ReadDiscountDTO>> call, @NonNull Response<OdataHaveCountResponse<ReadDiscountDTO>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                            List<ReadDiscountDTO> stadiumDiscounts = response.body().getValue();
                            List<ReadDiscountDTO> applicableStadium = stadiumDiscounts.stream()
                                    .filter(d -> d.getStadiumIds() != null && d.getStadiumIds().contains(stadiumId))
                                    .collect(Collectors.toList());
                            combinedList.addAll(applicableStadium);
                        }
                        checkCompletion.run();
                    }
                    @Override
                    public void onFailure(@NonNull Call<OdataHaveCountResponse<ReadDiscountDTO>> call, @NonNull Throwable t) {
                        Log.e(TAG, "Network error fetching stadium discounts", t);
                        checkCompletion.run();
                    }
                });
    }

    /**
     * Tạo một booking (lịch đơn) với status "waiting".
     * Được gọi bởi `proceedToCreateDailyBooking`.
     * Trả về BookingReadDto (chứa ID) qua LiveData `_bookingResult` để mở VNPay.
     */
    public void createBooking(BookingCreateDto bookingRequestDto) {
        _isLoadingBooking.setValue(true);
        _error.setValue(null);
        _bookingResult.setValue(null);

        Log.i(TAG, "Attempting to create booking via BookingRepository...");
        Log.d(TAG, "Booking Request DTO: " + bookingRequestDto.toString());

        bookingRepository.createBooking(bookingRequestDto)
                .enqueue(new Callback<BookingReadDto>() {
                    @Override
                    public void onResponse(@NonNull Call<BookingReadDto> call, @NonNull Response<BookingReadDto> response) {
                        _isLoadingBooking.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            Log.i(TAG, "✅ createBooking SUCCESS - ID: " + response.body().getId());
                            _bookingResult.postValue(response.body());
                        } else {
                            String errorMsg = "Lỗi tạo booking: " + response.code();
                            try { if(response.errorBody() != null) errorMsg += " - " + response.errorBody().string(); } catch (Exception e) {}
                            Log.e(TAG, "❌ createBooking FAILED: " + errorMsg);
                            _error.postValue(errorMsg);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<BookingReadDto> call, @NonNull Throwable t) {
                        _isLoadingBooking.setValue(false);
                        Log.e(TAG, "❌ createBooking NETWORK ERROR: " + t.getMessage(), t);
                        _error.postValue("Lỗi mạng khi tạo booking: " + t.getMessage());
                    }
                });
    }

    /**
     * Hàm tổng, bắt đầu luồng tạo Booking Tháng (từ MonthlyCheckoutFragment).
     */
    public void createMonthlyBooking(Bundle args) {
        if (args == null) { _error.postValue("Lỗi: Không có dữ liệu đặt sân."); return; }
        _isLoading.setValue(true);
        Log.i(TAG, "Starting monthly booking creation process...");
        int[] selectedCourtIds = args.getIntArray("COURT_IDS");
        if (selectedCourtIds == null || selectedCourtIds.length == 0) { _error.postValue("Lỗi: Không có sân nào được chọn."); _isLoading.postValue(false); return; }
        getAllInvolvedCourts(Arrays.stream(selectedCourtIds).boxed().collect(Collectors.toList()), allInvolvedIds -> {
            List<BookingSlotRequest> slotsToCheck = buildSlotRequests(args, allInvolvedIds);
            if (slotsToCheck.isEmpty()) { _error.postValue("Không có ngày hợp lệ."); _isLoading.postValue(false); return; }
            checkAvailabilityAndProceedMonthly(slotsToCheck, args);
        });
    }

    /**
     * (Helper) Lấy tất cả sân con/cha liên quan để kiểm tra trùng lịch.
     */
    private void getAllInvolvedCourts(List<Integer> selectedCourtIds, final OnInvolvedCourtsReady callback) {
        Log.d(TAG,"Getting all involved courts for: " + selectedCourtIds); Set<Integer> allInvolvedIds = new HashSet<>(selectedCourtIds); AtomicInteger pendingCalls = new AtomicInteger(selectedCourtIds.size() * 2);
        if (pendingCalls.get() == 0) { Log.w(TAG, "getAllInvolvedCourts: Input list empty..."); callback.onReady(new ArrayList<>(allInvolvedIds)); return; }
        Runnable checkCompletion = () -> { if (pendingCalls.decrementAndGet() == 0) { Log.i(TAG, "Finished getting all involved courts..."); callback.onReady(new ArrayList<>(allInvolvedIds)); } };
        for (int courtId : selectedCourtIds) {
            bookingRepository.getAllCourtRelationByChildId(courtId).enqueue(new Callback<List<ReadCourtRelationDTO>>() { @Override public void onResponse(Call<List<ReadCourtRelationDTO>> call, Response<List<ReadCourtRelationDTO>> response) { if(response.isSuccessful()&&response.body()!=null){ response.body().forEach(r -> allInvolvedIds.add(r.getParentCourtId())); } else { Log.w(TAG, "Failed parent: " + response.code());} checkCompletion.run(); } @Override public void onFailure(Call<List<ReadCourtRelationDTO>> call, Throwable t) { Log.e(TAG, "Net error parent", t); checkCompletion.run(); } });
            bookingRepository.getAllCourtRelationByParentId(courtId).enqueue(new Callback<List<ReadCourtRelationDTO>>() { @Override public void onResponse(Call<List<ReadCourtRelationDTO>> call, Response<List<ReadCourtRelationDTO>> response) { if(response.isSuccessful()&&response.body()!=null){ response.body().forEach(r -> allInvolvedIds.add(r.getChildCourtId())); } else { Log.w(TAG, "Failed child: " + response.code());} checkCompletion.run(); } @Override public void onFailure(Call<List<ReadCourtRelationDTO>> call, Throwable t) { Log.e(TAG, "Net error child", t); checkCompletion.run(); } });
        }
    }

    /**
     * (Helper) Tạo danh sách các slot (CourtId, StartTime, EndTime) cho Booking Tháng.
     */
    private List<BookingSlotRequest> buildSlotRequests(Bundle args, List<Integer> allInvolvedIds) {
        List<BookingSlotRequest> slots = new ArrayList<>(); int year = args.getInt("YEAR"); int month = args.getInt("MONTH"); int startHour = args.getInt("START_TIME"); int endHour = args.getInt("END_TIME"); String[] dateStrings = args.getStringArray("BOOKABLE_DATES"); if (dateStrings == null) return slots; DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"); for (String dateStr : dateStrings) { LocalDate date = LocalDate.parse(dateStr); LocalDateTime startTime = date.atTime(startHour, 0); LocalDateTime endTime = date.atTime(endHour, 0); for (int courtId : allInvolvedIds) { slots.add(new BookingSlotRequest(courtId, startTime.format(formatter), endTime.format(formatter))); } } Log.d(TAG, "Built " + slots.size() + " slots monthly..."); return slots;
    }

    /**
     * (Helper) Gọi API kiểm tra xem các slot có bị trùng không (cho Booking Tháng).
     */
    private void checkAvailabilityAndProceedMonthly(List<BookingSlotRequest> slotsToCheck, Bundle args) {
        Log.d(TAG, "Checking availability monthly...");
        bookingRepository.checkSlotsAvailability(slotsToCheck).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) { if (response.isSuccessful()) { Log.i(TAG, "Monthly slots available..."); proceedToCreateMonthlyBooking(args); } else { _isLoading.setValue(false); String errorMsg="Slots not available monthly..."; try {if(response.errorBody()!=null) errorMsg += response.errorBody().string();} catch(Exception e){} _error.postValue(errorMsg); Log.w(TAG,"Monthly slots not available");} }
            @Override public void onFailure(Call<Void> call, Throwable t) { _isLoading.setValue(false); _error.postValue("Net error check monthly..."); Log.e(TAG,"Net error check monthly",t);}
        });
    }

    /**
     * (Helper) Tạo DTO và gọi API tạo Booking Tháng (status "waiting").
     * Trả về BookingReadDto (chứa ID) qua LiveData `_bookingResult` để mở VNPay.
     */
    private void proceedToCreateMonthlyBooking(Bundle args) {
        Log.d(TAG, "Proceeding create monthly DTO...");
        int stadiumId = args.getInt("stadiumId", 0);
        float finalTotalPrice = args.getFloat("FINAL_PRICE", 0f);
        float originalTotalPrice = args.getFloat("ORIGINAL_PRICE", 0f);
        if (originalTotalPrice == 0f) { originalTotalPrice = finalTotalPrice; }
        Integer discountId = null;
        if (args.containsKey("DISCOUNT_ID")) {
            discountId = args.getInt("DISCOUNT_ID");
            Log.d(TAG, "Applying Discount ID: " + discountId);
        }
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
                stadiumId, originalTotalPrice, finalTotalPrice, "vnpay_100",
                startTimeStr, endTimeStr, month, year, dayList, courtIdList);
        if (discountId != null) {
            dto.setDiscountId(discountId);
        }
        Log.d(TAG, "Monthly DTO: " + dto.toString());

        bookingRepository.createMonthlyBooking(dto).enqueue(new Callback<BookingReadDto>() {
            @Override
            public void onResponse(Call<BookingReadDto> call, Response<BookingReadDto> response) {
                _isLoading.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    _bookingResult.postValue(response.body());
                    Log.i(TAG, "✅ createMonthlyBooking SUCCESS - ID: " + response.body().getId());
                } else {
                    String errorMsg = "Failed create monthly: " + response.code();
                    try {
                        if (response.errorBody() != null)
                            errorMsg += " - " + response.errorBody().string();
                    } catch (Exception e) {}
                    _error.postValue(errorMsg);
                    Log.e(TAG, "❌ createMonthlyBooking FAILED: " + errorMsg);
                }
            }
            @Override
            public void onFailure(Call<BookingReadDto> call, Throwable t) {
                _isLoading.postValue(false);
                _error.postValue("Net error create monthly: " + t.getMessage());
                Log.e(TAG, "❌ createMonthlyBooking NETWORK ERROR", t);
            }
        });
    }

    interface OnInvolvedCourtsReady { void onReady(List<Integer> allInvolvedIds); }

    /**
     * Được gọi bởi Fragment sau khi đã navigate đi (để reset cờ).
     */
    public void onMonthlyBookingNavigated() {
        _bookingSuccess.postValue(false);
        _bookingResult.postValue(null);
        Log.d(TAG, "Booking result/success flags reset.");
    }

    /**
     * Hàm tổng, bắt đầu luồng tạo Booking Ngày (từ CheckoutTimeZoneFragment).
     */
    public void createDailyBooking(Bundle args) {
        if (args == null) { _error.postValue("Lỗi: Không có dữ liệu đặt sân."); return; }
        _isLoadingBooking.setValue(true); _bookingResult.setValue(null); _error.setValue(null);
        Log.i(TAG, "Starting daily booking..."); int[] selectedCourtIds = args.getIntArray("courtIds");
        if (selectedCourtIds == null || selectedCourtIds.length == 0) { _error.postValue("Lỗi: Không có sân nào được chọn."); _isLoadingBooking.postValue(false); return; }
        getAllInvolvedCourts(Arrays.stream(selectedCourtIds).boxed().collect(Collectors.toList()), allInvolvedIds -> { List<BookingSlotRequest> slotsToCheck = buildSlotRequestsForDaily(args, allInvolvedIds); if (slotsToCheck.isEmpty()) { _error.postValue("Dữ liệu ngày giờ không hợp lệ."); _isLoadingBooking.postValue(false); return; } checkAvailabilityAndProceedDaily(slotsToCheck, args); });
    }

    /**
     * (Helper) Gọi API kiểm tra xem các slot có bị trùng không (cho Booking Ngày).
     */
    private void checkAvailabilityAndProceedDaily(List<BookingSlotRequest> slotsToCheck, Bundle args) {
        Log.d(TAG, "Checking availability daily...");
        bookingRepository.checkSlotsAvailability(slotsToCheck).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) { if (response.isSuccessful()) { Log.i(TAG, "Daily slots available..."); proceedToCreateDailyBooking(args); } else { _isLoadingBooking.postValue(false); String errorMsg="Slots not available daily..."; try {if(response.errorBody()!=null) errorMsg += response.errorBody().string();} catch(Exception e){} _error.postValue(errorMsg); Log.w(TAG,"Daily slots not available");} }
            @Override public void onFailure(Call<Void> call, Throwable t) { _isLoadingBooking.postValue(false); _error.postValue("Net error check daily..."); Log.e(TAG,"Net error check daily",t);}
        });
    }

    /**
     * (Helper) Tạo DTO và gọi API tạo Booking Ngày (status "waiting").
     * Hàm này gọi hàm `createBooking` chung.
     */
    private void proceedToCreateDailyBooking(Bundle args) {
        Log.d(TAG, "Proceeding create daily DTO..."); PrivateUserProfileDTO currentUser = _userProfile.getValue(); if (currentUser == null || currentUser.getUserId() == null) { _error.postValue("Lỗi user null."); _isLoadingBooking.postValue(false); Log.e(TAG, "User null."); return; } int userId; try { userId = Integer.parseInt(currentUser.getUserId()); } catch (NumberFormatException e){ _error.postValue("Lỗi User ID."); _isLoadingBooking.postValue(false); Log.e(TAG, "Invalid userId format."); return; }
        int stadiumId = args.getInt("stadiumId");
        double finalTotalPrice = (double) args.getFloat("FINAL_PRICE", 0f);
        double originalTotalPrice = (double) args.getFloat("ORIGINAL_PRICE", 0f);
        if (originalTotalPrice == 0f) { originalTotalPrice = finalTotalPrice; }
        Integer discountId = null;
        if (args.containsKey("DISCOUNT_ID")) {
            discountId = args.getInt("DISCOUNT_ID");
        }
        String dateString = args.getString("date"); int startHour = args.getInt("startTime"); int endHour = args.getInt("endTime"); int[] courtIds = args.getIntArray("courtIds"); String startTimeIso; String endTimeIso; try { DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"); LocalDate date = LocalDate.parse(dateString); startTimeIso = date.atTime(startHour, 0).format(outputFormatter); endTimeIso = date.atTime(endHour, 0).format(outputFormatter); } catch (Exception e) { _error.postValue("Lỗi format date/time."); _isLoadingBooking.postValue(false); Log.e(TAG, "Error formatting date/time", e); return; }
        List<BookingDetailCreateDto> details = new ArrayList<>(); if (courtIds != null) { for (int courtId : courtIds) { details.add(new BookingDetailCreateDto(courtId, startTimeIso, endTimeIso)); } } else { _error.postValue("Lỗi courtIds null."); _isLoadingBooking.postValue(false); Log.e(TAG, "courtIds null."); return; }
        BookingCreateDto bookingDto = new BookingCreateDto(userId, "waiting", startTimeIso,
                finalTotalPrice, originalTotalPrice, "vnpay_100", discountId, stadiumId, details);
        Log.d(TAG, "Calling common createBooking for daily.");
        createBooking(bookingDto);
    }

    /**
     * (Helper) Tạo danh sách các slot (CourtId, StartTime, EndTime) cho Booking Ngày.
     */
    private List<BookingSlotRequest> buildSlotRequestsForDaily(Bundle args, List<Integer> allInvolvedIds) {
        List<BookingSlotRequest> slots = new ArrayList<>(); String dateString = args.getString("date"); int startHour = args.getInt("startTime"); int endHour = args.getInt("endTime"); if (dateString == null) return slots; DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"); LocalDate date = LocalDate.parse(dateString); LocalDateTime startTime = date.atTime(startHour, 0); LocalDateTime endTime = date.atTime(endHour, 0); String startTimeStr = startTime.format(formatter); String endTimeStr = endTime.format(formatter); for (int courtId : allInvolvedIds) { slots.add(new BookingSlotRequest(courtId, startTimeStr, endTimeStr)); } Log.d(TAG, "Built " + slots.size() + " slots daily..."); return slots;
    }

    /**
     * Kiểm tra xem người dùng đã đăng nhập chưa.
     */
    public boolean isLoggedIn() {
        int currentUserId = sharedPreferences.getInt("user_id", -1);
        return currentUserId != -1;
    }

    /**
     * Xóa kết quả booking (sau khi đã mở VNPay) để tránh trigger lại.
     */
    public void clearBookingResult() {
        _bookingResult.setValue(null);
        Log.d(TAG,"Booking result cleared.");
    }

    // === START VNPay Update Status Logic ===

    /**
     * Hàm chính, được gọi từ MainActivity để xử lý callback VNPay.
     * @param entityId ID của Booking (1057) hoặc MonthlyBooking (99)
     * @param responseCode Mã trả về từ VNPay ("00" là thành công)
     * @param type Loại booking ("Booking" hoặc "MonthlyBooking")
     */
    public void updatePaymentStatus(int entityId, String responseCode, String type) {
        Log.i(TAG, "Nhận được callback VNPay cho: Type=" + type + ", ID=" + entityId + ", Code=" + responseCode);
        String newStatus = "00".equals(responseCode) ? "accepted" : "payfail";

        if ("MonthlyBooking".equalsIgnoreCase(type)) {
            updateMonthlyBookingStatus(entityId, newStatus);
        } else {
            updateSingleBookingStatus(entityId, newStatus);
        }
    }

    /**
     * (Helper) Xử lý logic cập nhật cho Booking Ngày (Single Booking).
     */
    private void updateSingleBookingStatus(int bookingId, String newStatus) {
        Log.d(TAG, "Bắt đầu cập nhật status cho SINGLE Booking ID: " + bookingId + " -> " + newStatus);

        // 1. Lấy thông tin booking cũ
        bookingRepository.getBookingByODataFilter(bookingId).enqueue(new Callback<BookingHistoryODataResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookingHistoryODataResponse> call, @NonNull Response<BookingHistoryODataResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getValue() != null && !response.body().getValue().isEmpty()) {
                    BookingViewDTO oldBooking = response.body().getValue().get(0);

                    // 2. Tạo DTO Update
                    BookingUpdateDTO updateDTO = new BookingUpdateDTO(
                            oldBooking.getUserId(), newStatus, formatDateToString(oldBooking.getDate()),
                            oldBooking.getTotalPrice(), oldBooking.getOriginalPrice(), oldBooking.getNote(),
                            oldBooking.getDiscountId(), oldBooking.getStadiumId()
                    );

                    // 3. Gọi API Update (và xử lý các logic phụ)
                    callUpdateApi(bookingId, updateDTO, oldBooking, newStatus);

                } else {
                    String errorMsg = "Lỗi lấy chi tiết Booking " + bookingId + " (OData response null/empty hoặc value rỗng).";
                    Log.e(TAG, errorMsg);
                    _error.postValue(errorMsg);
                }
            }
            @Override
            public void onFailure(@NonNull Call<BookingHistoryODataResponse> call, @NonNull Throwable t) {
                String errorMsg = "Lỗi mạng khi lấy Booking " + bookingId + " (OData): " + t.getMessage();
                Log.e(TAG, errorMsg);
                _error.postValue(errorMsg);
            }
        });
    }

    /**
     * (Helper) Xử lý logic cập nhật cho Booking Tháng (Monthly Booking).
     */
    private void updateMonthlyBookingStatus(int monthlyBookingId, String newStatus) {
        Log.d(TAG, "Bắt đầu cập nhật status cho MONTHLY Booking ID: " + monthlyBookingId + " -> " + newStatus);

        // 1. Lấy thông tin gói tháng cũ
        bookingRepository.getMonthlyBookingById(monthlyBookingId).enqueue(new Callback<MonthlyBookingODataResponse>() {
            @Override
            public void onResponse(@NonNull Call<MonthlyBookingODataResponse> call, @NonNull Response<MonthlyBookingODataResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getValue() != null && !response.body().getValue().isEmpty()) {
                    MonthlyBookingReadDTO oldMonthly = response.body().getValue().get(0);

                    // 2. Tạo DTO Update
                    MonthlyBookingUpdateDTO updateDTO = new MonthlyBookingUpdateDTO(
                            newStatus, oldMonthly.getTotalPrice(), oldMonthly.getPaymentMethod(),
                            oldMonthly.getOriginalPrice(), oldMonthly.getNote()
                    );

                    // 3. Gọi API Update (và xử lý các logic phụ)
                    callUpdateMonthlyApi(monthlyBookingId, updateDTO, oldMonthly, newStatus);

                } else {
                    String errorMsg = "❌ Lỗi GET MonthlyBooking (ID: " + monthlyBookingId + "): " + response.code() + " (hoặc response rỗng)";
                    Log.e(TAG, errorMsg);
                    _error.postValue("Lỗi lấy thông tin MonthlyBooking: " + response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call<MonthlyBookingODataResponse> call, @NonNull Throwable t) {
                String errorMsg = "❌ Lỗi mạng GET MonthlyBooking: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                _error.postValue("Lỗi mạng lấy MonthlyBooking: " + t.getMessage());
            }
        });
    }


    // --- HÀM HELPER (CHO LOGIC UPDATE) ---

    /**
     * (Helper) Chuyển java.util.Date sang String ISO 8601.
     */
    private String formatDateToString(Date date) {
        if (date == null) return null;
        java.time.LocalDateTime localDateTime = java.time.LocalDateTime.ofInstant(date.toInstant(), java.time.ZoneId.systemDefault());
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        return localDateTime.format(formatter);
    }

    /**
     * (Helper) Gọi API update cho Booking Ngày và chạy các logic phụ (Discount, Noti).
     */
    private void callUpdateApi(int bookingId, BookingUpdateDTO updateDTO, BookingViewDTO oldBooking, String newStatus) {
        bookingRepository.updateBooking(bookingId, updateDTO).enqueue(new Callback<BookingReadDto>() {
            @Override
            public void onResponse(@NonNull Call<BookingReadDto> call, @NonNull Response<BookingReadDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.i(TAG, "✅ Cập nhật status thành công: ID " + bookingId + " -> " + response.body().getStatus());

                    if (newStatus.equals("accepted")) {
                        // 1. Vô hiệu hóa Discount nếu có
                        if (oldBooking.getDiscountId() != null && oldBooking.getDiscountId() > 0) {
                            getAndDisableDiscount(oldBooking.getDiscountId());
                        }

                        // 2. Lấy chủ sân và tạo Notification cho chủ sân (Booking.New)
                        fetchStadiumOwnerAndNotify(
                                oldBooking.getStadiumId(),
                                "Booking", // bookingType
                                oldBooking.getId()
                        );
                    }

                } else {
                    String errorMsg = "Lỗi khi PUT update status: " + response.code();
                    try {
                        if(response.errorBody() != null) errorMsg += " - " + response.errorBody().string();
                    } catch (Exception e) { Log.e(TAG, "Lỗi đọc errorBody", e); }
                    Log.e(TAG, errorMsg);
                    _error.postValue(errorMsg);
                }
            }
            @Override
            public void onFailure(@NonNull Call<BookingReadDto> call, @NonNull Throwable t) {
                String errorMsg = "Lỗi mạng khi PUT update status: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                _error.postValue(errorMsg);
            }
        });
    }

    /**
     * (Helper) Gọi API update cho Booking Tháng và chạy các logic phụ (Discount, Noti, Booking con).
     */
    private void callUpdateMonthlyApi(int monthlyBookingId, MonthlyBookingUpdateDTO updateDTO, MonthlyBookingReadDTO oldMonthly, String newStatus) {
        bookingRepository.updateMonthlyBooking(monthlyBookingId, updateDTO).enqueue(new Callback<MonthlyBookingReadDTO>() {
            @Override
            public void onResponse(@NonNull Call<MonthlyBookingReadDTO> call, @NonNull Response<MonthlyBookingReadDTO> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "✅ Cập nhật status MONTHLY thành công: ID " + monthlyBookingId);

                    if (newStatus.equals("accepted")) {
                        // 1. Vô hiệu hóa Discount nếu có
                        if (oldMonthly.getDiscountId() != null && oldMonthly.getDiscountId() > 0) {
                            getAndDisableDiscount(oldMonthly.getDiscountId());
                        }

                        // 2. Lấy chủ sân và tạo Notification cho chủ sân
                        fetchStadiumOwnerAndNotify(
                                oldMonthly.getStadiumId(),
                                "MonthlyBooking", // bookingType
                                oldMonthly.getId()
                        );

                        // 3. Cập nhật tất cả booking con
                        updateChildBookingStatuses(monthlyBookingId);
                    }

                } else {
                    String errorMsg = "❌ Lỗi khi PUT update status MONTHLY: " + response.code();
                    try {
                        if(response.errorBody() != null) errorMsg += " - " + response.errorBody().string();
                    } catch (Exception e) { Log.e(TAG, "Lỗi đọc errorBody", e); }
                    Log.e(TAG, errorMsg);
                    _error.postValue("Lỗi update MonthlyBooking: " + errorMsg);
                }
            }
            @Override
            public void onFailure(@NonNull Call<MonthlyBookingReadDTO> call, @NonNull Throwable t) {
                String errorMsg = "❌ Lỗi mạng khi PUT update MONTHLY: " + t.getMessage();
                Log.e(TAG, errorMsg, t);
                _error.postValue("Lỗi mạng update MonthlyBooking: " + t.getMessage());
            }
        });
    }

    // === CÁC HÀM HELPER SỬA BUG ===

    /**
     * (Helper - Sửa Bug #1) Lấy discount về, rồi mới update
     * Vì API Update Discount (PUT /discounts) yêu cầu DTO đầy đủ.
     */
    private void getAndDisableDiscount(int discountId) {
        Log.d(TAG, "Đang lấy chi tiết Discount ID: " + discountId + " để vô hiệu hóa...");

        discountRepository.getDiscountById(discountId).enqueue(new Callback<ReadDiscountDTO>() {
            @Override
            public void onResponse(@NonNull Call<ReadDiscountDTO> call, @NonNull Response<ReadDiscountDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ReadDiscountDTO discountToUpdate = response.body();

                    String codeType = discountToUpdate.getCodeType();
                    // Thêm .trim() để tránh lỗi "Unique "
                    if (codeType != null && "Unique".equalsIgnoreCase(codeType.trim())) {
                        Log.d(TAG, "Discount là 'Unique'. Bắt đầu vô hiệu hóa...");

                        // Tạo DTO đầy đủ (theo C#) và set isActive = false
                        UpdateDiscountDTO fullUpdateDto = new UpdateDiscountDTO(discountToUpdate, false);

                        // Gọi API update (đã sửa trong repo là PUT /discounts)
                        discountRepository.updateDiscount(fullUpdateDto).enqueue(new Callback<ReadDiscountDTO>() {
                            @Override
                            public void onResponse(@NonNull Call<ReadDiscountDTO> call, @NonNull Response<ReadDiscountDTO> response) {
                                if (response.isSuccessful()) {
                                    Log.i(TAG, "✅ Vô hiệu hóa Discount ID " + discountId + " thành công.");
                                } else {
                                    Log.w(TAG, "❌ Vô hiệu hóa Discount ID " + discountId + " thất bại (lỗi PUT): " + response.code());
                                }
                            }
                            @Override
                            public void onFailure(@NonNull Call<ReadDiscountDTO> call, @NonNull Throwable t) {
                                Log.e(TAG, "❌ Lỗi mạng khi PUT vô hiệu hóa Discount: " + t.getMessage(), t);
                            }
                        });

                    } else {
                        Log.d(TAG, "Discount (ID: " + discountId + ") không phải là 'Unique' (CodeType from DTO: '" + codeType + "'), không cần vô hiệu hóa.");
                    }
                } else {
                    Log.w(TAG, "❌ Không tìm thấy Discount ID " + discountId + " (lỗi GET): " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReadDiscountDTO> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Lỗi mạng khi GET Discount ID " + discountId + ": " + t.getMessage(), t);
            }
        });
    }

    /**
     * (Helper - Sửa Bug #2) Hàm chung để tạo thông báo
     */
    private void createNotification(int userId, String type, String title, String message, String paramsJson) {
        Log.d(TAG, "Tạo notification cho User ID: " + userId + ", Type: " + type);
        CreateNotificationDTO notificationDTO = new CreateNotificationDTO(userId, type, title, message, paramsJson);

        notificationRepository.createNotification(notificationDTO).enqueue(new Callback<NotificationDTO>() {
            @Override
            public void onResponse(@NonNull Call<NotificationDTO> call, @NonNull Response<NotificationDTO> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "✅ Tạo notification cho User ID " + userId + " thành công.");
                } else {
                    Log.w(TAG, "❌ Tạo notification cho User ID " + userId + " thất bại: " + response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call<NotificationDTO> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Lỗi mạng khi tạo notification: " + t.getMessage(), t);
            }
        });
    }

    /**
     * (Helper - Sửa Bug #2) Lấy ID chủ sân rồi gửi thông báo (đúng format)
     */
    private void fetchStadiumOwnerAndNotify(int stadiumId, String bookingType, int bookingId) {
        Log.d(TAG, "Đang lấy chủ sân của Stadium ID: " + stadiumId + " để gửi thông báo...");
        stadiumRepository.getStadiumByListId(String.valueOf(stadiumId)).enqueue(new Callback<ODataResponse<StadiumDTO>>() {
            @Override
            public void onResponse(@NonNull Call<ODataResponse<StadiumDTO>> call, @NonNull Response<ODataResponse<StadiumDTO>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getItems().isEmpty()) {
                    StadiumDTO stadium = response.body().getItems().get(0);
                    int ownerId = stadium.getCreatedBy();
                    Log.i(TAG, "Tìm thấy chủ sân. Owner ID: " + ownerId);

                    // Chuẩn bị thông tin theo format C#
                    String notifType, notifTitle, notifMessage;
                    Map<String, Object> notifParams = new HashMap<>();
                    notifParams.put("bookingType", bookingType.toLowerCase());

                    if ("MonthlyBooking".equalsIgnoreCase(bookingType)) {
                        notifType = "MonthlyBooking.New";
                        notifTitle = "Gói đặt sân tháng mới";
                        notifMessage = "Sân '" + stadium.getName() + "' của bạn vừa có một gói đặt sân theo tháng được thanh toán thành công.";
                        notifParams.put("monthlyBookingId", bookingId);
                    } else { // Mặc định là "Booking"
                        notifType = "Booking.New";
                        notifTitle = "Lịch đặt sân mới";
                        notifMessage = "Sân '" + stadium.getName() + "' của bạn vừa có một lịch đặt mới đã thanh toán thành công.";
                        notifParams.put("bookingId", bookingId);
                    }

                    String paramsJson = gson.toJson(notifParams);

                    // Gửi thông báo cho chủ sân
                    createNotification(ownerId, notifType, notifTitle, notifMessage, paramsJson);

                } else {
                    Log.w(TAG, "❌ Không tìm thấy stadium/chủ sân (ID: " + stadiumId + ") để gửi thông báo: " + response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call<ODataResponse<StadiumDTO>> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Lỗi mạng khi lấy chủ sân: " + t.getMessage(), t);
            }
        });
    }

    /**
     * (Helper - Sửa Bug #3) Cập nhật status của các booking con
     */
    private void updateChildBookingStatuses(int monthlyBookingId) {
        Log.d(TAG, "Bắt đầu cập nhật các booking con cho Monthly ID: " + monthlyBookingId);

        bookingRepository.getBookingsForMonthlyPlan(monthlyBookingId).enqueue(new Callback<BookingHistoryODataResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookingHistoryODataResponse> call, @NonNull Response<BookingHistoryODataResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                    List<BookingViewDTO> childBookings = response.body().getValue();
                    Log.i(TAG, "Tìm thấy " + childBookings.size() + " booking con để cập nhật.");

                    for (BookingViewDTO child : childBookings) {

                        // Dùng thông tin của CHÍNH booking con
                        BookingUpdateDTO childUpdateDTO = new BookingUpdateDTO(
                                child.getUserId(),
                                "accepted", // Set status "accepted"
                                formatDateToString(child.getDate()),
                                child.getTotalPrice(),
                                child.getOriginalPrice(),
                                child.getNote(),
                                child.getDiscountId(),
                                child.getStadiumId()
                        );

                        Log.d(TAG, "Đang cập nhật booking con ID: " + child.getId());
                        // Chỉ gọi API update, không gọi lại helper (để tránh lặp vô hạn)
                        bookingRepository.updateBooking(child.getId(), childUpdateDTO).enqueue(new Callback<BookingReadDto>() {
                            @Override
                            public void onResponse(@NonNull Call<BookingReadDto> call, @NonNull Response<BookingReadDto> response) {
                                if(response.isSuccessful()) {
                                    Log.i(TAG, "✅ Cập nhật booking con ID " + child.getId() + " thành công.");
                                } else {
                                    Log.w(TAG, "❌ Cập nhật booking con ID " + child.getId() + " thất bại: " + response.code());
                                }
                            }
                            @Override
                            public void onFailure(@NonNull Call<BookingReadDto> call, @NonNull Throwable t) {
                                Log.e(TAG, "❌ Lỗi mạng khi update booking con: " + t.getMessage());
                            }
                        });
                    }
                } else {
                    Log.w(TAG, "Không tìm thấy booking con nào hoặc lỗi khi GET: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookingHistoryODataResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "❌ Lỗi mạng khi lấy booking con: " + t.getMessage(), t);
            }
        });
    }
}