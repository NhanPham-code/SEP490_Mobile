package com.example.sep490_mobile.utils;

import android.net.Uri;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class VnPayUtils {

    // --- LẤY TỪ CONFIG CỦA BẠN ---
    private static final String VNP_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    private static final String VNP_TMN_CODE = "0TFZONTS";
    private static final String VNP_HASH_SECRET = "5AOT3YTF06S8D8M80KM03LUDH4WZB9SL";
    private static final String VNP_VERSION = "2.1.0";
    private static final String VNP_COMMAND = "pay";
    private static final String VNP_CURR_CODE = "VND";
    private static final String VNP_LOCALE = "vn";
    // ---------------------------------

    // --- CẤU HÌNH CHO MOBILE ---
    private static final String VNP_RETURN_URL = "sep490://payment_return";
    private static final String VNP_ORDER_TYPE = "other";

    private static final String TAG = "VNPAY_DEBUG";

    /**
     * Tạo URL thanh toán VNPay (Phiên bản Hash chuẩn theo PHP Demo)
     *
     * @param txnRef Mã tham chiếu giao dịch
     * @param amount Số tiền (tính bằng VND, hàm sẽ tự nhân 100)
     * @param orderInfo Thông tin đơn hàng
     * @return URL thanh toán
     */
    public static String createPaymentUrl(String txnRef, long amount, String orderInfo) {
        try {
            String vnp_Amount = String.valueOf(amount * 100);
            String vnp_IpAddr = getIpAddress();

            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
            formatter.setTimeZone(TimeZone.getTimeZone("GMT+7"));
            String vnp_CreateDate = formatter.format(new Date());

            // Thiết lập thời gian hết hạn (ví dụ: 15 phút)
            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("GMT+7"));
            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());


            // Sắp xếp các tham số theo thứ tự alphabet
            Map<String, String> vnp_Params = new TreeMap<>();
            vnp_Params.put("vnp_Version", VNP_VERSION);
            vnp_Params.put("vnp_Command", VNP_COMMAND);
            vnp_Params.put("vnp_TmnCode", VNP_TMN_CODE);
            vnp_Params.put("vnp_Amount", vnp_Amount);
            vnp_Params.put("vnp_CurrCode", VNP_CURR_CODE);
            vnp_Params.put("vnp_TxnRef", txnRef);
            vnp_Params.put("vnp_OrderInfo", orderInfo);
            vnp_Params.put("vnp_OrderType", VNP_ORDER_TYPE);
            vnp_Params.put("vnp_Locale", VNP_LOCALE);
            vnp_Params.put("vnp_ReturnUrl", VNP_RETURN_URL);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate); // Thêm expire date


            // 1. Tạo chuỗi hash data (URL ENCODE CẢ KEY VÀ VALUE)
            StringBuilder hashData = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
                if (!first) {
                    hashData.append("&");
                }
                // >>> ĐIỂM SỬA QUAN TRỌNG: URL ENCODE CẢ KEY VÀ VALUE TRONG CHUỖI HASH DATA <<<
                hashData.append(URLEncoder.encode(entry.getKey(), StandardCharsets.US_ASCII.toString()))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII.toString()));
                first = false;
            }

            // 2. Tạo chữ ký
            String vnp_SecureHash = hmacSHA512(VNP_HASH_SECRET, hashData.toString());
            if (vnp_SecureHash.isEmpty()) return "";

            // 🔴 LOG DEBUG CHỮ KÝ 🔴
            Log.d(TAG, "---------------- VNPAY DEBUG HASH ----------------");
            Log.d(TAG, "Secret Key (VNP_HASH_SECRET): " + VNP_HASH_SECRET);
            Log.d(TAG, "1. Chuỗi HashData ĐÃ ENCODE (DÙNG ĐỂ BĂM): " + hashData.toString()); // Log chuỗi đã encode
            Log.d(TAG, "2. Chữ ký tạo ra (vnp_SecureHash): " + vnp_SecureHash);
            Log.d(TAG, "--------------------------------------------------");

            // 3. Tạo chuỗi query string (LƯU Ý: Uri.encode() là cách đơn giản và an toàn hơn cho Android)
            StringBuilder queryString = new StringBuilder();
            first = true;
            for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
                if (!first) {
                    queryString.append("&");
                }
                // Encode giá trị cho URL
                queryString.append(entry.getKey())
                        .append("=")
                        .append(Uri.encode(entry.getValue()));
                first = false;
            }

            // Thêm hash vào cuối URL
            queryString.append("&vnp_SecureHash=").append(Uri.encode(vnp_SecureHash));

            String finalUrl = VNP_URL + "?" + queryString.toString();

            Log.d(TAG, "3. URL Thanh toán Cuối cùng: " + finalUrl);

            return finalUrl;
        } catch (Exception e) {
            Log.e(TAG, "Lỗi tạo URL thanh toán: ", e);
            return "";
        }
    }

    // Giữ nguyên hàm hmacSHA512
    private static String hmacSHA512(final String key, final String data) {
        try {
            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512Hmac.init(secretKey);
            byte[] hash = sha512Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            Log.e(TAG, "Error generating HMAC-SHA512", e);
            return "";
        }
    }

    // Giữ nguyên hàm getIpAddress
    public static String getIpAddress() {
        try {
            for (NetworkInterface intf : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress addr : Collections.list(intf.getInetAddresses())) {
                    if (!addr.isLoopbackAddress() && addr instanceof java.net.Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("VnPayUtils", "Error getting IP", ex);
        }
        return "10.0.2.15"; // Giá trị mặc định cho emulator
    }
}