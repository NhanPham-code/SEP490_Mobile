package com.example.sep490_mobile.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;

public class DeviceUtils {
    @SuppressLint("HardwareIds")
    public static String getDeviceId(Context context) {
        // ANDROID_ID là một giá trị 64-bit duy nhất cho mỗi sự kết hợp của
        // người dùng ứng dụng, thiết bị, và signing key. Nó ổn định và đủ tốt cho mục đích này.
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }
}
