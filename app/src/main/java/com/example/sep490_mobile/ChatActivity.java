package com.example.sep490_mobile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

// BƯỚC 1: Implement interface để nhận kết quả từ Dialog
public class ChatActivity extends AppCompatActivity implements GifAdapter.OnGifSelectedListener {

    private static final String TAG = "ChatActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String IMGBB_API_KEY = "2f25b1206387ee1533a68c5bb1d1f54d";

    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;
    private List<ChatMessage> chatMessageList;
    private EditText messageInput;
    private ImageButton sendBtn, imageBtn, gifBtn; // Đã thêm biến cho nút GIF

    private DatabaseReference chatRef;
    private ValueEventListener chatListener;

    private String senderId, senderName, receiverId, receiverName;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        senderId = getIntent().getStringExtra("SENDER_ID");
        senderName = getIntent().getStringExtra("SENDER_NAME");
        receiverId = getIntent().getStringExtra("RECEIVER_ID");
        receiverName = getIntent().getStringExtra("RECEIVER_NAME");

        if (senderId == null || receiverId == null) {
            Toast.makeText(this, "Lỗi: Thiếu thông tin người dùng.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        getSupportActionBar().setTitle(receiverName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Ánh xạ các view
        messagesRecyclerView = findViewById(R.id.messages);
        messageInput = findViewById(R.id.messageInput);
        sendBtn = findViewById(R.id.sendBtn);
        imageBtn = findViewById(R.id.imageBtn);
        gifBtn = findViewById(R.id.gifBtn); // Ánh xạ nút GIF

        // Cài đặt RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(linearLayoutManager);

        // Cài đặt Adapter
        chatMessageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, chatMessageList, senderId);
        messagesRecyclerView.setAdapter(messageAdapter);

        // Tạo chatId và tham chiếu Firebase
        chatId = generateChatId(senderId, receiverId);
        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);

        // Gắn listener cho các nút
        sendBtn.setOnClickListener(v -> sendTextMessage());
        imageBtn.setOnClickListener(v -> openImagePicker());
        gifBtn.setOnClickListener(v -> showGifPicker()); // BƯỚC 2: Gắn sự kiện click cho nút GIF
    }

    // BƯỚC 3: Hàm để hiển thị Dialog chọn GIF
    private void showGifPicker() {
        GifPickerDialogFragment gifPicker = new GifPickerDialogFragment();
        gifPicker.show(getSupportFragmentManager(), "gif_picker_dialog");
    }

    // BƯỚC 4: Hàm này sẽ được tự động gọi khi người dùng chọn một GIF từ Dialog
    @Override
    public void onGifSelected(String gifUrl) {
        Log.d(TAG, "Selected GIF URL: " + gifUrl);
        sendMediaMessage("gif", "[GIF]" + gifUrl + "|gif");
    }

    // Hàm tạo chatId đã được sửa lỗi
    private String generateChatId(String id1, String id2) {
        if (id1.compareTo(id2) < 0) {
            return id1 + "_" + id2;
        } else {
            return id2 + "_" + id1;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        readMessages();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (chatRef != null && chatListener != null) {
            chatRef.removeEventListener(chatListener);
        }
    }

    private void readMessages() {
        chatListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatMessageList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        chatMessageList.add(dataSnapshot.getValue(ChatMessage.class));
                    }
                }
                messageAdapter.notifyDataSetChanged();
                messagesRecyclerView.scrollToPosition(chatMessageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi đọc tin nhắn: " + error.getMessage());
                Toast.makeText(ChatActivity.this, "Không thể tải tin nhắn.", Toast.LENGTH_SHORT).show();
            }
        };
        chatRef.addValueEventListener(chatListener);
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            uploadImageToImgBB(data.getData());
        }
    }

    private void uploadImageToImgBB(Uri imageUri) {
        Toast.makeText(this, "Đang tải ảnh...", Toast.LENGTH_SHORT).show();
        try (InputStream is = getContentResolver().openInputStream(imageUri)) {
            byte[] inputData = new byte[is.available()];
            is.read(inputData);
            String base64Image = Base64.encodeToString(inputData, Base64.DEFAULT);

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("key", IMGBB_API_KEY)
                    .addFormDataPart("image", base64Image)
                    .build();

            Request request = new Request.Builder().url("https://api.imgbb.com/1/upload").post(requestBody).build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Tải ảnh thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        ImgBBResponse imgBBResponse = gson.fromJson(responseBody, ImgBBResponse.class);
                        if (imgBBResponse != null && imgBBResponse.data != null) {
                            sendMediaMessage("image", "[IMAGE]" + imgBBResponse.data.url + "|image");
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Lỗi tải ảnh: " + response.message(), Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } catch (IOException e) {
            Toast.makeText(this, "Không thể đọc file ảnh.", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendTextMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!messageText.isEmpty()) {
            sendMediaMessage("text", messageText);
            messageInput.setText("");
        }
    }

    private void sendMediaMessage(String type, String content) {
        long timestamp = System.currentTimeMillis();
        ChatMessage chatMessage = new ChatMessage(senderId, senderName, content, timestamp, type);

        chatRef.push().setValue(chatMessage).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                updateLastMessage(type, content, timestamp);
                increaseUnreadCount(receiverId);
            } else {
                Toast.makeText(this, "Gửi tin nhắn thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateLastMessage(String type, String content, long timestamp) {
        String lastMsg;
        switch (type) {
            case "image":
                lastMsg = "📷 Photo";
                break;
            case "gif":
                lastMsg = "🎬 GIF";
                break;
            default:
                lastMsg = content;
                break;
        }

        DatabaseReference userChatRef1 = FirebaseDatabase.getInstance().getReference("userChats").child(senderId).child(receiverId);
        DatabaseReference userChatRef2 = FirebaseDatabase.getInstance().getReference("userChats").child(receiverId).child(senderId);

        userChatRef1.child("lastMessage").setValue(lastMsg);
        userChatRef1.child("timestamp").setValue(timestamp);
        userChatRef1.child("name").setValue(receiverName);

        userChatRef2.child("lastMessage").setValue(lastMsg);
        userChatRef2.child("timestamp").setValue(timestamp);
        userChatRef2.child("name").setValue(senderName);
    }
    private void increaseUnreadCount(String userId) {
        DatabaseReference unreadRef = FirebaseDatabase.getInstance()
                .getReference("unreadMessages")
                .child(userId); // node của người nhận

        unreadRef.get().addOnSuccessListener(snapshot -> {
            long currentCount = 0;
            if (snapshot.exists()) {
                Object value = snapshot.getValue();
                if (value instanceof Long) {
                    currentCount = (Long) value;
                } else if (value instanceof Integer) {
                    currentCount = ((Integer) value).longValue();
                }
            }
            unreadRef.setValue(currentCount + 1);
        }).addOnFailureListener(e ->
                Log.e("ChatActivity", "Không thể cập nhật unreadMessages: " + e.getMessage()));
    }

    private static class ImgBBResponse { Data data; }
    private static class Data { String url; }
}