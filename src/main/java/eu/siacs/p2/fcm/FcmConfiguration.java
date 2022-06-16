package eu.siacs.p2.fcm;

import org.immutables.gson.Gson;
import org.immutables.value.Value;

@Value.Immutable
@Gson.TypeAdapters
public interface FcmConfiguration {

    String serviceAccountFile();

    default boolean collapse() {
        return true;
    }
}
