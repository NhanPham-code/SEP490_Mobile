package com.example.sep490_mobile;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.webrtc.SurfaceViewRenderer;

import java.util.HashMap;

public class VideoCallActivity extends AppCompatActivity {
    private static final String TAG = "VideoCallActivity";

    private RtcManager rtcManager;
    private SurfaceViewRenderer localView, remoteView;
    private String senderId, receiverId, chatId;
    private String senderName, receiverName;
    private boolean isVideoCall;
    private boolean isOutgoing;

    // Outgoing UI
    private TextView peerNameView;
    private ImageButton btnEndCall;

    // Incoming UI
    private TextView tvCallerAvatar, tvCallerName, tvCallType, tvRinging;
    private ImageButton btnAccept, btnDecline;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        senderId = getIntent().getStringExtra("SENDER_ID");
        senderName = getIntent().getStringExtra("SENDER_NAME");
        receiverId = getIntent().getStringExtra("RECEIVER_ID");
        receiverName = getIntent().getStringExtra("RECEIVER_NAME");
        isVideoCall = getIntent().getBooleanExtra("IS_VIDEO_CALL", true);
        isOutgoing = getIntent().getBooleanExtra("IS_OUTGOING", true);

        chatId = generateChatId(senderId, receiverId);

        if (isOutgoing) {
            setContentView(R.layout.activity_video_call);
            setupOutgoingUi();
            startOutgoingCall();
        } else {
            setContentView(R.layout.activity_incoming_call);
            setupIncomingUi();
        }
    }

    private void setupOutgoingUi() {
        localView = findViewById(R.id.local_view);
        remoteView = findViewById(R.id.remote_view);
        peerNameView = findViewById(R.id.tv_peer_name);
        btnEndCall = findViewById(R.id.btn_end_call);

        // Khởi tạo RtcManager với người gọi (caller)
        rtcManager = new RtcManager(
                this,
                localView,
                remoteView,
                chatId,
                senderId,
                senderName,
                isVideoCall
        );

        String callTypeText = isVideoCall ? "Cuộc gọi Video" : "Cuộc gọi Audio";
        String displayName = receiverName != null && !receiverName.isEmpty() ? receiverName : receiverId;
        peerNameView.setText(callTypeText + " với " + displayName);

        btnEndCall.setOnClickListener(v -> {
            rtcManager.cleanUp();
            finish();
        });
    }

    private void startOutgoingCall() {
        if (rtcManager != null) {
            rtcManager.startCall(); // tạo offer, gửi callerName
        }
    }

    private void setupIncomingUi() {
        tvCallerAvatar = findViewById(R.id.tv_caller_avatar);
        tvCallerName = findViewById(R.id.tv_caller_name);
        tvCallType = findViewById(R.id.tv_call_type);
        tvRinging = findViewById(R.id.tv_ringing);
        btnAccept = findViewById(R.id.btn_accept);
        btnDecline = findViewById(R.id.btn_decline);

        tvCallType.setText(isVideoCall ? "Cuộc gọi Video" : "Cuộc gọi Audio");
        tvRinging.setText("Đang gọi đến...");

        // Lắng nghe offer để lấy tên người gọi + SDP/type
        DatabaseReference rtcOfferRef = FirebaseDatabase.getInstance()
                .getReference("webrtc")
                .child(chatId)
                .child("offer");

        rtcOfferRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snap) {
                if (!snap.exists()) {
                    // Fallback nếu chưa có offer: dùng giá trị từ Intent
                    String displayName = (senderName != null && !senderName.isEmpty()) ? senderName : senderId;
                    tvCallerName.setText(displayName);
                    tvCallerAvatar.setText(getInitial(displayName));
                    return;
                }

                String offerCallerName = snap.child("callerName").getValue(String.class);
                String offerFromId = snap.child("from").getValue(String.class);
                String offerSdp = snap.child("sdp").getValue(String.class);
                String offerType = snap.child("type").getValue(String.class);

                // Tên hiển thị: ưu tiên callerName, fallback ID
                String displayName = (offerCallerName != null && !offerCallerName.isEmpty()) ? offerCallerName : offerFromId;
                tvCallerName.setText(displayName);
                tvCallerAvatar.setText(getInitial(displayName));

                // Cập nhật người gọi theo offer để tránh sai lệch
                senderName = offerCallerName != null ? offerCallerName : senderName;
                senderId = offerFromId != null ? offerFromId : senderId;

                // Accept: chuyển sang layout video và nhận cuộc gọi với tư cách CALLEE (receiver)
                btnAccept.setOnClickListener(v -> {
                    setContentView(R.layout.activity_video_call);

                    // Ánh xạ lại view video
                    localView = findViewById(R.id.local_view);
                    remoteView = findViewById(R.id.remote_view);
                    peerNameView = findViewById(R.id.tv_peer_name);
                    btnEndCall = findViewById(R.id.btn_end_call);

                    // Callee: phải dùng receiverId/receiverName (người đang nhận cuộc gọi)
                    rtcManager = new RtcManager(
                            VideoCallActivity.this,
                            localView,
                            remoteView,
                            chatId,
                            receiverId,
                            receiverName,
                            isVideoCall
                    );

                    String callTypeText = isVideoCall ? "Cuộc gọi Video" : "Cuộc gọi Audio";
                    String headerName = senderName != null && !senderName.isEmpty() ? senderName : senderId;
                    peerNameView.setText(callTypeText + " với " + headerName);

                    btnEndCall.setOnClickListener(end -> {
                        rtcManager.cleanUp();
                        finish();
                    });

                    // Nhận cuộc gọi (callee): setRemote(offer) -> createAnswer -> ghi answer (có calleeName)
                    rtcManager.receiveCall(offerSdp, offerType);
                });
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                Log.e(TAG, "RTC offer listener cancelled: " + error.getMessage());
            }
        });

        btnDecline.setOnClickListener(v -> {
            // Gửi tín hiệu declined để caller/web biết
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

    private String getInitial(String name) {
        if (name == null || name.isEmpty()) return "?";
        return name.substring(0, 1).toUpperCase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rtcManager != null) rtcManager.cleanUp();
    }

    private String generateChatId(String id1, String id2) {
        return id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
    }
}