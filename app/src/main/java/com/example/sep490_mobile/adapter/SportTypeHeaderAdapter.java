package com.example.sep490_mobile.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.CourtHeaderItem;
import java.util.List;

public class SportTypeHeaderAdapter extends RecyclerView.Adapter<SportTypeHeaderAdapter.SportTypeViewHolder> {

    private final List<CourtHeaderItem> courtHeaderItems;

    public SportTypeHeaderAdapter(List<CourtHeaderItem> courtHeaderItems) {
        this.courtHeaderItems = courtHeaderItems;
    }

    @NonNull
    @Override
    public SportTypeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_sport_type_header, parent, false);
        return new SportTypeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SportTypeViewHolder holder, int position) {
        CourtHeaderItem currentItem = courtHeaderItems.get(position);

        if (currentItem.isCenterOfGroup()) {
            holder.tvSportType.setText(currentItem.getSportType());
            holder.tvSportType.setVisibility(View.VISIBLE);
        } else {
            holder.tvSportType.setVisibility(View.INVISIBLE);
        }

        boolean isLastInGroup = true;
        if (position + 1 < courtHeaderItems.size()) {
            CourtHeaderItem nextItem = courtHeaderItems.get(position + 1);
            if (nextItem.getSportType().equals(currentItem.getSportType())) {
                isLastInGroup = false;
            }
        }

        if (isLastInGroup) {
            holder.bottomDivider.setVisibility(View.VISIBLE);
        } else {
            holder.bottomDivider.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return courtHeaderItems != null ? courtHeaderItems.size() : 0;
    }

    static class SportTypeViewHolder extends RecyclerView.ViewHolder {
        TextView tvSportType;
        View bottomDivider;
        public SportTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSportType = itemView.findViewById(R.id.tv_sport_type);
            bottomDivider = itemView.findViewById(R.id.bottom_divider);
        }
    }
}