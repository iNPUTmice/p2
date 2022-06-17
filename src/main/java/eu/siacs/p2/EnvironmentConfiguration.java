package eu.siacs.p2;

import com.google.common.base.Strings;
import eu.siacs.p2.apns.ImmutableApnsConfiguration;
import eu.siacs.p2.fcm.ImmutableFcmConfiguration;
import java.util.Arrays;
import java.util.Locale;

public class EnvironmentConfiguration {

    private static final String P2_DATABASE_URL = "P2_DATABASE_URL";
    private static final String P2_DATABASE_USERNAME = "P2_DATABASE_USERNAME";
    private static final String P2_DATABASE_PASSWORD = "P2_DATABASE_PASSWORD";

    private static final String P2_JABBER_ID = "P2_JABBER_ID";
    private static final String P2_HOSTNAME = "P2_HOSTNAME";
    private static final String P2_PORT = "P2_PORT";
    private static final String P2_SHARED_SECRET = "P2_SHARED_SECRET";

    private static final String P2_FCM_SERVICE_ACCOUNT_FILE = "P2_SERVICE_ACCOUNT_FILE";
    private static final String P2_FCM_COLLAPSE = "P2_FCM_COLLAPSE";

    private static final String P2_APNS_CERTIFICATE = "P2_APNS_CERTIFICATE";
    private static final String P2_APNS_PRIVATE_KEY = "P2_APNS_PRIVATE_KEY";
    private static final String P2_APNS_BUNDLE_ID = "P2_APNS_BUNDLE_ID";
    private static final String P2_APNS_SANDBOX = "P2_APNS_SANDBOX";

    private EnvironmentConfiguration() {
        throw new IllegalStateException("Do not instantiate me");
    }

    public static Configuration create() {
        final var builder =
                ImmutableConfiguration.builder()
                        .dbUrl(readEnvironmentOrThrow(P2_DATABASE_URL))
                        .dbUsername(readEnvironmentOrThrow(P2_DATABASE_USERNAME))
                        .dbPassword(readEnvironmentOrThrow(P2_DATABASE_PASSWORD))
                        .jid((readEnvironmentOrThrow(P2_JABBER_ID)))
                        .host(readEnvironmentOrThrow(P2_HOSTNAME))
                        .port(readIntEnvironmentOrThrow(P2_PORT))
                        .sharedSecret(readEnvironmentOrThrow(P2_SHARED_SECRET));
        if (anySet(P2_FCM_SERVICE_ACCOUNT_FILE, P2_FCM_COLLAPSE)) {
            builder.fcmConfiguration(
                    ImmutableFcmConfiguration.builder()
                            .serviceAccountFile(readEnvironmentOrThrow(P2_FCM_SERVICE_ACCOUNT_FILE))
                            .collapse(readBooleanEnvironmentOrThrow(P2_FCM_COLLAPSE))
                            .build());
        }
        if (anySet(P2_APNS_CERTIFICATE, P2_APNS_PRIVATE_KEY, P2_APNS_BUNDLE_ID, P2_APNS_SANDBOX)) {
            builder.apnsConfiguration(
                    ImmutableApnsConfiguration.builder()
                            .certificate(readEnvironmentOrThrow(P2_APNS_CERTIFICATE))
                            .privateKey(readEnvironmentOrThrow(P2_APNS_PRIVATE_KEY))
                            .bundleId(readEnvironmentOrThrow(P2_APNS_BUNDLE_ID))
                            .sandbox(readBooleanEnvironmentOrThrow(P2_APNS_SANDBOX))
                            .build());
        }

        return builder.build();
    }

    private static String readEnvironmentOrThrow(final String name) {
        final var value = System.getenv(name);
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalStateException(
                    String.format("Environment variable '%s' not set", name));
        }
        return value;
    }

    private static int readIntEnvironmentOrThrow(final String name) {
        final var value = readEnvironmentOrThrow(name);
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException e) {
            throw new IllegalStateException(
                    String.format("Environment variable '%s' must be a valid integer", name));
        }
    }

    private static boolean anySet(final String... names) {
        return Arrays.stream(names).anyMatch(name -> !Strings.isNullOrEmpty(System.getenv(name)));
    }

    private static boolean readBooleanEnvironmentOrThrow(final String name) {
        final var value = readEnvironmentOrThrow(name);
        switch (value.toLowerCase(Locale.ROOT)) {
            case "true":
            case "1":
                return true;
            case "false":
            case "0":
                return false;
            default:
                throw new IllegalStateException(
                        String.format("Environment variable '%s' must be a valid boolean", name));
        }
    }
}
