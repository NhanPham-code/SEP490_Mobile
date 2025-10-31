package com.example.sep490_mobile.data.repository;

import android.content.Context;
import com.example.sep490_mobile.data.dto.OdataHaveCountResponse;
import com.example.sep490_mobile.data.dto.discount.ReadDiscountDTO;
import com.example.sep490_mobile.data.dto.discount.UpdateDiscountDTO;
import com.example.sep490_mobile.data.remote.ApiClient;
import com.example.sep490_mobile.data.remote.ApiService;
import java.util.Locale;
import android.util.Log;
import retrofit2.Call;

public class DiscountRepository {

    private static final String TAG = "DiscountRepository";
    private final ApiService apiService;

    public DiscountRepository(Context context) {
        this.apiService = ApiClient.getInstance(context).getApiService();
    }

    public Call<OdataHaveCountResponse<ReadDiscountDTO>> getDiscountsByType(
            String targetUserId, String codeType, int page, int pageSize) {

        int skip = (page - 1) * pageSize;
        String filter;

        if ("unique".equalsIgnoreCase(codeType)) {
            filter = String.format(Locale.US,
                    "(TargetUserId eq '%s') and (IsActive eq true)",
                    targetUserId);
        } else if ("stadium".equalsIgnoreCase(codeType)) {
            filter = "(CodeType eq 'stadium') and (IsActive eq true)";
        } else {
            Log.e(TAG, "Invalid codeType provided: " + codeType + ". Returning empty filter.");
            filter = String.format(Locale.US,
                    "(TargetUserId eq '%s') and (IsActive eq true)",
                    targetUserId);
        }

        String orderBy = "CreatedAt desc";

        Log.d(TAG, "Requesting discounts -> Filter: [" + filter + "] | OrderBy: " + orderBy + " | Skip: " + skip + " | Top: " + pageSize);

        return apiService.getDiscounts(
                filter,
                orderBy,
                true,
                skip,
                pageSize
        );
    }

    /**
     * Cập nhật discount theo Id.
     *
     * @param
     * @param updateDiscountDTO Dữ liệu cập nhật
     * @return Call object
     */
    public Call<ReadDiscountDTO> updateDiscount(UpdateDiscountDTO updateDiscountDTO) {
        // Bỏ id, chỉ truyền DTO
        return apiService.updateDiscount(updateDiscountDTO);
    }

    public Call<ReadDiscountDTO> getDiscountById(int id) {
        // (Giả định ApiService đã có @GET("discounts/{id}"))
        return apiService.getDiscountById(id);
    }
}
