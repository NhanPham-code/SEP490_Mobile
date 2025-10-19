package com.example.sep490_mobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sep490_mobile.data.dto.CourtsDTO;
import com.example.sep490_mobile.data.dto.ScheduleBookingDetailDTO;
import com.example.sep490_mobile.data.dto.SelectBookingDTO;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.utils.DurationConverter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import android.widget.TextView;

import com.example.sep490_mobile.R; // Assuming your resources are in the default R class
import com.example.sep490_mobile.data.dto.ScheduleBookingDTO;
import com.example.sep490_mobile.ui.home.OnItemClickListener;

import java.util.Dictionary;
import java.util.List;
import java.util.stream.Collectors;

public class SelectBookingAdapter extends RecyclerView.Adapter<SelectBookingAdapter.SelectBookingViewHolder> {

    private List<ScheduleBookingDTO> scheduleBookingDTOS;
    private Dictionary<Integer, StadiumDTO> stadiumDTODictionary;
    private Context context;
    private OnItemClickListener listener;

    public SelectBookingAdapter(Context context){
        this.context = context;
    }

    public void setSelectBookingList(SelectBookingDTO selectBookingDTO, OnItemClickListener listener){
        this.scheduleBookingDTOS = selectBookingDTO.getScheduleBookingDTOS();
        this.stadiumDTODictionary = selectBookingDTO.getStadiums();
        this.listener = listener;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SelectBookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 1. Inflate the layout from the XML provided
        // Assuming the XML file is named 'item_booking_card.xml' (you'll need to create this file)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_booking_for_find_team, parent, false);
        return new SelectBookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectBookingViewHolder holder, int position) {
        if (scheduleBookingDTOS != null) {
            ScheduleBookingDTO booking = scheduleBookingDTOS.get(position);

            // Set Header information
            holder.tvBookingId.setText(booking.getId()); // Assuming you have a string resource like "Booking #%s"
            holder.tvBookingStatus.setText(booking.getStatus());

            // Simple status-based background/text color logic (Example only)
            if ("Đã xác nhận".equals(booking.getStatus())) {
                holder.tvBookingStatus.setBackgroundColor(Color.parseColor("#71C863")); // Green
            } else if ("Đang chờ".equals(booking.getStatus())) {
                holder.tvBookingStatus.setBackgroundColor(Color.parseColor("#FFC107")); // Amber
            } else {
                holder.tvBookingStatus.setBackgroundColor(Color.parseColor("#9E9E9E")); // Gray default
            }

            List<ScheduleBookingDetailDTO> bookingDetailDTO = booking.getBookingDetails();
            List<CourtsDTO> courtsDTOS = stadiumDTODictionary.get(booking.getStadiumId()).getCourts().stream().collect(Collectors.toList());
            // Set Body details
            holder.tvStadiumName.setText(booking.getStadiumName());
            // Format date as needed, assuming it's a simple string for now
            holder.tvBookingDate.setText(""); // e.g., "Ngày chơi: %s"
            holder.tvSportType.setText(courtsDTOS.get(0).getSportType());
            holder.tvAddress.setText(stadiumDTODictionary.get(booking.getStadiumId()).getAddress());
            // Format price as a currency string, assuming a simple string for now
            holder.tvPrice.setText(booking.getTotalPrice() + "");

            // Set up click listener for the button
            holder.btnSelectBooking.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return scheduleBookingDTOS != null ? scheduleBookingDTOS.size() : 0;
    }

    public class SelectBookingViewHolder extends RecyclerView.ViewHolder {
        // Declare all views from the XML layout
        public TextView tvBookingId;
        public TextView tvBookingStatus;
        public TextView tvStadiumName;
        public TextView tvBookingDate;
        public TextView tvSportType;
        public TextView tvAddress;
        public TextView tvPrice;
        public MaterialButton btnSelectBooking;

        public SelectBookingViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize views by finding them by their ID
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvBookingStatus = itemView.findViewById(R.id.tvBookingStatus);
            tvStadiumName = itemView.findViewById(R.id.tvStadiumName);
            tvBookingDate = itemView.findViewById(R.id.tvBookingDate);
            tvSportType = itemView.findViewById(R.id.tvSportType);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            btnSelectBooking = itemView.findViewById(R.id.btnSelectBooking);
        }
    }
}