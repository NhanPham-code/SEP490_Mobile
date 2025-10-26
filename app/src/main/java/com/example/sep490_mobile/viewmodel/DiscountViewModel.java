package com.example.sep490_mobile.viewmodel; // Đảm bảo đúng package name

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.sep490_mobile.data.dto.OdataHaveCountResponse;
import com.example.sep490_mobile.data.dto.discount.ReadDiscountDTO;
import com.example.sep490_mobile.data.dto.favorite.ReadFavoriteDTO;
import com.example.sep490_mobile.data.dto.ScheduleODataStadiumResponseDTO;
import com.example.sep490_mobile.data.dto.ScheduleStadiumDTO;


import com.example.sep490_mobile.data.repository.BookingRepository; // Cần để lấy tên sân
import com.example.sep490_mobile.data.repository.DiscountRepository;
import com.example.sep490_mobile.data.repository.FavoriteRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiscountViewModel extends AndroidViewModel {

    // --- Constants ---
    private static final String TAG = "DiscountViewModel_Log";
    private static final int PAGE_SIZE = 5;
    public enum DiscountType { PERSONAL, FAVORITE }

    // --- Repositories and SharedPreferences ---
    private final DiscountRepository discountRepository;
    private final FavoriteRepository favoriteRepository;
    private final BookingRepository bookingRepository; // Dùng để lấy tên sân
    private final SharedPreferences sharedPreferences;

    // --- LiveData ---
    private final MutableLiveData<List<ReadDiscountDTO>> personalDiscounts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ReadDiscountDTO>> favoriteStadiumDiscounts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLastPage = new MutableLiveData<>(false);

    // --- State Variables ---
    private int currentPersonalPage = 1;
    private int totalPersonalCount = 0;
    private boolean isFetchingPersonal = false;

    private int currentFavoritePage = 1;
    private int totalFavoriteCount = 0; // Count TRƯỚC khi lọc client-side
    private boolean isFetchingFavorite = false;

    private List<ReadDiscountDTO> lastFetchedPersonal = null;
    private List<ReadDiscountDTO> lastFetchedFavorite = null; // List đã lọc

    private Set<Integer> favoriteStadiumIds = null;
    private boolean isLoadingFavorites = false;


    public DiscountViewModel(@NonNull Application application) {
        super(application);
        discountRepository = new DiscountRepository(application);
        favoriteRepository = new FavoriteRepository(application);
        bookingRepository = new BookingRepository(application);
        sharedPreferences = application.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        Log.d(TAG, "ViewModel initialized");
        fetchFavoriteStadiumIds(); // Lấy ID sân yêu thích khi khởi tạo
    }

    // --- Getters ---
    public LiveData<List<ReadDiscountDTO>> getPersonalDiscounts() { return personalDiscounts; }
    public LiveData<List<ReadDiscountDTO>> getFavoriteStadiumDiscounts() { return favoriteStadiumDiscounts; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLastPage() { return isLastPage; }
    public List<ReadDiscountDTO> getLastFetchedPersonal() { return lastFetchedPersonal; }
    public List<ReadDiscountDTO> getLastFetchedFavorite() { return lastFetchedFavorite; }

    // <<< THÊM: Getters cho trang hiện tại >>>
    public int getCurrentPersonalPage() { return currentPersonalPage; }
    public int getCurrentFavoritePage() { return currentFavoritePage; }
    // <<< KẾT THÚC THÊM >>>

    // --- Fetching Favorite IDs ---
    private void fetchFavoriteStadiumIds() {
        if (isLoadingFavorites || favoriteStadiumIds != null) return;
        isLoadingFavorites = true;
        Log.i(TAG, "Fetching favorite stadium IDs...");
        errorMessage.postValue(null);

        favoriteRepository.getMyFavoriteStadiums().enqueue(new Callback<List<ReadFavoriteDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReadFavoriteDTO>> call, @NonNull Response<List<ReadFavoriteDTO>> response) {
                isLoadingFavorites = false;
                if (response.isSuccessful() && response.body() != null) {
                    List<ReadFavoriteDTO> favoriteList = response.body();
                    Log.d(TAG, "fetchFavoriteStadiumIds: API Success! Raw size: " + favoriteList.size());
                    favoriteList.forEach(f -> Log.d(TAG, "Favorite Entry - StadiumId: " + f.getStadiumId())); // Log ID gốc
                    favoriteStadiumIds = favoriteList.stream()
                            .map(ReadFavoriteDTO::getStadiumId)
                            .collect(Collectors.toSet());
                    Log.i(TAG, "fetchFavoriteStadiumIds: Success! Final Set: " + favoriteStadiumIds);
                } else {
                    Log.e(TAG, "fetchFavoriteStadiumIds: API Error! Code: " + response.code());
                    errorMessage.postValue("Lỗi tải DS sân yêu thích: " + response.code());
                    favoriteStadiumIds = new HashSet<>();
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<ReadFavoriteDTO>> call, @NonNull Throwable t) {
                isLoadingFavorites = false;
                Log.e(TAG, "fetchFavoriteStadiumIds: Network Failure! Error: " + t.getMessage(), t);
                errorMessage.postValue("Lỗi mạng khi tải sân yêu thích: " + t.getMessage());
                favoriteStadiumIds = new HashSet<>();
            }
        });
    }

    // --- Public Methods ---
    public void fetchInitialDiscounts(DiscountType type) {
        Log.i(TAG, "fetchInitialDiscounts called for type: " + type);
        isLoading.setValue(true); // Luôn bật loading khi tải lại từ đầu
        errorMessage.setValue(null);
        isLastPage.setValue(false);
        lastFetchedPersonal = null;
        lastFetchedFavorite = null;

        if (type == DiscountType.PERSONAL) {
            currentPersonalPage = 1; // <<< Reset trang về 1
            totalPersonalCount = 0;
            isFetchingPersonal = false;
            // Không xóa LiveData ở đây để tránh chớp
            // personalDiscounts.setValue(new ArrayList<>());
            fetchPersonalDiscountsPage();
        } else { // FAVORITE
            currentFavoritePage = 1; // <<< Reset trang về 1
            totalFavoriteCount = 0;
            isFetchingFavorite = false;
            // Không xóa LiveData ở đây
            // favoriteStadiumDiscounts.setValue(new ArrayList<>());

            if (favoriteStadiumIds == null) {
                Log.w(TAG, "fetchInitialDiscounts (Favorite): Favorite IDs not ready. Trying to fetch.");
                if (!isLoadingFavorites) fetchFavoriteStadiumIds();
                isLoading.postValue(false); // Tạm tắt loading chính
                errorMessage.postValue("Đang tải DS sân yêu thích...");
                isLastPage.postValue(true); // Tạm coi là hết trang
            } else if (favoriteStadiumIds.isEmpty()){
                Log.i(TAG, "fetchInitialDiscounts (Favorite): No favorite stadiums.");
                finalizeFavoriteUpdate(new ArrayList<>()); // Post list rỗng, tắt loading
            } else {
                fetchFavoriteStadiumDiscountsPage();
            }
        }
    }

    public void fetchMoreDiscounts(DiscountType type) {
        Log.i(TAG, "fetchMoreDiscounts called for type: " + type);
        errorMessage.setValue(null);
        lastFetchedPersonal = null;
        lastFetchedFavorite = null;

        if (type == DiscountType.PERSONAL) {
            boolean canLoadMore = !isFetchingPersonal && (currentPersonalPage * PAGE_SIZE < totalPersonalCount);
            if (!canLoadMore) { Log.d(TAG, "fetchMoreDiscounts (Personal): Cannot load more."); return; }
            currentPersonalPage++; // <<< Tăng trang
            fetchPersonalDiscountsPage();
        } else { // FAVORITE
            boolean canLoadMore = !isFetchingFavorite && (currentFavoritePage * PAGE_SIZE < totalFavoriteCount);
            if (!canLoadMore) { Log.d(TAG, "fetchMoreDiscounts (Favorite): Cannot load more based on pre-filter count."); return; }
            if (favoriteStadiumIds == null || favoriteStadiumIds.isEmpty()) {
                Log.w(TAG, "fetchMoreDiscounts (Favorite): Favorite IDs missing.");
                isLastPage.postValue(true); return;
            }
            currentFavoritePage++; // <<< Tăng trang
            fetchFavoriteStadiumDiscountsPage();
        }
    }

    // --- Private Fetching Logic ---
    private void fetchPersonalDiscountsPage() {
        if (isFetchingPersonal) return;
        isFetchingPersonal = true;
        // Chỉ bật isLoading chính nếu trang đầu tiên VÀ LiveData đang rỗng (tránh bật khi load more)
        if (currentPersonalPage == 1 && (personalDiscounts.getValue() == null || personalDiscounts.getValue().isEmpty())) {
            isLoading.setValue(true);
        }
        Log.i(TAG, "Fetching PERSONAL discounts page: " + currentPersonalPage);

        String userIdStr = String.valueOf(sharedPreferences.getInt("user_id", -1));
        if (userIdStr.equals("-1")) { handleError("User ID không hợp lệ", DiscountType.PERSONAL); isFetchingPersonal = false; return; }

        discountRepository.getDiscountsByType(userIdStr, "unique", currentPersonalPage, PAGE_SIZE)
                .enqueue(new Callback<OdataHaveCountResponse<ReadDiscountDTO>>() {
                    @Override
                    public void onResponse(@NonNull Call<OdataHaveCountResponse<ReadDiscountDTO>> call, @NonNull Response<OdataHaveCountResponse<ReadDiscountDTO>> response) {
                        Log.d(TAG, "fetchPersonalDiscountsPage: onResponse - Code: " + response.code() + " page " + currentPersonalPage);
                        if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                            List<ReadDiscountDTO> fetchedData = response.body().getValue();
                            totalPersonalCount = response.body().getCount();
                            Log.i(TAG, "fetchPersonalDiscountsPage: Success! Fetched " + fetchedData.size() + ". Total: " + totalPersonalCount);
                            boolean isLast = (currentPersonalPage * PAGE_SIZE >= totalPersonalCount);
                            isLastPage.postValue(isLast);
                            processStadiumNames(fetchedData, DiscountType.PERSONAL); // Gọi hàm xử lý tên sân chung
                        } else {
                            handleApiError("Lỗi tải mã cá nhân", response.code(), DiscountType.PERSONAL);
                            isLastPage.postValue(true);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<OdataHaveCountResponse<ReadDiscountDTO>> call, @NonNull Throwable t) {
                        handleNetworkError("Lỗi mạng khi tải mã cá nhân", t, DiscountType.PERSONAL);
                        isLastPage.postValue(true);
                    }
                });
    }

    private void fetchFavoriteStadiumDiscountsPage() {
        if (isFetchingFavorite) return;
        if (favoriteStadiumIds == null || favoriteStadiumIds.isEmpty()) { handleError("Chưa có sân yêu thích", DiscountType.FAVORITE); isFetchingFavorite = false; return; }
        isFetchingFavorite = true;
        // Chỉ bật isLoading chính nếu trang đầu tiên VÀ LiveData đang rỗng
        if (currentFavoritePage == 1 && (favoriteStadiumDiscounts.getValue() == null || favoriteStadiumDiscounts.getValue().isEmpty())) {
            isLoading.setValue(true);
        }
        Log.i(TAG, "Fetching FAVORITE STADIUM discounts page: " + currentFavoritePage);

        discountRepository.getDiscountsByType("0", "stadium", currentFavoritePage, PAGE_SIZE)
                .enqueue(new Callback<OdataHaveCountResponse<ReadDiscountDTO>>() {
                    @Override
                    public void onResponse(@NonNull Call<OdataHaveCountResponse<ReadDiscountDTO>> call, @NonNull Response<OdataHaveCountResponse<ReadDiscountDTO>> response) {
                        Log.d(TAG, "fetchFavoriteStadiumDiscountsPage: onResponse - Code: " + response.code() + " page " + currentFavoritePage);
                        if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                            List<ReadDiscountDTO> fetchedData = response.body().getValue();
                            totalFavoriteCount = response.body().getCount();
                            Log.i(TAG, "fetchFavoriteStadiumDiscountsPage: Success! Fetched " + fetchedData.size() + " (before filter). Total: " + totalFavoriteCount);

                            Log.d(TAG, "--- Checking fetched discounts before filtering ---");
                            fetchedData.forEach(d -> Log.d(TAG, "Code: " + d.getCode() + ", StadiumIds: " + d.getStadiumIds()));
                            Log.d(TAG, "--- Finished checking ---");

                            List<ReadDiscountDTO> filteredData = fetchedData.stream()
                                    .filter(d -> d.getStadiumIds() != null && !Collections.disjoint(d.getStadiumIds(), favoriteStadiumIds))
                                    .collect(Collectors.toList());
                            Log.i(TAG, "fetchFavoriteStadiumDiscountsPage: Filtered down to " + filteredData.size() + " applicable discounts.");

                            boolean isLastBasedOnTotal = (currentFavoritePage * PAGE_SIZE >= totalFavoriteCount);
                            isLastPage.postValue(isLastBasedOnTotal);
                            Log.i(TAG, "fetchFavoriteStadiumDiscountsPage: Is last page (pre-filter)? " + isLastBasedOnTotal);

                            processStadiumNames(filteredData, DiscountType.FAVORITE); // Gọi hàm xử lý tên sân chung
                        } else {
                            handleApiError("Lỗi tải mã sân", response.code(), DiscountType.FAVORITE);
                            isLastPage.postValue(true);
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<OdataHaveCountResponse<ReadDiscountDTO>> call, @NonNull Throwable t) {
                        handleNetworkError("Lỗi mạng khi tải mã sân", t, DiscountType.FAVORITE);
                        isLastPage.postValue(true);
                    }
                });
    }

    // --- Hàm xử lý tên sân (Chung cho cả 2 loại) ---
    private void processStadiumNames(List<ReadDiscountDTO> discounts, DiscountType type) {
        Log.d(TAG, "processStadiumNames: Processing " + discounts.size() + " discounts for type: " + type);
        if (discounts.isEmpty()) {
            if (type == DiscountType.PERSONAL) finalizePersonalUpdate(discounts);
            else finalizeFavoriteUpdate(discounts);
            return;
        }

        Set<Integer> stadiumIdsToFetch = new HashSet<>();
        discounts.forEach(d -> { if (d.getStadiumIds() != null) stadiumIdsToFetch.addAll(d.getStadiumIds()); });
        Log.d(TAG, "processStadiumNames: Stadium IDs to fetch names: " + stadiumIdsToFetch);

        if (stadiumIdsToFetch.isEmpty()){
            if (type == DiscountType.PERSONAL) finalizePersonalUpdate(discounts);
            else finalizeFavoriteUpdate(discounts);
            return;
        }

        Call<ScheduleODataStadiumResponseDTO> stadiumCall = bookingRepository.getStadiumsByIds(new ArrayList<>(stadiumIdsToFetch));
        if (stadiumCall == null) {
            Log.w(TAG, "processStadiumNames: No valid stadium IDs to fetch names.");
            if (type == DiscountType.PERSONAL) finalizePersonalUpdate(discounts);
            else finalizeFavoriteUpdate(discounts);
            return;
        }

        stadiumCall.enqueue(new Callback<ScheduleODataStadiumResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<ScheduleODataStadiumResponseDTO> call, @NonNull Response<ScheduleODataStadiumResponseDTO> response) {
                Log.d(TAG, "getStadiumsByIds (" + type + "): onResponse - Code: " + response.code());
                Map<Integer, String> stadiumNameLookup = new HashMap<>();
                if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                    response.body().getValue().forEach(s -> stadiumNameLookup.put(s.getId(), s.getName()));
                    Log.d(TAG, "getStadiumsByIds (" + type + "): Success! Map size: " + stadiumNameLookup.size());
                } else {
                    Log.e(TAG, "getStadiumsByIds (" + type + "): Error! Code: " + response.code());
                }

                for (ReadDiscountDTO discount : discounts) {
                    if (discount.getStadiumIds() != null && !discount.getStadiumIds().isEmpty()) {
                        discount.setStadiumNames(discount.getStadiumIds().stream()
                                .map(id -> stadiumNameLookup.getOrDefault(id, "Sân ID:" + id))
                                .collect(Collectors.toList()));
                    } else {
                        discount.setStadiumNames(new ArrayList<>());
                    }
                }

                if (type == DiscountType.PERSONAL) finalizePersonalUpdate(discounts);
                else finalizeFavoriteUpdate(discounts);
            }
            @Override
            public void onFailure(@NonNull Call<ScheduleODataStadiumResponseDTO> call, @NonNull Throwable t) {
                Log.e(TAG, "getStadiumsByIds (" + type + "): onFailure - Error: " + t.getMessage());
                if (type == DiscountType.PERSONAL) finalizePersonalUpdate(discounts);
                else finalizeFavoriteUpdate(discounts);
            }
        });
    }


    // --- Finalize and Update (Đã sửa logic) ---
    // Cập nhật LiveData cá nhân
    private void finalizePersonalUpdate(List<ReadDiscountDTO> processedDiscounts) {
        Log.i(TAG, "finalizePersonalUpdate: Processing " + processedDiscounts.size() + " new personal discounts (processed).");
        lastFetchedPersonal = new ArrayList<>(processedDiscounts);

        List<ReadDiscountDTO> listToPost;
        if (currentPersonalPage == 1) {
            // Nếu là trang đầu tiên (fetchInitial) -> Dùng luôn list mới này
            listToPost = lastFetchedPersonal;
            Log.d(TAG, "finalizePersonalUpdate: Page 1, replacing LiveData.");
        } else {
            // Nếu là trang sau (fetchMore) -> Lấy list cũ và nối thêm
            listToPost = personalDiscounts.getValue();
            if (listToPost == null) listToPost = new ArrayList<>(); // Khởi tạo nếu null
            listToPost.addAll(lastFetchedPersonal);
            Log.d(TAG, "finalizePersonalUpdate: Page > 1, appending to LiveData.");
        }

        personalDiscounts.postValue(listToPost); // Post danh sách cuối cùng
        Log.i(TAG, "finalizePersonalUpdate: Posted Personal. Page: " + currentPersonalPage + ", New total size: " + (listToPost != null ? listToPost.size() : 0)); // Thêm kiểm tra null
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            isLoading.postValue(false); // Tắt loading sau 1 giây
            isFetchingFavorite = false;
            Log.d(TAG, "finalizeFavoriteUpdate: Delayed isLoading=false");
        }, 300);
    }

    // Cập nhật LiveData sân yêu thích
    private void finalizeFavoriteUpdate(List<ReadDiscountDTO> processedFilteredDiscounts) {
        Log.i(TAG, "finalizeFavoriteUpdate: Processing " + processedFilteredDiscounts.size() + " new favorite discounts (processed).");
        lastFetchedFavorite = new ArrayList<>(processedFilteredDiscounts);

        List<ReadDiscountDTO> listToPost;
        if (currentFavoritePage == 1) {
            // Nếu là trang đầu tiên (fetchInitial) -> Dùng luôn list mới này
            listToPost = lastFetchedFavorite;
            Log.d(TAG, "finalizeFavoriteUpdate: Page 1, replacing LiveData.");
        } else {
            // Nếu là trang sau (fetchMore) -> Lấy list cũ và nối thêm
            listToPost = favoriteStadiumDiscounts.getValue();
            if (listToPost == null) listToPost = new ArrayList<>();
            listToPost.addAll(lastFetchedFavorite);
            Log.d(TAG, "finalizeFavoriteUpdate: Page > 1, appending to LiveData.");
        }

        favoriteStadiumDiscounts.postValue(listToPost); // Post danh sách cuối cùng
        Log.i(TAG, "finalizeFavoriteUpdate: Posted Favorite. Page: " + currentFavoritePage + ", New total size: " + (listToPost != null ? listToPost.size() : 0)); // Thêm kiểm tra null
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            isLoading.postValue(false); // Tắt loading sau 1 giây
            isFetchingFavorite = false;
            Log.d(TAG, "finalizeFavoriteUpdate: Delayed isLoading=false");
        }, 300);

    }

    // --- Utilities ---
    private boolean isValidUserId(int userId) {
        if (userId <= 0) {
            handleError("User ID không hợp lệ: " + userId, null);
            return false;
        } return true;
    }
    private void handleApiError(String msg, int code, DiscountType type) {
        Log.e(TAG, msg + " - Code: " + code);
        errorMessage.postValue(msg + ": " + code);
        isLoading.postValue(false);
        if (type == DiscountType.PERSONAL) isFetchingPersonal = false;
        else isFetchingFavorite = false;
    }
    private void handleNetworkError(String msg, Throwable t, DiscountType type) {
        Log.e(TAG, msg + " - Error: " + t.getMessage(), t);
        errorMessage.postValue(msg + ": " + t.getMessage());
        isLoading.postValue(false);
        if (type == DiscountType.PERSONAL) isFetchingPersonal = false;
        else isFetchingFavorite = false;
    }
    private void handleError(String msg, DiscountType type) {
        Log.e(TAG, msg);
        errorMessage.postValue(msg);
        isLoading.postValue(false);
        if (type == DiscountType.PERSONAL) isFetchingPersonal = false;
        else if (type == DiscountType.FAVORITE) isFetchingFavorite = false;
    }
}