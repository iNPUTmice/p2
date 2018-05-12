package eu.siacs.p2;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import de.gultsch.xmpp.addr.adapter.Adapter;
import rocks.xmpp.addr.Jid;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;

public class Configuration {

    private static File FILE = new File("config.json");
    private static Configuration INSTANCE;

    private String host = "localhost";
    private int port = 5348; //prosody is 5347
    private Jid jid;
    private String sharedSecret;
    private String fcmAuthKey;
    private String storagePath = Paths.get("").toAbsolutePath().toString();

    private Configuration() {

    }

    public String getName() {
        return jid.toEscapedString();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getSharedSecret() {
        return sharedSecret;
    }

    public String getFcmAuthKey() {
        return fcmAuthKey;
    }

    public synchronized static void setFilename(String filename) throws FileNotFoundException {
        if (INSTANCE != null) {
            throw new IllegalStateException("Unable to set filename after instance has been created");
        }
        Configuration.FILE = new File(filename);
        if (!Configuration.FILE.exists()) {
            throw new FileNotFoundException();
        }
    }

    public synchronized static  Configuration getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public String getStorageFile(Class clazz) {
        return new File(storagePath,clazz.getSimpleName()+".db").getAbsolutePath();
    }

    private static Configuration load() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        Adapter.register(gsonBuilder);
        final Gson gson = gsonBuilder.create();
        try {
            System.out.println("Reading configuration from "+FILE.getAbsolutePath());
            return gson.fromJson(new FileReader(FILE),Configuration.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Configuration file not found");
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("Invalid syntax in config file");
        }
    }

    public Jid getJid() {
        return jid;
    }
}
