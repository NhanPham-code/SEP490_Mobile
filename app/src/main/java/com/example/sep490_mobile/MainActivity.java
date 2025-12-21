package com.example.sep490_mobile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.example.sep490_mobile.utils.NotificationConnector;
import com.example.sep490_mobile.utils.NotificationHelper;
import com.example.sep490_mobile.viewmodel.NotificationCountViewModel;
import com.example.sep490_mobile.viewmodel.BookingViewModel;
import com.example.sep490_mobile.call.CallManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity_VNPAY";

    private ActivityMainBinding binding;
    private boolean isDragging = false;
    private float initialTouchX, initialTouchY;

    private NotificationCountViewModel mainViewModel;
    private BookingViewModel bookingViewModel;

    private DatabaseReference unreadRef;
    private ValueEventListener unreadCountListener;

    private TextView fabBadge;
    private FrameLayout fabChatContainer;
    private FloatingActionButton fabChat;

    /* ================= Permission ================= */

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                Toast.makeText(
                        this,
                        isGranted ? "Đã cấp quyền thông báo." : "Bạn sẽ không nhận được thông báo real-time.",
                        Toast.LENGTH_SHORT
                ).show();
            });

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    /* ================= onCreate ================= */

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mainViewModel = new ViewModelProvider(this).get(NotificationCountViewModel.class);
        bookingViewModel = new ViewModelProvider(this).get(BookingViewModel.class);

        setupNavigation();
        askNotificationPermission();
        startSignalRAndCallManager();
        setupFabChat();
        setupUnreadCountListener();
        setupObservers(binding.navView);

        mainViewModel.fetchUnreadCount();
    }

    /* ================= Navigation ================= */

    private void setupNavigation() {
        BottomNavigationView navView = findViewById(R.id.nav_view);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home,
                R.id.navigation_map,
                R.id.navigation_find_team,
                R.id.navigation_notifications,
                R.id.navigation_account
        ).build();

        NavController navController =
                Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        NavigationUI.setupWithNavController(navView, navController);
        Objects.requireNonNull(getSupportActionBar()).hide();

        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_chat) {
                startActivity(new Intent(this, ChatListActivity.class));
                return false;
            } else {
                if (id == R.id.navigation_notifications) {
                    mainViewModel.fetchUnreadCount();
                }
                navController.navigate(id);
                return true;
            }
        });
    }

    /* ================= SignalR + Call ================= */

    private void startSignalRAndCallManager() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", null);
        int userId = prefs.getInt("user_id", -1);

        // SignalR
        if (accessToken != null && !accessToken.isEmpty() && userId > 0) {
            NotificationConnector.getInstance().startConnection(accessToken, userId);
        } else {
            Log.w(TAG, "Could not start SignalR: missing accessToken or userId");
        }

        // Incoming Call (singleton)
        if (userId > 0) {
            CallManager.start(getApplicationContext(), String.valueOf(userId));
            Log.d(TAG, "✅ CallManager started for userId: " + userId);
        } else {
            Log.e(TAG, "❌ Cannot start CallManager: user_id not found!");
        }
    }

    /* ================= FAB CHAT ================= */

    private void setupFabChat() {
        fabChat = findViewById(R.id.fabChat);
        fabBadge = findViewById(R.id.fab_badge);
        fabChatContainer = findViewById(R.id.fabChatContainer);

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

                startActivity(new Intent(this, ChatListActivity.class));
                resetAllUnreadCounts();
            }
        });

        fabChat.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ConstraintLayout container = findViewById(R.id.container);
                View bottomNav = findViewById(R.id.nav_view);

                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = fabChatContainer.getX() - event.getRawX();
                        dY = fabChatContainer.getY() - event.getRawY();
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        isDragging = false;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        if (!isDragging &&
                                (Math.abs(event.getRawX() - initialTouchX) > 10 ||
                                        Math.abs(event.getRawY() - initialTouchY) > 10)) {
                            isDragging = true;
                        }

                        newX = Math.max(0, Math.min(container.getWidth() - fabChatContainer.getWidth(), newX));
                        newY = Math.max(0, Math.min(
                                container.getHeight() - fabChatContainer.getHeight() - bottomNav.getHeight(),
                                newY
                        ));

                        fabChatContainer.setX(newX);
                        fabChatContainer.setY(newY);
                        return true;

                    case MotionEvent.ACTION_UP:
                        if (isDragging) {
                            float center = fabChatContainer.getX() + fabChatContainer.getWidth() / 2f;
                            float endX = center < container.getWidth() / 2f
                                    ? 0
                                    : container.getWidth() - fabChatContainer.getWidth();

                            fabChatContainer.animate().x(endX).setDuration(200).start();
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

    /* ================= Firebase Unread ================= */

    private void setupUnreadCountListener() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) return;

        unreadRef = FirebaseDatabase.getInstance()
                .getReference("unreadMessages")
                .child(String.valueOf(userId));

        unreadCountListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long total = 0;
                if (snapshot.exists() && snapshot.getValue() instanceof Number) {
                    total = ((Number) snapshot.getValue()).longValue();
                }
                updateChatBadge((int) total);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };

        unreadRef.addValueEventListener(unreadCountListener);
    }

    private void updateChatBadge(int count) {
        if (fabBadge == null) return;
        if (count > 0) {
            fabBadge.setText(String.valueOf(count));
            fabBadge.setVisibility(View.VISIBLE);
        } else {
            fabBadge.setVisibility(View.GONE);
        }
    }

    private void resetAllUnreadCounts() {
        int userId = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getInt("user_id", -1);
        if (userId != -1) {
            FirebaseDatabase.getInstance()
                    .getReference("unreadMessages")
                    .child(String.valueOf(userId))
                    .setValue(0);
        }
    }

    /* ================= Notification Badge ================= */

    private void setupObservers(BottomNavigationView navView) {
        mainViewModel.unreadCount.observe(this, count -> {
            BadgeDrawable badge = navView.getOrCreateBadge(R.id.navigation_notifications);
            if (count != null && count > 0) {
                badge.setVisible(true);
                badge.setNumber(count);
            } else {
                badge.setVisible(false);
            }
        });

        // Lắng nghe thông báo từ SignalR gửi về thông qua LiveData trong Connector
        NotificationConnector.getInstance().newNotificationReceived.observe(this, notificationSignalRDTO -> {
            if (notificationSignalRDTO != null) {
                NotificationHelper notificationHelper = new NotificationHelper(this);
                // Tạo channel (cần thiết cho Android O trở lên)
                notificationHelper.createNotificationChannel();
                // Hiển thị thông báo
                notificationHelper.showNotification(notificationSignalRDTO);
            }
        });
    }

    /* ================= VNPay ================= */

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleVnPayResult(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mainViewModel.fetchUnreadCount();
        handleVnPayResult(getIntent());
    }

    private void handleVnPayResult(Intent intent) {
        if (intent == null) return;

        Uri data = intent.getData();
        if (data != null &&
                "sep490".equals(data.getScheme()) &&
                "payment_return".equals(data.getHost())) {

            String code = data.getQueryParameter("vnp_ResponseCode");
            String ref = data.getQueryParameter("vnp_TxnRef");
            String info = data.getQueryParameter("vnp_OrderInfo");

            if (code != null && ref != null && info != null) {
                try {
                    int entityId = Integer.parseInt(ref);
                    String type = info.startsWith("MonthlyBooking:")
                            ? "MonthlyBooking" : "Booking";

                    bookingViewModel.updatePaymentStatus(entityId, code, type);
                    intent.setData(null);
                } catch (Exception e) {
                    Log.e(TAG, "VNPay parse error", e);
                }
            }
        }
    }

    /* ================= Lifecycle ================= */

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (unreadRef != null && unreadCountListener != null) {
            unreadRef.removeEventListener(unreadCountListener);
        }
        // ❌ KHÔNG stop CallManager ở đây
    }
}
