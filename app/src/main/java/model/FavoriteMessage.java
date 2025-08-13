package com.example.fantom.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_messages")
public class FavoriteMessage {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String messageId;
    private String senderName;
    private String senderAvatarUrl;
    private String messageText;
    private long timestamp;
    private boolean isPinned;

    public FavoriteMessage(String messageId, String senderName, String senderAvatarUrl,
                           String messageText, long timestamp, boolean isPinned) {
        this.messageId = messageId;
        this.senderName = senderName;
        this.senderAvatarUrl = senderAvatarUrl;
        this.messageText = messageText;
        this.timestamp = timestamp;
        this.isPinned = isPinned;
    }

    // геттеры и сеттеры для всех полей
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getSenderAvatarUrl() { return senderAvatarUrl; }
    public void setSenderAvatarUrl(String senderAvatarUrl) { this.senderAvatarUrl = senderAvatarUrl; }
    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }
}

