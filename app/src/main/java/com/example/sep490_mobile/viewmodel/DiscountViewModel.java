package com.example.sep490_mobile.viewmodel;

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
import com.example.sep490_mobile.data.repository.BookingRepository; // Needed for stadium names
import com.example.sep490_mobile.data.repository.DiscountRepository;
import com.example.sep490_mobile.data.repository.FavoriteRepository; // New repository

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
    private static final int PAGE_SIZE = 10;
    public enum DiscountType { PERSONAL, FAVORITE }

    // --- Repositories and SharedPreferences ---
    private final DiscountRepository discountRepository;
    private final FavoriteRepository favoriteRepository;
    private final BookingRepository bookingRepository; // Used for stadium names
    private final SharedPreferences sharedPreferences;

    // --- LiveData ---
    private final MutableLiveData<List<ReadDiscountDTO>> personalDiscounts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<ReadDiscountDTO>> favoriteStadiumDiscounts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false); // Controls MAIN progress bar
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLastPage = new MutableLiveData<>(false); // Tracks if the current tab reached its end

    // --- State Variables ---
    private int currentPersonalPage = 1;
    private int totalPersonalCount = 0;
    private boolean isFetchingPersonal = false;

    private int currentFavoritePage = 1;
    private int totalFavoriteCount = 0; // Total count BEFORE client-side filtering
    private boolean isFetchingFavorite = false;

    // Temporary storage for newly fetched items (used by Fragment for appendData)
    private List<ReadDiscountDTO> lastFetchedPersonal = null;
    private List<ReadDiscountDTO> lastFetchedFavorite = null;

    // Favorite stadium IDs state
    private Set<Integer> favoriteStadiumIds = null; // null means not loaded yet
    private boolean isLoadingFavorites = false;


    public DiscountViewModel(@NonNull Application application) {
        super(application);
        discountRepository = new DiscountRepository(application);
        favoriteRepository = new FavoriteRepository(application);
        bookingRepository = new BookingRepository(application);
        sharedPreferences = application.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        Log.d(TAG, "ViewModel initialized");
        // Fetch favorite stadium IDs once when the ViewModel is created
        fetchFavoriteStadiumIds();
    }

    // --- Getters ---
    public LiveData<List<ReadDiscountDTO>> getPersonalDiscounts() { return personalDiscounts; }
    public LiveData<List<ReadDiscountDTO>> getFavoriteStadiumDiscounts() { return favoriteStadiumDiscounts; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLastPage() { return isLastPage; }
    public List<ReadDiscountDTO> getLastFetchedPersonal() { return lastFetchedPersonal; }
    public List<ReadDiscountDTO> getLastFetchedFavorite() { return lastFetchedFavorite; }

    // --- Fetching Favorite IDs ---
    private void fetchFavoriteStadiumIds() {
        if (isLoadingFavorites || favoriteStadiumIds != null) {
            Log.d(TAG, "fetchFavoriteStadiumIds: Skipping fetch. Loading=" + isLoadingFavorites + ", Loaded=" + (favoriteStadiumIds != null));
            return;
        }
        isLoadingFavorites = true;
        Log.i(TAG, "Fetching favorite stadium IDs...");
        errorMessage.postValue(null); // Clear previous errors related to favorites

        favoriteRepository.getMyFavoriteStadiums().enqueue(new Callback<List<ReadFavoriteDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ReadFavoriteDTO>> call, @NonNull Response<List<ReadFavoriteDTO>> response) {
                isLoadingFavorites = false; // Đặt lại cờ
                if (response.isSuccessful() && response.body() != null) {
                    List<ReadFavoriteDTO> favoriteList = response.body(); // Lấy list gốc

                    // Log kiểm tra list gốc
                    Log.d(TAG, "fetchFavoriteStadiumIds: API Success! Raw response body size: " + favoriteList.size());
                    for (ReadFavoriteDTO favorite : favoriteList) {
                        Log.d(TAG, "Favorite Entry - FavoriteId: " + favorite.getFavoriteId() +
                                ", UserId: " + favorite.getUserId() +
                                ", StadiumId: " + favorite.getStadiumId()); // KIỂM TRA GIÁ TRỊ NÀY
                    }

                    // Xử lý stream
                    favoriteStadiumIds = favoriteList.stream()
                            .map(ReadFavoriteDTO::getStadiumId) // Lấy stadiumId
                            .collect(Collectors.toSet());
                    Log.i(TAG, "fetchFavoriteStadiumIds: Processing finished. Final favoriteStadiumIds Set: " + favoriteStadiumIds);
                } else {
                    Log.e(TAG, "fetchFavoriteStadiumIds: API Error! Code: " + response.code());
                    errorMessage.postValue("Lỗi tải danh sách sân yêu thích: " + response.code());
                    favoriteStadiumIds = new HashSet<>(); // Set empty on error to avoid null checks later
                }
                isLoadingFavorites = false;
                // Consider triggering a fetch for the Favorite tab if it was waiting
                // maybe check current tab and call fetchInitialDiscounts(FAVORITE) if needed?
            }

            @Override
            public void onFailure(@NonNull Call<List<ReadFavoriteDTO>> call, @NonNull Throwable t) {
                Log.e(TAG, "fetchFavoriteStadiumIds: Network Failure! Error: " + t.getMessage(), t);
                errorMessage.postValue("Lỗi mạng khi tải sân yêu thích: " + t.getMessage());
                favoriteStadiumIds = new HashSet<>(); // Set empty on error
                isLoadingFavorites = false;
            }
        });
    }


    // --- Public Methods for Fragment ---
    public void fetchInitialDiscounts(DiscountType type) {
        Log.i(TAG, "fetchInitialDiscounts called for type: " + type);
        isLoading.setValue(true); // Always show main loading for initial fetch
        errorMessage.setValue(null);
        isLastPage.setValue(false);
        lastFetchedPersonal = null;
        lastFetchedFavorite = null;

        if (type == DiscountType.PERSONAL) {
            currentPersonalPage = 1;
            totalPersonalCount = 0;
            isFetchingPersonal = false;
            personalDiscounts.setValue(new ArrayList<>()); // Clear previous data
            fetchPersonalDiscountsPage();
        } else { // FAVORITE
            currentFavoritePage = 1;
            totalFavoriteCount = 0;
            isFetchingFavorite = false;
            favoriteStadiumDiscounts.setValue(new ArrayList<>()); // Clear previous data

            // Ensure favorite IDs are loaded
            if (favoriteStadiumIds == null) {
                Log.w(TAG, "fetchInitialDiscounts (Favorite): Favorite IDs not loaded yet. Trying to fetch them.");
                if (!isLoadingFavorites) { // Only try fetching if not already in progress
                    fetchFavoriteStadiumIds();
                }
                // Set error and stop loading for now. User might need to retry or wait.
                isLoading.postValue(false);
                errorMessage.postValue("Đang tải danh sách sân yêu thích...");
                isLastPage.postValue(true); // Act as if it's the last page for now
            } else if (favoriteStadiumIds.isEmpty()){
                Log.i(TAG, "fetchInitialDiscounts (Favorite): No favorite stadiums. Finalizing with empty list.");
                finalizeFavoriteUpdate(new ArrayList<>(), new HashMap<>()); // Post empty list and turn off loading
            } else {
                fetchFavoriteStadiumDiscountsPage(); // Favorite IDs ready, fetch discounts
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
            if (!canLoadMore) {
                Log.d(TAG, "fetchMoreDiscounts (Personal): Cannot load more."); return;
            }
            currentPersonalPage++;
            // Don't set isLoading=true here, let fragment handle small progress bar
            fetchPersonalDiscountsPage();
        } else { // FAVORITE
            boolean canLoadMore = !isFetchingFavorite && (currentFavoritePage * PAGE_SIZE < totalFavoriteCount);
            if (!canLoadMore) {
                Log.d(TAG, "fetchMoreDiscounts (Favorite): Cannot load more based on pre-filter count."); return;
            }
            if (favoriteStadiumIds == null || favoriteStadiumIds.isEmpty()) {
                Log.w(TAG, "fetchMoreDiscounts (Favorite): Favorite IDs missing or empty.");
                isLastPage.postValue(true); // Cannot load more without IDs
                return;
            }
            currentFavoritePage++;
            // Don't set isLoading=true here
            fetchFavoriteStadiumDiscountsPage();
        }
    }

    // --- Private Fetching Logic ---
    private void fetchPersonalDiscountsPage() {
        if (isFetchingPersonal) return;
        isFetchingPersonal = true;
        // Only set global isLoading if it's the first page
        if (currentPersonalPage == 1) isLoading.setValue(true);
        Log.i(TAG, "Fetching PERSONAL discounts page: " + currentPersonalPage);

        String userIdStr = String.valueOf(sharedPreferences.getInt("user_id", -1));
        if (userIdStr.equals("-1")) {
            handleError("User ID không hợp lệ", DiscountType.PERSONAL);
            isFetchingPersonal = false; // Reset flag
            return;
        }

        discountRepository.getDiscountsByType(userIdStr, "unique", currentPersonalPage, PAGE_SIZE)
                .enqueue(new Callback<OdataHaveCountResponse<ReadDiscountDTO>>() {
                    @Override
                    public void onResponse(@NonNull Call<OdataHaveCountResponse<ReadDiscountDTO>> call, @NonNull Response<OdataHaveCountResponse<ReadDiscountDTO>> response) {
                        Log.d(TAG, "fetchPersonalDiscountsPage: onResponse - Code: " + response.code() + " for page " + currentPersonalPage);
                        if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                            List<ReadDiscountDTO> fetchedData = response.body().getValue();
                            totalPersonalCount = response.body().getCount();
                            Log.i(TAG, "fetchPersonalDiscountsPage: Success! Fetched " + fetchedData.size() + ". Total personal: " + totalPersonalCount);

                            boolean isLast = (currentPersonalPage * PAGE_SIZE >= totalPersonalCount);
                            isLastPage.postValue(isLast); // Update LiveData
                            Log.i(TAG, "fetchPersonalDiscountsPage: Is last page? " + isLast);

                            finalizePersonalUpdate(fetchedData); // Finalize data (no stadium names needed)

                        } else {
                            handleApiError("Lỗi tải mã cá nhân", response.code(), DiscountType.PERSONAL);
                            isLastPage.postValue(true); // Assume last page on error
                        }
                        // isLoading turned off in finalizePersonalUpdate
                    }
                    @Override
                    public void onFailure(@NonNull Call<OdataHaveCountResponse<ReadDiscountDTO>> call, @NonNull Throwable t) {
                        handleNetworkError("Lỗi mạng khi tải mã cá nhân", t, DiscountType.PERSONAL);
                        isLastPage.postValue(true); // Assume last page on error
                        // isLoading turned off in handleNetworkError
                    }
                });
    }

    private void fetchFavoriteStadiumDiscountsPage() {
        if (isFetchingFavorite) return;
        if (favoriteStadiumIds == null) { // Kiểm tra null trước
            Log.e(TAG, "fetchFavoriteStadiumDiscountsPage: Cannot fetch, favoriteStadiumIds is NULL.");
            handleError("Lỗi tải DS sân yêu thích", DiscountType.FAVORITE);
            isLastPage.postValue(true);
            isFetchingFavorite = false;
            return;
        }
        // <<< THÊM LOG KIỂM TRA FAVORITE IDS >>>
        Log.d(TAG, "fetchFavoriteStadiumDiscountsPage: Current favoriteStadiumIds before filtering: " + favoriteStadiumIds);
        // ------------------------------------

        if (favoriteStadiumIds.isEmpty()) { // Kiểm tra rỗng sau
            Log.i(TAG, "fetchFavoriteStadiumDiscountsPage: favoriteStadiumIds is EMPTY. No discounts to show.");
            handleError("Bạn chưa có sân yêu thích nào", DiscountType.FAVORITE); // Có thể dùng thông báo khác thay vì lỗi
            isLastPage.postValue(true);
            isFetchingFavorite = false;
            // Gọi finalize với list rỗng để tắt loading và hiện empty state
            finalizeFavoriteUpdate(new ArrayList<>(), new HashMap<>());
            return;
        }

        isFetchingFavorite = true;
        if (currentFavoritePage == 1) isLoading.setValue(true);
        Log.i(TAG, "Fetching FAVORITE STADIUM discounts page: " + currentFavoritePage);

        discountRepository.getDiscountsByType("0", "stadium", currentFavoritePage, PAGE_SIZE)
                .enqueue(new Callback<OdataHaveCountResponse<ReadDiscountDTO>>() {
                    @Override
                    public void onResponse(@NonNull Call<OdataHaveCountResponse<ReadDiscountDTO>> call, @NonNull Response<OdataHaveCountResponse<ReadDiscountDTO>> response) {
                        Log.d(TAG, "fetchFavoriteStadiumDiscountsPage: onResponse - Code: " + response.code() + " for page " + currentFavoritePage);
                        if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                            List<ReadDiscountDTO> fetchedData = response.body().getValue();
                            totalFavoriteCount = response.body().getCount();
                            Log.i(TAG, "fetchFavoriteStadiumDiscountsPage: Success! Fetched " + fetchedData.size() + " stadium codes (before filtering). Total stadium codes: " + totalFavoriteCount);

                            // <<< THÊM LOG KIỂM TRA DỮ LIỆU TRƯỚC KHI LỌC >>>
                            Log.d(TAG, "--- Checking fetched discounts before filtering ---");
                            for (ReadDiscountDTO discount : fetchedData) {
                                Log.d(TAG, "Discount Code: " + discount.getCode() + ", StadiumIds: " + discount.getStadiumIds());
                            }
                            Log.d(TAG, "--- Finished checking fetched discounts ---");
                            // ----------------------------------------------

                            // <<< LỌC CLIENT-SIDE >>>
                            List<ReadDiscountDTO> filteredData = fetchedData.stream()
                                    .filter(discount -> {
                                        boolean hasStadiumIds = discount.getStadiumIds() != null;
                                        boolean intersects = hasStadiumIds && !Collections.disjoint(discount.getStadiumIds(), favoriteStadiumIds);
                                        // <<< THÊM LOG BÊN TRONG FILTER >>>
                                        // Log.v(TAG, "Filtering Code: " + discount.getCode() + " - Has StadiumIds: " + hasStadiumIds + ", Intersects: " + intersects);
                                        // --------------------------------
                                        return intersects;
                                    })
                                    .collect(Collectors.toList());
                            Log.i(TAG, "fetchFavoriteStadiumDiscountsPage: Filtered down to " + filteredData.size() + " applicable discounts.");

                            boolean isLastBasedOnTotal = (currentFavoritePage * PAGE_SIZE >= totalFavoriteCount);
                            isLastPage.postValue(isLastBasedOnTotal);
                            Log.i(TAG, "fetchFavoriteStadiumDiscountsPage: Is last page (based on pre-filter count)? " + isLastBasedOnTotal);

                            processStadiumNamesForFavorite(filteredData);

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

    // --- Processing Names ---
    private void processStadiumNamesForFavorite(List<ReadDiscountDTO> discounts) {
        Log.d(TAG, "processStadiumNamesForFavorite: Processing " + discounts.size() + " discounts.");
        if (discounts.isEmpty()) {
            finalizeFavoriteUpdate(discounts, new HashMap<>()); // Still need to call finalize to turn off loading/reset flags
            return;
        }
        Set<Integer> stadiumIdsToFetch = new HashSet<>();
        for (ReadDiscountDTO discount : discounts) {
            if (discount.getStadiumIds() != null) {
                stadiumIdsToFetch.addAll(discount.getStadiumIds());
            }
        }
        Log.d(TAG, "processStadiumNamesForFavorite: Stadium IDs to fetch names for: " + stadiumIdsToFetch);
        if (stadiumIdsToFetch.isEmpty()){
            finalizeFavoriteUpdate(discounts, new HashMap<>());
            return;
        }
        Call<ScheduleODataStadiumResponseDTO> stadiumCall = bookingRepository.getStadiumsByIds(new ArrayList<>(stadiumIdsToFetch));
        if (stadiumCall == null) {
            Log.w(TAG, "processStadiumNamesForFavorite: No valid stadium IDs to fetch names.");
            finalizeFavoriteUpdate(discounts, new HashMap<>());
            return;
        }
        stadiumCall.enqueue(new Callback<ScheduleODataStadiumResponseDTO>() {
            @Override
            public void onResponse(@NonNull Call<ScheduleODataStadiumResponseDTO> call, @NonNull Response<ScheduleODataStadiumResponseDTO> response) {
                Log.d(TAG, "getStadiumsByIds (Favorite): onResponse - Code: " + response.code());
                Map<Integer, String> stadiumNameLookup = new HashMap<>();
                if (response.isSuccessful() && response.body() != null && response.body().getValue() != null) {
                    for (ScheduleStadiumDTO stadium : response.body().getValue()) {
                        stadiumNameLookup.put(stadium.getId(), stadium.getName());
                    }
                    Log.d(TAG, "getStadiumsByIds (Favorite): Success! Stadium map size: " + stadiumNameLookup.size());
                } else {
                    Log.e(TAG, "getStadiumsByIds (Favorite): Error! Code: " + response.code());
                }
                finalizeFavoriteUpdate(discounts, stadiumNameLookup);
            }
            @Override
            public void onFailure(@NonNull Call<ScheduleODataStadiumResponseDTO> call, @NonNull Throwable t) {
                Log.e(TAG, "getStadiumsByIds (Favorite): onFailure - Error: " + t.getMessage());
                finalizeFavoriteUpdate(discounts, new HashMap<>());
            }
        });
    }


    // --- Finalize and Update LiveData ---
    private void finalizePersonalUpdate(List<ReadDiscountDTO> newDiscountsFromApi) {
        Log.i(TAG, "finalizePersonalUpdate: Adding " + newDiscountsFromApi.size() + " new personal discounts.");
        // Personal discounts don't have stadium names applied here

        lastFetchedPersonal = new ArrayList<>(newDiscountsFromApi); // Store newly fetched items

        List<ReadDiscountDTO> currentList = personalDiscounts.getValue();
        if (currentList == null) currentList = new ArrayList<>();
        currentList.addAll(lastFetchedPersonal); // Append new data

        personalDiscounts.postValue(currentList); // Post the combined list
        Log.i(TAG, "finalizePersonalUpdate: Posted COMBINED Personal discounts. New total size: " + currentList.size());

        isLoading.postValue(false); // Turn off loading indicator
        isFetchingPersonal = false;    // Reset fetching flag
    }

    private void finalizeFavoriteUpdate(List<ReadDiscountDTO> newFilteredDiscounts, Map<Integer, String> stadiumNameLookup) {
        Log.i(TAG, "finalizeFavoriteUpdate: Adding " + newFilteredDiscounts.size() + " new favorite stadium discounts (after filtering).");
        // Add stadium names to the filtered list
        for (ReadDiscountDTO discount : newFilteredDiscounts) {
            if (discount.getStadiumIds() != null && !discount.getStadiumIds().isEmpty()) {
                List<String> names = discount.getStadiumIds().stream()
                        .map(id -> stadiumNameLookup.getOrDefault(id, "Sân ID:" + id))
                        .collect(Collectors.toList());
                discount.setStadiumNames(names);
            } else {
                discount.setStadiumNames(new ArrayList<>());
            }
        }

        lastFetchedFavorite = new ArrayList<>(newFilteredDiscounts); // Store newly fetched & processed items

        List<ReadDiscountDTO> currentList = favoriteStadiumDiscounts.getValue();
        if (currentList == null) currentList = new ArrayList<>();
        currentList.addAll(lastFetchedFavorite); // Append new data

        favoriteStadiumDiscounts.postValue(currentList); // Post the combined list
        Log.i(TAG, "finalizeFavoriteUpdate: Posted COMBINED Favorite discounts. New total size: " + currentList.size());

        isLoading.postValue(false); // Turn off loading indicator
        isFetchingFavorite = false;  // Reset fetching flag
    }

    // --- Utilities ---
    private boolean isValidUserId(int userId) {
        if (userId <= 0) {
            Log.e(TAG, "Invalid User ID: " + userId + ". Cannot fetch.");
            errorMessage.postValue("Không tìm thấy người dùng. Vui lòng đăng nhập lại.");
            isLoading.postValue(false);
            return false;
        }
        return true;
    }
    private void handleApiError(String contextMessage, int code, DiscountType type) {
        Log.e(TAG, contextMessage + " - Code: " + code);
        errorMessage.postValue(contextMessage + ": " + code);
        isLoading.postValue(false);
        if (type == DiscountType.PERSONAL) isFetchingPersonal = false;
        else isFetchingFavorite = false;
    }
    private void handleNetworkError(String contextMessage, Throwable t, DiscountType type) {
        Log.e(TAG, contextMessage + " - Error: " + t.getMessage(), t);
        errorMessage.postValue(contextMessage + ": " + t.getMessage());
        isLoading.postValue(false);
        if (type == DiscountType.PERSONAL) isFetchingPersonal = false;
        else isFetchingFavorite = false;
    }
    private void handleError(String message, DiscountType type) { // General error helper
        Log.e(TAG, message);
        errorMessage.postValue(message);
        isLoading.postValue(false);
        if (type == DiscountType.PERSONAL) isFetchingPersonal = false;
        else isFetchingFavorite = false;
    }
}