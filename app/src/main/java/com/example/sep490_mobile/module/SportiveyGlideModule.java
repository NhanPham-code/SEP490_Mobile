package com.example.sep490_mobile.module;

import android.content.Context;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.example.sep490_mobile.data.remote.ApiClient;

import java.io.InputStream;

import okhttp3.OkHttpClient;

@GlideModule
public final class SportiveyGlideModule extends AppGlideModule {

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {

        // 1. Lấy OkHttpClient đã được cấu hình của bạn (bỏ qua SSL)
        OkHttpClient client = ApiClient.getUnsafeOkHttpClientForGlide();

        // 2. Đăng ký nó với Glide: Thay thế giao thức tải mạng mặc định
        registry.replace(
                GlideUrl.class,
                InputStream.class,
                new OkHttpUrlLoader.Factory(client)
        );
    }
}
