package eu.siacs.p2;

import org.apache.commons.codec.digest.DigestUtils;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

public class Utils {

    public static String random(int length, SecureRandom random) {
        final byte[] bytes = new byte[3 * length];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String combineAndHash(String... parts) {
        return DigestUtils.sha1Hex(Arrays.stream(parts).collect(Collectors.joining("\00")));
    }

    static void sleep(long interval) {
        try {
            Thread.sleep(interval);
        } catch (InterruptedException e) {

        }
    }
}
