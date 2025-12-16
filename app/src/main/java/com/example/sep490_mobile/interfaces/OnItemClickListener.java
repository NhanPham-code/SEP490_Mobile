package com.example.sep490_mobile.interfaces;

public interface OnItemClickListener {
    // Phương thức sẽ được gọi khi một item trong Adapter được nhấn
    void onItemClick(int item);
    void onItemClick(int item, String type);

    // Thêm phương thức mới này
    void onItemClick(int stadiumId, String stadiumName, int createBy);

    void onChatClick(int postId, int creatorId, String creatorName);
    void onItemClickRemoveMember(int id , int memberUserId,int postId, String type);
    void onBookButtonClick(int stadiumId);
    void onDailyBookButtonClick(int stadiumId);
}