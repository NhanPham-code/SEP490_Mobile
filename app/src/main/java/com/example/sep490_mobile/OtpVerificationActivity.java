package com.example.sep490_mobile;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.sep490_mobile.databinding.ActivityOtpVerificationBinding;
import com.example.sep490_mobile.model.RegistrationData;
import com.example.sep490_mobile.viewmodel.OtpViewModel;

import java.util.Locale;
import java.util.Objects;

public class OtpVerificationActivity extends AppCompatActivity {

    private ActivityOtpVerificationBinding binding;
    private OtpViewModel otpViewModel;
    private CountDownTimer countDownTimer;

    // Các biến để xử lý cả 2 luồng
    private String verificationMode; // "REGISTER" hoặc "RESET_PASSWORD"
    private String userEmail;
    private RegistrationData registrationData; // Chỉ có giá trị ở mode "REGISTER"

    private static final long COUNTDOWN_TIME = 60000; // 60 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 1. Nhận dữ liệu từ Intent và xác định luồng
        verificationMode = getIntent().getStringExtra("VERIFICATION_MODE");

        if ("REGISTER".equals(verificationMode)) {
            registrationData = (RegistrationData) getIntent().getSerializableExtra("REGISTRATION_DATA");
            userEmail = (registrationData != null) ? registrationData.getEmail() : null;
        } else { // Mặc định là RESET_PASSWORD
            userEmail = getIntent().getStringExtra("USER_EMAIL");
        }

        // Kiểm tra an toàn, nếu không có email thì không thể tiếp tục
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Đã xảy ra lỗi, không tìm thấy email để xác thực.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 2. Khởi tạo ViewModel và thiết lập
        otpViewModel = new ViewModelProvider(this).get(OtpViewModel.class);
        setupUI();
        setupClickListeners();
        setupObservers();

        // 3. Tự động yêu cầu gửi OTP khi màn hình được tạo
        requestNewOtp();
    }

    private void setupUI() {
        // <-- SỬA LỖI Ở ĐÂY: Dùng biến `userEmail` đã được chuẩn hóa
        String description = "Mã xác thực 6 chữ số đã được gửi đến email\n" + userEmail;
        binding.tvOtpDescription.setText(description);
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnVerify.setOnClickListener(v -> handleVerification());
        binding.tvResendOtp.setOnClickListener(v -> requestNewOtp());
    }

    private void setupObservers() {
        otpViewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnVerify.setEnabled(!isLoading);
        });

        otpViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                // Hiển thị lỗi từ ViewModel, ví dụ "Mã OTP không chính xác."
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        otpViewModel.getOtpSent().observe(this, isSent -> {
            if (isSent) {
                Toast.makeText(this, "Đã gửi mã OTP!", Toast.LENGTH_SHORT).show();
                startCountdown();
            }
        });

        otpViewModel.getOtpVerified().observe(this, isVerified -> {
            if (isVerified) {
                Toast.makeText(this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();

                // Dựa vào mode để quyết định đi đâu tiếp theo
                if ("REGISTER".equals(verificationMode)) {
                    // Chuyển sang màn hình hoàn tất đăng ký
                    Intent intent = new Intent(this, CompleteRegisterActivity.class);
                    intent.putExtra("REGISTRATION_DATA", registrationData);
                    startActivity(intent);
                } else { // RESET_PASSWORD
                    // Chuyển sang màn hình đặt lại mật khẩu
                    Intent intent = new Intent(this, ResetPasswordActivity.class);
                    intent.putExtra("USER_EMAIL", userEmail);
                    startActivity(intent);
                }
                finish(); // Đóng màn hình OTP sau khi hoàn tất
            }
        });
    }

    private void resetResendButton() {
        binding.tvCountdown.setVisibility(View.GONE);
        binding.tvResendOtp.setEnabled(true);
        // Đảm bảo getContext() không null trước khi gọi getResources()
        if (this.getApplicationContext() != null) {
            binding.tvResendOtp.setTextColor(getResources().getColor(R.color.ocean_blue));
        }
    }

    private void handleVerification() {
        String otp = Objects.requireNonNull(binding.pinView.getText()).toString();
        if (otp.length() < 6) {
            Toast.makeText(this, "Vui lòng nhập đủ 6 chữ số.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Luôn dùng userEmail đã được chuẩn hóa
        otpViewModel.verifyOtp(userEmail, otp);
    }

    private void requestNewOtp() {
        // Luôn dùng userEmail đã được chuẩn hóa
        otpViewModel.sendOtp(userEmail);
    }

    private void startCountdown() {
        binding.tvResendOtp.setEnabled(false);
        binding.tvResendOtp.setTextColor(Color.GRAY);

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                String timeLeft = String.format(Locale.getDefault(), "Gửi lại sau 00:%02d", seconds);
                binding.tvCountdown.setText(timeLeft);
                binding.tvCountdown.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                binding.tvCountdown.setVisibility(View.GONE);
                binding.tvResendOtp.setEnabled(true);
                binding.tvResendOtp.setTextColor(getResources().getColor(R.color.ocean_blue));
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}