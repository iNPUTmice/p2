package eu.siacs.p2.pojo;

import eu.siacs.p2.P2;
import eu.siacs.p2.Utils;
import rocks.xmpp.addr.Jid;

public class Target {


    private String device;
    private String channel;
    private Jid domain;
    private String token;
    private String node;
    private String secret;

    private Target(String device, String channel, Jid domain, String token, String node, String secret) {
        this.device = device;
        this.channel = channel;
        this.domain = domain;
        this.token = token;
        this.node = node;
        this.secret = secret;
    }

    public static Target create(Jid account, String deviceId, String token) {
        String node = Utils.random(3, P2.SECURE_RANDOM);
        String secret = Utils.random(6, P2.SECURE_RANDOM);
        String device = Utils.combineAndHash(account.asBareJid().toEscapedString(),deviceId);
        return new Target(device, "", Jid.ofDomain(account.getDomain()), token, node, secret);
    }

    public static Target createMuc(Jid account, Jid muc, String deviceId, String token) {
        String node = Utils.random(3, P2.SECURE_RANDOM);
        String secret = Utils.random(6, P2.SECURE_RANDOM);
        String device = Utils.combineAndHash(account.asBareJid().toEscapedString(),deviceId);
        String channel = Utils.combineAndHash(muc.toEscapedString(), deviceId);
        return new Target(device, channel, Jid.ofDomain(account.getDomain()), token, node, secret);
    }

    public String getNode() {
        return node;
    }

    public String getSecret() {
        return secret;
    }

    public boolean setToken(String token) {
        if (this.token != null && this.token.equals(token)) {
            return false;
        }
        this.token = token;
        return true;
    }

    public String getDevice() {
        return device;
    }

    public String getChannel() {
        return channel;
    }

    public String getToken() {
        return token;
    }

    public Jid getDomain() {
        return domain;
    }
}
