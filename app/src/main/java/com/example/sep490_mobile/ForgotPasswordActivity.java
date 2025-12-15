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

import com.example.sep490_mobile.databinding.ActivityForgotPasswordBinding;
import com.example.sep490_mobile.viewmodel.ForgotPasswordViewModel;

import java.util.Objects;

public class ForgotPasswordActivity extends AppCompatActivity {

    private ActivityForgotPasswordBinding binding;
    private ForgotPasswordViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ẩn header
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        viewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);

        setupClickListeners();
        setupObservers();
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnContinue.setOnClickListener(v -> {
            String email = Objects.requireNonNull(binding.inputEmail.getText()).toString().trim();
            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.inputLayoutEmail.setError("Vui lòng nhập email hợp lệ.");
                return;
            }
            binding.inputLayoutEmail.setError(null);
            viewModel.checkEmail(email);
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnContinue.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getEmailExists().observe(this, exists -> {
            if (exists) {
                // Email tồn tại, chuyển sang màn hình OTP
                String email = Objects.requireNonNull(binding.inputEmail.getText()).toString().trim();
                Intent intent = new Intent(this, OtpVerificationActivity.class);
                // Gửi kèm "chế độ" và email
                intent.putExtra("VERIFICATION_MODE", "RESET_PASSWORD");
                intent.putExtra("USER_EMAIL", email);
                startActivity(intent);
            } else {
                binding.inputLayoutEmail.setError("Email không tồn tại trong hệ thống.");
            }
        });
    }
}