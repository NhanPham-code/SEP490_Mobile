package com.example.sep490_mobile.model;

public class ChatRoomInfo {
    public String name;
    public long timestamp;
    public String lastMessage;

    public ChatRoomInfo() {} // Required for Firebase

    public ChatRoomInfo(String name, long timestamp, String lastMessage) {
        this.name = name;
        this.timestamp = timestamp;
        this.lastMessage = lastMessage;
    }
}