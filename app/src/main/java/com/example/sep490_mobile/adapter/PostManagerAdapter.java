package com.example.sep490_mobile.adapter;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.FindTeamDTO;
import com.example.sep490_mobile.data.dto.PublicProfileDTO;
import com.example.sep490_mobile.data.dto.ReadTeamMemberDTO;
import com.example.sep490_mobile.data.dto.ReadTeamPostDTO;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.interfaces.OnItemClickListener;
import com.example.sep490_mobile.utils.DurationConverter;
import com.example.sep490_mobile.utils.HtmlConverter;
import com.example.sep490_mobile.utils.ImageUtils;
import com.example.sep490_mobile.utils.PriceFormatter;

import java.util.Dictionary;
import java.util.List;

// Đã đổi tên lớp từ FindTeamAdapter
public class PostManagerAdapter extends RecyclerView.Adapter<PostManagerAdapter.PostManagerViewHoder> {

    private List<ReadTeamPostDTO> teamPostDTOS;

    private final int MAX_COLLAPSED_LINES = 2;
    private Dictionary<Integer, StadiumDTO> stadiums;
    private Dictionary<Integer, PublicProfileDTO> users;
    private Context context;
    private int myId;
    private final Handler handler = new Handler();

    // Đã đổi tên hàm khởi tạo
    public PostManagerAdapter(Context context){
        this.context = context;
    }
    private OnItemClickListener listener;

    // Đã đổi tên phương thức setFindTeamDTO
    public void setPostData(FindTeamDTO findTeamDTO, OnItemClickListener listener){
        this.teamPostDTOS = findTeamDTO.getTeamPostDTOS();
        this.stadiums = findTeamDTO.getStadiums();
        this.users = findTeamDTO.getUsers();
        this.listener = listener;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    // Đã đổi kiểu trả về và tên ViewHolder
    public PostManagerViewHoder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_my_post, parent, false);

        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", context.MODE_PRIVATE);
        myId = sharedPreferences.getInt("user_id", 0);
        // Đã đổi tên ViewHolder
        return new PostManagerViewHoder(view);
    }

    @Override
    // Đã đổi tên ViewHolder trong tham số
    public void onBindViewHolder(@NonNull PostManagerViewHoder holder, int position) {
        ReadTeamPostDTO readTeamPostDTO = teamPostDTOS.get(position);
        StadiumDTO stadiumDTO = stadiums.get(readTeamPostDTO.getStadiumId());
        PublicProfileDTO publicProfileDTO = users.get(readTeamPostDTO.getCreatedBy());

        // 1. Kiểm tra NULL cho Người Tạo (PublicProfileDTO)
        if (publicProfileDTO != null) {
            // publicProfileDTO.fullName KHÔNG PHẢI LÀ PHƯƠNG THỨC.
            // Nếu fullName là trường (field) công khai, bạn dùng .fullName.
            // Nếu là trường private, bạn phải dùng Getter (getName() hoặc getFullName())
            Glide.with(this.context).load(ImageUtils.getFullUrl(publicProfileDTO.getAvatarUrl().length() > 0 ?  publicProfileDTO.getAvatarUrl() : "")).centerCrop().into(holder.playerAvatar);
            holder.playerName.setText(publicProfileDTO.fullName);
        } else {
            holder.playerName.setText("Người dùng không xác định");
            holder.playerAvatar.setImageResource(R.drawable.ic_default_avatar);
            // Bạn cũng có thể ẩn avatar hoặc đặt một ảnh mặc định
        }

        // 2. Kiểm tra NULL cho Sân (StadiumDTO)
        if (stadiumDTO != null) {
            holder.stadiumName.setText(stadiumDTO.getName());
        } else {
            holder.stadiumName.setText("Sân không rõ");
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", context.MODE_PRIVATE);
        int myId  = sharedPreferences.getInt("user_id", 0);

        int isLeader = -1;
        for (ReadTeamMemberDTO member : readTeamPostDTO.getTeamMembers()) {
            if(member.getRole().equalsIgnoreCase("Leader") && member.getUserId() == myId){
                isLeader = 1;
                break;
            }else if ((member.getRole().equalsIgnoreCase("Member") || member.getRole().equalsIgnoreCase("Waiting")) && member.getUserId() == myId){
                isLeader = 0;
                break;
            }
        }


        String price = PriceFormatter.formatPrice((int) readTeamPostDTO.pricePerPerson);
        String datePlay = "";
        String createdTime = "";
        boolean disableButtons = false; // Mặc định là KHÔNG tắt (false)

// Logic so sánh ngày
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // --- Lấy ngày như cũ ---
            datePlay = DurationConverter.convertIsoPlayDate(readTeamPostDTO.getPlayDate(), "dd/MM/yyyy");
            createdTime = DurationConverter.convertIsoPlayDate(readTeamPostDTO.getCreatedAt(), "dd/MM/yyyy - HH:mm");

            // --- Logic so sánh mới ---
            try {
                // 1. Lấy ngày hôm nay (chỉ ngày, không giờ) theo múi giờ thiết bị
                java.time.LocalDate today = java.time.LocalDate.now();

                // 2. Phân tích chuỗi ISO date gốc (ví dụ: "2025-10-25T17:00:00+07:00")
                // Chuyển nó về múi giờ của thiết bị và lấy ngày
                java.time.LocalDate localPlayDate = java.time.OffsetDateTime
                        .parse(readTeamPostDTO.getPlayDate())
                        .atZoneSameInstant(java.time.ZoneId.systemDefault())
                        .toLocalDate();

                // 3. So sánh: "nếu lớn hơn hoặc bằng hôm nay thì tắt"
                if (localPlayDate.isAfter(today) || localPlayDate.isEqual(today)) {
                    disableButtons = true; // Tắt nút nếu trận đấu diễn ra hôm nay hoặc trong tương lai
                }

            } catch (Exception e) {
                e.printStackTrace();
                // Nếu không phân tích được ngày, tắt nút để đảm bảo an toàn
                disableButtons = true;
            }
        } else {
            // --- Fallback cho API < 26 (dưới Android 8.0) ---
            datePlay = ""; // (Bạn có thể cần 1 hàm convert riêng cho API cũ)
            createdTime = ""; // (Bạn có thể cần 1 hàm convert riêng cho API cũ)

            try {
                // 1. Dùng SimpleDateFormat cho API cũ
                // Cú pháp này cố gắng phân tích ISO 8601
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
                // Chúng ta phải cắt bớt thông tin múi giờ vì SimpleDateFormat xử lý không tốt
                java.util.Date playDate = sdf.parse(readTeamPostDTO.getPlayDate().substring(0, 19));

                // 2. Lấy ngày hôm nay và xóa thông tin giờ
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
                cal.set(java.util.Calendar.MINUTE, 0);
                cal.set(java.util.Calendar.SECOND, 0);
                cal.set(java.util.Calendar.MILLISECOND, 0);
                java.util.Date today = cal.getTime();

                // 3. So sánh: if playDate IS NOT before today (tức là >= today)
                if (!playDate.before(today)) {
                    disableButtons = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                disableButtons = true; // An toàn là tắt nút
            }
        }

        // Các thành phần khác không liên quan đến lỗi Null
        holder.postTimestamp.setText(createdTime);
        holder.sportType.setText(readTeamPostDTO.getSportType());
        holder.playDateTime.setText(datePlay);
        holder.location.setText(readTeamPostDTO.getLocation());
        holder.playersInfo.setText("cần " + readTeamPostDTO.getJoinedPlayers() + " / " + readTeamPostDTO.getNeededPlayers()+ " người" );
        HtmlConverter.convertHtmlToMarkdown(readTeamPostDTO.getDescription(), holder.gameDescription);
        holder.price.setText(price + "đ");

        if (disableButtons == true) {
            if(isLeader == 1){

                holder.editButton.setVisibility(View.VISIBLE);
                holder.deleteButton.setVisibility(View.VISIBLE);
                holder.statusPost.setVisibility(View.GONE);
            }else{
                holder.editButton.setVisibility(View.GONE);
                holder.deleteButton.setVisibility(View.GONE);
                holder.statusPost.setVisibility(View.GONE);
            }


        }else {
            holder.editButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
            holder.statusPost.setText("Bài đăng quá hạng");
            holder.statusPost.setVisibility(View.VISIBLE);
            holder.statusPost.setTextColor(context.getResources().getColor(R.color.color_accent));
        }


        holder.editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){

                    listener.onItemClick(readTeamPostDTO.getId(), "edit");
                }
            }
        });
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context ctx = v.getContext();
                new AlertDialog.Builder(ctx)
                        .setTitle("Xóa bài đăng")
                        .setMessage("Bạn có muốn xóa bài đăng này không?")
                        .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (listener != null) {
                                    listener.onItemClick(holder.getBindingAdapterPosition(), "delete");
                                }
                            }
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            }
        });

        holder.seeMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean isExpanded = (Boolean) holder.gameDescription.getTag();
                if (isExpanded == null) {
                    isExpanded = false; // Mặc định là Thu gọn nếu chưa có trạng thái
                }
                isExpanded = !isExpanded; // Đảo ngược trạng thái
                holder.gameDescription.setTag(isExpanded);
                if (isExpanded) {
                    // Chuyển sang trạng thái Mở rộng (Expand)
                    holder.gameDescription.setMaxLines(Integer.MAX_VALUE); // Bỏ giới hạn dòng
                    holder.gameDescription.setEllipsize(null);               // Bỏ dấu "..."
                    holder.seeMore.setText("Thu gọn");
                } else {
                    // Chuyển sang trạng thái Thu gọn (Collapse)
                    holder.gameDescription.setMaxLines(MAX_COLLAPSED_LINES); // Giới hạn lại 4 dòng
                    holder.gameDescription.setEllipsize(TextUtils.TruncateAt.END); // Thêm dấu "..."
                    holder.seeMore.setText("Xem thêm");
                }
            }
        });


        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Logic trong đây sẽ chạy sau khoảng thời gian DELAY_TIME_MS
                holder.gameDescription.post(new Runnable() {
                    @Override
                    public void run() {
                        // LÚC NÀY, lineCount đã được cập nhật chính xác!
                        int newLineCount = holder.gameDescription.getLineCount();
                        // Ví dụ: Áp dụng lại logic "Xem thêm/Thu gọn"
                        if (newLineCount > MAX_COLLAPSED_LINES) {
                            holder.seeMore.setVisibility(View.VISIBLE);
                        } else {
                            holder.seeMore.setVisibility(View.GONE);
                        }
                        holder.gameDescription.setMaxLines(MAX_COLLAPSED_LINES);
                        holder.gameDescription.setEllipsize(TextUtils.TruncateAt.END);
                    }
                });
            }
        }, 100);

        holder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onItemClick(readTeamPostDTO.getId(), "detail");
                }
            }
        });

    }
    @Override
    public int getItemCount() {
        if (teamPostDTOS == null) {
            return 0;
        }
        return teamPostDTOS.size();
    }

    // Đã đổi tên lớp ViewHolder
    public class PostManagerViewHoder extends RecyclerView.ViewHolder {
        // Các thành phần giao diện của item
        public ImageView playerAvatar;
        public TextView playerName;
        public TextView postTimestamp;
        public TextView stadiumName;
        public TextView sportType;
        public TextView playDateTime;
        public TextView location;
        public TextView playersInfo;
        public TextView gameDescription;
        public TextView price;
        public ImageButton editButton; // Bạn có thể muốn đổi tên biến này thành actionButton
        public ImageButton deleteButton;
        public TextView seeMore;
        public ConstraintLayout card;
        public TextView statusPost;
        public LinearLayout bottomCard;

        // Đã đổi tên hàm khởi tạo của ViewHolder
        public PostManagerViewHoder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các thành phần giao diện
            playerAvatar = itemView.findViewById(R.id.playerAvatar);
            playerName = itemView.findViewById(R.id.playerName);
            postTimestamp = itemView.findViewById(R.id.postTimestamp);
            stadiumName = itemView.findViewById(R.id.stadiumName);
            sportType = itemView.findViewById(R.id.sportType);
            playDateTime = itemView.findViewById(R.id.playDateTime);
            location = itemView.findViewById(R.id.location);
            playersInfo = itemView.findViewById(R.id.playersInfo);
            gameDescription = itemView.findViewById(R.id.gameDescription);
            price = itemView.findViewById(R.id.price);
            editButton = itemView.findViewById(R.id.editButton);
            seeMore = itemView.findViewById(R.id.seeMore); // <-- ÁNH XẠ
            card = itemView.findViewById(R.id.find_team_card_item);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            statusPost = itemView.findViewById(R.id.status_post);
            bottomCard = itemView.findViewById(R.id.bottom_card);
        }
    }
}