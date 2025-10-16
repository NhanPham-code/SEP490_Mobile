package com.example.sep490_mobile.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sep490_mobile.CustomPlace;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.CourtsDTO;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.data.dto.StadiumImagesDTO;
import com.example.sep490_mobile.ui.home.OnItemClickListener;
import com.example.sep490_mobile.utils.DurationConverter;
import com.example.sep490_mobile.utils.ImageUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

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

        holder.stadiumName.setText(stadiumDTO.name);
        holder.stadiumAddress.setText(stadiumDTO.address);
        holder.stadiumTime.setText(time);
        holder.stadiumSportType.setText(sportType);
        Glide.with(this.context).load(ImageUtils.getFullUrl(stadiumImagesDTO.length > 0 ? "img/" + stadiumImagesDTO[0].imageUrl : "")).centerCrop().into(holder.stadiumImages);

        holder.book_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int stadiumId = stadiumDTO.getId();
                // Mở Activity mới
            }
        });

        holder.listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int stadiumId = stadiumDTO.getId();
                if (listener != null) {
                    listener.onItemClick(stadiumId);
                }
            }
        });

        // Xử lý sự kiện click cho nút bản đồ
        holder.map_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int stadiumId = stadiumDTO.getId();
                findPlaceAndOpenMap(stadiumId, stadiumDTO.getName());
            }
        });
    }

    private void findPlaceAndOpenMap(int stadiumId, String stadiumName) {
        DatabaseReference placesRef = FirebaseDatabase.getInstance().getReference("customPlaces");
        Query query = placesRef.orderByChild("id").equalTo(stadiumId);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        CustomPlace place = childSnapshot.getValue(CustomPlace.class);
                        if (place != null) {
                            openGoogleMapsDirections(place.lat, place.lng, stadiumName);
                            return; // Tìm thấy và đã xử lý, thoát khỏi vòng lặp
                        }
                    }
                } else {
                    Toast.makeText(context, "Không tìm thấy vị trí của sân trên bản đồ.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Lỗi khi tải dữ liệu vị trí.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void openGoogleMapsDirections(double lat, double lng, String label) {
        // Tạo URI cho Google Maps với tọa độ đích
        String uri = "http://maps.google.com/maps?daddr=" + lat + "," + lng + " (" + label + ")";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        // Chỉ định package để chắc chắn mở bằng Google Maps
        intent.setPackage("com.google.android.apps.maps");
        // Kiểm tra xem có ứng dụng nào có thể xử lý Intent này không
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "Google Maps chưa được cài đặt.", Toast.LENGTH_SHORT).show();
        }
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
        public ImageButton map_button; // Thêm ImageButton cho bản đồ
        public ConstraintLayout listItem;

        public StadiumViewHolder(@NonNull View itemView) {
            super(itemView);

            stadiumName = itemView.findViewById(R.id.stadium_name);
            stadiumAddress = itemView.findViewById(R.id.stadium_address);
            stadiumTime = itemView.findViewById(R.id.stadium_time);
            stadiumSportType = itemView.findViewById(R.id.stadium_sportType);
            stadiumImages = itemView.findViewById(R.id.stadium_image);
            book_button = itemView.findViewById(R.id.book_button);
            map_button = itemView.findViewById(R.id.map_button); // Ánh xạ view
            listItem = itemView.findViewById(R.id.list_item);

        }
    }
}