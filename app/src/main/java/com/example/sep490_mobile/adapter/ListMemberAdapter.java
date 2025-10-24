package com.example.sep490_mobile.adapter;

//import static com.example.sep490_mobile.utils.DurationConverter.convertIsoPlayDate;
import static com.example.sep490_mobile.utils.DurationConverter.convertTimeToIsoDuration;
import static com.example.sep490_mobile.utils.DurationConverter.formatJoinDateFindTeam;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sep490_mobile.R;
import com.example.sep490_mobile.data.dto.PublicProfileDTO;
import com.example.sep490_mobile.data.dto.ReadTeamMemberDTO;
import com.example.sep490_mobile.data.dto.ReadTeamMemberForDetailDTO;
import com.example.sep490_mobile.data.dto.TeamMemberDetailDTO;
import com.example.sep490_mobile.data.remote.OnItemClickListener;
import com.example.sep490_mobile.utils.DurationConverter;
import com.example.sep490_mobile.utils.ImageUtils;
import com.google.android.material.imageview.ShapeableImageView;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Dictionary;
import java.util.List;

public class ListMemberAdapter extends RecyclerView.Adapter<ListMemberAdapter.ListMemberViewHolder> {

    private List<ReadTeamMemberForDetailDTO> listMemberDTOS;
    private Dictionary<Integer, PublicProfileDTO> publicProfiles;
    private Context context;
    private OnItemClickListener listener;

    @NonNull
    @Override
    public ListMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_member_avatar, parent, false);
        return new ListMemberViewHolder(view);
    }

    public ListMemberAdapter(Context context){
        this.context = context;
    }
    public void setListMemberAdapter(TeamMemberDetailDTO teamMemberDetailDTO, OnItemClickListener listener){
        this.listMemberDTOS = teamMemberDetailDTO.getMember();
        this.publicProfiles = teamMemberDetailDTO.getUser();
        this.listener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull ListMemberViewHolder holder, int position) {
        ReadTeamMemberForDetailDTO readTeamMemberDTO = listMemberDTOS.get(position);
        PublicProfileDTO publicProfileDTO = publicProfiles.get(readTeamMemberDTO.getUserId());
        Glide.with(this.context).load(ImageUtils.getFullUrl(publicProfileDTO.getAvatarUrl().length() > 0 ?  publicProfileDTO.getAvatarUrl() : "")).centerCrop().into(holder.memberAvatar);
        holder.name.setText(publicProfileDTO.getFullName());
        String role = "";
        if(readTeamMemberDTO.getRole().equalsIgnoreCase("Leader")){
            role = "Trưởng nhóm";
            holder.role.setTextColor(context.getColor(R.color.error_red));
        }else if(readTeamMemberDTO.getRole().equalsIgnoreCase("Member")) {
            role = "Thành viên";
            holder.role.setTextColor(context.getColor(R.color.color_secondary));

        }else if(readTeamMemberDTO.getRole().equalsIgnoreCase("Waiting")) {
            role = "Chờ duyệt";
            holder.role.setTextColor(context.getColor(R.color.accent_orange));
        }

        holder.role.setText(role);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            holder.joinedAt.setText(DurationConverter.convertLocalTimeStringToDisplayString(readTeamMemberDTO.getJoinedAt()));
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences("MyAppPrefs", context.MODE_PRIVATE);
        int myId  = sharedPreferences.getInt("user_id", 0);

        int isLeader = -1;
        for (ReadTeamMemberForDetailDTO member : listMemberDTOS) {
            if(member.getRole().equalsIgnoreCase("Leader") && member.getUserId() == myId){
                isLeader = 1;
                break;
            }else if ((member.getRole().equalsIgnoreCase("Member") || member.getRole().equalsIgnoreCase("Waiting")) && member.getUserId() == myId){
                isLeader = 0;
                break;
            }
        }
        if(isLeader == 1){
          holder.layoutBottomActions.setVisibility(View.VISIBLE);

          if(readTeamMemberDTO.getRole().equalsIgnoreCase("Member")){
              holder.btnRemove.setVisibility(View.VISIBLE);
              holder.btnAccept.setVisibility(View.GONE);
                holder.btnRemove.setText("Đuổi thành viên");
                holder.btnRemove.setBackgroundColor(this.context.getResources().getColor(R.color.error_red));
          }else if (readTeamMemberDTO.getRole().equalsIgnoreCase("Waiting")){
              holder.btnRemove.setVisibility(View.VISIBLE);
              holder.btnAccept.setVisibility(View.VISIBLE);
            holder.btnAccept.setText("Chấp nhận");
            holder.btnAccept.setBackgroundColor(this.context.getResources().getColor(R.color.green_500));
            holder.btnRemove.setText("Từ chối");
            holder.btnRemove.setBackgroundColor(this.context.getResources().getColor(R.color.error_red));
          }else{
              holder.layoutBottomActions.setVisibility(View.GONE);
          }
        }else{
            holder.layoutBottomActions.setVisibility(View.GONE);
        }
        holder.btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onItemClickRemoveMember(readTeamMemberDTO.getId(), readTeamMemberDTO.getTeamPostId(), "accept");
                }
            }
        });
        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onItemClickRemoveMember(readTeamMemberDTO.getId(), readTeamMemberDTO.getTeamPostId(), "remove");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if(listMemberDTOS != null){
            return listMemberDTOS.size();
        }
        return 0;
    }

    public class ListMemberViewHolder extends RecyclerView.ViewHolder{

        public ShapeableImageView memberAvatar;
        public TextView name;
        public TextView role;
        public TextView joinedAt;
        public Button btnRemove;
        public Button btnAccept;
        public LinearLayout layoutBottomActions;
        public LinearLayout layoutRoleBadge;

        public ListMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            memberAvatar = itemView.findViewById(R.id.memberAvatar);
            name = itemView.findViewById(R.id.tvFullName);
            role = itemView.findViewById(R.id.tvRoleText);
            joinedAt = itemView.findViewById(R.id.tvJoinedAt);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            layoutBottomActions = itemView.findViewById(R.id.layoutBottomActions);
            layoutRoleBadge = itemView.findViewById(R.id.layoutRoleBadge);
        }
    }
}
