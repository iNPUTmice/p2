package eu.siacs.p2.apns;

import java.io.File;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
public interface ApnsConfiguration {

    String privateKey();

    String certificate();

    String bundleId();

    @Value.Default
    default boolean sandbox() {
        return false;
    }

    default void validate() {
        final var certificate = new File(certificate());
        if (!certificate.exists()) {
            throw new IllegalStateException(
                    String.format("%s does not exist", certificate.getAbsolutePath()));
        }

        final var privateKey = new File(privateKey());
        if (!privateKey.exists()) {
            throw new IllegalStateException(
                    String.format("%s does not exist", privateKey.getAbsolutePath()));
        }
    }
}
