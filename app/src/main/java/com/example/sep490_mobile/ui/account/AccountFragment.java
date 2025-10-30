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
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.sep490_mobile.ChatActivity;
import com.example.sep490_mobile.EditProfileActivity;
import com.example.sep490_mobile.LoginActivity;
import com.example.sep490_mobile.MainActivity;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.RegisterFormActivity;
import com.example.sep490_mobile.SettingsActivity;
import com.example.sep490_mobile.callback.ChatRoomCreationCallback;
import com.example.sep490_mobile.databinding.FragmentAccountBinding;
import com.example.sep490_mobile.model.ChatRoomInfo;
import com.example.sep490_mobile.ui.stadiumDetail.StadiumDetailFragment;
import com.example.sep490_mobile.utils.BiometricHelper;
import com.example.sep490_mobile.utils.ImageUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

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
                // <-- SỬA LỖI 1: Gán `result` cho biến tạm, không phải `null`
                tempAuthResult = result;
                // Bây giờ mới gọi server để lấy token
                accountViewModel.generateBiometricToken();
            }

            @Override
            public void onAuthenticationError(String errorMessage) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                binding.switchBiometric.setChecked(false);
                tempAuthResult = null; // Dọn dẹp nếu có lỗi
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

    private void updateBiometricSwitchState() {
        if (biometricHelper != null) {
            // Lấy trạng thái đã lưu và cập nhật switch mà không kích hoạt listener
            binding.switchBiometric.setChecked(biometricHelper.isBiometricLoginEnabled());
        }
    }

    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> startActivity(new Intent(getActivity(), LoginActivity.class)));
        binding.btnLogout.setOnClickListener(v -> accountViewModel.logout());
        binding.btnRegister.setOnClickListener(v -> startActivity(new Intent(getActivity(), RegisterFormActivity.class)));
        binding.btnSettings.setOnClickListener(v -> startActivity(new Intent(getActivity(), SettingsActivity.class)));
        binding.menuChatAdmin.getRoot().setOnClickListener(v -> {
            goToChatWithAdmin();
        });
        // --- SỬ DỤNG LAUNCHER ĐỂ MỞ EditProfileActivity ---
        binding.ivEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            editProfileLauncher.launch(intent);
        });

        binding.menuBookingHistory.getRoot().setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.action_navigation_account_to_navigation_schedule);
        });

        binding.menuSubject.getRoot().setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(AccountFragment.this);
            navController.navigate(R.id.action_navigation_account_to_bookingHistoryFragment);
        });binding.menuDiscount.getRoot().setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(AccountFragment.this);
            navController.navigate(R.id.action_navigation_account_to_discountListFragment);
        });


        // --- XỬ LÝ SWITCH BIOMETRIC ---
        binding.switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Chỉ xử lý khi người dùng thực sự nhấn vào, không phải khi code tự set
            if (!buttonView.isPressed()) return;

            if (isChecked) {
                // Logic BẬT tính năng (giữ nguyên)
                BiometricManager biometricManager = BiometricManager.from(requireActivity());
                int canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
                if (canAuth == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                    Toast.makeText(getContext(), "Bạn cần cài đặt ít nhất một dấu vân tay hoặc khuôn mặt trong Cài đặt của điện thoại.", Toast.LENGTH_LONG).show();
                    binding.switchBiometric.setChecked(false);
                    return;
                }
                biometricHelper.setupForEncryption();
            } else {
                // Logic TẮT tính năng (giữ nguyên)
                biometricHelper.disableBiometricLogin();
                accountViewModel.deleteBiometricToken();
                Toast.makeText(getContext(), "Đã tắt đăng nhập bằng sinh trắc học.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void goToChatWithAdmin() {
        int userId = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE).getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập để chat!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (userId == 1) {
            Toast.makeText(getContext(), "Bạn không thể chat với chính mình!", Toast.LENGTH_SHORT).show();
            return;
        }

        int adminId = 1;
        String adminName = "Admin";
        String senderName = requireContext().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                .getString("full_name", "Bạn");

        createChatRoomIfNeeded(userId, adminId, adminName, new ChatRoomCreationCallback() {
            @Override
            public void onComplete(boolean success) {
                if (success) {
                    Intent intent = new Intent(getActivity(), ChatActivity.class);
                    intent.putExtra("SENDER_ID", String.valueOf(userId));
                    intent.putExtra("SENDER_NAME", senderName);
                    intent.putExtra("RECEIVER_ID", String.valueOf(adminId));
                    intent.putExtra("RECEIVER_NAME", adminName);
                    startActivity(intent);
                }
            }
        });
    }
    // Trong AccountFragment.java, ngoài class AccountFragment

    private void createChatRoomIfNeeded(    int userId, int ownerId, String stadiumName, ChatRoomCreationCallback callback) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();

        long timestamp = System.currentTimeMillis();

        Map<String, Object> updates = new HashMap<>();
        updates.put("userChats/" + userId + "/" + ownerId, new ChatRoomInfo(stadiumName, timestamp, ""));
        updates.put("userChats/" + ownerId + "/" + userId, new ChatRoomInfo("Người dùng", timestamp, ""));

        dbRef.updateChildren(updates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onComplete(true);
                    } else {
                        Toast.makeText(getContext(), "Tạo phòng chat thất bại!", Toast.LENGTH_SHORT).show();
                        callback.onComplete(false);
                    }
                });
    }
    private void setupObservers() {
        // Quan sát trạng thái đăng xuất
        accountViewModel.getLogoutSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                binding.layoutLoggedIn.setVisibility(View.GONE);
                binding.layoutLoggedOut.setVisibility(View.VISIBLE);
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
            updateBiometricSwitchState(); // Cập nhật trạng thái switch
        } else {
            binding.layoutLoggedIn.setVisibility(View.GONE);
            binding.layoutLoggedOut.setVisibility(View.VISIBLE);
        }
    }

    private void setupMenu() {
        binding.menuBookingHistory.imgMenuIcon.setImageResource(R.drawable.ic_history_booking);
        binding.menuBookingHistory.tvMenuText.setText("Lịch chơi");
        binding.menuSubject.imgMenuIcon.setImageResource(R.drawable.ic_history); // Dùng icon lịch sử
        binding.menuSubject.tvMenuText.setText("Lịch sử");
        binding.menuDiscount.imgMenuIcon.setImageResource(R.drawable.ic_discount);
        binding.menuDiscount.tvMenuText.setText("Mã giảm giá");
        binding.menuChatAdmin.imgMenuIcon.setImageResource(R.drawable.ic_chat); // icon chat
        binding.menuChatAdmin.tvMenuText.setText("Chat với Admin");
    }
}