package eu.siacs.p2.apns;

import com.google.common.base.Strings;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import eu.siacs.p2.PushService;
import eu.siacs.p2.pojo.Target;
import eu.siacs.p2.util.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApnsPushService implements PushService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApnsPushService.class);

    private static final String BASE_URL = "https://api.push.apple.com";

    private static final String SANDBOX_BASE_URL = "https://api.sandbox.push.apple.com";

    private final ApnsHttpInterface httpInterface;

    private final ApnsConfiguration configuration;

    public ApnsPushService(final ApnsConfiguration configuration) {
        this.configuration = configuration;
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES);

        final SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(
                    new KeyManager[] {new ClientCertificateKeyManager(configuration)}, null, null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new AssertionError(e);
        }

        final X509TrustManager trustManager = TrustManager.getDefault();
        if (trustManager == null) {
            throw new AssertionError("Unable to find default trust manager");
        }
        final OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        okHttpBuilder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);

        final Retrofit.Builder retrofitBuilder = new Retrofit.Builder();
        if (configuration != null && configuration.sandbox()) {
            retrofitBuilder.baseUrl(SANDBOX_BASE_URL);
        } else {
            retrofitBuilder.baseUrl(BASE_URL);
        }
        retrofitBuilder.addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()));
        retrofitBuilder.client(okHttpBuilder.build());

        final Retrofit retrofit = retrofitBuilder.build();

        this.httpInterface = retrofit.create(ApnsHttpInterface.class);
    }

    @Override
    public boolean push(final Target target, final boolean highPriority) {
        LOGGER.info("attempt push to APNS (" + target.getToken() + ")");
        final String bundleId = configuration.bundleId();
        if (Strings.isNullOrEmpty(bundleId)) {
            LOGGER.error("bundle id not configured");
            return false;
        }
        try {
            final Notification notification =
                    highPriority
                            ? Notification.createAlert()
                            : Notification.createContentAvailable();
            final Response<Void> response =
                    this.httpInterface
                            .sendAlert(target.getToken(), bundleId, notification)
                            .execute();
            if (response.isSuccessful()) {
                LOGGER.info("push to APNS was successful");
                return true;
            } else {
                final ResponseBody errorBody = response.errorBody();
                final String errorBodyString = errorBody == null ? null : errorBody.string();
                LOGGER.warn(
                        "push to APNS failed with response code="
                                + response.code()
                                + ", body="
                                + errorBodyString);
            }
        } catch (Exception e) {
            LOGGER.warn("push to APNS failed", e);
            return false;
        }

        return false;
    }
}
