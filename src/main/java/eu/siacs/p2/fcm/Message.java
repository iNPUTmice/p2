package eu.siacs.p2.fcm;

import com.google.gson.annotations.SerializedName;

public class Message {

    private final Priority priority;
    private final Data data;
    private final String to;

    private Message(Priority priority, Data data, String to) {
        this.priority = priority;
        this.data = data;
        this.to = to;
    }

    public static Message createHighPriority(String account, String token) {
        Data data = new Data();
        data.account = account;
        return new Message(Priority.HIGH, data, token);
    }

    public enum Priority {
        @SerializedName("high") HIGH,
        @SerializedName("normal") NORMAL
    }
    public static class Data {
        String account;
    }
}
