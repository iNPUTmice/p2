package eu.siacs.p2;

import com.google.gson.annotations.SerializedName;
import eu.siacs.p2.apns.ApnsConfiguration;
import eu.siacs.p2.fcm.FcmConfiguration;
import java.util.Optional;
import org.immutables.value.Value;

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

    @SerializedName("fcm")
    Optional<FcmConfiguration> fcmConfiguration();

    String jid();

    String host();

    int port();

    String sharedSecret();

    default void validate() {
        fcmConfiguration().ifPresent(FcmConfiguration::validate);
        apnsConfiguration().ifPresent(ApnsConfiguration::validate);
    }
}
