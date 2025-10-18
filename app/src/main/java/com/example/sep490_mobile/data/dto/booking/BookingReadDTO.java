package com.example.sep490_mobile.data.dto.booking;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.Date;
import java.util.List;

public class BookingReadDTO implements Parcelable {
    @SerializedName("Id") private int id;
    @SerializedName("Status") private String status;
    @SerializedName("Date") private Date date;
    @SerializedName("OriginalPrice") private Double originalPrice;
    @SerializedName("TotalPrice") private Double totalPrice;
    @SerializedName("DiscountId") private Integer discountId;
    @SerializedName("StadiumId") private int stadiumId;
    private String stadiumName;
    @SerializedName("MonthlyBookingId") private Integer monthlyBookingId;
    @SerializedName("BookingDetails") private List<BookingDetailViewModelDTO> bookingDetails;

    protected BookingReadDTO(Parcel in) {
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
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<BookingReadDTO> CREATOR = new Creator<BookingReadDTO>() {
        @Override
        public BookingReadDTO createFromParcel(Parcel in) { return new BookingReadDTO(in); }
        @Override
        public BookingReadDTO[] newArray(int size) { return new BookingReadDTO[size]; }
    };

    public int getId() { return id; }
    public String getStatus() { return status; }
    public Date getDate() { return date; }
    public Double getOriginalPrice() { return originalPrice; }
    public Double getTotalPrice() { return totalPrice; }
    public Integer getDiscountId() { return discountId; }
    public String getStadiumName() { return stadiumName; }
    public int getStadiumId() { return stadiumId; }
    public Integer getMonthlyBookingId() { return monthlyBookingId; }
    public List<BookingDetailViewModelDTO> getBookingDetails() { return bookingDetails; }
    public void setStadiumName(String stadiumName) { this.stadiumName = stadiumName; }
}