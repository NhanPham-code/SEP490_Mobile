package com.example.sep490_mobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.CourtsDTO;
import com.example.sep490_mobile.data.dto.SelectedCourtInfo;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.ViewHolder> {

    private final List<SelectedCourtInfo> selectedCourts;
    private final List<CourtsDTO> allCourtsInStadium;
    private final NumberFormat currencyFormatter = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    public CheckoutAdapter(List<SelectedCourtInfo> selectedCourts, List<CourtsDTO> allCourtsInStadium) {
        this.selectedCourts = selectedCourts;
        this.allCourtsInStadium = allCourtsInStadium;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_checkout_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SelectedCourtInfo selectedCourt = selectedCourts.get(position);
        CourtsDTO courtDetails = findCourtById(selectedCourt.getCourtId());

        if (courtDetails != null) {
            holder.tvCourtName.setText(courtDetails.getName());
            holder.tvPricePerHour.setText(currencyFormatter.format(courtDetails.getPricePerHour()));

            // Nối các khoảng thời gian lại
            String timeRanges = String.join("\n", selectedCourt.getTimes());
            holder.tvTimeRange.setText(timeRanges);

            // Tính tổng số giờ và thành tiền
            long totalHours = 0;
            for (String range : selectedCourt.getTimes()) {
                try {
                    String[] parts = range.split("-");
                    int start = Integer.parseInt(parts[0].split(":")[0]);
                    int end = Integer.parseInt(parts[1].split(":")[0]);
                    totalHours += (end - start);
                } catch (Exception e) {
                    // Xử lý lỗi nếu định dạng thời gian sai
                }
            }
            long subtotal = totalHours * courtDetails.getPricePerHour();
            holder.tvSubtotal.setText(currencyFormatter.format(subtotal));
        }
    }

    @Override
    public int getItemCount() {
        return selectedCourts.size();
    }

    private CourtsDTO findCourtById(int courtId) {
        for (CourtsDTO court : allCourtsInStadium) {
            if (court.getId() == courtId) {
                return court;
            }
        }
        return null;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourtName, tvTimeRange, tvPricePerHour, tvSubtotal;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourtName = itemView.findViewById(R.id.tv_court_name);
            tvTimeRange = itemView.findViewById(R.id.tv_time_range);
            tvPricePerHour = itemView.findViewById(R.id.tv_price_per_hour);
            tvSubtotal = itemView.findViewById(R.id.tv_subtotal);
        }
    }
}