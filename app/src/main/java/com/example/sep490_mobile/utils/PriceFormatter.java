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
}
