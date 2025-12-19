package com.example.sep490_mobile;

import android.Manifest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.webrtc.SurfaceViewRenderer;

import java.util.HashMap;
import java.util.Map;

/**
 * VideoCallActivity
 * - Incoming từ web: chỉ audio (IS_VIDEO_CALL=false) vì đã lọc ở IncomingCallListener.
 * - Outgoing từ app: giữ nguyên (video/audio tùy Intent).
 * - Hangup đồng bộ, guard Accept một lần, dọn phiên sau cùng.
 */
public class VideoCallActivity extends AppCompatActivity {
    private static final String TAG = "VideoCallActivity";

    private RtcManager rtcManager;
    private SurfaceViewRenderer localView, remoteView;
    private String senderId, receiverId, chatId;
    private String senderName, receiverName;
    private boolean isVideoCall;
    private boolean isOutgoing;

    private boolean hasAccepted = false;
    private boolean hasEnded = false;

    // Outgoing UI
    private TextView peerNameView;
    private ImageButton btnEndCall;

    // Incoming UI
    private TextView tvCallerAvatar, tvCallerName, tvCallType, tvRinging;
    private ImageButton btnAccept, btnDecline;

    // Firebase
    private DatabaseReference rtcOfferRef;
    private com.google.firebase.database.ValueEventListener offerListener;
    private DatabaseReference hangupRef;
    private com.google.firebase.database.ValueEventListener hangupListener;
    private DatabaseReference webrtcRootRef;

    private String myUserId;

    // Offer cache
    private String pendingOfferSdp;
    private String pendingOfferType;

    // Permissions
    private ActivityResultLauncher<String[]> permissionLauncher;
    private Runnable permissionGrantedAction;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        senderId = getIntent().getStringExtra("SENDER_ID");
        senderName = getIntent().getStringExtra("SENDER_NAME");
        receiverId = getIntent().getStringExtra("RECEIVER_ID");
        receiverName = getIntent().getStringExtra("RECEIVER_NAME");
        isVideoCall = getIntent().getBooleanExtra("IS_VIDEO_CALL", true);
        isOutgoing = getIntent().getBooleanExtra("IS_OUTGOING", false);

        if (senderId == null || receiverId == null) {
            Log.e(TAG, "Missing SENDER_ID/RECEIVER_ID");
            Toast.makeText(this, "Thiếu thông tin người gọi", Toast.LENGTH_SHORT).show();
        }

        chatId = generateChatId(senderId != null ? senderId : "", receiverId != null ? receiverId : "");
        webrtcRootRef = FirebaseDatabase.getInstance().getReference("webrtc").child(chatId);

        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean mic = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.RECORD_AUDIO, false));
                    boolean cam = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.CAMERA, false));
                    if (isOutgoing && isVideoCall && !cam) {
                        isVideoCall = false; // fallback video -> audio
                        Toast.makeText(this, "Không có quyền camera, sẽ gọi âm thanh", Toast.LENGTH_SHORT).show();
                    }
                    if (!mic) {
                        Toast.makeText(this, "Thiếu quyền micro", Toast.LENGTH_LONG).show();
                    }
                    if (permissionGrantedAction != null) {
                        permissionGrantedAction.run();
                        permissionGrantedAction = null;
                    }
                }
        );

        if (isOutgoing) {
            requestPermissionsThen(() -> {
                setContentView(R.layout.activity_video_call);
                setupOutgoingUi();
                startOutgoingCall();
            }, /*needCamera*/ isVideoCall);
        } else {
            // Incoming từ web đã được lọc chỉ audio ở IncomingCallListener
            isVideoCall = false;
            setContentView(R.layout.activity_incoming_call);
            setupIncomingUi();
        }
    }

    private void requestPermissionsThen(Runnable onGranted, boolean needCamera) {
        this.permissionGrantedAction = onGranted;
        boolean micGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;
        boolean camGranted = !needCamera || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;

        if (micGranted && camGranted) {
            if (permissionGrantedAction != null) {
                permissionGrantedAction.run();
                permissionGrantedAction = null;
            }
        } else {
            if (needCamera) {
                permissionLauncher.launch(new String[]{ Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA });
            } else {
                permissionLauncher.launch(new String[]{ Manifest.permission.RECORD_AUDIO });
            }
        }
    }

    // ========= OUTGOING =========
    private void setupOutgoingUi() {
        localView = findViewById(R.id.local_view);
        remoteView = findViewById(R.id.remote_view);
        peerNameView = findViewById(R.id.tv_peer_name);
        btnEndCall = findViewById(R.id.btn_end_call);

        rtcManager = new RtcManager(
                this,
                localView,
                remoteView,
                chatId,
                senderId,
                senderName,
                isVideoCall
        );

        String displayName = (receiverName != null && !receiverName.isEmpty()) ? receiverName : receiverId;
        peerNameView.setText((isVideoCall ? "Cuộc gọi Video với " : "Cuộc gọi Audio với ") + displayName);

        myUserId = senderId;
        initHangupListener(true);

        btnEndCall.setOnClickListener(v -> {
            if (hasEnded) return;
            hasEnded = true;
            sendHangup();
            safeEndCall();
        });
    }

    private void startOutgoingCall() {
        if (rtcManager != null) rtcManager.startCall();
    }

    // ========= INCOMING (audio-only) =========
    private void setupIncomingUi() {
        tvCallerAvatar = findViewById(R.id.tv_caller_avatar);
        tvCallerName = findViewById(R.id.tv_caller_name);
        tvCallType = findViewById(R.id.tv_call_type);
        tvRinging = findViewById(R.id.tv_ringing);
        btnAccept = findViewById(R.id.btn_accept);
        btnDecline = findViewById(R.id.btn_decline);

        tvCallType.setText("Cuộc gọi Audio");
        tvRinging.setText("Đang gọi đến...");

        rtcOfferRef = webrtcRootRef.child("offer");
        offerListener = new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snap) {
                if (!snap.exists()) {
                    String displayName = (senderName != null && !senderName.isEmpty()) ? senderName : senderId;
                    tvCallerName.setText(displayName);
                    tvCallerAvatar.setText(getInitial(displayName));
                    return;
                }
                String offerCallerName = snap.child("callerName").getValue(String.class);
                String offerFromId = snap.child("from").getValue(String.class);
                pendingOfferSdp = snap.child("sdp").getValue(String.class);
                pendingOfferType = snap.child("type").getValue(String.class);

                String displayName = (offerCallerName != null && !offerCallerName.isEmpty()) ? offerCallerName : offerFromId;
                tvCallerName.setText(displayName);
                tvCallerAvatar.setText(getInitial(displayName));

                senderName = (offerCallerName != null) ? offerCallerName : senderName;
                senderId = (offerFromId != null) ? offerFromId : senderId;
            }
            @Override public void onCancelled(com.google.firebase.database.DatabaseError error) {
                Log.e(TAG, "offer listener cancelled: " + error.getMessage());
            }
        };
        rtcOfferRef.addValueEventListener(offerListener);

        myUserId = receiverId;
        initHangupListener(true);

        btnAccept.setOnClickListener(v -> {
            if (hasAccepted) return;
            hasAccepted = true;
            requestPermissionsThen(this::doAcceptAfterPermission, /*needCamera*/ false);
        });

        btnDecline.setOnClickListener(v -> {
            webrtcRootRef.child("declined").setValue(new HashMap<String, Object>() {{
                put("val", true);
                put("ts", System.currentTimeMillis());
            }});
            safeEndCall();
        });
    }

    private void doAcceptAfterPermission() {
        if (rtcOfferRef != null && offerListener != null) rtcOfferRef.removeEventListener(offerListener);

        if (pendingOfferSdp == null || pendingOfferType == null) {
            rtcOfferRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override public void onDataChange(com.google.firebase.database.DataSnapshot snap) {
                    pendingOfferSdp = snap.child("sdp").getValue(String.class);
                    pendingOfferType = snap.child("type").getValue(String.class);
                    if (pendingOfferSdp == null || pendingOfferType == null) {
                        Toast.makeText(VideoCallActivity.this, "Cuộc gọi đã kết thúc", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    buildCalleeUiAndStart();
                }
                @Override public void onCancelled(com.google.firebase.database.DatabaseError error) {
                    Toast.makeText(VideoCallActivity.this, "Không thể nhận cuộc gọi (SDP lỗi)", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            buildCalleeUiAndStart();
        }
    }

    private void buildCalleeUiAndStart() {
        setContentView(R.layout.activity_video_call);

        localView = findViewById(R.id.local_view);
        remoteView = findViewById(R.id.remote_view);
        peerNameView = findViewById(R.id.tv_peer_name);
        btnEndCall = findViewById(R.id.btn_end_call);

        rtcManager = new RtcManager(
                VideoCallActivity.this,
                localView,
                remoteView,
                chatId,
                receiverId,   // callee
                receiverName,
                false         // incoming audio-only
        );

        String headerName = (senderName != null && !senderName.isEmpty()) ? senderName : senderId;
        peerNameView.setText("Cuộc gọi Audio với " + headerName);

        btnEndCall.setOnClickListener(end -> {
            if (hasEnded) return;
            hasEnded = true;
            sendHangup();
            safeEndCall();
        });

        try {
            rtcManager.receiveCall(pendingOfferSdp, pendingOfferType);
        } catch (Exception e) {
            Log.e(TAG, "receiveCall error: " + e.getMessage());
            Toast.makeText(this, "Lỗi kết nối cuộc gọi", Toast.LENGTH_SHORT).show();
        }
    }

    private void initHangupListener(boolean clearOldFirst) {
        hangupRef = webrtcRootRef.child("hangup");
        if (clearOldFirst) hangupRef.removeValue();
        hangupListener = new com.google.firebase.database.ValueEventListener() {
            @Override public void onDataChange(com.google.firebase.database.DataSnapshot snap) {
                if (!snap.exists()) return;
                String by = snap.child("by").getValue(String.class);
                Long ts = snap.child("ts").getValue(Long.class);
                long now = System.currentTimeMillis();
                if (by != null && !by.equals(myUserId) && ts != null && (now - ts) < 5000) {
                    Log.d(TAG, "Remote hung up (fresh), closing locally.");
                    safeEndCall();
                } else {
                    Log.d(TAG, "Ignore stale hangup");
                }
            }
            @Override public void onCancelled(com.google.firebase.database.DatabaseError error) {}
        };
        hangupRef.addValueEventListener(hangupListener);
    }

    private void sendHangup() {
        if (hangupRef != null) {
            Map<String, Object> val = new HashMap<>();
            val.put("by", myUserId);
            val.put("ts", System.currentTimeMillis());
            hangupRef.setValue(val);
        }
    }

    private void safeEndCall() {
        try {
            if (rtcOfferRef != null && offerListener != null) rtcOfferRef.removeEventListener(offerListener);
            if (hangupRef != null && hangupListener != null) hangupRef.removeEventListener(hangupListener);
        } catch (Exception ignored) {}
        if (rtcManager != null) {
            rtcManager.cleanUp();
            rtcManager = null;
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try { webrtcRootRef.removeValue(); } catch (Exception ignored) {}
            if (!isFinishing()) finish();
        }, 600);
    }

    private String getInitial(String name) {
        return (name == null || name.isEmpty()) ? "?" : name.substring(0, 1).toUpperCase();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (rtcOfferRef != null && offerListener != null) rtcOfferRef.removeEventListener(offerListener);
            if (hangupRef != null && hangupListener != null) hangupRef.removeEventListener(hangupListener);
        } catch (Exception ignored) {}
        if (rtcManager != null) {
            rtcManager.cleanUp();
            rtcManager = null;
        }
    }

    private String generateChatId(String id1, String id2) {
        return id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
    }
}