package com.example.sep490_mobile.utils;

import android.text.Spanned;
import android.widget.TextView;

import androidx.core.text.HtmlCompat;

import org.apache.commons.text.StringEscapeUtils;

public class HtmlConverter {
    public static void convertHtmlToMarkdown(String html, TextView descriptionTextView) {
        if (html == null) {
            descriptionTextView.setText("");
            return;
        }

        // ⭐ Chỉ giải mã các ký tự escape Unicode đại diện cho < và >
        String decodedHtml = html
                .replace("\\u003C", "<")
                .replace("\\u003E", ">");

        // Lưu ý: Nếu chuỗi HTML cũng chứa các thực thể HTML khác (như &amp;, &lt;),
        // bạn có thể cần thêm StringEscapeUtils.unescapeHtml4() TỪ THƯ VIỆN BÊN NGOÀI
        // hoặc xử lý chúng bằng replace().

        Spanned spannedText = HtmlCompat.fromHtml(decodedHtml, HtmlCompat.FROM_HTML_MODE_LEGACY);
        descriptionTextView.setText(spannedText);
    }
    public static String convertSpannedToEscapedHtml(TextView descriptionTextView) {
        if (descriptionTextView == null || descriptionTextView.getText() == null) {
            return "";
        }

        // 1. Lấy nội dung Spanned
        Spanned spannedText = (Spanned) descriptionTextView.getText();

        // 2. Chuyển đổi Spanned sang chuỗi HTML
        // => Kết quả thường có dạng "<p>...</p>\n"
        String rawHtml = HtmlCompat.toHtml(spannedText, HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);

        // 3. Sử dụng regex để loại bỏ các thẻ <p> rỗng ở đầu và cuối
        String cleanedHtml = rawHtml.replaceAll("^(<p dir=\"(ltr|rtl)\">((<br>)|(&nbsp;)|\\s)*</p>\\s*)|(\\s*<p dir=\"(ltr|rtl)\">((<br>)|(&nbsp;)|\\s)*</p>)$", "");

        // 4. (BƯỚC THÊM VÀO) Loại bỏ bất kỳ khoảng trắng hoặc ký tự xuống dòng nào còn sót lại ở đầu và cuối chuỗi
        // Bước này sẽ xử lý ký tự `\n` do HtmlCompat.toHtml tự động thêm vào.
        String trimmedHtml = cleanedHtml.trim();

        // 5. Mã hóa chuỗi HTML đã được làm sạch hoàn toàn
        String escapedHtml = StringEscapeUtils.escapeJava(trimmedHtml);

        return escapedHtml;
    }
}
