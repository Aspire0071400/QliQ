package com.aspire.qliq;

public class MessagesModel {

    String msg,senderId,currentTime;
    Long timeStamp;


    public MessagesModel() {
    }

    public MessagesModel(String msg, String senderId, String currentTime, Long timeStamp) {
        this.msg = msg;
        this.senderId = senderId;
        this.currentTime = currentTime;
        this.timeStamp = timeStamp;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
