package com.example.sep490_mobile.data.dto.discount; // (Giữ package của ông)

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class ReadDiscountDTO implements Parcelable {

    // === BẮT ĐẦU SỬA: Dùng "value" (camelCase) và "alternate" (PascalCase) ===

    @SerializedName(value = "id", alternate = "Id")
    private int id;

    @SerializedName(value = "code", alternate = "Code")
    private String code;

    @SerializedName(value = "description", alternate = "Description")
    private String description;

    @SerializedName(value = "percentValue", alternate = "PercentValue")
    private double percentValue;

    @SerializedName(value = "maxDiscountAmount", alternate = "MaxDiscountAmount")
    private double maxDiscountAmount;

    @SerializedName(value = "minOrderAmount", alternate = "MinOrderAmount")
    private double minOrderAmount;

    @SerializedName(value = "startDate", alternate = "StartDate")
    private String startDate;

    @SerializedName(value = "endDate", alternate = "EndDate")
    private String endDate;

    @SerializedName(value = "codeType", alternate = "CodeType") // <<< SỬA CHÍNH
    private String codeType;

    @SerializedName(value = "isActive", alternate = "IsActive")
    private boolean isActive;

    @SerializedName(value = "targetUserId", alternate = "TargetUserId") // <<< SỬA CHÍNH
    private String targetUserId;

    @SerializedName(value = "stadiumIds", alternate = "StadiumIds")
    private List<Integer> stadiumIds;

    // === KẾT THÚC SỬA ===


    private transient List<String> stadiumNames;

    // (Constructor và các hàm Parcelable giữ nguyên, không cần sửa)

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
        isActive = in.readByte() != 0;
        targetUserId = in.readString();

        byte stadiumIdsPresent = in.readByte();
        if (stadiumIdsPresent == 1) {
            stadiumIds = new ArrayList<>();
            in.readList(stadiumIds, Integer.class.getClassLoader());
        } else {
            stadiumIds = null;
        }

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
        dest.writeByte((byte) (isActive ? 1 : 0));
        dest.writeString(targetUserId);

        if (stadiumIds != null) {
            dest.writeByte((byte) 1);
            dest.writeList(stadiumIds);
        } else {
            dest.writeByte((byte) 0);
        }

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

    // Setter
    public void setStadiumNames(List<String> stadiumNames) { this.stadiumNames = stadiumNames; }
}