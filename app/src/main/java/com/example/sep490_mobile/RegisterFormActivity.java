package com.example.sep490_mobile;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.sep490_mobile.databinding.ActivityRegisterFormBinding;
import com.example.sep490_mobile.model.RegistrationData;
import com.example.sep490_mobile.viewmodel.CheckEmailViewModel;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class RegisterFormActivity extends AppCompatActivity {

    private final String TAG = "RegisterFormActivity";
    private ActivityRegisterFormBinding binding;
    private CheckEmailViewModel checkEmailViewModel;
    private RegistrationData registrationData; // Biến để giữ dữ liệu tạm thời

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        checkEmailViewModel = new ViewModelProvider(this).get(CheckEmailViewModel.class);

        setupClickListeners();
        setupObservers();
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterFormActivity.this, LoginActivity.class);
            startActivity(intent);
        });
        binding.inputDob.setOnClickListener(v -> showDatePickerDialog());
        binding.btnRegisterMain.setOnClickListener(v -> handleValidationAndCheckEmail());
    }

    private void setupObservers() {
        // Lắng nghe trạng thái loading từ ViewModel
        checkEmailViewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnRegisterMain.setEnabled(!isLoading);
        });

        // Lắng nghe kết quả kiểm tra email
        checkEmailViewModel.getEmailExists().observe(this, emailExists -> {
            if (emailExists != null) {
                if (emailExists) {
                    // Nếu email đã tồn tại, báo lỗi
                    binding.inputLayoutEmail.setError("Email này đã được sử dụng!");
                    Toast.makeText(this, "Email đã tồn tại.", Toast.LENGTH_SHORT).show();
                } else {
                    // Nếu email hợp lệ, chuyển sang bước tiếp theo
                    binding.inputLayoutEmail.setError(null);
                    proceedToOtpStep();
                }
            }
        });

        // Lắng nghe thông báo lỗi từ ViewModel
        checkEmailViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleValidationAndCheckEmail() {
        String fullName = Objects.requireNonNull(binding.inputFullname.getText()).toString().trim();
        String email = Objects.requireNonNull(binding.inputEmail.getText()).toString().trim();
        String phone = Objects.requireNonNull(binding.inputPhone.getText()).toString().trim();
        String dob = Objects.requireNonNull(binding.inputDob.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.inputPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(binding.inputConfirmPassword.getText()).toString().trim();

        // --- Client-side Validation ---
        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng điền đầy đủ các trường bắt buộc.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputLayoutEmail.setError("Định dạng email không hợp lệ!");
            return;
        } else {
            binding.inputLayoutEmail.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            binding.inputLayoutConfirmPassword.setError("Mật khẩu không khớp!");
            return;
        } else {
            binding.inputLayoutConfirmPassword.setError(null);
        }

        // Nếu validation thành công, lưu dữ liệu và gọi ViewModel để kiểm tra email
        registrationData = new RegistrationData(fullName, email, phone, dob, password);
        checkEmailViewModel.checkEmailExists(email);
    }

    private void proceedToOtpStep() {
        // TODO: Gọi API send-otp ở đây (bạn có thể thêm vào CheckEmailViewModel hoặc OtpViewModel)
        // Ví dụ: otpViewModel.sendOtp(registrationData.getEmail());

        Toast.makeText(this, "Gửi mã OTP đến email của bạn...", Toast.LENGTH_SHORT).show();

        // Chuyển sang OtpVerificationActivity và gửi kèm dữ liệu đã nhập
        Intent intent = new Intent(RegisterFormActivity.this, OtpVerificationActivity.class);
        intent.putExtra("REGISTRATION_DATA", registrationData); // Gửi đối tượng qua Intent
        startActivity(intent);
    }

    private void showDatePickerDialog() {
        // Lấy ngày hiện tại
        final Calendar calendar = Calendar.getInstance();

        // Tính toán ngày tối đa cho phép (cách đây 15 năm)
        // Đây sẽ là ngày sinh muộn nhất có thể để đủ 15 tuổi
        Calendar maxDateCalendar = Calendar.getInstance();
        maxDateCalendar.add(Calendar.YEAR, -15);

        // Lấy năm, tháng, ngày để hiển thị ban đầu (có thể lấy từ ngày đã chọn trước đó hoặc ngày mặc định)
        int initialYear = maxDateCalendar.get(Calendar.YEAR);
        int initialMonth = maxDateCalendar.get(Calendar.MONTH);
        int initialDay = maxDateCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    // Định dạng lại ngày tháng năm và hiển thị lên EditText
                    String selectedDate = year + "-" + String.format(Locale.getDefault(),"%02d", month + 1) + "-" + String.format(Locale.getDefault(),"%02d", dayOfMonth);
                    binding.inputDob.setText(selectedDate);
                }, initialYear, initialMonth, initialDay);

        // Đặt ngày tối đa có thể chọn là ngày đã tính toán ở trên
        datePickerDialog.getDatePicker().setMaxDate(maxDateCalendar.getTimeInMillis());

        // Hiển thị dialog
        datePickerDialog.show();
    }
}