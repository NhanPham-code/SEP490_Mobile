package com.example.sep490_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.sep490_mobile.data.dto.LoginRequestDTO;
import com.example.sep490_mobile.data.dto.LoginResponseDTO;
import com.example.sep490_mobile.data.repository.UserRepository;
import com.example.sep490_mobile.databinding.ActivityLoginBinding;
import com.example.sep490_mobile.utils.BiometricHelper;
import com.example.sep490_mobile.viewmodel.LoginViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel loginViewModel;

    private GoogleSignInClient mGoogleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;
    private static final String TAG = "LoginActivity";

    private BiometricHelper biometricHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo ViewModel
        loginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Ẩn Action Bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Thiết lập các sự kiện click
        setupClickListeners();

        // Thiết lập các observer để lắng nghe thay đổi từ ViewModel
        setupObservers();

        // --- Cấu hình Google Sign-In ---
        configureGoogleSignIn();

        // --- Cấu hình biometric ---
        initializeBiometricHelper();
    }

    private void initializeBiometricHelper() {
        biometricHelper = new BiometricHelper(this, new BiometricHelper.BiometricCallback() {
            @Override
            public void onAuthenticationSuccess(BiometricPrompt.AuthenticationResult result) {
                // Giải mã để lấy ra BiometricToken
                String biometricToken = biometricHelper.onDecryptionSuccess(result);
                if (biometricToken != null) {
                    // Gọi ViewModel để đăng nhập
                    loginViewModel.loginWithBiometric(biometricToken);
                } else {
                    showError("Lỗi giải mã. Vui lòng đăng nhập lại.");
                }
            }

            @Override
            public void onAuthenticationError(String errorMessage) {
                Toast.makeText(LoginActivity.this, "Xác thực bị hủy hoặc thất bại.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        // Sự kiện click nút Đăng nhập
        binding.btnLoginMain.setOnClickListener(v -> handleLogin());

        // Sự kiện click link Đăng ký
        binding.tvRegisterLink.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterFormActivity.class))
        );

        // Sự kiện click nút Back
        binding.btnBack.setOnClickListener(v -> finish());

        // Sự kiện click google login
        binding.btnGoogleLogin.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });

        // Sự kiện click nút đăng nhập bằng sinh trắc học
        binding.btnFaceLogin.setOnClickListener(v -> {
            if (biometricHelper.isBiometricLoginEnabled()) {
                biometricHelper.setupForDecryption();
            } else {
                Toast.makeText(this, "Tính năng chưa được bật. Vui lòng đăng nhập và kích hoạt trong phần tài khoản.", Toast.LENGTH_LONG).show();
            }
        });

        // Sự kiện click link Quên mật khẩu
        binding.tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
        );
    }

    private void setupObservers() {
        // 1. Lắng nghe trạng thái loading
        loginViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                binding.progressBar.setVisibility(View.VISIBLE);
                binding.btnLoginMain.setEnabled(false); // Vô hiệu hóa nút khi đang tải
            } else {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnLoginMain.setEnabled(true); // Kích hoạt lại nút
            }
        });

        // 2. Lắng nghe kết quả đăng nhập
        loginViewModel.getLoginResult().observe(this, loginResponse -> {
            if (loginResponse == null) return;

            if (loginResponse.isValid()) {
                // Đăng nhập thành công, chuyển màn hình
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        // 3. Lắng nghe các lỗi khác (mạng, server sập, etc.)
        loginViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                showError(errorMessage);
            }
        });
    }

    private void handleLogin() {
        // Xóa lỗi cũ trước khi validate
        binding.tvErrorMessage.setVisibility(View.GONE);
        binding.inputLayoutEmail.setError(null);
        binding.inputLayoutPassword.setError(null);

        String email = Objects.requireNonNull(binding.inputEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.inputPassword.getText()).toString().trim();

        // Validate input
        if (email.isEmpty()) {
            binding.inputLayoutEmail.setError("Vui lòng nhập email");
            return;
        }

        if (password.isEmpty()) {
            binding.inputLayoutPassword.setError("Vui lòng nhập mật khẩu");
            return;
        }

        // Nếu hợp lệ, gọi ViewModel để xử lý logic đăng nhập
        loginViewModel.login(email, password);
    }

    private void showError(String message) {
        binding.tvErrorMessage.setText(message);
        binding.tvErrorMessage.setVisibility(View.VISIBLE);
    }

    private void configureGoogleSignIn() {
        // Lấy Web Client ID từ file string.xml
        String serverClientId = getString(R.string.server_web_client_id);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(serverClientId) // Yêu cầu IdToken
                .requestEmail() // Yêu cầu email
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Khởi tạo Launcher để nhận kết quả từ Google
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        handleGoogleSignInResult(task);
                    } else {
                        Toast.makeText(this, "Đăng nhập Google bị hủy.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String idToken = account.getIdToken();
            if (idToken != null) {
                Log.d(TAG, "Google ID Token: " + idToken);
                loginViewModel.loginWithGoogle(idToken);
            } else {
                showError("Không lấy được Google Token.");
            }

        } catch (ApiException e) {
            // *** PHẦN CẢI TIẾN ***
            // Log mã lỗi cụ thể để biết chính xác vấn đề là gì.
            // Mã lỗi 10 (DEVELOPER_ERROR) là lỗi phổ biến nhất, thường do SHA-1 sai.
            String errorMessage = "Lỗi đăng nhập Google. Mã lỗi: " + e.getStatusCode();
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            showError(errorMessage);
        }
    }
}