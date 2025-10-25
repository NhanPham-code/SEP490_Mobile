package com.example.sep490_mobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.CourtsDTO;
import com.example.sep490_mobile.data.model.CourtDisplayItem;
import com.google.android.material.card.MaterialCardView;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CourtSelectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<CourtDisplayItem> items;
    private final Context context;
    private final OnCourtClickListener listener;

    private Set<Integer> bookedCourtIds = new HashSet<>();
    private Set<Integer> bookedCourtRelationIds = new HashSet<>();
    private Set<Integer> selectedCourtIds = new HashSet<>();
    private Set<Integer> selectedCourtRelationIds = new HashSet<>();

    public CourtSelectionAdapter(Context context, List<CourtDisplayItem> items, OnCourtClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    public interface OnCourtClickListener {
        void onCourtClick(int courtId); // Chỉ cần listener này
    }

    // (Các hàm setBookedCourtIds, setSelectedCourtIds... giữ nguyên)
    public void setBookedCourtIds(Set<Integer> bookedCourtIds) {
        this.bookedCourtIds = bookedCourtIds != null ? bookedCourtIds : new HashSet<>();
        notifyDataSetChanged();
    }
    public void setBookedCourtRelationIds(Set<Integer> relationIds) {
        this.bookedCourtRelationIds = relationIds != null ? relationIds : new HashSet<>();
        notifyDataSetChanged();
    }
    public void setSelectedCourtIds(Set<Integer> selectedIds) {
        this.selectedCourtIds = selectedIds != null ? selectedIds : new HashSet<>();
        notifyDataSetChanged();
    }
    public void setSelectedCourtRelationIds(Set<Integer> relationIds) {
        this.selectedCourtRelationIds = relationIds != null ? relationIds : new HashSet<>();
        notifyDataSetChanged();
    }
    // =========================================================

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == CourtDisplayItem.TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_court_sport_type_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_court_selection, parent, false);
            return new CourtViewHolder(view, listener); // Truyền listener vào constructor
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == CourtDisplayItem.TYPE_HEADER) {
            ((HeaderViewHolder) holder).bind((CourtDisplayItem.Header) items.get(position));
        } else {
            ((CourtViewHolder) holder).bind((CourtDisplayItem.CourtItem) items.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // (HeaderViewHolder giữ nguyên)
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvSportType;
        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSportType = itemView.findViewById(R.id.tv_sport_type);
        }
        void bind(CourtDisplayItem.Header header) {
            tvSportType.setText(header.getSportType());
        }
    }

    // === CẬP NHẬT LOGIC CỦA COURT VIEWHOLDER ===
    class CourtViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourtName, tvCourtPrice, tvCourtStatus;
        MaterialCardView cardView;

        CourtViewHolder(@NonNull View itemView, OnCourtClickListener clickListener) {
            super(itemView);
            tvCourtName = itemView.findViewById(R.id.tv_court_name);
            tvCourtPrice = itemView.findViewById(R.id.tv_court_price);
            tvCourtStatus = itemView.findViewById(R.id.tv_court_status);
            cardView = itemView.findViewById(R.id.court_card);

            // Listener giờ chỉ gọi onCourtClick, không cần phân luồng
            itemView.setOnClickListener(v -> {
                if (!itemView.isClickable() || clickListener == null) {
                    return;
                }
                int position = getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) return;

                CourtDisplayItem item = items.get(position);
                if (item.getType() == CourtDisplayItem.TYPE_COURT) {
                    int courtId = ((CourtDisplayItem.CourtItem) item).getCourt().getId();
                    clickListener.onCourtClick(courtId);
                }
            });
        }

        void bind(CourtDisplayItem.CourtItem courtItem) {
            CourtsDTO court = courtItem.getCourt();
            tvCourtName.setText(court.getName());

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvCourtPrice.setText(currencyFormat.format(court.getPricePerHour()));

            boolean isBlocked = !court.isAvailable();
            boolean isBooked = bookedCourtIds.contains(court.getId());
            boolean isRelatedToBooked = bookedCourtRelationIds.contains(court.getId());
            boolean isSelected = selectedCourtIds.contains(court.getId());
            boolean isRelatedToSelected = selectedCourtRelationIds.contains(court.getId());

            itemView.setAlpha(1.0f);
            itemView.setClickable(true); // Mặc định là click được

            if (isBlocked) {
                setStatus("Không hoạt động", R.drawable.bg_status_unavailable, R.color.status_text_unavailable);
                itemView.setAlpha(0.6f);
                itemView.setClickable(false); // CHỈ sân bị khóa là không click được
            } else if (isSelected) {
                // Ưu tiên trạng thái ĐÃ CHỌN lên trên hết
                setStatus("Đã chọn", R.drawable.bg_status_selected, R.color.status_text_selected);
            } else if (isRelatedToSelected) {
                setStatus("Liên quan", R.drawable.bg_status_related, R.color.status_text_related);
                itemView.setClickable(false); // Sân liên quan đến lựa chọn -> không click
            } else if (isBooked) {
                setStatus("Đã đặt", R.drawable.bg_status_booked, R.color.status_text_booked);
                // VẪN CHO PHÉP CLICK
            } else if (isRelatedToBooked) {
                // Không cần trạng thái này nữa vì isSelected được ưu tiên
                // Nếu cần, bạn có thể giữ lại nhưng nó sẽ không bao giờ được hiển thị
                // vì logic chọn sân sẽ biến nó thành "Liên quan" (isRelatedToSelected)
                setStatus("Liên quan", R.drawable.bg_status_related, R.color.status_text_related);
                itemView.setClickable(false);
            } else {
                setStatus("Có thể đặt", R.drawable.bg_status_available, R.color.status_text_available);
            }
        }

        private void setStatus(String text, int backgroundRes, int colorRes) {
            tvCourtStatus.setText(text);
            tvCourtStatus.setBackgroundResource(backgroundRes);
            tvCourtStatus.setTextColor(ContextCompat.getColor(context, colorRes));
            cardView.setStrokeColor(ContextCompat.getColor(context, colorRes));
        }
    }
}