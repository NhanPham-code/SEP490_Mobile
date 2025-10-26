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
        void onCourtClick(int courtId);
    }

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
            return new CourtViewHolder(view, listener);
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

    class CourtViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourtName, tvCourtPrice, tvCourtStatus;
        MaterialCardView cardView;

        CourtViewHolder(@NonNull View itemView, OnCourtClickListener clickListener) {
            super(itemView);
            tvCourtName = itemView.findViewById(R.id.tv_court_name);
            tvCourtPrice = itemView.findViewById(R.id.tv_court_price);
            tvCourtStatus = itemView.findViewById(R.id.tv_court_status);
            cardView = itemView.findViewById(R.id.court_card);

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

            // Xác định các trạng thái
            boolean isBlockedBySystem = !court.isAvailable();
            boolean isBookedByOther = bookedCourtIds.contains(court.getId());
            boolean isRelatedToBooked = bookedCourtRelationIds.contains(court.getId());
            boolean isSelectedByMe = selectedCourtIds.contains(court.getId());
            boolean isRelatedToSelected = selectedCourtRelationIds.contains(court.getId());

            // Mặc định view có thể tương tác
            itemView.setAlpha(1.0f);
            itemView.setClickable(true);

            // SỬA ĐỔI: Logic ưu tiên mới
            if (isBlockedBySystem) {
                setStatus("Bảo trì", R.drawable.bg_status_unavailable, R.color.status_text_unavailable);
                itemView.setClickable(false);
            } else if (isSelectedByMe) {
                setStatus("Đã chọn", R.drawable.bg_status_selected, R.color.status_text_selected);
            } else if (isRelatedToSelected) {
                // Chỉ vô hiệu hóa click cho sân liên quan đến sân ĐÃ CHỌN
                setStatus("Liên quan", R.drawable.bg_status_related, R.color.status_text_related);
                itemView.setClickable(false);
            } else if (isBookedByOther) {
                setStatus("Đã đặt", R.drawable.bg_status_booked, R.color.status_text_booked);
                // VẪN CHO PHÉP CLICK
            } else if (isRelatedToBooked) {
                setStatus("Liên quan", R.drawable.bg_status_related, R.color.status_text_related);
                // VẪN CHO PHÉP CLICK
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