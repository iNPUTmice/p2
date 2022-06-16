package eu.siacs.p2.apns;

public class Notification {

    public Aps aps = new Aps();

    private Notification() {}

    public static Notification createContentAvailable() {
        final Notification notification = new Notification();
        notification.aps.contentAvailable = 1;
        return notification;
    }

    public static Notification createAlert() {
        final Notification notification = new Notification();
        notification.aps.alert = new Alert();
        notification.aps.alert.title = "New message";
        notification.aps.sound = "default";
        return notification;
    }

    public static class Aps {
        public Alert alert;
        public Integer badge;
        public String sound;
        public Integer contentAvailable;
    }

    public static class Alert {
        public String title;
        public String subtitle;
        public String body;
    }
}
