package com.example.sep490_mobile.data.dto.booking;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class BookingViewDTO implements Parcelable {
    // --- CÁC TRƯỜNG CHUNG ĐƯỢC ĐỒNG BỘ HÓA ---
    @SerializedName("Id") private int id;
    @SerializedName("Status") private String status;
    @SerializedName("Date") private Date date; // java.util.Date cho UI
    @SerializedName("OriginalPrice") private Double originalPrice;
    @SerializedName("TotalPrice") private Double totalPrice;
    @SerializedName("DiscountId") private Integer discountId;
    @SerializedName("StadiumId") private int stadiumId;
    @SerializedName("MonthlyBookingId") private Integer monthlyBookingId;

    // --- CÁC TRƯỜNG BỔ SUNG ĐỂ HỢP NHẤT VÀ PHỤC VỤ LOGIC CẬP NHẬT ---
    @SerializedName("UserId") private int userId; // Thêm vào để đồng bộ
    @SerializedName("Note") private String note; // Thêm vào để đồng bộ
    @SerializedName("PaymentMethod") private String paymentMethod; // Thêm vào để đồng bộ
    @SerializedName("CreatedAt") private String createdAt; // Thêm vào để đồng bộ
    @SerializedName("UpdatedAt") private String updatedAt; // Thêm vào để đồng bộ

    // --- TRƯỜNG ĐẶC THÙ UI/LOCAL ---
    private String stadiumName;
    @SerializedName("BookingDetails") private List<BookingDetailViewModelDTO> bookingDetails;

    // CONSTRUCTOR CHO PARCELABLE
    protected BookingViewDTO(Parcel in) {
        id = in.readInt();
        status = in.readString();
        long tmpDate = in.readLong();
        date = tmpDate == -1 ? null : new Date(tmpDate);
        if (in.readByte() == 0) { originalPrice = null; } else { originalPrice = in.readDouble(); }
        if (in.readByte() == 0) { totalPrice = null; } else { totalPrice = in.readDouble(); }
        if (in.readByte() == 0) { discountId = null; } else { discountId = in.readInt(); }
        stadiumId = in.readInt();
        stadiumName = in.readString();
        if (in.readByte() == 0) { monthlyBookingId = null; } else { monthlyBookingId = in.readInt(); }
        bookingDetails = in.createTypedArrayList(BookingDetailViewModelDTO.CREATOR);

        // ĐỌC CÁC TRƯỜNG MỚI BỔ SUNG
        userId = in.readInt();
        note = in.readString();
        paymentMethod = in.readString();
        createdAt = in.readString();
        updatedAt = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(status);
        dest.writeLong(date != null ? date.getTime() : -1);
        if (originalPrice == null) { dest.writeByte((byte) 0); } else { dest.writeByte((byte) 1); dest.writeDouble(originalPrice); }
        if (totalPrice == null) { dest.writeByte((byte) 0); } else { dest.writeByte((byte) 1); dest.writeDouble(totalPrice); }
        if (discountId == null) { dest.writeByte((byte) 0); } else { dest.writeByte((byte) 1); dest.writeInt(discountId); }
        dest.writeInt(stadiumId);
        dest.writeString(stadiumName);
        if (monthlyBookingId == null) { dest.writeByte((byte) 0); } else { dest.writeByte((byte) 1); dest.writeInt(monthlyBookingId); }
        dest.writeTypedList(bookingDetails);

        // GHI CÁC TRƯỜNG MỚI BỔ SUNG
        dest.writeInt(userId);
        dest.writeString(note);
        dest.writeString(paymentMethod);
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<BookingViewDTO> CREATOR = new Creator<BookingViewDTO>() {
        @Override
        public BookingViewDTO createFromParcel(Parcel in) { return new BookingViewDTO(in); }
        @Override
        public BookingViewDTO[] newArray(int size) { return new BookingViewDTO[size]; }
    };

    // --- GETTERS (Cần cho logic cập nhật và UI) ---
    public int getId() { return id; }
    public String getStatus() { return status; }
    public Date getDate() { return date; } // java.util.Date
    public Double getOriginalPrice() { return originalPrice; }
    public Double getTotalPrice() { return totalPrice; }
    public Integer getDiscountId() { return discountId; }
    public String getStadiumName() { return stadiumName; }
    public int getStadiumId() { return stadiumId; }
    public Integer getMonthlyBookingId() { return monthlyBookingId; }
    public List<BookingDetailViewModelDTO> getBookingDetails() { return bookingDetails; }
    public int getUserId() { return userId; } // GETTER BỔ SUNG
    public String getNote() { return note; } // GETTER BỔ SUNG
    public String getPaymentMethod() { return paymentMethod; } // GETTER BỔ SUNG
    public String getCreatedAt() { return createdAt; } // GETTER BỔ SUNG
    public String getUpdatedAt() { return updatedAt; } // GETTER BỔ SUNG

    // --- SETTERS ---
    public void setStadiumName(String stadiumName) { this.stadiumName = stadiumName; }
    // Có thể thêm setters cho các trường khác nếu cần thiết.
}