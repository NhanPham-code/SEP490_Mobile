package com.example.sep490_mobile.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class PriceFormatter {
    /**
     * Định dạng số nguyên thành chuỗi tiền tệ VND có dấu phân cách hàng nghìn.
     * Ví dụ: 200000 -> 200,000
     */
    public static String formatPrice(int price) {
        // Khởi tạo DecimalFormatSymbols dựa trên Locale mặc định
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.JAPAN);

        // Thiết lập dấu phân cách nhóm là dấu chấm (Group Separator)
        symbols.setGroupingSeparator('.');

        // Pattern #,### được sử dụng với symbols đã định nghĩa
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);

        return formatter.format(price);
    }
    public static double parsePriceToDouble(String formattedPrice) {
        // 1. Kiểm tra xem chuỗi có rỗng hoặc null không
        if (formattedPrice == null || formattedPrice.isEmpty()) {
            return 0.0;
        }

        try {
            // 2. Loại bỏ tất cả các dấu chấm phân cách hàng nghìn
            String cleanString = formattedPrice.replace(".", "");

            // 3. Chuyển chuỗi đã làm sạch thành kiểu double
            return Double.parseDouble(cleanString);

        } catch (NumberFormatException e) {
            // 4. Xử lý lỗi nếu chuỗi không phải là một số hợp lệ
            System.err.println("Lỗi chuyển đổi chuỗi thành double: " + formattedPrice);
            // Trả về một giá trị mặc định trong trường hợp lỗi
            return 0.0;
        }
    }

    public static String formatPriceDouble(double price) {
        // Khởi tạo DecimalFormatSymbols dựa trên Locale mặc định
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.JAPAN);

        // Thiết lập dấu phân cách nhóm là dấu chấm (Group Separator)
        symbols.setGroupingSeparator('.');

        // Pattern #,### được sử dụng với symbols đã định nghĩa
        DecimalFormat formatter = new DecimalFormat("#,###", symbols);

        return formatter.format(price);
    }
}
