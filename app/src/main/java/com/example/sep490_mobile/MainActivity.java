package com.example.sep490_mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_map,
                R.id.navigation_notifications,
                R.id.navigation_account
        ).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        Objects.requireNonNull(getSupportActionBar()).hide();

        // 🔹 Khi chọn item trong bottom nav
        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_chat) {
                // 👉 Mở ChatListActivity
                Intent intent = new Intent(MainActivity.this, ChatListActivity.class);
                startActivity(intent);
                return false; // không giữ trạng thái selected
            } else {
                // xử lý bằng navController bình thường
                navController.navigate(id);
                return true;
            }
        });

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
}