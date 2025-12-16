package com.example.sep490_mobile;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class IncomingCallActivity extends AppCompatActivity {
    private static final String TAG = "IncomingCallActivity";

    private String callerId, callerName, chatId, offerSdp, offerType, myUserId;
    private boolean isVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_call);

        // Nhận dữ liệu từ Intent
        callerId = getIntent().getStringExtra("CALLER_ID");
        callerName = getIntent().getStringExtra("CALLER_NAME");
        chatId = getIntent().getStringExtra("CHAT_ID");
        offerSdp = getIntent().getStringExtra("OFFER_SDP");
        offerType = getIntent().getStringExtra("OFFER_TYPE");
        isVideo = getIntent().getBooleanExtra("IS_VIDEO", true);
        myUserId = getMyUserId(); // từ extra hoặc SharedPreferences

        Log.d(TAG, "📞 Incoming call from: " + (callerName != null ? callerName : callerId));

        // Ánh xạ views
        TextView tvCallerName = findViewById(R.id.tv_caller_name);
        TextView tvCallType = findViewById(R.id.tv_call_type);
        TextView tvCallerAvatar = findViewById(R.id.tv_caller_avatar);
        ImageButton btnAccept = findViewById(R.id.btn_accept);
        ImageButton btnDecline = findViewById(R.id.btn_decline);

        // Hiển thị thông tin
        tvCallerName.setText(callerName != null && !callerName.isEmpty() ? callerName : "Người dùng");
        tvCallType.setText(isVideo ? "📹 Cuộc gọi Video" : "📞 Cuộc gọi Âm thanh");

        tvCallerAvatar.setText(getInitial(callerName != null ? callerName : callerId));
        makeCircleButton(tvCallerAvatar, 0xFF667eea); // Avatar màu tím
        makeCircleButton(btnAccept, 0xFF4CAF50);      // Accept màu xanh
        makeCircleButton(btnDecline, 0xFFF44336);     // Decline màu đỏ

        // Accept: mở VideoCallActivity ở chế độ RECEIVE (callee)
        // Accept: mở VideoCallActivity ở chế độ RECEIVE (callee)
        btnAccept.setOnClickListener(v -> {
            Log.d(TAG, "✅ User accepted call");

            Intent intent = new Intent(this, VideoCallActivity.class);

            // caller (đối phương)
            intent.putExtra("SENDER_ID", callerId);
            intent.putExtra("SENDER_NAME", callerName);

            // callee (mình)
            intent.putExtra("RECEIVER_ID", myUserId);
            intent.putExtra("RECEIVER_NAME", getMyFullName());

            intent.putExtra("CHAT_ID", chatId);
            intent.putExtra("OFFER_SDP", offerSdp);
            intent.putExtra("OFFER_TYPE", offerType);

            // Khớp đúng khóa mà VideoCallActivity đọc
            intent.putExtra("IS_VIDEO_CALL", isVideo);
            intent.putExtra("IS_OUTGOING", false);

            startActivity(intent);
            finish();
        });

        // Decline: ghi declined và đóng
        btnDecline.setOnClickListener(v -> {
            Log.d(TAG, "❌ User declined call");

            FirebaseDatabase.getInstance()
                    .getReference("webrtc")
                    .child(chatId)
                    .child("declined")
                    .setValue(new HashMap<String, Object>() {{
                        put("val", true);
                        put("ts", System.currentTimeMillis());
                    }});

            finish();
        });
    }

    private String getInitial(String nameOrId) {
        if (nameOrId == null || nameOrId.isEmpty()) return "?";
        return nameOrId.substring(0, 1).toUpperCase();
    }

    private void makeCircleButton(android.view.View view, int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(color);
        view.setBackground(shape);
        view.setElevation(12f);
    }

    private String getMyUserId() {
        String explicit = getIntent().getStringExtra("MY_USER_ID");
        if (explicit != null) return explicit;
        return getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("user_id", "UNKNOWN_USER");
    }

    private String getMyFullName() {
        return getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
                .getString("full_name", "Bạn");
    }
}