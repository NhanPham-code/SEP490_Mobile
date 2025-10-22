package com.example.sep490_mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatListActivity extends AppCompatActivity {

    private static final String TAG = "ChatListActivity";

    // Views
    private RecyclerView chatListRecyclerView;
    private EditText searchEditText;
    private Toolbar toolbar;

    // RecyclerView components
    private ChatListAdapter chatListAdapter;
    private List<Chat> chatList;
    private List<Chat> originalChatList; // Giữ dữ liệu gốc để lọc

    // Firebase
    private DatabaseReference userChatsRef;
    private ValueEventListener userChatsListener;

    // Dữ liệu người dùng hiện tại (lấy từ SharedPreferences)
    private String currentUserId;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Lấy thông tin người dùng từ SharedPreferences
        loadUserData();

        // Nếu không có thông tin người dùng, không thực hiện các bước còn lại
        if (currentUserId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem tin nhắn.", Toast.LENGTH_LONG).show();
            finish(); // Đóng Activity
            return;
        }

        // Khởi tạo Views
        initViews();

        // Cấu hình Toolbar
        setupToolbar();

        // Cấu hình RecyclerView
        setupRecyclerView();

        // Thêm listener tìm kiếm
        setupSearchListener();
    }

    /**
     * Lấy thông tin người dùng (ID và Tên) từ SharedPreferences.
     */
    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1); // Lấy user_id, mặc định là -1 nếu không có

        if (userId != -1) {
            // Chuyển int thành String để dùng với Firebase
            this.currentUserId = String.valueOf(userId);
            this.currentUserName = prefs.getString("full_name", "Người dùng"); // Lấy tên, nếu không có thì dùng tên mặc định
        }
        // Nếu userId == -1, currentUserId sẽ vẫn là null
    }

    /**
     * Ánh xạ và khởi tạo các thành phần giao diện.
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        chatListRecyclerView = findViewById(R.id.chatList);
        searchEditText = findViewById(R.id.searchEditText);
    }

    /**
     * Cài đặt Toolbar với tiêu đề và nút quay lại.
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tin nhắn");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    /**
     * Cài đặt RecyclerView, Adapter và List.
     */
    private void setupRecyclerView() {
        chatList = new ArrayList<>();
        originalChatList = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(chatList, this, currentUserId, currentUserName);
        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatListRecyclerView.setAdapter(chatListAdapter);
    }

    /**
     * Cài đặt sự kiện lắng nghe cho ô tìm kiếm.
     */
    private void setupSearchListener() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterChats(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Xử lý sự kiện nhấn nút quay lại trên Toolbar
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Chỉ tải danh sách chat nếu người dùng đã được xác thực
        if (currentUserId != null) {
            loadChatList();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Gỡ bỏ listener để tránh rò rỉ bộ nhớ
        if (userChatsRef != null && userChatsListener != null) {
            userChatsRef.removeEventListener(userChatsListener);
        }
    }

    /**
     * Tải danh sách các cuộc trò chuyện từ Firebase.
     */
    private void loadChatList() {
        userChatsRef = FirebaseDatabase.getInstance().getReference("userChats").child(currentUserId);

        userChatsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                originalChatList.clear();

                if (!snapshot.exists()) {
                    Toast.makeText(ChatListActivity.this, "Bạn chưa có cuộc trò chuyện nào.", Toast.LENGTH_SHORT).show();
                } else {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Chat chat = dataSnapshot.getValue(Chat.class);
                        if (chat != null) {
                            String friendId = dataSnapshot.getKey();
                            chat.setFriendId(friendId);
                            originalChatList.add(chat); // Thêm vào danh sách gốc
                        }
                    }
                    // Sắp xếp danh sách gốc
                    Collections.sort(originalChatList, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                }

                // Áp dụng bộ lọc hiện tại (hoặc hiển thị toàn bộ danh sách nếu không có filter)
                filterChats(searchEditText.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Không thể tải danh sách chat: " + error.getMessage());
                Toast.makeText(ChatListActivity.this, "Lỗi tải dữ liệu.", Toast.LENGTH_SHORT).show();
            }
        };

        userChatsRef.addValueEventListener(userChatsListener);
    }

    /**
     * Lọc danh sách trò chuyện dựa trên tên người dùng.
     * @param query Từ khóa tìm kiếm.
     */
    private void filterChats(String query) {
        chatList.clear();

        if (query.isEmpty()) {
            chatList.addAll(originalChatList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Chat chat : originalChatList) {
                if (chat.getName() != null && chat.getName().toLowerCase().contains(lowerQuery)) {
                    chatList.add(chat);
                }
            }
        }
        chatListAdapter.notifyDataSetChanged();
    }
}