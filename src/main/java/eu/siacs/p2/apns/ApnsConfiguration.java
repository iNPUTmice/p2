package eu.siacs.p2.apns;

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
}
