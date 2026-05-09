package com.example.termproj_172.domainModels;

public class NotificationResponse {

    private String status;
    private String messageId;

    public NotificationResponse() {
    }

    public NotificationResponse(String status, String messageId) {
        this.status = status;
        this.messageId = messageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
        System.out.println("Notification Message: "+this.messageId);
    }
}
