package com.example.sep490_mobile;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.database.*;

public class IncomingCallListener {
    private final DatabaseReference userChatsRef;
    private final String myUserId;
    private final Context context;

    public IncomingCallListener(Context context, String myUserId) {
        this.context = context.getApplicationContext();
        this.myUserId = myUserId;
        this.userChatsRef = FirebaseDatabase.getInstance()
                .getReference("userChats")
                .child(myUserId);

        startListening();
    }

    private void startListening() {
        Log.d("IncomingCall", "👂 Đang lắng nghe cuộc gọi đến cho user: " + myUserId);

        userChatsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                for (DataSnapshot chatSnap : snapshot.getChildren()) {
                    String otherUserId = chatSnap.getKey();
                    if (otherUserId == null) continue;

                    String chatId = generateChatId(myUserId, otherUserId);
                    listenForOffer(chatId, otherUserId);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("IncomingCall", "Lỗi lắng nghe userChats: " + error.getMessage());
            }
        });
    }

    private void listenForOffer(String chatId, String otherUserId) {
        DatabaseReference offerRef = FirebaseDatabase.getInstance()
                .getReference("webrtc")
                .child(chatId)
                .child("offer");

        offerRef.addValueEventListener(new ValueEventListener() {
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

                // Bỏ qua offer của chính mình
                if (from.equals(myUserId)) return;

                // Bỏ qua offer quá cũ (>30s)
                if (timestamp != null && System.currentTimeMillis() - timestamp > 30000) {
                    Log.d("IncomingCall", "⏰ Bỏ qua offer cũ từ: " + from);
                    return;
                }

                Log.d("IncomingCall", "📞 Có cuộc gọi đến từ: " + (callerName != null ? callerName : from));
                Log.d("IncomingCall", "  ChatId: " + chatId + " | isVideo: " + isVideo);

                // Mở màn IncomingCallActivity
                Intent intent = new Intent(context, IncomingCallActivity.class);
                intent.putExtra("CALLER_ID", from);
                intent.putExtra("CALLER_NAME", callerName); // QUAN TRỌNG: truyền tên người gọi
                intent.putExtra("CHAT_ID", chatId);
                intent.putExtra("OFFER_SDP", sdp);
                intent.putExtra("OFFER_TYPE", type);
                intent.putExtra("IS_VIDEO", isVideo != null ? isVideo : true);

                // Truyền MY_USER_ID để fallback nếu cần
                intent.putExtra("MY_USER_ID", myUserId);

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("IncomingCall", "Lỗi lắng nghe offer: " + error.getMessage());
            }
        });
    }

    private String generateChatId(String id1, String id2) {
        return id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
    }
}