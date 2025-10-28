package com.example.sep490_mobile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import com.example.sep490_mobile.viewmodel.NotificationCountViewModel;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.sep490_mobile.databinding.ActivityMainBinding;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean isDragging = false;
    private float initialTouchX, initialTouchY;

    private NotificationCountViewModel mainViewModel;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Người dùng đã cấp quyền
                    Toast.makeText(this, "Đã cấp quyền thông báo.", Toast.LENGTH_SHORT).show();
                } else {
                    // Người dùng đã từ chối quyền
                    Toast.makeText(this, "Bạn sẽ không nhận được thông báo real-time.", Toast.LENGTH_LONG).show();
                }
            });

    private void askNotificationPermission() {
        // Chỉ chạy trên Android 13 (TIRAMISU) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Kiểm tra xem quyền đã được cấp chưa
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // Quyền đã được cấp, không cần làm gì thêm
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // (Tùy chọn) Hiển thị một giao diện giải thích tại sao bạn cần quyền này
                // nếu người dùng đã từ chối một lần trước đó.
                // Ở đây chúng ta sẽ hỏi lại trực tiếp.
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                // Hỏi quyền lần đầu tiên
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mainViewModel = new ViewModelProvider(this).get(NotificationCountViewModel.class);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_map,
                R.id.navigation_find_team,
                R.id.navigation_notifications,
                R.id.navigation_account
        ).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        Objects.requireNonNull(getSupportActionBar()).hide();

        // Yêu cầu quyền thông báo khi khởi động ứng dụng
        askNotificationPermission();

        // 🔹 Khi chọn item trong bottom nav
        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_chat) {
                // 👉 Mở ChatListActivity
                Intent intent = new Intent(MainActivity.this, ChatListActivity.class);
                startActivity(intent);
                return false; // không giữ trạng thái selected
            } else {
                // cập nhật số lượng thông báo chưa đọc khi vào tab Thông báo
                if (id == R.id.navigation_notifications) {
                    mainViewModel.fetchUnreadCount();
                }
                // Điều hướng đến các fragment khác bình thường
                navController.navigate(id);
                return true;
            }
        });

        // --- THIẾT LẬP OBSERVER ---
        setupObservers(navView);

        // 🔹 Lấy số lượng thông báo chưa đọc và lắng nghe thay đổi
        mainViewModel.fetchUnreadCount();

        // 🔹 Floating Chat Bubble
        FloatingActionButton fabChat = findViewById(R.id.fabChat);
        fabChat.setOnClickListener(v -> {
            if (!isDragging) {
                v.animate()
                        .scaleX(0.85f)
                        .scaleY(0.85f)
                        .setDuration(100)
                        .withEndAction(() -> v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                                .setInterpolator(new OvershootInterpolator())
                                .start())
                        .start();

                Intent intent = new Intent(MainActivity.this, ChatListActivity.class);
                startActivity(intent);
            }
        });

        // 🔹 Cho phép kéo nút bong bóng (ĐÃ CẬP NHẬT)
        fabChat.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;
            private float lastActionUpTime = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Lấy ra layout cha và bottom navigation view
                ConstraintLayout container = findViewById(R.id.container);
                View navView = findViewById(R.id.nav_view);

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        // Lưu vị trí ban đầu của con trỏ so với view
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isDragging = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // Tính toán vị trí mới
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        // Kiểm tra xem có phải là hành động kéo không
                        if (!isDragging && (Math.abs(event.getRawX() - initialTouchX) > 10 || Math.abs(event.getRawY() - initialTouchY) > 10)) {
                            isDragging = true;
                        }

                        // Giới hạn không cho kéo ra ngoài màn hình và không đè lên nav_view
                        // Giới hạn trái
                        newX = Math.max(0, newX);
                        // Giới hạn trên
                        newY = Math.max(0, newY);
                        // Giới hạn phải
                        newX = Math.min(container.getWidth() - v.getWidth(), newX);
                        // Giới hạn dưới (không đè lên nav_view)
                        newY = Math.min(container.getHeight() - v.getHeight() - navView.getHeight(), newY);

                        // Cập nhật vị trí của FAB
                        v.setX(newX);
                        v.setY(newY);

                        return true;

                    case MotionEvent.ACTION_UP:
                        long now = System.currentTimeMillis();
                        if (isDragging) {
                            // Hít vào cạnh gần nhất
                            float center = v.getX() + (float) v.getWidth() / 2;
                            float endPosition = (center < (float) container.getWidth() / 2) ? 0 : (container.getWidth() - v.getWidth());
                            v.animate()
                                    .x(endPosition)
                                    .setDuration(200)
                                    .start();
                        } else {
                            // Xử lý như một cú click
                            v.performClick();
                        }
                        // Reset trạng thái kéo
                        isDragging = false;
                        lastActionUpTime = now;
                        return true;
                }
                return false;
            }
        });
    }

    // --- PHƯƠNG THỨC MỚI ĐỂ QUẢN LÝ OBSERVER ---
    private void setupObservers(BottomNavigationView navView) {
        mainViewModel.unreadCount.observe(this, count -> {
            // Lấy hoặc tạo badge cho item thông báo
            BadgeDrawable badge = navView.getOrCreateBadge(R.id.navigation_notifications);

            if (count != null && count > 0) {
                badge.setVisible(true);
                badge.setNumber(count);
            } else {
                badge.setVisible(false);
            }
        });
    }



    @Override
    protected void onResume() {
        super.onResume();
        // Mỗi khi người dùng quay lại MainActivity, hãy cập nhật lại số lượng
        mainViewModel.fetchUnreadCount();
    }

}