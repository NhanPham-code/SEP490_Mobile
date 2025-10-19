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

        // ⭐ Sử dụng unescapeJava để giải mã \u003C và \u003E
        String decodedHtml = StringEscapeUtils.unescapeJava(html);

        Spanned spannedText = HtmlCompat.fromHtml(decodedHtml, HtmlCompat.FROM_HTML_MODE_LEGACY);
        descriptionTextView.setText(spannedText);
    }
}
