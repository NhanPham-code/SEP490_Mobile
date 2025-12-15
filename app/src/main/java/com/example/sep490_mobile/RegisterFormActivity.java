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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

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

        boolean isValid = true;

        // --- VALIDATION: Họ và tên ---
        if (TextUtils.isEmpty(fullName)) {
            binding.inputLayoutFullname.setError("Họ và tên không được để trống");
            isValid = false;
        } else {
            binding.inputLayoutFullname.setError(null);
        }

        // --- VALIDATION: Email ---
        if (TextUtils.isEmpty(email)) {
            binding.inputLayoutEmail.setError("Email không được để trống");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.inputLayoutEmail.setError("Định dạng email không hợp lệ!");
            isValid = false;
        } else {
            binding.inputLayoutEmail.setError(null);
        }

        // --- VALIDATION: Số điện thoại ---
        String phonePattern = "^(0[35789])\\d{8}$";
        if (TextUtils.isEmpty(phone)) {
            binding.inputLayoutPhone.setError("Số điện thoại không được để trống");
            isValid = false;
        } else if (!Pattern.matches(phonePattern, phone)) {
            binding.inputLayoutPhone.setError("Số điện thoại không đúng định dạng Việt Nam");
            isValid = false;
        } else {
            binding.inputLayoutPhone.setError(null);
        }

        // --- VALIDATION: Ngày sinh ---
        if (TextUtils.isEmpty(dob)) {
            binding.inputLayoutDob.setError("Ngày sinh không được để trống");
            isValid = false;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setLenient(false);
            try {
                Date birthDate = sdf.parse(dob);
                Calendar dobCal = Calendar.getInstance();
                dobCal.setTime(birthDate);

                if (dobCal.get(Calendar.YEAR) < 1900) {
                    binding.inputLayoutDob.setError("Năm sinh không hợp lệ");
                    isValid = false;
                } else {
                    Calendar todayMinus15Years = Calendar.getInstance();
                    todayMinus15Years.add(Calendar.YEAR, -15);
                    if (dobCal.after(todayMinus15Years)) {
                        binding.inputLayoutDob.setError("Bạn phải đủ 15 tuổi");
                        isValid = false;
                    } else {
                        binding.inputLayoutDob.setError(null);
                    }
                }
            } catch (ParseException e) {
                binding.inputLayoutDob.setError("Định dạng ngày không hợp lệ (yyyy-MM-dd)");
                isValid = false;
            }
        }


        // --- VALIDATION: Mật khẩu ---
        if (TextUtils.isEmpty(password)) {
            binding.inputLayoutPassword.setError("Mật khẩu không được để trống");
            isValid = false;
        } else {
            // 1. Danh sách các quy tắc bị thiếu (để hiển thị thông báo lỗi chi tiết)
            List<String> missingRules = new ArrayList<>();
            if (password.length() < 8) missingRules.add("8 ký tự");
            if (!password.matches(".*[A-Z].*")) missingRules.add("chữ in hoa");
            if (!password.matches(".*\\d.*")) missingRules.add("số");
            if (!password.matches(".*[^a-zA-Z0-9\\s].*")) missingRules.add("ký tự đặc biệt");

            // 2. Tính điểm độ mạnh
            int strength = 0;
            if (password.length() >= 8) strength++;
            // Quy tắc 2: Cần cả chữ thường VÀ chữ hoa mới được 1 điểm
            if (password.matches(".*[a-z].*") && password.matches(".*[A-Z].*")) strength++;
            if (password.matches(".*\\d.*")) strength++;
            if (password.matches(".*[^a-zA-Z0-9\\s].*")) strength++;

            // 3. Kiểm tra điều kiện chấp nhận (Phải đạt điểm >= 3)
            if (strength < 3) {
                StringBuilder msg = new StringBuilder("Mật khẩu chưa đủ mạnh.");
                if (!missingRules.isEmpty()) {
                    // TextUtils.join giúp nối mảng thành chuỗi: "số, ký tự đặc biệt"
                    msg.append(" Cần thêm: ").append(TextUtils.join(", ", missingRules)).append(".");
                }
                binding.inputLayoutPassword.setError(msg.toString());
                isValid = false;
            } else {
                binding.inputLayoutPassword.setError(null);
            }
        }

        // --- VALIDATION: Xác nhận mật khẩu ---
        if (isValid && TextUtils.isEmpty(confirmPassword)) {
            binding.inputLayoutConfirmPassword.setError("Vui lòng xác nhận mật khẩu");
            isValid = false;
        } else if (isValid && !password.equals(confirmPassword)) {
            binding.inputLayoutConfirmPassword.setError("Mật khẩu không khớp!");
            isValid = false;
        } else if (isValid) {
            binding.inputLayoutConfirmPassword.setError(null);
        }

        if (!isValid) {
            Toast.makeText(this, "Vui lòng kiểm tra lại các thông tin đã nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        // Nếu tất cả validation thành công, lưu dữ liệu và gọi ViewModel để kiểm tra email
        registrationData = new RegistrationData(fullName, email, phone, dob, password);
        checkEmailViewModel.checkEmailExists(email);
    }

    private void proceedToOtpStep() {
        Toast.makeText(this, "Gửi mã OTP đến email của bạn...", Toast.LENGTH_SHORT).show();

        // Chuyển sang OtpVerificationActivity và gửi kèm dữ liệu đã nhập
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra("VERIFICATION_MODE", "REGISTER");
        intent.putExtra("REGISTRATION_DATA", registrationData);
        startActivity(intent);
    }

    private void showDatePickerDialog() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, monthOfYear, dayOfMonth) -> {
            String selectedDate = year1 + "-" + String.format(Locale.getDefault(), "%02d", monthOfYear + 1) + "-" + String.format(Locale.getDefault(), "%02d", dayOfMonth);
            binding.inputDob.setText(selectedDate);
            binding.inputLayoutDob.setError(null); // Xóa lỗi khi người dùng chọn ngày mới
        }, year, month, day);

        // --- VALIDATION: Giới hạn ngày sinh ---
        // 1. Người dùng phải đủ 15 tuổi -> không được sinh sau ngày này (ngày hiện tại - 15 năm)
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.YEAR, -15);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        // 2. Không cho phép ngày sinh quá xa trong quá khứ (ví dụ: trước năm 1900)
        Calendar minDate = Calendar.getInstance();
        minDate.set(1900, Calendar.JANUARY, 1);
        datePickerDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());

        datePickerDialog.show();
    }
}