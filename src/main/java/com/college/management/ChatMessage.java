package com.college.management;

import java.time.LocalDateTime;

public class ChatMessage {
    private String senderName;
    private int senderId;
    private String content;
    private String timestamp;

    public ChatMessage(String senderName, int senderId, String content, String timestamp) {
        this.senderName = senderName;
        this.senderId = senderId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getSenderName() { return senderName; }
    public int getSenderId() { return senderId; }
    public String getContent() { return content; }
    public String getTimestamp() { return timestamp; }
}
