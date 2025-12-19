package com.example.sep490_mobile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import org.json.JSONException;
import org.json.JSONObject;
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

public class ChatActivity extends AppCompatActivity implements GifAdapter.OnGifSelectedListener {

    private static final String TAG = "ChatActivity";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_VIDEO_REQUEST = 2;

    private static final String CLOUDINARY_CLOUD_NAME = "dwt7k4avh";
    private static final String CLOUDINARY_UPLOAD_PRESET = "ChatMoBe";

    private final OkHttpClient client = new OkHttpClient();

    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;
    private List<ChatMessage> chatMessageList;
    private EditText messageInput;
    private ImageButton sendBtn, imageBtn, gifBtn, videoBtn, btnAudioCall, btnVideoCall;

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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(receiverName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        messagesRecyclerView = findViewById(R.id.messages);
        messageInput = findViewById(R.id.messageInput);
        sendBtn = findViewById(R.id.sendBtn);
        imageBtn = findViewById(R.id.imageBtn);
        gifBtn = findViewById(R.id.gifBtn);
        videoBtn = findViewById(R.id.videoBtn);
        btnAudioCall = findViewById(R.id.btnAudioCall);
        btnVideoCall = findViewById(R.id.btnVideoCall);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messagesRecyclerView.setLayoutManager(linearLayoutManager);

        chatMessageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, chatMessageList, senderId);
        messagesRecyclerView.setAdapter(messageAdapter);

        chatId = generateChatId(senderId, receiverId);
        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);

        sendBtn.setOnClickListener(v -> sendTextMessage());
        imageBtn.setOnClickListener(v -> openImagePicker());
        gifBtn.setOnClickListener(v -> showGifPicker());
        videoBtn.setOnClickListener(v -> openVideoPicker());

        // Audio call
        btnAudioCall.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, VideoCallActivity.class);
            intent.putExtra("SENDER_ID", senderId);
            intent.putExtra("SENDER_NAME", senderName);
            intent.putExtra("RECEIVER_ID", receiverId);
            intent.putExtra("RECEIVER_NAME", receiverName);
            intent.putExtra("IS_VIDEO_CALL", false);
            intent.putExtra("IS_OUTGOING", true);
            startActivity(intent);
        });

        // Video call
        btnVideoCall.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, VideoCallActivity.class);
            intent.putExtra("SENDER_ID", senderId);
            intent.putExtra("SENDER_NAME", senderName);
            intent.putExtra("RECEIVER_ID", receiverId);
            intent.putExtra("RECEIVER_NAME", receiverName);
            intent.putExtra("IS_VIDEO_CALL", true);
            intent.putExtra("IS_OUTGOING", true);
            startActivity(intent);
        });
    }

    private void showGifPicker() {
        GifPickerDialogFragment gifPicker = new GifPickerDialogFragment();
        gifPicker.show(getSupportFragmentManager(), "gif_picker_dialog");
    }

    @Override
    public void onGifSelected(String gifUrl) {
        Log.d(TAG, "Selected GIF URL: " + gifUrl);
        sendMediaMessage("gif", "[GIF]" + gifUrl + "|gif");
    }

    private String generateChatId(String id1, String id2) {
        return id1.compareTo(id2) < 0 ? id1 + "_" + id2 : id2 + "_" + id1;
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
                messagesRecyclerView.scrollToPosition(Math.max(chatMessageList.size() - 1, 0));
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

    private void openVideoPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_VIDEO_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri fileUri = data.getData();
            if (requestCode == PICK_IMAGE_REQUEST) {
                uploadImageToCloudinary(fileUri);
            } else if (requestCode == PICK_VIDEO_REQUEST) {
                uploadVideoToCloudinary(fileUri);
            }
        }
    }

    private void uploadImageToCloudinary(Uri imageUri) {
        Toast.makeText(this, "Đang tải ảnh lên Cloudinary...", Toast.LENGTH_SHORT).show();
        try (InputStream is = getContentResolver().openInputStream(imageUri)) {
            byte[] inputData = new byte[is.available()];
            is.read(inputData);

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "image.jpg", RequestBody.create(okhttp3.MediaType.parse("image/*"), inputData))
                    .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
                    .build();

            String url = "https://api.cloudinary.com/v1_1/" + CLOUDINARY_CLOUD_NAME + "/image/upload";
            Request request = new Request.Builder().url(url).post(requestBody).build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Tải ảnh thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Cloudinary response: " + responseBody);
                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            String imageUrl = json.getString("secure_url");
                            sendMediaMessage("image", "[IMAGE]" + imageUrl + "|image");
                        } catch (JSONException e) {
                            runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Lỗi đọc JSON Cloudinary!", Toast.LENGTH_SHORT).show());
                            Log.e(TAG, "JSONException: " + e.getMessage());
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

    private void uploadVideoToCloudinary(Uri videoUri) {
        Toast.makeText(this, "Đang tải video lên Cloudinary...", Toast.LENGTH_SHORT).show();
        try (InputStream is = getContentResolver().openInputStream(videoUri)) {
            byte[] inputData = new byte[is.available()];
            is.read(inputData);

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "video.mp4", RequestBody.create(okhttp3.MediaType.parse("video/*"), inputData))
                    .addFormDataPart("upload_preset", CLOUDINARY_UPLOAD_PRESET)
                    .build();

            String url = "https://api.cloudinary.com/v1_1/" + CLOUDINARY_CLOUD_NAME + "/video/upload";
            Request request = new Request.Builder().url(url).post(requestBody).build();

            client.newCall(request).enqueue(new Callback() {
                @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Tải video thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Cloudinary video response: " + responseBody);
                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            String videoUrl = json.getString("secure_url");
                            sendMediaMessage("video", "[VIDEO]" + videoUrl + "|video");
                        } catch (JSONException e) {
                            runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Lỗi đọc JSON Cloudinary!", Toast.LENGTH_SHORT).show());
                            Log.e(TAG, "JSONException: " + e.getMessage());
                        }
                    } else {
                        runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Lỗi tải video: " + response.message(), Toast.LENGTH_SHORT).show());
                    }
                }
            });
        } catch (IOException e) {
            Toast.makeText(this, "Không thể đọc file video.", Toast.LENGTH_SHORT).show();
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
            case "image": lastMsg = "📷 Photo"; break;
            case "gif": lastMsg = "🎬 GIF"; break;
            case "video": lastMsg = "🎥 Video"; break;
            default: lastMsg = content; break;
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
                .child(userId);

        unreadRef.get().addOnSuccessListener(snapshot -> {
            long currentCount = 0;
            if (snapshot.exists()) {
                Object value = snapshot.getValue();
                if (value instanceof Long) currentCount = (Long) value;
                else if (value instanceof Integer) currentCount = ((Integer) value).longValue();
            }
            unreadRef.setValue(currentCount + 1);
        }).addOnFailureListener(e ->
                Log.e("ChatActivity", "Không thể cập nhật unreadMessages: " + e.getMessage()));
    }
}