package com.example.sep490_mobile.utils;

public class ImageUtils {
    private static final String BASE_URL = "https://10.0.2.2:7136";

    public static String getFullUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return null;
        }
        // Xử lý nếu path có dấu / ở đầu
        if (relativePath.startsWith("/")) {
            return BASE_URL + relativePath;
        }
        return BASE_URL + "/" + relativePath;
    }
}
