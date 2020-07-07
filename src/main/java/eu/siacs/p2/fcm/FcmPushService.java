package eu.siacs.p2.fcm;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import eu.siacs.p2.Configuration;
import eu.siacs.p2.PushService;
import eu.siacs.p2.pojo.Target;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FcmPushService implements PushService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FcmPushService.class);

    private static final String BASE_URL = "https://fcm.googleapis.com";

    private final FcmHttpInterface httpInterface;


    public FcmPushService() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);

        final Retrofit.Builder retrofitBuilder = new Retrofit.Builder();
        retrofitBuilder.baseUrl(BASE_URL);
        retrofitBuilder.addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()));

        final Retrofit retrofit = retrofitBuilder.build();

        this.httpInterface = retrofit.create(FcmHttpInterface.class);
    }

    @Override
    public boolean push(Target target, boolean highPriority) {
        final FcmConfiguration config = Configuration.getInstance().getFcmConfiguration();
        final Message message = Message.createHighPriority(target, config.isCollapse());
        return push(message);
    }

    private boolean push(Message message) {
        final FcmConfiguration config = Configuration.getInstance().getFcmConfiguration();
        final String authKey = config == null ? null :config.getAuthKey();
        if (authKey == null) {
            LOGGER.warn("No fcm auth key configured");
            return false;
        }
        try {
            final Response<Result> response = this.httpInterface.send(message, "key=" + authKey).execute();
            if (response.isSuccessful()) {
                final Result result = response.body();
                return result != null && result.getSuccess() > 0;
            } else {
                final ResponseBody errorBody = response.errorBody();
                final String errorBodyString = errorBody == null ? null : errorBody.string();
                LOGGER.warn("push to FCM failed with response code=" +response.code()+", body="+errorBodyString);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static class FcmConfiguration {
        private String authKey;
        private boolean collapse;

        public String getAuthKey() {
            return authKey;
        }

        public boolean isCollapse() {
            return collapse;
        }
    }
}
