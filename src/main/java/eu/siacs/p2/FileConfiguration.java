package eu.siacs.p2;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import eu.siacs.p2.apns.GsonAdaptersApnsConfiguration;
import eu.siacs.p2.fcm.GsonAdaptersFcmConfiguration;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class FileConfiguration {

    private FileConfiguration() {
        throw new IllegalStateException("Do not instantiate me");
    }

    public static Configuration read(final String filename) {
        final var file = new File(filename);
        final var gson =
                new GsonBuilder()
                        .registerTypeAdapterFactory(new eu.siacs.p2.GsonAdaptersConfiguration())
                        .registerTypeAdapterFactory(new GsonAdaptersApnsConfiguration())
                        .registerTypeAdapterFactory(new GsonAdaptersFcmConfiguration())
                        .create();
        try {
            System.out.println("Reading configuration from " + file.getAbsolutePath());
            return gson.fromJson(new FileReader(file), Configuration.class);
        } catch (final FileNotFoundException e) {
            throw new RuntimeException("Configuration file not found");
        } catch (final JsonSyntaxException e) {
            throw new RuntimeException("Invalid syntax in config file", e);
        }
    }
}
