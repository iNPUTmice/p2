package eu.siacs.p2.fcm;

import com.google.gson.annotations.SerializedName;
import eu.siacs.p2.pojo.Target;

public class Message {

    private final Priority priority;
    private final Data data;
    private final String to;
    private final String collapseKey;

    private Message(Priority priority, Data data, String to, String collapseKey) {
        this.priority = priority;
        this.data = data;
        this.to = to;
        this.collapseKey = collapseKey;
    }

    public static Message createHighPriority(Target target, boolean collapse) {
        return createHighPriority(target.getDevice(), target.getToken(), collapse);
    }

    private static Message createHighPriority(String account, String token, boolean collapse) {
        final Data data = new Data();
        data.account = account;
        final String collapseKey = collapse ? account.substring(0, 6) : null;
        return new Message(Priority.HIGH, data, token, collapseKey);
    }

    public enum Priority {
        @SerializedName("high") HIGH,
        @SerializedName("normal") NORMAL
    }

    public static class Data {
        String account;
    }
}
