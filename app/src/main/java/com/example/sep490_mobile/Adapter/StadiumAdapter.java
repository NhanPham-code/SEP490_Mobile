package com.example.sep490_mobile.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.CourtsDTO;
import com.example.sep490_mobile.data.dto.StadiumDTO;

import java.util.List;

public class StadiumAdapter extends RecyclerView.Adapter<StadiumAdapter.StadiumViewHolder> {

    private List<StadiumDTO> stadiumDTOS;
    private Context context;

    public StadiumAdapter(Context context, List<StadiumDTO> stadiumDTOS){
        this.stadiumDTOS = stadiumDTOS;
        this.context = context;
    }

    @NonNull
    @Override
    public StadiumAdapter.StadiumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_stadium, parent, false);
        return new StadiumViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull StadiumAdapter.StadiumViewHolder holder, int position) {
        StadiumDTO stadiumDTO = stadiumDTOS.get(position);
        CourtsDTO[] courtsDTOS = stadiumDTO.courts.toArray(new CourtsDTO[0]);
        String time = stadiumDTO.openTime + " - " + stadiumDTO.closeTime;

        holder.stadiumName.setText(stadiumDTO.name);
        holder.stadiumAddress.setText(stadiumDTO.address);
        holder.stadiumTime.setText(time);
        holder.stadiumSportType.setText(courtsDTOS.length > 0 ? courtsDTOS[0].sportType : "");
    }

    @Override
    public int getItemCount() {
        return stadiumDTOS.size();
    }

    public class StadiumViewHolder extends RecyclerView.ViewHolder {

        public TextView stadiumName;
        public TextView stadiumAddress;
        public TextView stadiumTime;
        public TextView stadiumSportType;

        public StadiumViewHolder(@NonNull View itemView) {
            super(itemView);

            stadiumName = itemView.findViewById(R.id.stadium_name);
            stadiumAddress = itemView.findViewById(R.id.stadium_address);
            stadiumTime = itemView.findViewById(R.id.stadium_time);
            stadiumSportType = itemView.findViewById(R.id.stadium_sportType);
        }
    }
}
