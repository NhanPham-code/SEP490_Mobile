package com.example.sep490_mobile.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar; // Thêm import này
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sep490_mobile.CustomPlace;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.CourtsDTO;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.data.dto.StadiumImagesDTO;
import com.example.sep490_mobile.interfaces.OnItemClickListener;
import com.example.sep490_mobile.interfaces.OnFavoriteClickListener;
import com.example.sep490_mobile.ui.home.BookingOptionsBottomSheet;
import com.example.sep490_mobile.utils.DurationConverter;
import com.example.sep490_mobile.utils.ImageUtils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map; // Thêm import này

public class StadiumAdapter extends RecyclerView.Adapter<StadiumAdapter.StadiumViewHolder> {

    private List<StadiumDTO> stadiumDTOS = new ArrayList<>();
    private Context context;
    private OnItemClickListener listener;

    private Set<Integer> favoriteIds = new HashSet<>();
    private OnFavoriteClickListener favoriteClickListener;

    // ⭐ Thêm biến này để lưu Map<stadiumId, averageRating>
    private Map<Integer, Float> stadiumAverageRatings = null;

    // Constructor mới để truyền thêm average ratings nếu cần
    public StadiumAdapter(Context context, OnItemClickListener listener, OnFavoriteClickListener favoriteClickListener, Map<Integer, Float> stadiumAverageRatings) {
        this.context = context;
        this.listener = listener;
        this.favoriteClickListener = favoriteClickListener;
        this.stadiumAverageRatings = stadiumAverageRatings;
    }

    // Nếu muốn giữ constructor cũ
    public StadiumAdapter(Context context) {
        this.context = context;
    }
    public StadiumAdapter(Context context, OnItemClickListener listener, OnFavoriteClickListener favoriteClickListener) {
        this.context = context;
        this.listener = listener;
        this.favoriteClickListener = favoriteClickListener;
    }

    public void setStadiumDTOS(List<StadiumDTO> stadiumDTOS, OnItemClickListener listener){
        this.stadiumDTOS = stadiumDTOS;
        this.listener = listener;
        notifyDataSetChanged();
    }

    public void setData(List<StadiumDTO> newStadiums, Set<Integer> newFavoriteIds) {
        this.stadiumDTOS.clear();
        this.stadiumDTOS.addAll(newStadiums);
        this.favoriteIds = newFavoriteIds;
        notifyDataSetChanged();
    }

    public void updateFavoriteIds(Set<Integer> newFavoriteIds) {
        this.favoriteIds = newFavoriteIds;
        notifyDataSetChanged();
    }

    // ⭐ Hàm này để update average ratings từ ngoài truyền vào
    public void setAverageRatings(Map<Integer, Float> stadiumAverageRatings) {
        this.stadiumAverageRatings = stadiumAverageRatings;
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
        String time = DurationConverter.convertDuration(stadiumDTO.openTime) + " - " + DurationConverter.convertDuration(stadiumDTO.closeTime);
        String sportType = "";
        StadiumImagesDTO[] stadiumImagesDTO = stadiumDTO.stadiumImages.toArray(new StadiumImagesDTO[0]);
        switch (courtsDTOS.length > 0 ? courtsDTOS[0].sportType : ""){
            case "Bóng đá sân 7":
            case "Bóng đá sân 5":
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

        // ⭐ Hiển thị số sao trung bình nếu có (nếu không thì để 0)
        float avgRating = 0f;
        if (stadiumAverageRatings != null && stadiumAverageRatings.containsKey(stadiumDTO.getId())) {
            avgRating = stadiumAverageRatings.get(stadiumDTO.getId());
        }
        holder.ratingBar.setRating(avgRating);
        Log.d("RATING", "Sân: " + stadiumDTO.getName() + " - ID: " + stadiumDTO.getId() + " - Rating: " + avgRating);
        // Xử lý icon trái tim yêu thích
        if (favoriteIds.contains(stadiumDTO.getId())) {
            holder.favorite_icon.setImageResource(R.drawable.ic_favorite_filled);
        } else {
            holder.favorite_icon.setImageResource(R.drawable.ic_favorite_border);
        }

        holder.favorite_icon.setOnClickListener(v -> {
            if (favoriteClickListener != null) {
                favoriteClickListener.onFavoriteClick(stadiumDTO.getId());
            }
        });

        // nếu bị khóa hoặc chưa được chấp thuận thì không cho đặt sân
        if(stadiumDTO.isApproved == false){
            holder.listItem.setEnabled(false);
            holder.book_button.setEnabled(false);
            holder.book_button.setText("Sân chưa thể đặt");
            holder.listItem.setBackgroundColor(context.getResources().getColor(R.color.accent_orange));
        } else if (stadiumDTO.isLocked() == true) {
            holder.listItem.setEnabled(false);
            holder.book_button.setEnabled(false);
            holder.book_button.setText("Sân đã bị khóa");
            holder.listItem.setBackgroundColor(context.getResources().getColor(R.color.color_primary));
        }else{
            holder.listItem.setEnabled(true);
            holder.book_button.setEnabled(true);
            holder.book_button.setText("Đặt Sân");
            holder.listItem.setBackgroundColor(context.getResources().getColor(R.color.white));
        }

        holder.book_button.setOnClickListener(v -> {
            if (context instanceof FragmentActivity) {
                FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
                BookingOptionsBottomSheet bottomSheet = BookingOptionsBottomSheet.newInstance(stadiumDTO.getId());
                bottomSheet.show(fragmentManager, bottomSheet.getTag());
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
        holder.ratingBar.setRating(avgRating);
        holder.ratingValue.setText(
                avgRating > 0 ? String.format(Locale.US, "%.2f", avgRating) : "Chưa có"
        );
        holder.map_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int stadiumId = stadiumDTO.getId();
                findPlaceAndOpenMap(stadiumId, stadiumDTO.getName());
            }
        });

        holder.listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(
                            stadiumDTO.getId(),
                            stadiumDTO.getName(),
                            stadiumDTO.getCreatedBy()
                    );
                }
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
                            return;
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
        String uri = "http://maps.google.com/maps?daddr=" + lat + "," + lng + " (" + label + ")";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
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
        public ImageButton map_button;
        public ConstraintLayout listItem;
        public ImageButton favorite_icon;

        public TextView ratingValue;
        public RatingBar ratingBar; // ⭐ Thêm RatingBar cho số sao trung bình

        public StadiumViewHolder(@NonNull View itemView) {
            super(itemView);
            stadiumName = itemView.findViewById(R.id.stadium_name);
            stadiumAddress = itemView.findViewById(R.id.stadium_address);
            stadiumTime = itemView.findViewById(R.id.stadium_time);
            stadiumSportType = itemView.findViewById(R.id.stadium_sportType);
            stadiumImages = itemView.findViewById(R.id.stadium_image);
            book_button = itemView.findViewById(R.id.book_button);
            map_button = itemView.findViewById(R.id.map_button);
            listItem = itemView.findViewById(R.id.list_item);
            favorite_icon = itemView.findViewById(R.id.favorite_button);
            ratingBar = itemView.findViewById(R.id.rating_bar); // ⭐ Ánh xạ RatingBar
            ratingValue = itemView.findViewById(R.id.rating_value);
        }
    }
}