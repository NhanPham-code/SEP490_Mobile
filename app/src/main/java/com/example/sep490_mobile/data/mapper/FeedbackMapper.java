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

    public static Feedback toDomain(FeedbackDto dto) {
        if (dto == null) {
            return null;
        }

        Date createdAtDate = null;
        String dateString = dto.getCreatedAt();

        if (dateString != null && !dateString.isEmpty()) {
            // API trả về tới 7 chữ số cho phần thập phân, nhưng Java SimpleDateFormat chỉ xử lý được 3 (milliseconds).
            // Chúng ta cần cắt chuỗi để lấy 3 chữ số đầu tiên sau dấu chấm.
            if (dateString.contains(".")) {
                int dotIndex = dateString.indexOf('.');
                if (dateString.length() > dotIndex + 4) {
                    dateString = dateString.substring(0, dotIndex + 4);
                }
            }

            // Bỏ 'Z' ở cuối nếu có, vì TimeZone đã được set là UTC
            if (dateString.endsWith("Z")) {
                dateString = dateString.substring(0, dateString.length() - 1);
            }

            // Thử parse với định dạng có milliseconds
            SimpleDateFormat formatWithMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
            formatWithMillis.setTimeZone(TimeZone.getTimeZone("UTC"));

            try {
                createdAtDate = formatWithMillis.parse(dateString);
            } catch (ParseException e) {
                // Nếu lỗi, thử lại với định dạng không có milliseconds
                try {
                    SimpleDateFormat fallbackFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                    fallbackFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    createdAtDate = fallbackFormat.parse(dateString);
                } catch (ParseException e2) {
                    System.err.println("Error parsing date: " + dto.getCreatedAt());
                    createdAtDate = null; // Tốt nhất là trả về null nếu không thể parse
                }
            }
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

    public static List<Feedback> toDomain(List<FeedbackDto> dtoList) {
        if (dtoList == null) {
            return new ArrayList<>();
        }
        List<Feedback> domainList = new ArrayList<>();
        for (FeedbackDto dto : dtoList) {
            Feedback feedback = toDomain(dto);
            if (feedback != null) {
                domainList.add(feedback);
            }
        }
        return domainList;
    }
}