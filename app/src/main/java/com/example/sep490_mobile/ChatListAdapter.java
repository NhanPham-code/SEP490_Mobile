package com.example.sep490_mobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {

    private List<Chat> chatList;
    private Context context;
    private String currentUserId;
    private String currentUserName;

    public ChatListAdapter(List<Chat> chatList, Context context, String currentUserId, String currentUserName) {
        this.chatList = chatList;
        this.context = context;
        this.currentUserId = currentUserId;
        this.currentUserName = currentUserName;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.chatName.setText(chat.getName());

        String lastMsg = chat.getLastMessage();
        if (lastMsg != null) {
            if (lastMsg.startsWith("[IMAGE]")) {
                lastMsg = "📷 Photo";
            } else if (lastMsg.startsWith("[GIF]")) {
                lastMsg = "🎬 GIF";
            }
        }
        holder.lastMessage.setText(lastMsg);

        // --- AVATAR HANDLING MODIFIED HERE ---
        // Remove the Glide call for random avatars
        // Glide.with(context)
        //         .load("https://i.pravatar.cc/150?u=" + chat.getFriendId())
        //         .into(holder.chatAvatar);

        // Create a drawable with the first letter of the name
        String chatName = chat.getName();
        if (chatName != null && !chatName.isEmpty()) {
            holder.chatAvatar.setImageDrawable(createAvatarDrawable(chatName));
        } else {
            // Fallback for empty name
            holder.chatAvatar.setImageDrawable(createAvatarDrawable("?"));
        }
        // --- END OF MODIFICATION ---


        holder.itemView.setOnClickListener(v -> {
            String friendId = chat.getFriendId();
            // ---- SỬA LỖI QUAN TRỌNG Ở ĐÂY ----
            // Kiểm tra friendId trước khi chuyển màn hình
            if (friendId != null && !friendId.isEmpty()) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("RECEIVER_ID", friendId); // Dùng friendId đã được gán chính xác
                intent.putExtra("RECEIVER_NAME", chat.getName());
                intent.putExtra("SENDER_ID", currentUserId);
                intent.putExtra("SENDER_NAME", currentUserName);
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Lỗi: Không tìm thấy ID người nhận.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Creates a circular drawable with a background color and the first letter of the given text.
     */
    private BitmapDrawable createAvatarDrawable(String text) {
        String firstLetter = text.substring(0, 1).toUpperCase();
        int size = 150; // Avatar size in pixels

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        // --- Background Circle ---
        Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // Generate a consistent color from the text's hash code
        int hue = text.hashCode() % 360;
        backgroundPaint.setColor(Color.HSVToColor(new float[]{hue, 0.5f, 0.8f}));
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, backgroundPaint);

        // --- Text (First Letter) ---
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(size * 0.6f); // Text size is 60% of avatar size
        textPaint.setTextAlign(Paint.Align.CENTER);

        // Adjust Y position to center the text vertically
        float yPos = (size / 2f) - ((textPaint.descent() + textPaint.ascent()) / 2f);
        canvas.drawText(firstLetter, size / 2f, yPos, textPaint);

        return new BitmapDrawable(context.getResources(), bitmap);
    }


    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView chatName, lastMessage;
        CircleImageView chatAvatar;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            chatName = itemView.findViewById(R.id.chatName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            chatAvatar = itemView.findViewById(R.id.chatAvatar);
        }
    }
}