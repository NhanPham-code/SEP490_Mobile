package com.example.sep490_mobile.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.CourtsDTO;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.data.dto.StadiumImagesDTO;
import com.example.sep490_mobile.ui.home.HomeFragment;
import com.example.sep490_mobile.ui.home.OnItemClickListener;
import com.example.sep490_mobile.utils.DurationConverter;
import com.example.sep490_mobile.utils.ImageUtils;

import java.util.List;

public class StadiumAdapter extends RecyclerView.Adapter<StadiumAdapter.StadiumViewHolder> {

    private List<StadiumDTO> stadiumDTOS;
    private Context context;
    public StadiumAdapter(Context context){
        this.context = context;
    }
    private OnItemClickListener listener;
    public void setStadiumDTOS(List<StadiumDTO> stadiumDTOS, OnItemClickListener listener){
        this.stadiumDTOS = stadiumDTOS;
        this.listener = listener;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StadiumAdapter.StadiumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_stadium, parent, false);
        return new StadiumViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull StadiumAdapter.StadiumViewHolder holder, int position) {

//        Toast.makeText(context, " Stadium: " + stadiumDTOS.size(), Toast.LENGTH_SHORT).show();

        StadiumDTO stadiumDTO = stadiumDTOS.get(position);
        CourtsDTO[] courtsDTOS = stadiumDTO.courts.toArray(new CourtsDTO[0]);
        String time = DurationConverter.convertDuration(String.valueOf(stadiumDTO.openTime).toString()) + " - " + DurationConverter.convertDuration(stadiumDTO.closeTime.toString());
        String sportType = "";
        StadiumImagesDTO[] stadiumImagesDTO = stadiumDTO.stadiumImages.toArray(new StadiumImagesDTO[0]);
        switch (courtsDTOS.length > 0 ? courtsDTOS[0].sportType : ""){
            case "Bóng đá sân 7":
                sportType = "Bóng đá";
                break;
            case "Bóng đá sân 5":
                sportType = "Bóng đá";
                break;
            case "Bóng đá sân 11":
                sportType = "Bóng đá";
                break;
            default:
                sportType = courtsDTOS.length > 0 ? courtsDTOS[0].sportType : "";
        }

//        holder.stadiumId.setText(stadiumDTO.id);
        holder.stadiumName.setText(stadiumDTO.name);
        holder.stadiumAddress.setText(stadiumDTO.address);
        holder.stadiumTime.setText(time);
        holder.stadiumSportType.setText(sportType);
        Glide.with(this.context).load(ImageUtils.getFullUrl(stadiumImagesDTO.length > 0 ? "img/" + stadiumImagesDTO[0].imageUrl : "")).centerCrop().into(holder.stadiumImages);
        holder.book_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Lấy giá trị ID trực tiếp từ đối tượng dữ liệu (Cách đúng đắn)
                int stadiumId = stadiumDTO.getId(); // Giả sử MyDataModel có phương thức getId()


                // Mở Activity mới

            }
        });
        holder.listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Lấy giá trị ID trực tiếp từ đối tượng dữ liệu (Cách đúng đắn)
                int stadiumId = stadiumDTO.getId(); // Giả sử MyDataModel có phương thức getId()

                if (listener != null) {
                    listener.onItemClick(stadiumId);
                }
                // Mở Activity mới

            }
        });
    }

    @Override
    public int getItemCount() {
        if (stadiumDTOS == null) {
            return 0;
        }
        return stadiumDTOS.size();
    }

    public class StadiumViewHolder extends RecyclerView.ViewHolder {

        public TextView stadiumName;
        public TextView stadiumAddress;
        public TextView stadiumTime;
        public TextView stadiumSportType;
        public ImageView stadiumImages;
        public Button book_button;
        public ConstraintLayout listItem;

        public StadiumViewHolder(@NonNull View itemView) {
            super(itemView);

            stadiumName = itemView.findViewById(R.id.stadium_name);
            stadiumAddress = itemView.findViewById(R.id.stadium_address);
            stadiumTime = itemView.findViewById(R.id.stadium_time);
            stadiumSportType = itemView.findViewById(R.id.stadium_sportType);
            stadiumImages = itemView.findViewById(R.id.stadium_image);
            book_button = itemView.findViewById(R.id.book_button);
            listItem = itemView.findViewById(R.id.list_item);

        }
    }
}
