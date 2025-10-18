package com.example.sep490_mobile.data.dto.booking;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class BookingDetailViewModelDTO implements Parcelable {
    @SerializedName("Id") private int id;
    @SerializedName("CourtId") private int courtId;
    private String courtName;
    @SerializedName("StartTime") private Date startTime;
    @SerializedName("EndTime") private Date endTime;

    protected BookingDetailViewModelDTO(Parcel in) {
        id = in.readInt();
        courtId = in.readInt();
        courtName = in.readString();
        long tmpStartTime = in.readLong();
        startTime = tmpStartTime == -1 ? null : new Date(tmpStartTime);
        long tmpEndTime = in.readLong();
        endTime = tmpEndTime == -1 ? null : new Date(tmpEndTime);
    }

    public static final Creator<BookingDetailViewModelDTO> CREATOR = new Creator<BookingDetailViewModelDTO>() {
        @Override
        public BookingDetailViewModelDTO createFromParcel(Parcel in) { return new BookingDetailViewModelDTO(in); }
        @Override
        public BookingDetailViewModelDTO[] newArray(int size) { return new BookingDetailViewModelDTO[size]; }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(courtId);
        dest.writeString(courtName);
        dest.writeLong(startTime != null ? startTime.getTime() : -1);
        dest.writeLong(endTime != null ? endTime.getTime() : -1);
    }

    public int getId() { return id; }
    public int getCourtId() { return courtId; }
    public String getCourtName() { return courtName; }
    public Date getStartTime() { return startTime; }
    public Date getEndTime() { return endTime; }
    public void setCourtName(String courtName) { this.courtName = courtName; }
}