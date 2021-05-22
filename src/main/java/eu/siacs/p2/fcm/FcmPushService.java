package eu.siacs.p2.fcm;

import java.io.FileInputStream;
import java.io.IOException;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.*;
import eu.siacs.p2.Configuration;
import eu.siacs.p2.PushService;
import eu.siacs.p2.TargetDeviceNotFoundException;
import eu.siacs.p2.pojo.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FcmPushService implements PushService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FcmPushService.class);

    public FcmPushService() {
        final FcmConfiguration config = Configuration.getInstance().getFcmConfiguration();
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(config.getServiceAccountFile())))
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (final IOException e) {
            LOGGER.error("Unable to initialize firebase app", e);
        }
    }

    @Override
    public boolean push(Target target, boolean highPriority) throws TargetDeviceNotFoundException {
        final FcmConfiguration config = Configuration.getInstance().getFcmConfiguration();
        String account = target.getDevice();

        String channel = target.getChannel() == null || target.getChannel().isEmpty() ? null : target.getChannel();
        final String collapseKey;
        if (config.collapse) {
            if (channel == null) {
                collapseKey = account.substring(0, 6);
            } else {
                collapseKey = channel.substring(0, 6);
            }
        } else {
            collapseKey = null;
        }

        final Message.Builder message = Message.builder().
                setToken(target.getToken()).
                setAndroidConfig(AndroidConfig.builder().
                        setCollapseKey(collapseKey).build()).
                putData("account", account);
        if (channel != null) {
            message.putData("channel", channel);
        }
        return push(message.build());
    }

    private boolean push(Message message) throws TargetDeviceNotFoundException {
        try {
            FirebaseMessaging.getInstance().send(message);
            return true;
        } catch (final FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                throw new TargetDeviceNotFoundException(e);
            } else {
                LOGGER.warn("push to FCM failed with unknown messaging error code", e);
                return false;
            }
        }
    }


    public static class FcmConfiguration {
        private String serviceAccountFile;
        private boolean collapse;

        public String getServiceAccountFile() {
            return serviceAccountFile;
        }

        public boolean isCollapse() {
            return collapse;
        }
    }
}