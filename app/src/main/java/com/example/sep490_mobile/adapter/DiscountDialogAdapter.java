package com.example.sep490_mobile.adapter; // Adjust package if needed

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.discount.ReadDiscountDTO;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

// <<< SỬA: Adapter này giờ sẽ xử lý cả item null (position 0) >>>
public class DiscountDialogAdapter extends ArrayAdapter<ReadDiscountDTO> {

    private final LayoutInflater inflater;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final DecimalFormat percentFormat = new DecimalFormat("#.#");

    // Constructor accepts the list (which includes null at index 0)
    public DiscountDialogAdapter(@NonNull Context context, @NonNull List<ReadDiscountDTO> discountsIncludingNull) {
        super(context, 0, discountsIncludingNull);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Get the discount item (can be null for position 0)
        ReadDiscountDTO discount = getItem(position);

        // <<< SỬA: Xử lý item null (position 0) riêng >>>
        if (discount == null) {
            // Inflate or reuse a simple text view for "Không sử dụng mã"
            TextView textView;
            if (convertView instanceof TextView) {
                // Reuse if the recycled view is already a simple TextView
                textView = (TextView) convertView;
            } else {
                // Inflate the simple layout if convertView is null or wrong type
                textView = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            textView.setText("Không sử dụng mã");
            textView.setTextAppearance(android.R.style.TextAppearance_Material_Body1);
            int padding = (int) (16 * getContext().getResources().getDisplayMetrics().density); // ~16dp padding
            textView.setPadding(padding, padding, padding, padding);
            return textView;
        }
        // <<< KẾT THÚC SỬA >>>
        else {
            // --- Logic xử lý discount item (dùng ViewHolder) ---
            ViewHolder holder;
            // Check if convertView is reusable AND has the correct tag
            if (convertView != null && convertView.getTag() instanceof ViewHolder) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                // Inflate the custom layout ONLY for discount items
                convertView = inflater.inflate(R.layout.dialog_discount_item, parent, false);
                holder = new ViewHolder();
                holder.tvCode = convertView.findViewById(R.id.tv_dialog_discount_code);
                holder.tvPercent = convertView.findViewById(R.id.tv_dialog_discount_percent);
                holder.tvDesc = convertView.findViewById(R.id.tv_dialog_discount_desc);
                holder.tvConditions = convertView.findViewById(R.id.tv_dialog_discount_conditions);
                convertView.setTag(holder);
            }

            // Bind data (đã chắc chắn discount không null ở đây)
            holder.tvCode.setText(discount.getCode());
            holder.tvPercent.setText(String.format(Locale.US, "(Giảm %s%%)", percentFormat.format(discount.getPercentValue())));

            if (discount.getDescription() != null && !discount.getDescription().isEmpty()) {
                holder.tvDesc.setText(discount.getDescription());
                holder.tvDesc.setVisibility(View.VISIBLE);
            } else {
                holder.tvDesc.setVisibility(View.GONE);
            }

            String conditionsText = "";
            boolean hasMax = discount.getMaxDiscountAmount() > 0;
            boolean hasMin = discount.getMinOrderAmount() > 0;
            if (hasMax) { conditionsText += String.format("Giảm tối đa: %s", currencyFormatter.format(discount.getMaxDiscountAmount())); }
            if (hasMax && hasMin) { conditionsText += " - "; }
            if (hasMin) { conditionsText += String.format("Đơn tối thiểu: %s", currencyFormatter.format(discount.getMinOrderAmount())); }

            if (!conditionsText.isEmpty()) {
                holder.tvConditions.setText(conditionsText);
                holder.tvConditions.setVisibility(View.VISIBLE);
            } else {
                holder.tvConditions.setVisibility(View.GONE);
            }
            return convertView; // Return the view with ViewHolder
            // --- Kết thúc logic xử lý discount item ---
        }
    }

    private static class ViewHolder {
        TextView tvCode;
        TextView tvPercent;
        TextView tvDesc;
        TextView tvConditions;
    }
}