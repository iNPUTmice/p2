package eu.siacs.p2.xmpp.extensions.push;

import rocks.xmpp.extensions.data.model.DataForm;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "notification")
public class Notification {

    public static final String NAMESPACE = "urn:xmpp:push:0";

    @XmlElement(name = "x", namespace = DataForm.NAMESPACE)
    private DataForm pushSummary;

    private Notification() {

    }

    public Notification(DataForm pushSummary) {
        this.pushSummary = pushSummary;
    }

    public DataForm getPushSummary() {
        return this.pushSummary;
    }

}
