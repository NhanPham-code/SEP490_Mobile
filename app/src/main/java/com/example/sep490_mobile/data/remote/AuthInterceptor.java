package com.example.sep490_mobile.data.remote;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;

import com.example.sep490_mobile.utils.DeviceUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("access_token", null);

        Request originalRequest = chain.request();
        Request.Builder builder = originalRequest.newBuilder();

        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        // Nếu request là để lấy biometric token, hãy thêm các header cần thiết
        if (originalRequest.url().encodedPath().endsWith("/users/biometric-token")) {
            String deviceId = DeviceUtils.getDeviceId(context);
            String deviceName = Build.MANUFACTURER + " " + Build.MODEL;

            builder.header("Device-Id", deviceId);
            builder.header("Device-Name", deviceName);
        }

        // Nếu request là xóa biometric token, hãy thêm các header cần thiết
        if (originalRequest.url().encodedPath().endsWith("/users/biometric-delete")) {
            String deviceId = DeviceUtils.getDeviceId(context);
            String deviceName = Build.MANUFACTURER + " " + Build.MODEL;

            builder.header("Device-Id", deviceId);
            builder.header("Device-Name", deviceName);
        }

        Request request = builder.build();
        return chain.proceed(request);
    }
}
