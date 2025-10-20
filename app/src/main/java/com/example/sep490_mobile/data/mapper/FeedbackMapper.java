package com.example.sep490_mobile.data.mapper;

import com.example.sep490_mobile.data.dto.FeedbackDto;
import com.example.sep490_mobile.model.Feedback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class FeedbackMapper {

    // Chuyển một DTO thành một Domain Model
    public static Feedback toDomain(FeedbackDto dto) {
        if (dto == null) {
            return null;
        }

        // Định dạng này khớp với DateTime.UtcNow của .NET
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date createdAtDate;
        try {
            createdAtDate = format.parse(dto.getCreatedAt());
        } catch (ParseException e) {
            e.printStackTrace();
            createdAtDate = new Date(); // Giá trị mặc định nếu parse lỗi
        }

        return new Feedback(
                dto.getId(),
                dto.getUserId(),
                dto.getStadiumId(),
                dto.getRating(),
                dto.getComment(),
                dto.getImagePath(),
                createdAtDate
        );
    }

    // Chuyển một danh sách DTO thành danh sách Domain Model
    public static List<Feedback> toDomain(List<FeedbackDto> dtoList) {
        if (dtoList == null) {
            return new ArrayList<>();
        }
        List<Feedback> domainList = new ArrayList<>();
        for (FeedbackDto dto : dtoList) {
            domainList.add(toDomain(dto));
        }
        return domainList;
    }
}