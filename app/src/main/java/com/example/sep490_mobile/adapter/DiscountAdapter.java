package com.example.sep490_mobile.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.discount.ReadDiscountDTO; // Adjust path if needed
import com.example.sep490_mobile.ui.discount.DiscountListFragmentDirections; // Ensure this is correct

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DiscountAdapter extends RecyclerView.Adapter<DiscountAdapter.DiscountViewHolder> {

    private static final String TAG = "DiscountAdapter_Log";

    // Use List<ReadDiscountDTO> directly
    private final List<ReadDiscountDTO> discounts = new ArrayList<>();
    private final Context context;
    // Format for parsing API date strings (e.g., 2025-10-17T23:59:59+07:00)
    private final SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
    // Format for displaying dates to the user
    private final SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public DiscountAdapter(Context context) {
        this.context = context;
        Log.d(TAG, "Adapter initialized");
    }

    /**
     * Clears old data and sets a new list (for initial load or tab switch).
     * @param newData The new list of discounts.
     */
    public void setData(List<ReadDiscountDTO> newData) {
        Log.d(TAG, "setData called with " + (newData != null ? newData.size() : "null") + " items.");
        int previousSize = discounts.size();
        discounts.clear();
        if (newData != null) {
            discounts.addAll(newData);
        }
        // Use more specific notifications
        if (previousSize == 0 && !discounts.isEmpty()) {
            notifyItemRangeInserted(0, discounts.size());
        } else if (previousSize > 0 && discounts.isEmpty()) {
            notifyItemRangeRemoved(0, previousSize);
        } else if (previousSize > 0) { // Only call changed if there was data before
            notifyDataSetChanged(); // Safest for complete replacement
        }
        // Do nothing if both were empty
        else {
            Log.d(TAG, "setData: List was empty and is still empty.");
        }
    }


    /**
     * Appends new data to the end of the current list (for loading more).
     * @param newData The list of new discounts to append.
     */
    public void appendData(List<ReadDiscountDTO> newData) {
        if (newData == null || newData.isEmpty()) {
            Log.d(TAG, "appendData called with empty or null data.");
            return;
        }
        int startPosition = discounts.size();
        discounts.addAll(newData);
        Log.d(TAG, "appendData: Added " + newData.size() + " items. New total size: " + discounts.size() + ". Notifying range inserted from " + startPosition);
        notifyItemRangeInserted(startPosition, newData.size());
    }


    @NonNull
    @Override
    public DiscountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View view = LayoutInflater.from(context).inflate(R.layout.item_discount, parent, false);
        return new DiscountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscountViewHolder holder, int position) {
        // Log.d(TAG, "onBindViewHolder for position: " + position); // Can be noisy
        if (position >= 0 && position < discounts.size()) {
            holder.bind(discounts.get(position));
        } else {
            Log.e(TAG, "Invalid position requested in onBindViewHolder: " + position + ", size is " + discounts.size());
            // Optionally clear the holder's views to prevent showing wrong data
            // holder.clearViews();
        }
    }

    @Override
    public int getItemCount() {
        return discounts.size();
    }

    // --- ViewHolder ---
    class DiscountViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvCode, tvDescription, tvExpiry, tvStadiums;
        ImageButton btnCopy;

        public DiscountViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.iv_discount_icon);
            tvCode = itemView.findViewById(R.id.tv_discount_code);
            tvDescription = itemView.findViewById(R.id.tv_discount_description);
            tvExpiry = itemView.findViewById(R.id.tv_discount_expiry);
            tvStadiums = itemView.findViewById(R.id.tv_applicable_stadiums);
            btnCopy = itemView.findViewById(R.id.btn_copy_code);
        }

        void bind(ReadDiscountDTO discount) {
            // Log.d(TAG, "Binding data for discount code: " + discount.getCode()); // Can be noisy

            if (discount == null) {
                Log.w(TAG, "Attempting to bind DiscountViewHolder with null data at pos " + getBindingAdapterPosition());
                // Reset views
                tvCode.setText("");
                tvDescription.setText("");
                tvExpiry.setText("");
                tvStadiums.setText("");
                ivIcon.setImageResource(R.drawable.ic_discount_stadium); // Default icon
                tvStadiums.setVisibility(View.GONE);
                itemView.setOnClickListener(null);
                btnCopy.setOnClickListener(null);
                return;
            }


            tvCode.setText(discount.getCode());

            // Use value directly from API (no * 100)
            String description = String.format(Locale.US, "Giảm %.0f%%", discount.getPercentValue());
            if (discount.getMaxDiscountAmount() > 0) {
                description += String.format(Locale.US, " (tối đa %,.0fk)", discount.getMaxDiscountAmount() / 1000);
            }
            tvDescription.setText(description);

            // Parse and display expiry date
            try {
                String endDateStr = discount.getEndDate();
                if (endDateStr != null) {
                    Date endDate = inputDateFormat.parse(endDateStr);
                    tvExpiry.setText("HSD: " + outputDateFormat.format(endDate));
                } else {
                    tvExpiry.setText("HSD: Không xác định");
                }
            } catch (ParseException | NullPointerException e) {
                Log.e(TAG, "Error parsing end date: '" + discount.getEndDate() + "' at pos " + getBindingAdapterPosition(), e);
                tvExpiry.setText("HSD: Lỗi định dạng");
            }

            // Display applicable stadiums
            List<String> stadiumNames = discount.getStadiumNames();
            if (stadiumNames != null && !stadiumNames.isEmpty()) {
                String stadiumsText = "Áp dụng tại: " + stadiumNames.get(0);
                if (stadiumNames.size() > 1) {
                    stadiumsText += " (+" + (stadiumNames.size() - 1) + " sân khác)";
                }
                tvStadiums.setText(stadiumsText);
                tvStadiums.setVisibility(View.VISIBLE);
            } else {
                // Hide if no specific stadiums listed (could be personal or general stadium code without names)
                tvStadiums.setVisibility(View.GONE);
            }

            // Copy button listener
            btnCopy.setOnClickListener(v -> {
                if (discount.getCode() == null) return; // Avoid copying null
                Log.d(TAG, "Copy button clicked for code: " + discount.getCode());
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Discount Code", discount.getCode());
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, "Đã sao chép mã: " + discount.getCode(), Toast.LENGTH_SHORT).show();
                }
            });

            // Set icon based on type
            if ("unique".equalsIgnoreCase(discount.getCodeType())) {
                ivIcon.setImageResource(R.drawable.ic_person);
            } else { // Assume stadium type
                ivIcon.setImageResource(R.drawable.ic_discount_stadium);
            }

            // Click listener for navigation
            itemView.setOnClickListener(v -> {
                if (discount != null) { // Double check before navigating
                    try {
                        DiscountListFragmentDirections.ActionDiscountListFragmentToDiscountDetailFragment action =
                                DiscountListFragmentDirections.actionDiscountListFragmentToDiscountDetailFragment(discount);
                        Navigation.findNavController(v).navigate(action);
                    } catch (Exception e) { // Catch broader exceptions during navigation
                        Log.e(TAG, "Navigation failed for Discount item at pos " + getBindingAdapterPosition(), e);
                        Toast.makeText(context, "Không thể mở chi tiết", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Cannot navigate, discount became null at pos " + getBindingAdapterPosition());
                }
            });
        }

        // Optional: Method to clear views if needed when position is invalid
        // void clearViews() { ... }
    }
}