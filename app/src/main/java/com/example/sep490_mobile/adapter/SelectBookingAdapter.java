package com.example.sep490_mobile.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sep490_mobile.data.dto.CourtsDTO;
import com.example.sep490_mobile.data.dto.SelectBookingDTO;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.data.dto.booking.BookingViewDTO;
import com.example.sep490_mobile.utils.DurationConverter;
import com.example.sep490_mobile.utils.PriceFormatter;
import com.google.android.material.button.MaterialButton;

import android.widget.TextView;

import com.example.sep490_mobile.R; // Assuming your resources are in the default R class
import com.example.sep490_mobile.interfaces.OnItemClickListener;

import java.util.Dictionary;
import java.util.List;

public class SelectBookingAdapter extends RecyclerView.Adapter<SelectBookingAdapter.SelectBookingViewHolder> {

    private List<BookingViewDTO> bookingViewDTOS;
    private Dictionary<Integer, StadiumDTO> stadiumDTODictionary;
    private Context context;
    private OnItemClickListener listener;

    public SelectBookingAdapter(Context context){
        this.context = context;
    }

    public void setSelectBookingList(SelectBookingDTO selectBookingDTO, OnItemClickListener listener){
        this.bookingViewDTOS = selectBookingDTO.getBookingReadDTOS();
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
        // 1. Kiểm tra an toàn: Nếu bookingReadDTOS == null, không làm gì cả (đảm bảo bởi getItemCount() nhưng vẫn giữ để an toàn)
        if (bookingViewDTOS == null || stadiumDTODictionary == null) return;

        BookingViewDTO booking = bookingViewDTOS.get(position);

        // --- 2. Set Header Information ---
        // Tối ưu: Đảm bảo không bị null và thêm prefix
        holder.tvBookingId.setText("Mã Booking: " + String.valueOf(booking.getId()));
        String status = booking.getStatus();
        holder.tvBookingStatus.setText(status != null ? status : "Không rõ trạng thái");

        // Simple status-based background/text color logic
        if ("Đã xác nhận".equalsIgnoreCase(status)) {
            holder.tvBookingStatus.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
            holder.tvBookingStatus.setTextColor(Color.WHITE);
        } else if ("Đang chờ".equalsIgnoreCase(status)) {
            holder.tvBookingStatus.setBackgroundColor(Color.parseColor("#FF9800")); // Amber
            holder.tvBookingStatus.setTextColor(Color.BLACK);
        } else {
            holder.tvBookingStatus.setBackgroundColor(Color.parseColor("#9E9E9E")); // Gray default
            holder.tvBookingStatus.setTextColor(Color.WHITE);
        }

        // --- 3. Xử lý Dữ liệu Sân (Stadium Data) VÀ SỬA LỖI NullPointerException (LỖI 1) ---
        // BẮT BUỘC: Lấy và kiểm tra đối tượng Stadium trước khi truy cập
        StadiumDTO stadiumDTO = stadiumDTODictionary.get(booking.getStadiumId());

        // Set Body details
        holder.tvStadiumName.setText(booking.getStadiumName());

        if (stadiumDTO != null) {
            // Địa chỉ
            holder.tvAddress.setText(stadiumDTO.getAddress());

            // Loại sân (Sport Type)
            // SỬA LỖI 2: Xử lý IndexOutOfBoundsException nếu danh sách courts rỗng
            CourtsDTO[] courtsDTOS = stadiumDTO.getCourts().toArray(new CourtsDTO[0]);
            if (courtsDTOS != null && courtsDTOS.length > 0) {
                holder.tvSportType.setText(courtsDTOS[0].getSportType());
            } else {
                holder.tvSportType.setText("Chưa rõ loại hình");
            }
        } else {
            // Trường hợp không tìm thấy thông tin sân (giá trị an toàn)
            holder.tvAddress.setText("Địa chỉ không khả dụng");
            holder.tvSportType.setText("Loại hình không khả dụng");
        }
        holder.tvStadiumName.setText(stadiumDTO.getName());
        // Format date: Điền giá trị thực tế (giả sử getBookingDate() trả về String)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.tvBookingDate.setText("Ngày chơi: " + ( booking.getBookingDetails().get(0).getStartTime() != null ? DurationConverter.convertCustomToReadable(booking.getBookingDetails().get(0).getStartTime().toString(), "dd/MM/yyyy") : "N/A"));
        }

        // Tối ưu: Format price as a currency string
        try {

            String price = PriceFormatter.formatPriceDouble(booking.getTotalPrice());
            holder.tvPrice.setText(price);
        } catch (Exception e) {
            // Fallback nếu có lỗi định dạng
            holder.tvPrice.setText(booking.getTotalPrice() + " đ");
        }

        // --- 4. Set up click listener ---
        holder.btnSelectBooking.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingViewDTOS != null ? bookingViewDTOS.size() : 0;
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