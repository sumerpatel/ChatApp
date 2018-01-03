package com.example.sumerpatel.chatapp.model;

/**
 * Created by madhur on 17/01/15.
 */
public class ChatMessage {

    private static Status messageStatus;
    private String messageText;
    private UserType userType;
    private long messageTime;

    public ChatMessage() {
    }

    public ChatMessage(String s, String username) {
    }

    public static Status getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(Status messageStatus) {
        this.messageStatus = messageStatus;
    }

    public long getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(long messageTime) {
        this.messageTime = messageTime;
    }

    public String getMessageText() {

        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }
}
