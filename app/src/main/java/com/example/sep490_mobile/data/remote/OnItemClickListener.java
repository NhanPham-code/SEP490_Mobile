package com.example.sep490_mobile.data.remote;

public interface OnItemClickListener {
    // Phương thức sẽ được gọi khi một item trong Adapter được nhấn
    void onItemClick(int item);

    void onItemClick(int item, String type);
    void onItemClickRemoveMember(int id ,int postId, String type);

    void onBookButtonClick(int stadiumId);
}
