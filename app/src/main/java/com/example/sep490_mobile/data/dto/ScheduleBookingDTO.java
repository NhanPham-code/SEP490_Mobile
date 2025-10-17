package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.List;

public class ScheduleBookingDTO implements Parcelable {

    @SerializedName("Id")
    private int id;

    @SerializedName("UserId")
    private int userId;

    @SerializedName("CreatedById")
    private int createdById;

    @SerializedName("Status")
    private String status;

    @SerializedName("Date")
    private String date;

    @SerializedName("TotalPrice")
    private double totalPrice;

    @SerializedName("OriginalPrice")
    private double originalPrice;

    @SerializedName("Note")
    private String note;

    @SerializedName("PaymentMethod")
    private String paymentMethod;

    @SerializedName("CreatedAt")
    private String createdAt;

    @SerializedName("UpdatedAt")
    private String updatedAt;

    // Key này cũng cần là PascalCase
    @SerializedName("BookingDetails")
    private List<ScheduleBookingDetailDTO> bookingDetails;

    @SerializedName("StadiumId")
    private int stadiumId;

    private transient String stadiumName;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCreatedById() {
        return createdById;
    }

    public void setCreatedById(int createdById) {
        this.createdById = createdById;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(double originalPrice) {
        this.originalPrice = originalPrice;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<ScheduleBookingDetailDTO> getBookingDetails() {
        return bookingDetails;
    }

    public void setBookingDetails(List<ScheduleBookingDetailDTO> bookingDetails) {
        this.bookingDetails = bookingDetails;
    }

    public int getStadiumId() {
        return stadiumId;
    }

    public void setStadiumId(int stadiumId) {
        this.stadiumId = stadiumId;
    }

    public String getStadiumName() {
        return stadiumName;
    }

    public void setStadiumName(String stadiumName) {
        this.stadiumName = stadiumName;
    }

    protected ScheduleBookingDTO(Parcel in) {
        // Đọc các trường theo thứ tự đã ghi
        id = in.readInt();
        userId = in.readInt();
        createdById = in.readInt();
        status = in.readString();
        date = in.readString();
        totalPrice = in.readDouble();
        originalPrice = in.readDouble();
        note = in.readString();
        paymentMethod = in.readString();
        createdAt = in.readString();
        updatedAt = in.readString();
        stadiumId = in.readInt();
        stadiumName = in.readString();
        bookingDetails = in.createTypedArrayList(ScheduleBookingDetailDTO.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Ghi các trường vào parcel
        dest.writeInt(id);
        dest.writeInt(userId);
        dest.writeInt(createdById);
        dest.writeString(status);
        dest.writeString(date);
        dest.writeDouble(totalPrice);
        dest.writeDouble(originalPrice);
        dest.writeString(note);
        dest.writeString(paymentMethod);
        dest.writeString(createdAt);
        dest.writeString(updatedAt);
        dest.writeInt(stadiumId);
        dest.writeString(stadiumName);
        dest.writeTypedList(bookingDetails);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ScheduleBookingDTO> CREATOR = new Creator<ScheduleBookingDTO>() {
        @Override
        public ScheduleBookingDTO createFromParcel(Parcel in) {
            return new ScheduleBookingDTO(in);
        }

        @Override
        public ScheduleBookingDTO[] newArray(int size) {
            return new ScheduleBookingDTO[size];
        }
    };
}