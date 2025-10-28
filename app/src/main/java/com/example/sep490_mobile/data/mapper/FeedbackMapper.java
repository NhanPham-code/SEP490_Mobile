package com.example.sep490_mobile.data.mapper;

import com.example.sep490_mobile.data.dto.FeedbackDto;
import com.example.sep490_mobile.model.Feedback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FeedbackMapper {

    public static Feedback toDomain(FeedbackDto dto) {
        if (dto == null) return null;

        Date createdAtDate = null;
        String dateString = dto.getCreatedAt();
        if (dateString != null && !dateString.isEmpty()) {
            if (dateString.contains(".")) {
                int dotIndex = dateString.indexOf('.');
                if (dateString.length() > dotIndex + 4) {
                    dateString = dateString.substring(0, dotIndex + 4);
                }
            }
            if (dateString.endsWith("Z")) {
                dateString = dateString.substring(0, dateString.length() - 1);
            }
            SimpleDateFormat formatWithMillis = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
            formatWithMillis.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                createdAtDate = formatWithMillis.parse(dateString);
            } catch (ParseException e) {
                try {
                    SimpleDateFormat fallbackFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                    fallbackFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    createdAtDate = fallbackFormat.parse(dateString);
                } catch (ParseException e2) {
                    createdAtDate = null;
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
        if (dtoList == null) return new ArrayList<>();
        List<Feedback> domainList = new ArrayList<>();
        for (FeedbackDto dto : dtoList) {
            Feedback feedback = toDomain(dto);
            if (feedback != null) domainList.add(feedback);
        }
        return domainList;
    }

    // ⭐ Hàm group by stadiumId và tính trung bình
    public static Map<Integer, Float> calculateAverageRatingByStadium(List<FeedbackDto> dtoList) {
        Map<Integer, Integer> totalRating = new HashMap<>();
        Map<Integer, Integer> countRating = new HashMap<>();
        if (dtoList != null) {
            for (FeedbackDto dto : dtoList) {
                int stadiumId = dto.getStadiumId();
                int rating = dto.getRating();
                totalRating.put(stadiumId, totalRating.getOrDefault(stadiumId, 0) + rating);
                countRating.put(stadiumId, countRating.getOrDefault(stadiumId, 0) + 1);
            }
        }
        Map<Integer, Float> averageRatingMap = new HashMap<>();
        for (int stadiumId : totalRating.keySet()) {
            float avg = (float) totalRating.get(stadiumId) / countRating.get(stadiumId);
            averageRatingMap.put(stadiumId, avg);
        }
        return averageRatingMap;
    }
}