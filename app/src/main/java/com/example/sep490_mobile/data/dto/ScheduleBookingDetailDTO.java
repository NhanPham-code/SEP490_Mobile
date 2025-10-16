package com.example.sep490_mobile.data.dto;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class ScheduleBookingDetailDTO implements Parcelable {

    // SỬA LẠI CÁC ANNOTATION SAU
    @SerializedName("Id")
    private int id;

    @SerializedName("BookingId")
    private int bookingId;

    @SerializedName("CourtId")
    private int courtId;

    @SerializedName("StartTime")
    private String startTime;

    @SerializedName("EndTime")
    private String endTime;

    // Trường transient này giữ nguyên
    private transient String courtName;
    private transient double pricePerHour;

    // Getters và Setters (giữ nguyên)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getCourtId() {
        return courtId;
    }

    public void setCourtId(int courtId) {
        this.courtId = courtId;
    }

    public String getCourtName() {
        return courtName;
    }

    public void setCourtName(String courtName) {
        this.courtName = courtName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public double getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(double pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    protected ScheduleBookingDetailDTO(Parcel in) {
        id = in.readInt();
        bookingId = in.readInt();
        courtId = in.readInt();
        startTime = in.readString();
        endTime = in.readString();
        courtName = in.readString();
        pricePerHour = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(bookingId);
        dest.writeInt(courtId);
        dest.writeString(startTime);
        dest.writeString(endTime);
        dest.writeString(courtName);
        dest.writeDouble(pricePerHour);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ScheduleBookingDetailDTO> CREATOR = new Creator<ScheduleBookingDetailDTO>() {
        @Override
        public ScheduleBookingDetailDTO createFromParcel(Parcel in) {
            return new ScheduleBookingDetailDTO(in);
        }

        @Override
        public ScheduleBookingDetailDTO[] newArray(int size) {
            return new ScheduleBookingDetailDTO[size];
        }
    };
}