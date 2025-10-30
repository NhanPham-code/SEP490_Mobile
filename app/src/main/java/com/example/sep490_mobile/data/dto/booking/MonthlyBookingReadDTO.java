package com.example.sep490_mobile.data.dto.booking;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

public class MonthlyBookingReadDTO implements Parcelable {
    @SerializedName("Id") private int id;
    private String stadiumName;
    @SerializedName("StadiumId") private int stadiumId;

    @SerializedName("OriginalPrice")
    private Double originalPrice;

    @SerializedName("TotalPrice")
    private Double totalPrice;

    @SerializedName("DiscountId")
    private Integer discountId;

    @SerializedName("Status") private String status;
    @SerializedName("StartTime") private String startTime;
    @SerializedName("EndTime") private String endTime;
    @SerializedName("Month") private int month;
    @SerializedName("Year") private int year;

    // === BỔ SUNG 2 TRƯỜNG ĐỂ ĐỒNG BỘ VỚI UPDATE DTO ===
    @SerializedName("PaymentMethod")
    private String paymentMethod;

    @SerializedName("Note")
    private String note;
    // ===================================================

    protected MonthlyBookingReadDTO(Parcel in) {
        id = in.readInt();
        stadiumName = in.readString();
        stadiumId = in.readInt();
        if (in.readByte() == 0) { originalPrice = null; } else { originalPrice = in.readDouble(); }
        if (in.readByte() == 0) { totalPrice = null; } else { totalPrice = in.readDouble(); }
        if (in.readByte() == 0) { discountId = null; } else { discountId = in.readInt(); }
        status = in.readString();
        startTime = in.readString();
        endTime = in.readString();
        month = in.readInt();
        year = in.readInt();

        // === ĐỌC 2 TRƯỜNG MỚI TỪ PARCEL ===
        paymentMethod = in.readString();
        note = in.readString();
        // ====================================
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(stadiumName);
        dest.writeInt(stadiumId);
        if (originalPrice == null) { dest.writeByte((byte) 0); } else { dest.writeByte((byte) 1); dest.writeDouble(originalPrice); }
        if (totalPrice == null) { dest.writeByte((byte) 0); } else { dest.writeByte((byte) 1); dest.writeDouble(totalPrice); }
        if (discountId == null) { dest.writeByte((byte) 0); } else { dest.writeByte((byte) 1); dest.writeInt(discountId); }
        dest.writeString(status);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeInt(month);
        dest.writeInt(year);

        // === GHI 2 TRƯỜNG MỚI VÀO PARCEL ===
        dest.writeString(paymentMethod);
        dest.writeString(note);
        // ===================================
    }


    @Override
    public int describeContents() { return 0; }

    public static final Creator<MonthlyBookingReadDTO> CREATOR = new Creator<MonthlyBookingReadDTO>() {
        @Override
        public MonthlyBookingReadDTO createFromParcel(Parcel in) { return new MonthlyBookingReadDTO(in); }
        @Override
        public MonthlyBookingReadDTO[] newArray(int size) { return new MonthlyBookingReadDTO[size]; }
    };


    public Double getOriginalPrice() { return originalPrice; }
    public Integer getDiscountId() { return discountId; }

    public int getId() { return id; }
    public String getStadiumName() { return stadiumName; }
    public int getStadiumId() { return stadiumId; }
    public Double getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public int getMonth() { return month; }
    public int getYear() { return year; }
    public void setStadiumName(String stadiumName) { this.stadiumName = stadiumName; }

    // === GETTERS CHO 2 TRƯỜNG MỚI ===
    public String getPaymentMethod() { return paymentMethod; }
    public String getNote() { return note; }
    // ================================
}