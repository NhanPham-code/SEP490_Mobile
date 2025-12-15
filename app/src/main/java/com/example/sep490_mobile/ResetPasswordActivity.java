package com.example.sep490_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.util.ArrayList;
import java.util.List;
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

        // ẩn header
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

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
        binding.btnBack.setOnClickListener(v -> finish());
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

        boolean isValid = true;

        // --- VALIDATION: Mật khẩu mới ---
        if (TextUtils.isEmpty(newPassword)) {
            binding.inputLayoutNewPassword.setError("Mật khẩu không được để trống.");
            isValid = false;
        } else {
            // 1. Tìm các quy tắc bị thiếu
            List<String> missingRules = new ArrayList<>();
            if (newPassword.length() < 8) missingRules.add("8 ký tự");
            if (!newPassword.matches(".*[A-Z].*")) missingRules.add("chữ in hoa");
            if (!newPassword.matches(".*\\d.*")) missingRules.add("số");
            if (!newPassword.matches(".*[^a-zA-Z0-9\\s].*")) missingRules.add("ký tự đặc biệt");

            // 2. Tính điểm độ mạnh (Thang điểm 0-4)
            int strength = 0;
            if (newPassword.length() >= 8) strength++;
            // Quy tắc: Cần cả chữ thường VÀ chữ hoa mới được cộng điểm này
            if (newPassword.matches(".*[a-z].*") && newPassword.matches(".*[A-Z].*")) strength++;
            if (newPassword.matches(".*\\d.*")) strength++;
            if (newPassword.matches(".*[^a-zA-Z0-9\\s].*")) strength++;

            // 3. Kiểm tra: Điểm phải >= 3 mới cho qua
            if (strength < 3) {
                StringBuilder msg = new StringBuilder("Mật khẩu chưa đủ mạnh.");
                if (!missingRules.isEmpty()) {
                    msg.append(" Cần thêm: ").append(TextUtils.join(", ", missingRules)).append(".");
                }
                binding.inputLayoutNewPassword.setError(msg.toString());
                isValid = false;
            } else {
                binding.inputLayoutNewPassword.setError(null);
            }
        }

        // --- VALIDATION: Xác nhận mật khẩu ---
        // Chỉ kiểm tra confirm nếu mật khẩu chính đã hợp lệ (để tránh rối mắt)
        if (isValid) {
            if (TextUtils.isEmpty(confirmPassword)) {
                binding.inputLayoutConfirmPassword.setError("Vui lòng xác nhận mật khẩu.");
                isValid = false;
            } else if (!newPassword.equals(confirmPassword)) {
                binding.inputLayoutConfirmPassword.setError("Mật khẩu xác nhận không khớp.");
                isValid = false;
            } else {
                binding.inputLayoutConfirmPassword.setError(null);
            }
        }

        // Nếu tất cả đều hợp lệ, gọi ViewModel
        if (isValid) {
            viewModel.resetPassword(userEmail, newPassword);
        }
    }
}