package com.example.sep490_mobile.ui.account;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sep490_mobile.EditProfileActivity;
import com.example.sep490_mobile.LoginActivity;
import com.example.sep490_mobile.MainActivity;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.RegisterFormActivity;
import com.example.sep490_mobile.SettingsActivity;
import com.example.sep490_mobile.databinding.FragmentAccountBinding;
import com.example.sep490_mobile.utils.BiometricHelper;
import com.example.sep490_mobile.utils.ImageUtils;

public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private AccountViewModel accountViewModel;

    // --- LAUNCHER MỚI ĐỂ NHẬN KẾT QUẢ TỪ EditProfileActivity ---
    private ActivityResultLauncher<Intent> editProfileLauncher;

    private BiometricHelper biometricHelper;
    private BiometricPrompt.AuthenticationResult tempAuthResult;

    public AccountFragment() { }

    public static AccountFragment newInstance() {
        return new AccountFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        accountViewModel = new ViewModelProvider(this).get(AccountViewModel.class);

        // Khởi tạo launcher
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    // Kiểm tra xem EditProfileActivity có trả về kết quả OK không
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Nếu có, nghĩa là profile đã thay đổi.
                        // Làm mới lại UI bằng cách đọc lại SharedPreferences.
                        updateUIFromSharedPrefs();
                    }
                }
        );

        // Khởi tạo BiometricHelper
        biometricHelper = new BiometricHelper(requireActivity(), new BiometricHelper.BiometricCallback() {
            @Override
            public void onAuthenticationSuccess(BiometricPrompt.AuthenticationResult result) {
                // Khi xác thực thành công, ta có `result` chứa Cipher.
                // Bây giờ, hãy lấy BiometricToken từ server.
                accountViewModel.generateBiometricToken();
                tempAuthResult = null; // Dọn dẹp biến tạm
            }

            @Override
            public void onAuthenticationError(String errorMessage) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                binding.switchBiometric.setChecked(false);
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAccountBinding.inflate(inflater, container, false);
        setupMenu();
        checkLoginStateAndSetupUI();
        setupClickListeners();
        setupObservers();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLoginStateAndSetupUI(); // Kiểm tra trạng thái đăng nhập mỗi khi fragment được hiển thị lại
        updateUIFromSharedPrefs(); // Cập nhật UI mỗi khi fragment được hiển thị lại
    }

    // Tách hàm này ra để có thể gọi lại khi cần làm mới
    private void updateUIFromSharedPrefs() {
        if (getContext() == null) return;

        String fullName = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).getString("full_name", "User");
        binding.tvUserName.setText(fullName);

        String avatarUrl = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).getString("avatar_url", null);
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            String fullImageUrl = ImageUtils.getFullUrl(avatarUrl);
            Glide.with(this).load(fullImageUrl).circleCrop().placeholder(R.drawable.ic_account_black_24dp).into(binding.ivProfilePicture);
        } else {
            binding.ivProfilePicture.setImageResource(R.drawable.ic_account_black_24dp);
        }
    }


    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> startActivity(new Intent(getActivity(), LoginActivity.class)));
        binding.btnLogout.setOnClickListener(v -> accountViewModel.logout());
        binding.btnRegister.setOnClickListener(v -> startActivity(new Intent(getActivity(), RegisterFormActivity.class)));
        binding.btnSettings.setOnClickListener(v -> startActivity(new Intent(getActivity(), SettingsActivity.class)));

        // --- SỬ DỤNG LAUNCHER ĐỂ MỞ EditProfileActivity ---
        binding.ivEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            editProfileLauncher.launch(intent);
        });

        // --- XỬ LÝ SWITCH BIOMETRIC ---
        binding.switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!buttonView.isPressed()) return;

            if (isChecked) {
                // --- KIỂM TRA TRƯỚC KHI BẬT ---
                BiometricManager biometricManager = BiometricManager.from(requireActivity());
                int canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);

                if (canAuth == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                    // Nếu người dùng chưa cài đặt vân tay/khuôn mặt
                    Toast.makeText(getContext(), "Bạn cần cài đặt ít nhất một dấu vân tay hoặc khuôn mặt trong Cài đặt của điện thoại.", Toast.LENGTH_LONG).show();

                    // (Tùy chọn nâng cao) Mở thẳng màn hình cài đặt bảo mật cho người dùng
                    // Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                    // startActivity(enrollIntent);

                    binding.switchBiometric.setChecked(false); // Gạt switch về off
                    return; // Dừng lại
                }

                // Nếu mọi thứ ổn, bắt đầu quá trình
                biometricHelper.setupForEncryption();
            } else {
                // Người dùng tắt tính năng sinh trắc học

                // 1. Xóa dữ liệu sinh trắc học ở phía client ngay lập tức để có phản hồi tức thì.
                biometricHelper.disableBiometricLogin();

                // 2. Yêu cầu ViewModel gọi API để xóa token trên server.
                accountViewModel.deleteBiometricToken();

                // 3. Thông báo cho người dùng.
                Toast.makeText(getContext(), "Đã tắt đăng nhập bằng sinh trắc học.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupObservers() {
        // Quan sát trạng thái đăng xuất
        accountViewModel.getLogoutSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                binding.layoutLoggedIn.setVisibility(View.GONE);
                binding.layoutLoggedOut.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        // --- OBSERVER MỚI CHO BIOMETRIC TOKEN ---
        accountViewModel.getBiometricToken().observe(getViewLifecycleOwner(), token -> {
            // 3. Khi server trả về token, kiểm tra xem có `result` tạm và `token` không.
            if (token != null && tempAuthResult != null) {
                // 4. ĐÃ CÓ CẢ HAI! Bây giờ mới tiến hành mã hóa.
                biometricHelper.onEncryptionSuccess(tempAuthResult, token);

                Toast.makeText(getContext(), "Đã bật đăng nhập sinh trắc học.", Toast.LENGTH_SHORT).show();

                // 5. Dọn dẹp biến tạm sau khi dùng xong.
                tempAuthResult = null;
            }
        });

        accountViewModel.getBiometricError().observe(getViewLifecycleOwner(), error -> {
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            binding.switchBiometric.setChecked(false);
            tempAuthResult = null; // Dọn dẹp biến tạm
        });
    }

    private void checkLoginStateAndSetupUI() {
        if (getContext() == null) return;
        boolean isLoggedIn = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).getString("access_token", null) != null;

        if (isLoggedIn) {
            binding.layoutLoggedIn.setVisibility(View.VISIBLE);
            binding.layoutLoggedOut.setVisibility(View.GONE);
            updateUIFromSharedPrefs(); // Gọi hàm cập nhật UI
        } else {
            binding.layoutLoggedIn.setVisibility(View.GONE);
            binding.layoutLoggedOut.setVisibility(View.VISIBLE);
        }
    }

    private void setupMenu() {
        binding.menuBookingHistory.imgMenuIcon.setImageResource(R.drawable.ic_history_booking);
        binding.menuBookingHistory.tvMenuText.setText("Lịch đặt sân");
        binding.menuNotifications.imgMenuIcon.setImageResource(R.drawable.ic_notification);
        binding.menuNotifications.tvMenuText.setText("Thông báo");
        binding.menuSubject.imgMenuIcon.setImageResource(R.drawable.ic_dashboard_black_24dp);
        binding.menuSubject.tvMenuText.setText("Thêm Sau");
        binding.menuDiscount.imgMenuIcon.setImageResource(R.drawable.ic_discount);
        binding.menuDiscount.tvMenuText.setText("Mã giảm giá");
    }
}