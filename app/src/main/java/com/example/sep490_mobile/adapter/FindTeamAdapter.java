package com.example.sep490_mobile.adapter;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class FindTeamAdapter extends RecyclerView.Adapter<FindTeamAdapter.FindTeamViewHolder> {

    private List<ReadTeamPostDTO> teamPostDTOS;

    private final int MAX_COLLAPSED_LINES = 2;
    private Dictionary<Integer, StadiumDTO> stadiums;
    private Dictionary<Integer, PublicProfileDTO> users;
    private Context context;
    private int myId;
    private final Handler handler = new Handler();
    public FindTeamAdapter(Context context){
        this.context = context;
    }
    private OnItemClickListener listener;

    public void setFindTeamDTO(FindTeamDTO findTeamDTO, OnItemClickListener listener){
        this.teamPostDTOS = findTeamDTO.getTeamPostDTOS();
        this.stadiums = findTeamDTO.getStadiums();
        this.users = findTeamDTO.getUsers();
        this.listener = listener;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FindTeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_findteam, parent, false);

        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", context.MODE_PRIVATE);
        myId = sharedPreferences.getInt("user_id", 0);
        return new FindTeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FindTeamViewHolder holder, int position) {
        ReadTeamPostDTO readTeamPostDTO = teamPostDTOS.get(position);
        StadiumDTO stadiumDTO = stadiums.get(readTeamPostDTO.getStadiumId());
        PublicProfileDTO publicProfileDTO = users.get(readTeamPostDTO.getCreatedBy());
        int leaderId = -1; // Khởi tạo giá trị mặc định

        for (ReadTeamMemberDTO member : readTeamPostDTO.getTeamMembers()) {
            // 1. Lọc: Chỉ giữ lại thành viên có Role là Leader
            if ("Leader".equalsIgnoreCase(member.getRole()) && member.getUserId() == myId) {


                // 2. Map: Lấy ID và gán cho leaderId
                leaderId = member.getUserId();

                // 3. Dừng: Vì chỉ cần Leader đầu tiên (tương đương với findFirst())
                break;
            }
//            System.out.println("leaderId: " + member.getId());
        }
        int memberId = -1; // Khởi tạo giá trị mặc định
        int waiting = -1;
        for (ReadTeamMemberDTO member : readTeamPostDTO.getTeamMembers()) {
            // 1. Lọc: Chỉ giữ lại thành viên có ID trùng với myId
            if (myId == member.getUserId() && "Member".equalsIgnoreCase(member.getRole())) {


                // 2. Map: Lấy ID (chính là myId) và gán cho memberId
                memberId = member.getUserId(); // Hoặc đơn giản: memberId = myId;

                // 3. Dừng: Vì đã tìm thấy ID của người dùng hiện tại
                break;
            }else if (myId == member.getUserId() && "Waiting".equalsIgnoreCase(member.getRole())){
                waiting = member.getUserId(); // Hoặc đơn giản: memberId = myId;

                // 3. Dừng: Vì đã tìm thấy ID của người dùng hiện tại
                break;
            }
            System.out.println("leaderId: member " + member.getUserId());
        }
// Nếu vòng lặp kết thúc mà không tìm thấy, memberId vẫn là -1.
// Nếu vòng lặp kết thúc mà không tìm thấy, leaderId vẫn là -1.
// Nếu myId có trong team, kết quả sẽ là myId. Nếu không, kết quả là -1.

        System.out.println("finteamId leader: " + leaderId);
        System.out.println("finteamId member: " + memberId);


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

        String price = PriceFormatter.formatPrice((int) readTeamPostDTO.pricePerPerson);
        String datePlay = "";
        String createdTime = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            datePlay = DurationConverter.convertIsoPlayDate(readTeamPostDTO.getPlayDate(), "dd/MM/yyyy");
            createdTime = DurationConverter.convertIsoPlayDate(readTeamPostDTO.getCreatedAt(), "dd/MM/yyyy - HH:mm");
        }

        String time = DurationConverter.convertDuration(readTeamPostDTO.getTimePlay(), 1);

        // Các thành phần khác không liên quan đến lỗi Null
        holder.postTimestamp.setText(createdTime);
        holder.sportType.setText(readTeamPostDTO.getSportType());
        holder.playDateTime.setText(datePlay + " - " + time);
        holder.location.setText(readTeamPostDTO.getLocation());
        holder.playersInfo.setText("cần " + readTeamPostDTO.getJoinedPlayers() + " / " + readTeamPostDTO.getNeededPlayers()+ " người" );
        HtmlConverter.convertHtmlToMarkdown(readTeamPostDTO.getDescription(), holder.gameDescription);
        holder.price.setText(price + "đ");
        if(readTeamPostDTO.getJoinedPlayers() >= readTeamPostDTO.getNeededPlayers()){
            holder.joinButton.setText("Đã đủ người");
            holder.joinButton.setBackgroundColor(context.getResources().getColor(R.color.grey_700));
            holder.joinButton.setEnabled(false);
        }
        else if(leaderId == myId && memberId != myId){
            holder.joinButton.setText("Bạn là chủ bài đăng");
            holder.joinButton.setBackgroundColor(context.getResources().getColor(R.color.gradient_end));
            holder.joinButton.setEnabled(false);
        }
        else if(memberId > 0){
            holder.joinButton.setText("Đã tham gia");
            holder.joinButton.setBackgroundColor(context.getResources().getColor(R.color.grey_700));
            holder.joinButton.setEnabled(false);

        } else if (waiting > 0) {
            holder.joinButton.setText("Đang chờ duyệt");
            holder.joinButton.setBackgroundColor(context.getResources().getColor(R.color.accent_orange));
            holder.joinButton.setEnabled(false);

        } else if(memberId < 0 || leaderId < 0){
            holder.joinButton.setText("Tham gia");
            holder.joinButton.setBackgroundColor(context.getResources().getColor(R.color.gradient_start));
            holder.joinButton.setEnabled(true);
        }

        holder.chatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null && publicProfileDTO != null){
                    listener.onChatClick(
                            readTeamPostDTO.getId(),
                            readTeamPostDTO.getCreatedBy(),
                            publicProfileDTO.fullName // hoặc getFullName()
                    );
                }
            }
        });
        holder.joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onItemClickRemoveMember(readTeamPostDTO.getId(), readTeamPostDTO.getCreatedBy(), 0, "join");
                }
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
                    listener.onItemClickRemoveMember(readTeamPostDTO.getId(), readTeamPostDTO.getCreatedBy(),0 , "detail");
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

    public class FindTeamViewHolder extends RecyclerView.ViewHolder {
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
        public ImageButton chatButton;
        public Button joinButton;
        public TextView seeMore;
        public ConstraintLayout card;

        public FindTeamViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các thành phần giao diện
            chatButton = itemView.findViewById(R.id.chatButton);
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
            joinButton = itemView.findViewById(R.id.joinButton);
            seeMore = itemView.findViewById(R.id.seeMore); // <-- ÁNH XẠ
            card = itemView.findViewById(R.id.find_team_card_item);
        }
    }
}
