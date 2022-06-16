package eu.siacs.p2;

import com.google.gson.annotations.SerializedName;
import eu.siacs.p2.apns.ApnsConfiguration;
import eu.siacs.p2.fcm.FcmConfiguration;
import org.immutables.value.Value;

import java.io.File;
import java.util.Optional;

@org.immutables.gson.Gson.TypeAdapters
@Value.Immutable
public interface Configuration {

    String dbUrl();

    String dbUsername();

    String dbPassword();

    @Value.Default
    default boolean debug() {
        return true;
    }

    @SerializedName("apns")
    Optional<ApnsConfiguration> apnsConfiguration();

    String jid();

    String host();

    int port();

    String sharedSecret();

    default void validate() {
        final var optionalFcm = fcmConfiguration();
        if (optionalFcm.isPresent()) {
            final String filename = optionalFcm.get().serviceAccountFile();
            if (filename == null) {
                throw new IllegalStateException("FCM enabled but no service account file set");
            }
            final File file = new File(filename);
            if (file.exists()) {
                return;
            }
            throw new IllegalStateException(
                    String.format("%s does not exist", file.getAbsolutePath()));
        }
    }

    @SerializedName("fcm")
    Optional<FcmConfiguration> fcmConfiguration();
}
