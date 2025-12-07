package com.example.sep490_mobile.ui.bookinghistory;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.ScheduleODataStadiumResponseDTO;
import com.example.sep490_mobile.data.dto.ScheduleStadiumDTO;
import com.example.sep490_mobile.data.dto.booking.response.BookingHistoryODataResponse;
import com.example.sep490_mobile.data.dto.booking.BookingViewDTO;
import com.example.sep490_mobile.data.dto.booking.DailyBookingDTO;
import com.example.sep490_mobile.data.dto.booking.MonthlyBookingDTO;
import com.example.sep490_mobile.data.dto.booking.response.MonthlyBookingODataResponse;
import com.example.sep490_mobile.data.dto.booking.MonthlyBookingReadDTO;
import com.example.sep490_mobile.data.repository.BookingRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet; // Import HashSet
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap; // Import ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger; // Import AtomicInteger
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingHistoryViewModel extends AndroidViewModel {

    // --- Hằng số ---
    private static final String TAG = "BookingHistoryVM_Log";
    private static final int PAGE_SIZE = 5; // Số item mỗi trang
    public enum BookingType { DAILY, MONTHLY }

    // --- Repositories và SharedPreferences ---
    private final BookingRepository repository;
    private final SharedPreferences sharedPreferences;

    // --- LiveData ---
    private final MutableLiveData<List<DailyBookingDTO>> dailyBookings = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<MonthlyBookingDTO>> monthlyBookings = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false); // Trạng thái loading chung
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLastPage = new MutableLiveData<>(false); // Hết trang cho tab hiện tại

    // --- Biến trạng thái phân trang ---
    private int currentDailyPage = 1;
    private int totalDailyCount = 0;
    private boolean isFetchingDaily = false;

    private int currentMonthlyPage = 1;
    private int totalMonthlyCount = 0;      // Tổng số gói tháng (summaries)
    private boolean isFetchingMonthly = false; // Đang tải tóm tắt gói tháng?
    // <<< THÊM: Cờ báo đang tải chi tiết bên trong gói tháng >>>
    private boolean isFetchingMonthlyDetails = false;

    // --- Biến tạm lưu dữ liệu mới fetch ---
    private List<DailyBookingDTO> lastFetchedDaily = null;
    private List<MonthlyBookingDTO> lastFetchedMonthly = null; // Sẽ chứa cả chi tiết


    public BookingHistoryViewModel(@NonNull Application application) {
        super(application);
        repository = new BookingRepository(application);
        sharedPreferences = application.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        Log.d(TAG, "ViewModel initialized");
    }

    // --- Getters ---
    public LiveData<List<DailyBookingDTO>> getDailyBookings() { return dailyBookings; }
    public LiveData<List<MonthlyBookingDTO>> getMonthlyBookings() { return monthlyBookings; }
    // isLoading giờ là true nếu đang tải tóm tắt HOẶC đang tải chi tiết
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLastPage() { return isLastPage; }
    public List<DailyBookingDTO> getLastFetchedDaily() { return lastFetchedDaily; }
    public List<MonthlyBookingDTO> getLastFetchedMonthly() { return lastFetchedMonthly; }


    // --- Phương thức công khai ---
    public void fetchInitialBookings(BookingType type) {
        Log.i(TAG, "fetchInitialBookings called for type: " + type);
        isLoading.setValue(true);
        errorMessage.setValue(null);
        isLastPage.setValue(false);
        lastFetchedDaily = null;
        lastFetchedMonthly = null;

        if (type == BookingType.DAILY) {
            currentDailyPage = 1;
            totalDailyCount = 0;
            isFetchingDaily = false;
            dailyBookings.setValue(new ArrayList<>());
            fetchDailyBookingsPage();
        } else { // MONTHLY
            currentMonthlyPage = 1;
            totalMonthlyCount = 0;
            isFetchingMonthly = false;
            isFetchingMonthlyDetails = false; // <<< Reset cờ tải chi tiết
            monthlyBookings.setValue(new ArrayList<>());
            fetchMonthlyBookingsPage(); // <<< Bắt đầu chuỗi tải cho Lịch Tháng
        }
    }

    public void fetchMoreBookings(BookingType type) {
        Log.i(TAG, "fetchMoreBookings called for type: " + type);
        errorMessage.setValue(null);
        lastFetchedDaily = null;
        lastFetchedMonthly = null;

        if (type == BookingType.DAILY) {
            boolean canLoadMore = !isFetchingDaily && (currentDailyPage * PAGE_SIZE < totalDailyCount);
            if (!canLoadMore) { Log.d(TAG, "fetchMoreBookings (Daily): Cannot load more."); return; }
            currentDailyPage++;
            fetchDailyBookingsPage();
        } else { // MONTHLY
            // <<< Kiểm tra cả hai cờ fetching của Lịch Tháng >>>
            boolean isCurrentlyFetchingMonthly = isFetchingMonthly || isFetchingMonthlyDetails;
            boolean canLoadMore = !isCurrentlyFetchingMonthly && (currentMonthlyPage * PAGE_SIZE < totalMonthlyCount);
            if (!canLoadMore) {
                Log.d(TAG, "fetchMoreBookings (Monthly): Cannot load more (fetching=" + isCurrentlyFetchingMonthly + ", page=" + currentMonthlyPage + ", total=" + totalMonthlyCount + ")");
                return;
            }
            currentMonthlyPage++;
            // Chỉ tải trang tóm tắt tiếp theo
            fetchMonthlyBookingsPage();
        }
    }

    // --- Tải Lịch Ngày (Giữ nguyên logic gốc) ---
    private void fetchDailyBookingsPage() {
        if (isFetchingDaily) return;
        isFetchingDaily = true;
        if (currentDailyPage == 1) isLoading.setValue(true);
        Log.i(TAG, "Fetching DAILY bookings page: " + currentDailyPage);
        int userId = sharedPreferences.getInt("user_id", -1);
        if (!isValidUserId(userId)) { isFetchingDaily = false; return; }

        repository.getDailyBookingsHistory(userId, currentDailyPage, PAGE_SIZE)
                .enqueue(new Callback<BookingHistoryODataResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<BookingHistoryODataResponse> call, @NonNull Response<BookingHistoryODataResponse> response) {
                        Log.d(TAG, "fetchDailyBookingsPage: onResponse - Code: " + response.code() + " page " + currentDailyPage);
                        if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                            List<BookingViewDTO> fetchedData = response.body().getValue();
                            totalDailyCount = response.body().getCount();
                            Log.i(TAG, "fetchDailyBookingsPage: Success! Fetched " + fetchedData.size() + ". Total: " + totalDailyCount);
                            boolean isLast = (currentDailyPage * PAGE_SIZE >= totalDailyCount);
                            isLastPage.postValue(isLast);
                            processStadiumAndCourtNamesForDaily(fetchedData); // Xử lý tên
                        } else {
                            handleApiError("Lỗi tải lịch đặt ngày", response.code(), BookingType.DAILY);
                            isLastPage.postValue(true);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<BookingHistoryODataResponse> call, @NonNull Throwable t) {
                        handleNetworkError("Lỗi mạng khi tải lịch đặt ngày", t, BookingType.DAILY);
                        isLastPage.postValue(true);
                    }
                });
    }


    // --- Tải Lịch Tháng (Logic mới phức tạp hơn) ---

    // Bước 1: Tải danh sách tóm tắt các gói tháng
    private void fetchMonthlyBookingsPage() {
        if (isFetchingMonthly || isFetchingMonthlyDetails) { // <<< Kiểm tra cả 2 cờ
            Log.d(TAG, "fetchMonthlyBookingsPage: Already fetching summaries or details. Returning.");
            return;
        }
        isFetchingMonthly = true; // <<< Đánh dấu đang tải tóm tắt
        if (currentMonthlyPage == 1) isLoading.setValue(true); // Chỉ bật loading chính khi tải trang đầu
        Log.i(TAG, "Fetching MONTHLY summaries page: " + currentMonthlyPage);
        int userId = sharedPreferences.getInt("user_id", -1);
        if (!isValidUserId(userId)) { isFetchingMonthly = false; return; }

        repository.getMonthlyBookingsHistory(userId, currentMonthlyPage, PAGE_SIZE)
                .enqueue(new Callback<MonthlyBookingODataResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MonthlyBookingODataResponse> call, @NonNull Response<MonthlyBookingODataResponse> response) {
                        isFetchingMonthly = false; // <<< Tải tóm tắt xong (dù thành công hay lỗi)
                        Log.d(TAG, "fetchMonthlyBookingsPage: onResponse - Code: " + response.code() + " page " + currentMonthlyPage);
                        if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                            List<MonthlyBookingReadDTO> fetchedSummaries = response.body().getValue();
                            totalMonthlyCount = response.body().getCount();
                            Log.i(TAG, "fetchMonthlyBookingsPage: Success! Fetched " + fetchedSummaries.size() + " summaries. Total: " + totalMonthlyCount);

                            boolean isLastBasedOnSummaries = (currentMonthlyPage * PAGE_SIZE >= totalMonthlyCount);
                            isLastPage.postValue(isLastBasedOnSummaries); // Cập nhật dựa trên tổng số tóm tắt
                            Log.i(TAG, "fetchMonthlyBookingsPage: Is last page (summaries)? " + isLastBasedOnSummaries);

                            if (fetchedSummaries.isEmpty()) {
                                // Nếu không có tóm tắt nào trả về (dù chưa phải trang cuối theo count)
                                finalizeMonthlyUpdate(new ArrayList<>()); // Gọi finalize với list rỗng để tắt loading
                            } else {
                                // <<< CHUYỂN SANG BƯỚC 2: Tải chi tiết cho các tóm tắt này >>>
                                fetchDetailsForMonthlyBookings(fetchedSummaries);
                            }
                        } else {
                            handleApiError("Lỗi tải lịch đặt tháng", response.code(), BookingType.MONTHLY);
                            isLastPage.postValue(true);
                            isLoading.postValue(false); // <<< Tắt loading nếu lỗi ngay bước 1
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<MonthlyBookingODataResponse> call, @NonNull Throwable t) {
                        isFetchingMonthly = false; // <<< Tải tóm tắt xong (lỗi mạng)
                        handleNetworkError("Lỗi mạng khi tải lịch đặt tháng", t, BookingType.MONTHLY);
                        isLastPage.postValue(true);
                        // isLoading sẽ tắt trong handleNetworkError
                    }
                });
    }

    // Bước 2: Tải chi tiết (các BookingReadDTO) cho từng gói tháng đã lấy được
    private void fetchDetailsForMonthlyBookings(List<MonthlyBookingReadDTO> monthlySummaries) {
        if (isFetchingMonthlyDetails) { // Tránh gọi lại nếu đang thực hiện
            Log.w(TAG, "fetchDetailsForMonthlyBookings: Already fetching details. Skipping.");
            return;
        }
        isFetchingMonthlyDetails = true; // <<< Đánh dấu đang tải chi tiết
        // Không cần bật isLoading nữa vì nó đã bật từ fetchMonthlyBookingsPage (nếu là trang 1)
        // Hoặc không cần bật nếu là load more
        Log.i(TAG, "Fetching details for " + monthlySummaries.size() + " monthly summaries...");

        // Dùng Map để lưu kết quả: ID gói tháng -> List các BookingReadDTO chi tiết
        ConcurrentHashMap<Integer, List<BookingViewDTO>> detailsMap = new ConcurrentHashMap<>();
        // Đếm số lượng cuộc gọi API chi tiết cần hoàn thành
        AtomicInteger pendingCalls = new AtomicInteger(monthlySummaries.size());
        // Tập hợp tất cả ID sân cần lấy tên (từ tóm tắt + chi tiết)
        Set<Integer> allStadiumIds = new HashSet<>();
        monthlySummaries.forEach(summary -> allStadiumIds.add(summary.getStadiumId()));

        // Lặp qua từng tóm tắt để gọi API lấy chi tiết
        for (MonthlyBookingReadDTO summary : monthlySummaries) {
            repository.getBookingsForMonthlyPlan(summary.getId()) // Gọi hàm mới trong Repository
                    .enqueue(new Callback<BookingHistoryODataResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<BookingHistoryODataResponse> call, @NonNull Response<BookingHistoryODataResponse> response) {
                            List<BookingViewDTO> bookingsForThisSummary = new ArrayList<>(); // List chi tiết cho gói này
                            if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                                bookingsForThisSummary = response.body().getValue();
                                Log.d(TAG, "fetchDetails: Success for Monthly ID " + summary.getId() + ", found " + bookingsForThisSummary.size() + " bookings.");
                                // Thu thập thêm ID sân từ các booking chi tiết này
                                bookingsForThisSummary.forEach(b -> allStadiumIds.add(b.getStadiumId()));
                            } else {
                                Log.e(TAG, "fetchDetails: Failed for Monthly ID " + summary.getId() + ". Code: " + response.code());
                                // Vẫn thêm list rỗng vào map để đánh dấu là đã xử lý
                            }
                            detailsMap.put(summary.getId(), bookingsForThisSummary); // Lưu kết quả (hoặc list rỗng)

                            // Kiểm tra xem tất cả các cuộc gọi chi tiết đã xong chưa
                            if (pendingCalls.decrementAndGet() == 0) {
                                Log.i(TAG, "fetchDetails: All detail calls finished.");
                                isFetchingMonthlyDetails = false; // <<< Tải chi tiết xong
                                // <<< CHUYỂN SANG BƯỚC 3: Xử lý tên sân >>>
                                processStadiumNamesForMonthly(monthlySummaries, detailsMap, allStadiumIds);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<BookingHistoryODataResponse> call, @NonNull Throwable t) {
                            Log.e(TAG, "fetchDetails: Network failure for Monthly ID " + summary.getId(), t);
                            detailsMap.put(summary.getId(), new ArrayList<>()); // Lưu list rỗng khi lỗi mạng
                            errorMessage.postValue("Lỗi mạng khi tải chi tiết gói tháng."); // Có thể hiển thị lỗi chung

                            // Kiểm tra xem tất cả đã xong chưa (kể cả lỗi)
                            if (pendingCalls.decrementAndGet() == 0) {
                                Log.i(TAG, "fetchDetails: All detail calls finished (with errors).");
                                isFetchingMonthlyDetails = false; // <<< Tải chi tiết xong (dù lỗi)
                                // <<< CHUYỂN SANG BƯỚC 3: Vẫn xử lý tên sân với dữ liệu đã có >>>
                                processStadiumNamesForMonthly(monthlySummaries, detailsMap, allStadiumIds);
                            }
                        }
                    });
        }
    }

    // Bước 3: Lấy tên sân/sân con cho cả tóm tắt và chi tiết
    private void processStadiumNamesForMonthly(List<MonthlyBookingReadDTO> summaries,
                                               Map<Integer, List<BookingViewDTO>> detailsMap,
                                               Set<Integer> allStadiumIds) {
        Log.d(TAG, "processStadiumNamesForMonthly: Processing names for " + summaries.size() + " summaries. All Stadium IDs: " + allStadiumIds);
        // Nếu không có ID sân nào cần lấy tên
        if (allStadiumIds.isEmpty()) {
            // Gọi hàm tạo DTO cuối cùng với map tên rỗng
            finalizeMonthlyUpdate(createMonthlyDTOs(summaries, detailsMap, new HashMap<>(), new HashMap<>()));
            return;
        }

        // Gọi API lấy thông tin sân (cần $expand=Courts)
        Call<ScheduleODataStadiumResponseDTO> stadiumCall = repository.getStadiumsByIds(new ArrayList<>(allStadiumIds));
        if (stadiumCall == null) {
            Log.w(TAG, "processStadiumNamesForMonthly: No valid stadium IDs.");
            finalizeMonthlyUpdate(createMonthlyDTOs(summaries, detailsMap, new HashMap<>(), new HashMap<>()));
            return;
        }

        stadiumCall.enqueue(new Callback<ScheduleODataStadiumResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<ScheduleODataStadiumResponseDTO> call, @NonNull Response<ScheduleODataStadiumResponseDTO> response) {
                Log.d(TAG, "getStadiumsByIds (Monthly Details): onResponse - Code: " + response.code());
                Map<Integer, String> stadiumNameLookup = new HashMap<>();
                Map<Integer, String> courtNameLookup = new HashMap<>(); // <<< Cần map tên sân con

                if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                    for (ScheduleStadiumDTO stadium : response.body().getValue()) {
                        stadiumNameLookup.put(stadium.getId(), stadium.getName());
                        // <<< Lấy tên sân con >>>
                        if (stadium.getCourts() != null) {
                            stadium.getCourts().forEach(court -> courtNameLookup.put(court.getId(), court.getName()));
                        }
                    }
                    Log.d(TAG, "getStadiumsByIds (Monthly Details): Success! Stadium map size: " + stadiumNameLookup.size() + ", Court map size: " + courtNameLookup.size());
                } else {
                    Log.e(TAG, "getStadiumsByIds (Monthly Details): Error! Code: " + response.code());
                }
                // <<< CHUYỂN SANG BƯỚC 4: Tạo DTO cuối và finalize >>>
                finalizeMonthlyUpdate(createMonthlyDTOs(summaries, detailsMap, stadiumNameLookup, courtNameLookup));
            }
            @Override
            public void onFailure(@NonNull Call<ScheduleODataStadiumResponseDTO> call, @NonNull Throwable t) {
                Log.e(TAG, "getStadiumsByIds (Monthly Details): onFailure - Error: " + t.getMessage());
                // <<< CHUYỂN SANG BƯỚC 4: Vẫn finalize dù không lấy được tên >>>
                finalizeMonthlyUpdate(createMonthlyDTOs(summaries, detailsMap, new HashMap<>(), new HashMap<>()));
            }
        });
    }

    // --- Hàm trợ giúp: Tạo danh sách MonthlyBookingDTO cuối cùng ---
    private List<MonthlyBookingDTO> createMonthlyDTOs(List<MonthlyBookingReadDTO> summaries,
                                                      Map<Integer, List<BookingViewDTO>> detailsMap,
                                                      Map<Integer, String> stadiumNameLookup,
                                                      Map<Integer, String> courtNameLookup) {
        List<MonthlyBookingDTO> resultList = new ArrayList<>();
        // Duyệt qua danh sách tóm tắt gốc
        for (MonthlyBookingReadDTO summary : summaries) {
            // Lấy danh sách chi tiết tương ứng từ map (mặc định là list rỗng nếu không tìm thấy)
            List<BookingViewDTO> details = detailsMap.getOrDefault(summary.getId(), new ArrayList<>());

            // Gán tên sân cho tóm tắt
            summary.setStadiumName(stadiumNameLookup.getOrDefault(summary.getStadiumId(), "Sân ID:" + summary.getStadiumId()));

            // Gán tên sân và tên sân con cho từng booking chi tiết
            for (BookingViewDTO detail : details) {
                detail.setStadiumName(stadiumNameLookup.getOrDefault(detail.getStadiumId(), "Sân ID:" + detail.getStadiumId()));
                if (detail.getBookingDetails() != null) {
                    detail.getBookingDetails().forEach(bd ->
                            bd.setCourtName(courtNameLookup.getOrDefault(bd.getCourtId(), "Sân con ID:" + bd.getCourtId()))
                    );
                }
            }
            // Tạo đối tượng MonthlyBookingDTO hoàn chỉnh và thêm vào kết quả
            resultList.add(new MonthlyBookingDTO(summary, details));
        }
        return resultList;
    }


    // Bước 4: Hoàn tất cập nhật LiveData cho Lịch Tháng
    private void finalizeMonthlyUpdate(List<MonthlyBookingDTO> newMonthlyDTOs) { // <<< Nhận vào List<MonthlyBookingDTO>
        Log.i(TAG, "finalizeMonthlyUpdate: Adding " + newMonthlyDTOs.size() + " new processed monthly bookings.");

        // Lưu lại list mới (đã có chi tiết) vào biến tạm
        lastFetchedMonthly = new ArrayList<>(newMonthlyDTOs);

        // Lấy list hiện tại và nối thêm
        List<MonthlyBookingDTO> currentList = monthlyBookings.getValue();
        if (currentList == null) currentList = new ArrayList<>();
        currentList.addAll(lastFetchedMonthly);

        // Cập nhật LiveData
        monthlyBookings.postValue(currentList);
        Log.i(TAG, "finalizeMonthlyUpdate: Posted COMBINED Monthly bookings (with details). New total size: " + currentList.size());

        // <<< Tắt loading và reset CẢ HAI cờ fetching >>>
        isLoading.postValue(false);
        isFetchingMonthly = false;
        isFetchingMonthlyDetails = false;
    }


    // --- Xử lý tên cho Lịch Ngày (Giữ nguyên) ---
    private void processStadiumAndCourtNamesForDaily(List<BookingViewDTO> bookings) {
        Log.d(TAG, "processStadiumAndCourtNamesForDaily: Processing " + bookings.size() + " bookings.");
        if (bookings.isEmpty()) { finalizeDailyUpdate(new ArrayList<>(), new HashMap<>(), new HashMap<>()); return; }
        Set<Integer> stadiumIds = bookings.stream().map(BookingViewDTO::getStadiumId).collect(Collectors.toSet());
        Log.d(TAG, "processStadiumAndCourtNamesForDaily: Stadium IDs: " + stadiumIds);
        Call<ScheduleODataStadiumResponseDTO> stadiumCall = repository.getStadiumsByIds(new ArrayList<>(stadiumIds));
        if (stadiumCall == null) { finalizeDailyUpdate(bookings, new HashMap<>(), new HashMap<>()); return; }

        stadiumCall.enqueue(new Callback<ScheduleODataStadiumResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<ScheduleODataStadiumResponseDTO> call, @NonNull Response<ScheduleODataStadiumResponseDTO> response) {
                Map<Integer, String> stadiumLookup = new HashMap<>();
                Map<Integer, String> courtLookup = new HashMap<>();
                if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                    for (ScheduleStadiumDTO stadium : response.body().getValue()) {
                        stadiumLookup.put(stadium.getId(), stadium.getName());
                        if (stadium.getCourts() != null) {
                            stadium.getCourts().forEach(c -> courtLookup.put(c.getId(), c.getName()));
                        }
                    }
                }
                finalizeDailyUpdate(bookings, stadiumLookup, courtLookup);
            }
            @Override
            public void onFailure(@NonNull Call<ScheduleODataStadiumResponseDTO> call, @NonNull Throwable t) {
                finalizeDailyUpdate(bookings, new HashMap<>(), new HashMap<>());
            }
        });
    }

    // --- Finalize cho Lịch Ngày (Giữ nguyên) ---
    private void finalizeDailyUpdate(List<BookingViewDTO> newBookingsFromApi, Map<Integer, String> stadiumNameLookup, Map<Integer, String> courtNameLookup) {
        Log.i(TAG, "finalizeDailyUpdate: Processing " + newBookingsFromApi.size() + " new daily bookings.");
        List<DailyBookingDTO> newDailyList = new ArrayList<>();
        for (BookingViewDTO booking : newBookingsFromApi) {
            booking.setStadiumName(stadiumNameLookup.getOrDefault(booking.getStadiumId(), "Sân ID:" + booking.getStadiumId()));
            if (booking.getBookingDetails() != null) {
                booking.getBookingDetails().forEach(detail ->
                        detail.setCourtName(courtNameLookup.getOrDefault(detail.getCourtId(), "Sân con ID:" + detail.getCourtId()))
                );
            }
            newDailyList.add(new DailyBookingDTO(booking));
        }
        lastFetchedDaily = new ArrayList<>(newDailyList);
        List<DailyBookingDTO> currentList = dailyBookings.getValue();
        if (currentList == null) currentList = new ArrayList<>();
        currentList.addAll(lastFetchedDaily);
        dailyBookings.postValue(currentList);
        Log.i(TAG, "finalizeDailyUpdate: Posted COMBINED Daily. Total size: " + currentList.size());
        isLoading.postValue(false);
        isFetchingDaily = false;
    }


    // --- Hàm tiện ích ---
    private boolean isValidUserId(int userId) {
        if (userId <= 0) {
            handleError("User ID không hợp lệ: " + userId, null);
            return false;
        } return true;
    }
    // <<< SỬA: handleApiError reset cả 2 cờ monthly >>>
    private void handleApiError(String contextMessage, int code, BookingType type) {
        Log.e(TAG, contextMessage + " - Code: " + code);
        errorMessage.postValue(contextMessage + ": " + code);
        isLoading.postValue(false);
        if (type == BookingType.DAILY) isFetchingDaily = false;
        else { isFetchingMonthly = false; isFetchingMonthlyDetails = false; } // Reset cả 2
    }
    // <<< SỬA: handleNetworkError reset cả 2 cờ monthly >>>
    private void handleNetworkError(String contextMessage, Throwable t, BookingType type) {
        Log.e(TAG, contextMessage + " - Error: " + t.getMessage(), t);
        errorMessage.postValue(contextMessage + ": " + t.getMessage());
        isLoading.postValue(false);
        if (type == BookingType.DAILY) isFetchingDaily = false;
        else { isFetchingMonthly = false; isFetchingMonthlyDetails = false; } // Reset cả 2
    }
    // <<< SỬA: handleError reset cả 2 cờ monthly >>>
    private void handleError(String message, BookingType type) {
        Log.e(TAG, message);
        errorMessage.postValue(message);
        isLoading.postValue(false);
        if (type == BookingType.DAILY) isFetchingDaily = false;
        else if (type == BookingType.MONTHLY) { isFetchingMonthly = false; isFetchingMonthlyDetails = false; } // Reset cả 2
    }
}