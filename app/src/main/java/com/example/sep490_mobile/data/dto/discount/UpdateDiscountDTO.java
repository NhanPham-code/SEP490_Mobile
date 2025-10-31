package com.example.sep490_mobile.data.dto.discount; // (Giữ package của ông)

import com.google.gson.annotations.SerializedName;
import java.util.List;

// DTO này phải khớp với UpdateDiscountDTO của C# backend
public class UpdateDiscountDTO {

    @SerializedName("Id")
    private int id;

    @SerializedName("Code")
    private String code;

    @SerializedName("Description")
    private String description;

    @SerializedName("PercentValue")
    private double percentValue;

    @SerializedName("MaxDiscountAmount")
    private double maxDiscountAmount; // Dùng double cho an toàn với Gson

    @SerializedName("MinOrderAmount")
    private double minOrderAmount; // Dùng double

    @SerializedName("StartDate")
    private String startDate; // "yyyy-MM-ddTHH:mm:ss"

    @SerializedName("EndDate")
    private String endDate; // "yyyy-MM-ddTHH:mm:ss"

    @SerializedName("CodeType")
    private String codeType;

    @SerializedName("IsActive")
    private boolean isActive;

    @SerializedName("TargetUserId")
    private String targetUserId;

    @SerializedName("StadiumIds")
    private List<Integer> stadiumIds;

    // Constructor để tạo DTO update từ ReadDiscountDTO
    public UpdateDiscountDTO(ReadDiscountDTO readDTO, boolean newActiveState) {
        this.id = readDTO.getId();
        this.code = readDTO.getCode();
        this.description = readDTO.getDescription();
        this.percentValue = readDTO.getPercentValue();
        this.maxDiscountAmount = readDTO.getMaxDiscountAmount();
        this.minOrderAmount = readDTO.getMinOrderAmount();
        this.startDate = readDTO.getStartDate(); // Giữ nguyên chuỗi ISO
        this.endDate = readDTO.getEndDate(); // Giữ nguyên chuỗi ISO
        this.codeType = readDTO.getCodeType();
        this.isActive = newActiveState; // <<< ĐÂY LÀ GIÁ TRỊ MỚI
        this.targetUserId = readDTO.getTargetUserId();
        this.stadiumIds = readDTO.getStadiumIds();
    }

    // Getters (Nếu cần)
    public int getId() { return id; }
    public boolean isActive() { return isActive; }
    // ... (Thêm các getter khác nếu cần)
}