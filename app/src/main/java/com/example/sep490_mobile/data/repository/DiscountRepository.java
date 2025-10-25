package com.example.sep490_mobile.data.repository;

import android.content.Context;
import android.util.Log;
import com.example.sep490_mobile.data.dto.OdataHaveCountResponse;
import com.example.sep490_mobile.data.dto.discount.ReadDiscountDTO; // Adjust path if needed
import com.example.sep490_mobile.data.remote.ApiClient;
import com.example.sep490_mobile.data.remote.ApiService;
import java.util.Locale;
import retrofit2.Call;

public class DiscountRepository {

    private static final String TAG = "DiscountRepository";
    private final ApiService apiService;

    public DiscountRepository(Context context) {
        this.apiService = ApiClient.getInstance(context).getApiService();
    }

    /**
     * Gets paged discounts based on type (personal or stadium) for a user.
     *
     * @param targetUserId The ID of the current user (as String). Can be ignored if fetching 'stadium' type.
     * @param codeType     The type of code to fetch ("unique" or "stadium").
     * @param page         The current page number (starting from 1).
     * @param pageSize     The number of items per page.
     * @return Call object.
     */
    public Call<OdataHaveCountResponse<ReadDiscountDTO>> getDiscountsByType(
            String targetUserId, String codeType, int page, int pageSize) {

        int skip = (page - 1) * pageSize;
        String filter;

        // Build filter based on codeType
        if ("unique".equalsIgnoreCase(codeType)) {
            // Personal codes: TargetUserId matches and is active
            filter = String.format(Locale.US,
                    "(TargetUserId eq '%s') and (IsActive eq true)",
                    targetUserId);
        } else if ("stadium".equalsIgnoreCase(codeType)) {
            // Stadium codes: CodeType is 'stadium' and is active
            // Filtering by favorite stadiums happens in the ViewModel
            filter = "(CodeType eq 'stadium') and (IsActive eq true)";
        } else {
            // Fallback for invalid codeType - return empty or handle error
            Log.e(TAG, "Invalid codeType provided: " + codeType + ". Returning empty filter.");
            // Returning an empty filter might fetch unwanted data,
            // perhaps return personal codes as a default or null?
            // Let's return personal as a safer default:
            filter = String.format(Locale.US,
                    "(TargetUserId eq '%s') and (IsActive eq true)",
                    targetUserId);
            // Alternatively, return null or throw:
            // return null;
        }

        // Sort by newest first
        String orderBy = "CreatedAt desc";

        Log.d(TAG, "Requesting discounts -> Filter: [" + filter + "] | OrderBy: " + orderBy + " | Skip: " + skip + " | Top: " + pageSize);

        // Call the ApiService method
        return apiService.getDiscounts(
                filter,
                orderBy,
                true, // Request total count ($count=true)
                skip,
                pageSize
        );
    }
}