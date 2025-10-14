package com.example.sep490_mobile.data.remote;

import android.content.Context;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.sep490_mobile.Adapter.BigDecimalTypeAdapter;
import com.example.sep490_mobile.Adapter.DurationTypeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.math.BigDecimal;
import java.security.cert.CertificateException;
import java.time.Duration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Authenticator;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // QUAN TRỌNG: Đối với localhost trên trình giả lập Android, sử dụng 10.0.2.2
    // Đối với thiết bị thực, sử dụng địa chỉ IP của máy tính của bạn trong mạng cục bộ
    private static final String BASE_URL = "https://localhost:7136/"; // Giữ nguyên HTTPS nếu bạn chọn giải pháp này
    private static ApiClient instance;
    private ApiService apiService;
    // Khai báo một ApiService KHÔNG CÓ token để sử dụng cho Authenticator
    private ApiService authApiService;

    @RequiresApi(api = Build.VERSION_CODES.O)
    private ApiClient(Context context) {
        Gson gson = new GsonBuilder()
                // Đăng ký bộ chuyển đổi tùy chỉnh cho BigDecimal
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();
        // ... (Giữ nguyên phần khởi tạo loggingInterceptor) ...
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // 1. TẠO CLIENT CƠ BẢN (KHÔNG CÓ AuthInterceptor/Authenticator)
        OkHttpClient baseClient = getUnsafeOkHttpClient(loggingInterceptor, null, null);

        // Khởi tạo AuthApiService để TokenAuthenticator sử dụng (Không cần token trong header khi gọi Refresh Token)
        Retrofit baseRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(baseClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        authApiService = baseRetrofit.create(ApiService.class);

        // 2. TẠO AUTHENTICATOR VÀ INTERCEPTOR CỦA CHÚNG TA
        AuthInterceptor authInterceptor = new AuthInterceptor(context);
        TokenAuthenticator tokenAuthenticator = new TokenAuthenticator(context, authApiService);

        // 3. TẠO CLIENT CUỐI CÙNG (CÓ INTERCEPTOR VÀ AUTHENTICATOR)
        OkHttpClient clientWithAuth = getUnsafeOkHttpClient(loggingInterceptor, authInterceptor, tokenAuthenticator);

        // 4. Khởi tạo Retrofit cuối cùng
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(clientWithAuth)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context);
        }
        return instance;
    }

    public ApiService getApiService() {
        return apiService;
    }

    /**
     * Phương thức này tạo một OkHttpClient bỏ qua tất cả các kiểm tra chứng chỉ SSL.
     * CHỈ ĐƯỢC SỬ DỤNG CHO MÔI TRƯỜNG PHÁT TRIỂN/THỬ NGHIỆM!
     * KHÔNG BAO GIỜ SỬ DỤNG TRONG MÔI TRƯỜNG PRODUCTION!
     */
    private static OkHttpClient getUnsafeOkHttpClient(HttpLoggingInterceptor loggingInterceptor,
                                                      @Nullable Interceptor authInterceptor,
                                                      @Nullable Authenticator tokenAuthenticator) {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}
                        @Override public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}
                        @Override public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.addInterceptor(loggingInterceptor);

            if (authInterceptor != null) {
                builder.addInterceptor(authInterceptor); // Gắn AuthInterceptor để thêm Access Token
            }
            if (tokenAuthenticator != null) {
                builder.authenticator(tokenAuthenticator); // Gắn Authenticator để xử lý 401
            }

            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);
            return builder.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static OkHttpClient getUnsafeOkHttpClientForGlide() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        // Gọi phương thức tạo client bỏ qua SSL
        return getUnsafeOkHttpClient(loggingInterceptor, null, null);
    }
}
