package eu.siacs.p2;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import eu.siacs.p2.apns.GsonAdaptersApnsConfiguration;
import eu.siacs.p2.fcm.GsonAdaptersFcmConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class ConfigurationFile {

    private static File FILE = new File("config.json");
    private static Configuration INSTANCE;

    public static synchronized void setFilename(final String filename)
            throws FileNotFoundException {
        if (INSTANCE != null) {
            throw new IllegalStateException(
                    "Unable to set filename after instance has been created");
        }
        final var file = new File(filename);
        if (file.exists()) {
            FILE = file;
        } else {
            throw new FileNotFoundException();
        }
        FILE = new File(filename);
    }

    public static synchronized Configuration getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    private static Configuration load() {
        final var gson =
                new GsonBuilder()
                        .registerTypeAdapterFactory(new eu.siacs.p2.GsonAdaptersConfiguration())
                        .registerTypeAdapterFactory(new GsonAdaptersApnsConfiguration())
                        .registerTypeAdapterFactory(new GsonAdaptersFcmConfiguration())
                        .create();
        try {
            System.out.println("Reading configuration from " + FILE.getAbsolutePath());
            return gson.fromJson(new FileReader(FILE), Configuration.class);
        } catch (final FileNotFoundException e) {
            throw new RuntimeException("Configuration file not found");
        } catch (final JsonSyntaxException e) {
            throw new RuntimeException("Invalid syntax in config file", e);
        }
    }
}
