package com.example.sep490_mobile.data.dto;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

public class SelectedCourtInfo implements Parcelable {
    private int courtId;
    private List<String> times;

    public SelectedCourtInfo(int courtId, List<String> times) {
        this.courtId = courtId;
        this.times = times;
    }

    // --- Bắt đầu phần code của Parcelable ---

    protected SelectedCourtInfo(Parcel in) {
        courtId = in.readInt();
        times = in.createStringArrayList();
    }

    public static final Creator<SelectedCourtInfo> CREATOR = new Creator<SelectedCourtInfo>() {
        @Override
        public SelectedCourtInfo createFromParcel(Parcel in) {
            return new SelectedCourtInfo(in);
        }

        @Override
        public SelectedCourtInfo[] newArray(int size) {
            return new SelectedCourtInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(courtId);
        dest.writeStringList(times);
    }

    // --- Kết thúc phần code của Parcelable ---


    // --- Getters ---
    public int getCourtId() {
        return courtId;
    }

    public List<String> getTimes() {
        return times;
    }

    public void setCourtId(int courtId) {
        this.courtId = courtId;
    }

    public void setTimes(List<String> times) {
        this.times = times;
    }
}