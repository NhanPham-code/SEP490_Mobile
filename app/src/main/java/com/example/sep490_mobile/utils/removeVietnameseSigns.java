package com.example.sep490_mobile.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class removeVietnameseSigns {
    public static String removeVietnameseSigns(String str) {
        String temp = Normalizer.normalize(str, Normalizer.Form.NFD);

        // 2. Loại bỏ tất cả các ký tự dấu (diacritical marks)
        // Dãy ký tự [\p{Mn}] đại diện cho tất cả các dấu phụ (Combining Diacritical Marks)
        Pattern pattern = Pattern.compile("\\p{Mn}");
        String result = pattern.matcher(temp).replaceAll("");

        // 3. Xử lý ký tự 'Đ' và 'đ'
        result = result.replace('Đ', 'D').replace('đ', 'd');

        // 4. Chuyển về chữ thường (optional)
        return result.toLowerCase();
    }
}
