package io.github.dhamith93.projectone.pojo;

public class Message {
    private String from;
    private String message;
    private long timeStamp;

    public Message() { }

    public Message(String from, String message, long timeStamp) {
        this.from = from;
        this.message = message;
        this.timeStamp = timeStamp;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
