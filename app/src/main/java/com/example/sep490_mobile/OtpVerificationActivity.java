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
    private RegistrationData registrationData;
    private CountDownTimer countDownTimer;
    private static final long COUNTDOWN_TIME = 60000; // 60 giây

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpVerificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // 1. Nhận dữ liệu từ RegisterFormActivity
        if (getIntent().hasExtra("REGISTRATION_DATA")) {
            registrationData = (RegistrationData) getIntent().getSerializableExtra("REGISTRATION_DATA");
        }

        if (registrationData == null) {
            Toast.makeText(this, "Đã xảy ra lỗi, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
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
        String description = "Mã xác thực 6 chữ số đã được gửi đến email\n" + registrationData.getEmail();
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
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
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
                // Chuyển sang màn hình hoàn tất đăng ký và gửi kèm dữ liệu
                Intent intent = new Intent(this, CompleteRegisterActivity.class);
                intent.putExtra("REGISTRATION_DATA", registrationData);
                startActivity(intent);
            }
        });
    }

    private void handleVerification() {
        String otp = Objects.requireNonNull(binding.pinView.getText()).toString();
        if (otp.length() < 6) {
            Toast.makeText(this, "Vui lòng nhập đủ 6 chữ số.", Toast.LENGTH_SHORT).show();
            return;
        }
        otpViewModel.verifyOtp(registrationData.getEmail(), otp);
    }

    private void requestNewOtp() {
        otpViewModel.sendOtp(registrationData.getEmail());
    }

    private void startCountdown() {
        binding.tvResendOtp.setEnabled(false);
        binding.tvResendOtp.setTextColor(Color.GRAY);

        countDownTimer = new CountDownTimer(COUNTDOWN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                String timeLeft = String.format(Locale.getDefault(), "Gửi lại sau 00:%02d", seconds);
                binding.tvCountdown.setText(timeLeft);
            }

            @Override
            public void onFinish() {
                binding.tvCountdown.setText("");
                binding.tvResendOtp.setEnabled(true);
                binding.tvResendOtp.setTextColor(getResources().getColor(R.color.ocean_blue)); // Thay bằng màu gốc
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hủy CountDownTimer để tránh memory leak
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}