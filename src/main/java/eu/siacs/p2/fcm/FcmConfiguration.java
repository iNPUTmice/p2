package eu.siacs.p2.fcm;

import java.io.File;
import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
public interface FcmConfiguration {

    String serviceAccountFile();

    @Value.Default
    default boolean collapse() {
        return true;
    }

    default void validate() {
        final var file = new File(serviceAccountFile());
        if (file.exists()) {
            return;
        }
        throw new IllegalStateException(String.format("%s does not exist", file.getAbsolutePath()));
    }
}
