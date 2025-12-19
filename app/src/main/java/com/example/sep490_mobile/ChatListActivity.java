package com.example.sep490_mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget. Toast;
import androidx.annotation. NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx. recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview. widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com. google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database. FirebaseDatabase;
import com. google.firebase.database.ValueEventListener;
import java.util. ArrayList;
import java.util. Collections;
import java.util. List;

public class ChatListActivity extends AppCompatActivity {

    private static final String TAG = "ChatListActivity";

    // Views
    private RecyclerView chatListRecyclerView;
    private EditText searchEditText;
    private Toolbar toolbar;

    // RecyclerView components
    private ChatListAdapter chatListAdapter;
    private List<Chat> chatList;
    private List<Chat> originalChatList;

    // Firebase
    private DatabaseReference userChatsRef;
    private ValueEventListener userChatsListener;

    // Dữ liệu người dùng hiện tại
    private String currentUserId;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        loadUserData();

        if (currentUserId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem tin nhắn.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSearchListener();
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId != -1) {
            this.currentUserId = String.valueOf(userId);
            this.currentUserName = prefs.getString("full_name", "Người dùng");
            Log.d(TAG, "✅ Loaded user:  " + currentUserName + " (ID: " + currentUserId + ")");
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        chatListRecyclerView = findViewById(R.id.chatList);
        searchEditText = findViewById(R.id.searchEditText);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tin nhắn");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupRecyclerView() {
        chatList = new ArrayList<>();
        originalChatList = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(chatList, this, currentUserId, currentUserName);
        chatListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatListRecyclerView.setAdapter(chatListAdapter);
    }

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
            finish();
            return true;
        }
        return super. onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentUserId != null) {
            loadChatList();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (userChatsRef != null && userChatsListener != null) {
            userChatsRef.removeEventListener(userChatsListener);
        }
    }

    /**
     * ✅ SỬA LẠI ĐỂ XỬ LÝ CẢ BOOLEAN VÀ OBJECT
     */
    private void loadChatList() {
        userChatsRef = FirebaseDatabase. getInstance().getReference("userChats").child(currentUserId);

        userChatsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                originalChatList.clear();

                if (!snapshot.exists()) {
                    Log.w(TAG, "⚠️ No chats found for user: " + currentUserId);
                    Toast.makeText(ChatListActivity.this, "Bạn chưa có cuộc trò chuyện nào.", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "📦 Found " + snapshot.getChildrenCount() + " chats");

                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        try {
                            String friendId = dataSnapshot.getKey();
                            Object value = dataSnapshot.getValue();

                            Chat chat;

                            // ✅ KIỂM TRA KIỂU DỮ LIỆU
                            if (value instanceof Boolean) {
                                // ❌ Nếu là Boolean (dữ liệu cũ), tạo Chat mặc định
                                Log.w(TAG, "⚠️ Found Boolean value for userId: " + friendId + ", creating default Chat");

                                chat = new Chat();
                                chat.setFriendId(friendId);
                                chat.setName("Người dùng"); // Tên mặc định
                                chat.setLastMessage("Bắt đầu cuộc trò chuyện");
                                chat.setTimestamp(System.currentTimeMillis());

                                // ✅ TÙY CHỌN:  Fetch tên thật từ database users
                                fetchUserNameAndUpdate(friendId, chat);

                            } else {
                                // ✅ Nếu là Object, convert bình thường
                                chat = dataSnapshot.getValue(Chat.class);
                                if (chat != null) {
                                    chat. setFriendId(friendId);
                                    Log.d(TAG, "✅ Loaded chat with:  " + chat.getName());
                                }
                            }

                            if (chat != null) {
                                originalChatList.add(chat);
                            }

                        } catch (Exception e) {
                            Log.e(TAG, "❌ Error parsing chat:  " + e.getMessage());
                            e.printStackTrace();
                        }
                    }

                    // Sắp xếp theo timestamp
                    Collections.sort(originalChatList, (o1, o2) ->
                            Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                }

                // Áp dụng bộ lọc
                filterChats(searchEditText.getText().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log. e(TAG, "❌ Không thể tải danh sách chat: " + error.getMessage());
                Toast.makeText(ChatListActivity.this, "Lỗi tải dữ liệu.", Toast.LENGTH_SHORT).show();
            }
        };

        userChatsRef.addValueEventListener(userChatsListener);
    }

    /**
     * ✅ FETCH TÊN NGƯỜI DÙNG TỪ DATABASE (TÙY CHỌN)
     * Nếu bạn có node "users" chứa thông tin user
     */
    private void fetchUserNameAndUpdate(String userId, Chat chat) {
        DatabaseReference userRef = FirebaseDatabase. getInstance()
                .getReference("users")
                .child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    if (name != null) {
                        chat. setName(name);
                        chatListAdapter.notifyDataSetChanged();
                        Log.d(TAG, "✅ Updated name for userId " + userId + ": " + name);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "❌ Error fetching user name: " + error. getMessage());
            }
        });
    }

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
        Log.d(TAG, "🔍 Filtered chats:  " + chatList.size() + " results");
    }
}