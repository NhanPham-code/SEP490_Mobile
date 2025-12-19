package com.example.sep490_mobile;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * IncomingCallListener
 * - Bỏ qua (IGNORE) mọi offer VIDEO từ web/app (không mở Activity, không viết declined).
 * - Chỉ mở Activity cho offer AUDIO.
 */
public class IncomingCallListener {
    private static final String TAG = "IncomingCallListener";

    private final DatabaseReference userChatsRef;
    private final String myUserId;
    private final Context context;

    private ValueEventListener userChatsListener;
    private final Map<String, DatabaseReference> offerRefs = new HashMap<>();
    private final Map<String, ValueEventListener> offerListeners = new HashMap<>();

    public IncomingCallListener(Context context, String myUserId) {
        this.context = context.getApplicationContext();
        this.myUserId = myUserId;
        this.userChatsRef = FirebaseDatabase.getInstance()
                .getReference("userChats")
                .child(myUserId);
        startListening();
    }

    private void startListening() {
        Log.d(TAG, "👂 Listening incoming calls for user: " + myUserId);

        userChatsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                for (DataSnapshot chatSnap : snapshot.getChildren()) {
                    String otherUserId = chatSnap.getKey();
                    if (otherUserId == null) continue;

                    String chatId = generateChatId(myUserId, otherUserId);
                    if (!offerListeners.containsKey(chatId)) {
                        listenForOffer(chatId);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "userChats listener cancelled: " + error.getMessage());
            }
        };

        userChatsRef.addValueEventListener(userChatsListener);
    }

    private void listenForOffer(String chatId) {
        DatabaseReference offerRef = FirebaseDatabase.getInstance()
                .getReference("webrtc").child(chatId).child("offer");

        ValueEventListener offerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                String from = snapshot.child("from").getValue(String.class);
                String callerName = snapshot.child("callerName").getValue(String.class);
                String sdp = snapshot.child("sdp").getValue(String.class);
                String type = snapshot.child("type").getValue(String.class);
                Boolean isVideo = snapshot.child("isVideo").getValue(Boolean.class);
                Long timestamp = snapshot.child("timestamp").getValue(Long.class);

                if (from == null || sdp == null || type == null) return;
                if (from.equals(myUserId)) return; // ignore self
                if (timestamp != null && System.currentTimeMillis() - timestamp > 30000) {
                    Log.d(TAG, "⏰ Ignoring stale offer from: " + from);
                    return;
                }

                boolean looksVideoSdp = sdp != null && sdp.contains("m=video");
                boolean videoOffer = Boolean.TRUE.equals(isVideo) || looksVideoSdp;

                if (videoOffer) {
                    // BỎ QUA VIDEO: không mở Activity, không ghi declined
                    Log.d(TAG, "⛔ Ignoring incoming VIDEO call from " + (callerName != null ? callerName : from));
                    return;
                }

                // AUDIO: mở Activity nhận bình thường
                Log.d(TAG, "📞 Incoming AUDIO call from " + (callerName != null ? callerName : from)
                        + " | chatId=" + chatId);

                Intent intent = new Intent(context, VideoCallActivity.class);
                intent.putExtra("SENDER_ID", from);
                intent.putExtra("SENDER_NAME", callerName);
                intent.putExtra("RECEIVER_ID", myUserId);
                intent.putExtra("RECEIVER_NAME", (String) null);
                intent.putExtra("IS_VIDEO_CALL", false); // chắc chắn audio cho incoming
                intent.putExtra("IS_OUTGOING", false);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "offer listener cancelled: " + error.getMessage());
            }
        };

        offerRefs.put(chatId, offerRef);
        offerListeners.put(chatId, offerListener);
        offerRef.addValueEventListener(offerListener);
    }

    public void stop() {
        Log.d(TAG, "🧹 Stopping IncomingCallListener for user: " + myUserId);
        try {
            if (userChatsListener != null) {
                userChatsRef.removeEventListener(userChatsListener);
                userChatsListener = null;
            }
            for (Map.Entry<String, DatabaseReference> entry : offerRefs.entrySet()) {
                String chatId = entry.getKey();
                DatabaseReference ref = entry.getValue();
                ValueEventListener listener = offerListeners.get(chatId);
                if (ref != null && listener != null) {
                    ref.removeEventListener(listener);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during stop(): " + e.getMessage());
        } finally {
            offerRefs.clear();
            offerListeners.clear();
        }
    }

    private String generateChatId(String id1, String id2) {
        return id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
    }
}