package eu.siacs.p2;

import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.ImmutableMap;
import eu.siacs.p2.apns.ApnsPushService;
import eu.siacs.p2.fcm.FcmPushService;
import eu.siacs.p2.pojo.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PushServiceManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PushServiceManager.class);

    private static final ImmutableClassToInstanceMap<PushService> SERVICES;

    private static final ImmutableMap<Service, Class<? extends PushService>> SERVICE_TO_CLASS;

    static {
        final var builder = ImmutableClassToInstanceMap.<PushService>builder();
        final ImmutableMap.Builder<Service, Class<? extends PushService>> serviceToClassBuilder = ImmutableMap.builder();
        final var configuration = Configuration.getInstance();
        final var fcmConfiguration = configuration.getFcmConfiguration();
        if (fcmConfiguration != null) {
            builder.put(FcmPushService.class, new FcmPushService(fcmConfiguration));
            serviceToClassBuilder.put(Service.FCM, FcmPushService.class);
        }
        final var apnsConfiguration = configuration.getApnsConfiguration();
        if (apnsConfiguration != null) {
            builder.put(ApnsPushService.class, new ApnsPushService(apnsConfiguration));
            serviceToClassBuilder.put(Service.APNS, ApnsPushService.class);
        }

        SERVICES = builder.build();

        if (SERVICES.isEmpty()) {
            LOGGER.warn("No push service have been configured");
        }

        SERVICE_TO_CLASS = serviceToClassBuilder.build();
    }

    public static PushService getPushServiceInstance(Service service) {
        final Class<? extends PushService> clazz = SERVICE_TO_CLASS.get(service);
        if (clazz == null) {
            throw new IllegalStateException(String.format("No corresponding class found for service=%s", service));
        }
        final PushService pushService = SERVICES.getInstance(clazz);
        if (pushService == null) {
            throw new IllegalStateException(String.format("No instance found for %s", clazz.getName()));
        }
        return SERVICES.getInstance(clazz);
    }
}
