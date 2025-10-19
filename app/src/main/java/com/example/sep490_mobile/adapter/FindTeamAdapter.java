package com.example.sep490_mobile.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.FindTeamDTO;
import com.example.sep490_mobile.data.dto.PublicProfileDTO;
import com.example.sep490_mobile.data.dto.ReadTeamMemberDTO;
import com.example.sep490_mobile.data.dto.ReadTeamPostDTO;
import com.example.sep490_mobile.data.dto.StadiumDTO;
import com.example.sep490_mobile.ui.home.OnItemClickListener;
import com.example.sep490_mobile.utils.DurationConverter;
import com.example.sep490_mobile.utils.HtmlConverter;
import com.example.sep490_mobile.utils.ImageUtils;
import com.example.sep490_mobile.utils.PriceFormatter;

import java.util.Dictionary;
import java.util.List;

public class FindTeamAdapter extends RecyclerView.Adapter<FindTeamAdapter.FindTeamViewHolder> {

    private List<ReadTeamPostDTO> teamPostDTOS;
    private Dictionary<Integer, StadiumDTO> stadiums;
    private Dictionary<Integer, PublicProfileDTO> users;
    private Context context;
    private int myId;
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

        int leaderId = readTeamPostDTO.getTeamMembers().stream()
                // 1. Lọc: Chỉ giữ lại thành viên là Leader
                .filter(member -> "Leader".equalsIgnoreCase(member.getRole()))

                // 2. Map: Chuyển LeaderDTO thành ID của họ
                .map(ReadTeamMemberDTO::getId)

                // 3. Tìm: Lấy phần tử đầu tiên (Optional<Integer>)
                .findFirst()

                // 4. Giá trị mặc định: Lấy giá trị nếu có, nếu không thì dùng -1
                .orElse(-1);

        int memberId = readTeamPostDTO.getTeamMembers().stream()
                // 1. Lọc: Chỉ giữ lại thành viên là Leader
                .filter(member -> myId == member.getId())

                // 2. Map: Chuyển LeaderDTO thành ID của họ
                .map(ReadTeamMemberDTO::getId)

                // 3. Tìm: Lấy phần tử đầu tiên (Optional<Integer>)
                .findFirst()

                // 4. Giá trị mặc định: Lấy giá trị nếu có, nếu không thì dùng -1
                .orElse(-1);

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

        String time = DurationConverter.convertDuration(String.valueOf(stadiumDTO.openTime).toString(), 1);
        String price = PriceFormatter.formatPrice((int) readTeamPostDTO.pricePerPerson);
        String datePlay = "";
        String createdTime = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            datePlay = DurationConverter.convertIsoPlayDate(readTeamPostDTO.getPlayDate(), "dd/MM/yyyy");
            createdTime = DurationConverter.convertIsoDate(readTeamPostDTO.getCreatedAt(), "dd/MM/yyyy");
        }


        // Các thành phần khác không liên quan đến lỗi Null
        holder.postTimestamp.setText(createdTime + " - " + time);
        holder.sportType.setText(readTeamPostDTO.getSportType());
        holder.playDateTime.setText(datePlay);
        holder.location.setText(readTeamPostDTO.getLocation());
        holder.playersInfo.setText(readTeamPostDTO.getNeededPlayers() + " người cần");
        HtmlConverter.convertHtmlToMarkdown(readTeamPostDTO.getDescription(), holder.gameDescription);
        holder.price.setText(price + "đ");
        if(leaderId == myId){
            holder.joinButton.setText("Bạn là chủ bài đăng");
            holder.joinButton.setBackgroundColor(context.getResources().getColor(R.color.gradient_end));
        }
        if(memberId > 0){
            holder.joinButton.setText("Đã tham gia");
            holder.joinButton.setBackgroundColor(context.getResources().getColor(R.color.accent_orange));
        }
        if(memberId < 0 || leaderId < 0){
            holder.joinButton.setText("Tham gia");
        }

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
        public Button joinButton;

        public FindTeamViewHolder(@NonNull View itemView) {
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
            joinButton = itemView.findViewById(R.id.joinButton);
        }
    }
}
