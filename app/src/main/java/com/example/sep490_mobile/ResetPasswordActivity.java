package com.example.sep490_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.sep490_mobile.databinding.ActivityResetPasswordBinding;
import com.example.sep490_mobile.viewmodel.ResetPasswordViewModel;

import java.util.Objects;

public class ResetPasswordActivity extends AppCompatActivity {

    private ActivityResetPasswordBinding binding;
    private ResetPasswordViewModel viewModel;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Nhận email từ OtpVerificationActivity
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Đã xảy ra lỗi, không tìm thấy email.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(ResetPasswordViewModel.class);

        setupClickListeners();
        setupObservers();
    }

    private void setupClickListeners() {
        binding.btnResetPassword.setOnClickListener(v -> handleResetPassword());
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnResetPassword.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getResetSuccess().observe(this, isSuccess -> {
            if (isSuccess) {
                Toast.makeText(this, "Đặt lại mật khẩu thành công!", Toast.LENGTH_LONG).show();

                // Tạo Intent để quay về màn hình Login
                Intent intent = new Intent(this, LoginActivity.class);
                // Xóa tất cả các Activity trước đó khỏi stack để người dùng không thể nhấn "Back" quay lại
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Đóng Activity hiện tại
            }
        });
    }

    private void handleResetPassword() {
        String newPassword = Objects.requireNonNull(binding.inputNewPassword.getText()).toString();
        String confirmPassword = Objects.requireNonNull(binding.inputConfirmPassword.getText()).toString();

        // Xóa lỗi cũ
        binding.inputLayoutNewPassword.setError(null);
        binding.inputLayoutConfirmPassword.setError(null);

        // --- VALIDATION ---
        if (newPassword.length() < 6) {
            binding.inputLayoutNewPassword.setError("Mật khẩu phải có ít nhất 6 ký tự.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            binding.inputLayoutConfirmPassword.setError("Mật khẩu xác nhận không khớp.");
            return;
        }

        // Nếu hợp lệ, gọi ViewModel
        viewModel.resetPassword(userEmail, newPassword);
    }
}