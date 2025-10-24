// Đường dẫn: data/dto/ReadCourtRelationDTO.java
package com.example.sep490_mobile.data.dto;

import com.google.gson.annotations.SerializedName;

// Lớp này tương đương với ReadCourtRelationDTO.cs
public class ReadCourtRelationDTO {

    @SerializedName("id")
    private int id;

    @SerializedName("parentCourtId")
    private int parentCourtId;

    @SerializedName("childCourtId")
    private int childCourtId;

    // Các đối tượng lồng nhau, Gson sẽ tự động ánh xạ nếu JSON trả về có cấu trúc tương ứng
    @SerializedName("parentCourt")
    private CourtsDTO parentCourt;

    @SerializedName("childCourt")
    private CourtsDTO childCourt;


    // --- Getters ---

    public int getId() {
        return id;
    }

    public int getParentCourtId() {
        return parentCourtId;
    }

    public int getChildCourtId() {
        return childCourtId;
    }

    public CourtsDTO getParentCourt() {
        return parentCourt;
    }

    public CourtsDTO getChildCourt() {
        return childCourt;
    }
}