package eu.siacs.p2.persistance;

import de.gultsch.xmpp.addr.adapter.Adapter;
import eu.siacs.p2.Configuration;
import eu.siacs.p2.pojo.Service;
import eu.siacs.p2.pojo.Target;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.converters.Converter;
import org.sql2o.quirks.NoQuirks;
import org.sql2o.quirks.Quirks;
import rocks.xmpp.addr.Jid;

import java.util.HashMap;

public class TargetStore {

    private static final String CREATE_TARGET_TABLE = "create table if not exists target(service char(4), device CHAR(40) NOT NULL, channel CHAR(40) NOT NULL DEFAULT '', domain varchar(253), token varchar(255), node char(12), secret char(24), primary key(device, channel), index nodeDomain (node,domain));";

    private static TargetStore INSTANCE = null;
    private final Sql2o database;

    private TargetStore() {
        final Configuration configuration = Configuration.getInstance();
        HashMap<Class, Converter> converters = new HashMap<>();
        Adapter.register(converters);
        Quirks quirks = new NoQuirks(converters);
        database = new Sql2o(configuration.getDbUrl(), configuration.getDbUsername(), configuration.getDbPassword(), quirks);
        try (Connection connection = database.open()) {
            connection.createQuery(CREATE_TARGET_TABLE).executeUpdate();
        }
    }


    public static synchronized TargetStore getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TargetStore();
        }
        return INSTANCE;
    }

    public void create(Target target) {
        try (Connection connection = database.open()) {
            connection.createQuery("INSERT INTO target (service,device,domain,token,node,secret) VALUES(:service,:device,:domain,:token,:node,:secret)").bind(target).executeUpdate();
        }
    }

    public Target find(Jid domain, String node) {
        try (Connection connection = database.open()) {
            return connection.createQuery("select service,device,domain,token,node,secret from target where domain=:domain and node=:node limit 1").addParameter("domain", domain).addParameter("node", node).executeAndFetchFirst(Target.class);
        }
    }

    public Target find(Service service, String device, String channel) {
        try (Connection connection = database.open()) {
            return connection
                    .createQuery("select service,device,domain,channel,token,node,secret from target where service=:service and device=:device and channel=:channel")
                    .addParameter("service", service)
                    .addParameter("device", device)
                    .addParameter("channel", channel)
                    .executeAndFetchFirst(Target.class);
        }
    }

    public boolean update(Target target) {
        try (Connection connection = database.open()) {
            return connection.createQuery("update target set token=:token where device=:device and channel=:channel").bind(target).executeUpdate().getResult() == 1;
        }
    }

    public boolean delete(String device, String channel) {
        try(Connection connection = database.open()) {
            return connection
                    .createQuery("delete from target where device=:device and channel=:channel")
                    .addParameter("device",device)
                    .addParameter("channel",channel)
                    .executeUpdate()
                    .getResult() == 1;
        }
    }

}
