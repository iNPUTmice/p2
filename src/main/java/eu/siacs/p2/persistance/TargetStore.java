package eu.siacs.p2.persistance;

import de.gultsch.xmpp.addr.adapter.Adapter;
import eu.siacs.p2.Configuration;
import eu.siacs.p2.pojo.Target;
import org.sql2o.Connection;
import org.sql2o.Sql2o;
import org.sql2o.converters.Converter;
import org.sql2o.quirks.NoQuirks;
import org.sql2o.quirks.Quirks;
import rocks.xmpp.addr.Jid;

import java.util.HashMap;

public class TargetStore {

    private final Sql2o database;

    private static TargetStore INSTANCE = null;

    private TargetStore() {
        String filename = Configuration.getInstance().getStorageFile(TargetStore.class);
        HashMap<Class, Converter> converters = new HashMap<>();
        Adapter.register(converters);
        Quirks quirks = new NoQuirks(converters);
        database = new Sql2o("sqlite:"+filename, null, null, quirks);
        TableHelper.create(database, Target.class);
    }


    public static synchronized TargetStore getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TargetStore();
        }
        return INSTANCE;
    }

    public void create(Target target) {
        try (Connection connection = database.open()) {
            connection.createQuery("INSERT INTO "+TableHelper.name(Target.class)+" (device,domain,token,node,secret) VALUES(:device,:domain,:token,:node,:secret)").bind(target).executeUpdate();
        }
    }

    public Target find(Jid domain, String node) {
        try (Connection connection = database.open()) {
            return connection.createQuery("select device,domain,token,node,secret from "+TableHelper.name(Target.class)+" where domain=:domain and node=:node limit 1").addParameter("domain",domain).addParameter("node",node).executeAndFetchFirst(Target.class);
        }
    }

    public Target find(String device) {
        try (Connection connection = database.open()) {
            return connection
                    .createQuery("select device,domain,token,node,secret from "+TableHelper.name(Target.class)+" where device=:device")
                    .addParameter("device",device)
                    .executeAndFetchFirst(Target.class);
        }
    }

    public void update(Target target) {
        try(Connection connection = database.open()) {
            connection.createQuery("update "+TableHelper.name(Target.class)+" set token=:token where device=:device").bind(target).executeUpdate();
        }
    }

}
