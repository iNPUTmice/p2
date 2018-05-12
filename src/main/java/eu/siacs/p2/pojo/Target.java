package eu.siacs.p2.pojo;

import eu.siacs.p2.P2;
import eu.siacs.p2.Utils;
import eu.siacs.p2.persistance.PrimaryKey;
import rocks.xmpp.addr.Jid;

public class Target {


    @PrimaryKey(replace = true)
    private String device;
    private Jid domain;
    private String token;
    private String node;
    private String secret;

    private Target(String device, Jid domain, String token, String node, String secret) {
        this.device = device;
        this.domain = domain;
        this.token = token;
        this.node = node;
        this.secret = secret;
    }


    @Override
    public String toString() {
        return "Target{" +
                "device='" + device + '\'' +
                ", domain=" + domain +
                ", token='" + token + '\'' +
                ", node='" + node + '\'' +
                ", secret='" + secret + '\'' +
                '}';
    }

    public static Target create(Jid account, String deviceId, String token) {
        String node = Utils.random(3, P2.SECURE_RANDOM);
        String secret = Utils.random(6, P2.SECURE_RANDOM);
        String device = Utils.combineAndHash(account.asBareJid().toEscapedString(),deviceId);
        return new Target(device, Jid.ofDomain(account.getDomain()), token, node, secret);
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

    public String getToken() {
        return token;
    }

    public Jid getDomain() {
        return domain;
    }
}
