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

public class CourtHeaderAdapter extends RecyclerView.Adapter<CourtHeaderAdapter.CourtViewHolder> {
    private final List<CourtHeaderItem> courtHeaderItems;

    public CourtHeaderAdapter(List<CourtHeaderItem> courtHeaderItems) {
        this.courtHeaderItems = courtHeaderItems;
    }

    @NonNull
    @Override
    public CourtViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_court_name_header, parent, false);
        return new CourtViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CourtViewHolder holder, int position) {
        CourtHeaderItem currentItem = courtHeaderItems.get(position);
        holder.tvCourtName.setText(currentItem.getCourtName());

        // Logic để ẩn đường kẻ, tương tự như SportTypeHeaderAdapter
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

    static class CourtViewHolder extends RecyclerView.ViewHolder {
        TextView tvCourtName;
        View bottomDivider;
        public CourtViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCourtName = itemView.findViewById(R.id.tv_court_name);
            bottomDivider = itemView.findViewById(R.id.bottom_divider);
        }
    }
}