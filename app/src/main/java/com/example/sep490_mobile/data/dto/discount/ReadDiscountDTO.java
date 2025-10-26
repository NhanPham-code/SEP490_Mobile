package com.example.sep490_mobile.data.dto.discount; // Adjust package name if needed

import android.os.Parcel;
import android.os.Parcelable; // Import Parcelable
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList; // Import ArrayList
import java.util.List;

// Implement Parcelable
public class ReadDiscountDTO implements Parcelable {

    @SerializedName("Id")
    private int id;

    @SerializedName("Code")
    private String code;

    @SerializedName("Description")
    private String description;

    @SerializedName("PercentValue")
    private double percentValue;

    @SerializedName("MaxDiscountAmount")
    private double maxDiscountAmount;

    @SerializedName("MinOrderAmount")
    private double minOrderAmount;

    @SerializedName("StartDate")
    private String startDate;

    @SerializedName("EndDate")
    private String endDate;

    @SerializedName("CodeType")
    private String codeType;

    @SerializedName("IsActive")
    private boolean isActive;

    @SerializedName("TargetUserId")
    private String targetUserId;

    @SerializedName("StadiumIds")
    private List<Integer> stadiumIds;

    // Still transient for Gson, but needs Parcelable handling
    private transient List<String> stadiumNames;

    // Parcelable Constructor
    protected ReadDiscountDTO(Parcel in) {
        id = in.readInt();
        code = in.readString();
        description = in.readString();
        percentValue = in.readDouble();
        maxDiscountAmount = in.readDouble();
        minOrderAmount = in.readDouble();
        startDate = in.readString();
        endDate = in.readString();
        codeType = in.readString();
        isActive = in.readByte() != 0; // Read boolean as byte
        targetUserId = in.readString();
        // Read List<Integer>
        byte stadiumIdsPresent = in.readByte();
        if (stadiumIdsPresent == 1) {
            stadiumIds = new ArrayList<>();
            in.readList(stadiumIds, Integer.class.getClassLoader());
        } else {
            stadiumIds = null;
        }
        // Read List<String> for stadiumNames
        byte stadiumNamesPresent = in.readByte();
        if (stadiumNamesPresent == 1) {
            stadiumNames = new ArrayList<>();
            in.readStringList(stadiumNames);
        } else {
            stadiumNames = null;
        }
    }

    // Parcelable writeToParcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(code);
        dest.writeString(description);
        dest.writeDouble(percentValue);
        dest.writeDouble(maxDiscountAmount);
        dest.writeDouble(minOrderAmount);
        dest.writeString(startDate);
        dest.writeString(endDate);
        dest.writeString(codeType);
        dest.writeByte((byte) (isActive ? 1 : 0)); // Write boolean as byte
        dest.writeString(targetUserId);
        // Write List<Integer>
        if (stadiumIds != null) {
            dest.writeByte((byte) 1);
            dest.writeList(stadiumIds);
        } else {
            dest.writeByte((byte) 0);
        }
        // Write List<String> for stadiumNames
        if (stadiumNames != null) {
            dest.writeByte((byte) 1);
            dest.writeStringList(stadiumNames);
        } else {
            dest.writeByte((byte) 0);
        }
    }

    // Parcelable describeContents
    @Override
    public int describeContents() {
        return 0;
    }

    // Parcelable CREATOR
    public static final Creator<ReadDiscountDTO> CREATOR = new Creator<ReadDiscountDTO>() {
        @Override
        public ReadDiscountDTO createFromParcel(Parcel in) {
            return new ReadDiscountDTO(in);
        }

        @Override
        public ReadDiscountDTO[] newArray(int size) {
            return new ReadDiscountDTO[size];
        }
    };

    // Getters
    public int getId() { return id; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public double getPercentValue() { return percentValue; }
    public double getMaxDiscountAmount() { return maxDiscountAmount; }
    public double getMinOrderAmount() { return minOrderAmount; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getCodeType() { return codeType; }
    public boolean isActive() { return isActive; }
    public String getTargetUserId() { return targetUserId; }
    public List<Integer> getStadiumIds() { return stadiumIds; }
    public List<String> getStadiumNames() { return stadiumNames; }

    // Setter for stadiumNames (used by ViewModel)
    public void setStadiumNames(List<String> stadiumNames) { this.stadiumNames = stadiumNames; }
}