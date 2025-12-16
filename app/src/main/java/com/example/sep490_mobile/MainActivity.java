package com.example.sep490_mobile;

import android. Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view. MotionEvent;
import android. view.View;
import android. view.animation. OvershootInterpolator;
import android.widget. FrameLayout;
import android. widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx. navigation.Navigation;
import androidx. navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.sep490_mobile.databinding.ActivityMainBinding;
import com.example.sep490_mobile.viewmodel.NotificationCountViewModel;
import com.google. android.material.badge.BadgeDrawable;
import com.google. android.material.bottomnavigation. BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com. google.firebase.database.DataSnapshot;
import com. google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database. FirebaseDatabase;
import com. google.firebase.database.ValueEventListener;

import java.util. Objects;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private boolean isDragging = false;
    private float initialTouchX, initialTouchY;
    private IncomingCallListener incomingCallListener;
    private NotificationCountViewModel mainViewModel;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Đã cấp quyền thông báo.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Bạn sẽ không nhận được thông báo real-time.", Toast.LENGTH_LONG).show();
                }
            });

    private DatabaseReference unreadRef;
    private ValueEventListener unreadCountListener;
    private TextView fabBadge;
    private FrameLayout fabChatContainer;
    private FloatingActionButton fabChat;

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES. TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager. PERMISSION_GRANTED) {
                // Quyền đã được cấp
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
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

        mainViewModel = new ViewModelProvider(this).get(NotificationCountViewModel. class);

        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_map, R.id.navigation_find_team,
                R.id.navigation_notifications, R.id.navigation_account
        ).build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI. setupWithNavController(binding.navView, navController);

        Objects.requireNonNull(getSupportActionBar()).hide();

        askNotificationPermission();

        // ✅ LẤY USERID THẬT TỪ SHAREDPREFERENCES
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId != -1) {
            String myUserId = String.valueOf(userId);
            incomingCallListener = new IncomingCallListener(this, myUserId);
            android.util.Log.d(TAG, "✅ Started IncomingCallListener for userId: " + myUserId);
        } else {
            android.util.Log.e(TAG, "❌ Cannot start IncomingCallListener: user_id not found!");
            Toast.makeText(this, "Lỗi:  Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
        }

        // Ánh xạ FAB và Badge
        fabChat = findViewById(R.id.fabChat);
        fabBadge = findViewById(R.id.fab_badge);
        fabChatContainer = findViewById(R.id. fabChatContainer);

        // Lắng nghe tổng số tin chưa đọc
        setupUnreadCountListener();

        // Navigation bottom menu
        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_chat) {
                Intent intent = new Intent(MainActivity.this, ChatListActivity.class);
                startActivity(intent);
                return false;
            } else {
                if (id == R.id.navigation_notifications) {
                    mainViewModel.fetchUnreadCount();
                }
                navController.navigate(id);
                return true;
            }
        });

        // Observer cho badge notification
        setupObservers(navView);

        // Lấy số lượng thông báo chưa đọc và lắng nghe thay đổi
        mainViewModel.fetchUnreadCount();

        fabChat.setOnClickListener(v -> {
            if (! isDragging) {
                v.animate()
                        .scaleX(0.85f)
                        .scaleY(0.85f)
                        .setDuration(100)
                        .withEndAction(() -> v.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                . setDuration(150)
                                .setInterpolator(new OvershootInterpolator())
                                .start())
                        .start();

                Intent intent = new Intent(MainActivity.this, ChatListActivity.class);
                startActivity(intent);

                resetAllUnreadCounts();
            }
        });

        // Kéo thả bubble chat + snap về cạnh SÁT mép (không margin)
        fabChat.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ConstraintLayout container = findViewById(R.id.container);
                View navView = findViewById(R.id. nav_view);

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = fabChatContainer.getX() - event.getRawX();
                        dY = fabChatContainer.getY() - event.getRawY();
                        initialTouchX = event.getRawX();
                        initialTouchY = event. getRawY();
                        isDragging = false;
                        return true;

                    case MotionEvent. ACTION_MOVE:
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        if (! isDragging && (Math.abs(event.getRawX() - initialTouchX) > 10
                                || Math.abs(event.getRawY() - initialTouchY) > 10)) {
                            isDragging = true;
                        }

                        // SÁT mép, không margin
                        newX = Math.max(0, newX);
                        newY = Math.max(0, newY);
                        newX = Math.min(container.getWidth() - fabChatContainer.getWidth(), newX);
                        newY = Math.min(container. getHeight() - fabChatContainer.getHeight()
                                - navView.getHeight(), newY);

                        fabChatContainer. setX(newX);
                        fabChatContainer.setY(newY);
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (isDragging) {
                            // Snap về cạnh SÁT mép
                            float center = fabChatContainer.getX() + (float) fabChatContainer.getWidth() / 2;
                            float endPosition = (center < (float) container.getWidth() / 2)
                                    ? 0
                                    : (container.getWidth() - fabChatContainer.getWidth());
                            fabChatContainer. animate()
                                    .x(endPosition)
                                    .setDuration(200)
                                    .start();
                        } else {
                            v.performClick();
                        }
                        isDragging = false;
                        return true;
                }
                return false;
            }
        });
    }

    // === CÁC HÀM CHO BADGE ===

    private void setupUnreadCountListener() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy user_id", Toast.LENGTH_SHORT).show();
            return;
        }

        unreadRef = FirebaseDatabase.getInstance()
                .getReference("unreadMessages")
                .child(String.valueOf(userId));

        if (unreadCountListener != null) {
            unreadRef.removeEventListener(unreadCountListener);
        }

        unreadCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalUnreadCount = 0;
                if (snapshot.exists()) {
                    Object value = snapshot.getValue();
                    if (value instanceof Long) {
                        totalUnreadCount = (Long) value;
                    } else if (value instanceof Integer) {
                        totalUnreadCount = ((Integer) value).longValue();
                    }
                }
                updateChatBadge((int) totalUnreadCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        unreadRef.addValueEventListener(unreadCountListener);
    }

    private void updateChatBadge(int count) {
        if (fabBadge == null) return;
        if (count > 0) {
            fabBadge.setText(String.valueOf(count));
            fabBadge.setVisibility(View.VISIBLE);
        } else {
            fabBadge.setVisibility(View. GONE);
        }
    }

    private void resetAllUnreadCounts() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) return;

        DatabaseReference refToReset = FirebaseDatabase.getInstance()
                .getReference("unreadMessages")
                .child(String.valueOf(userId));
        refToReset.setValue(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unreadRef != null && unreadCountListener != null) {
            unreadRef.removeEventListener(unreadCountListener);
        }

        // ✅ Cleanup IncomingCallListener

    }

    // --- PHƯƠNG THỨC MỚI ĐỂ QUẢN LÝ OBSERVER ---
    private void setupObservers(BottomNavigationView navView) {
        mainViewModel.unreadCount.observe(this, count -> {
            BadgeDrawable badge = navView. getOrCreateBadge(R.id.navigation_notifications);

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
        mainViewModel.fetchUnreadCount();
    }
}