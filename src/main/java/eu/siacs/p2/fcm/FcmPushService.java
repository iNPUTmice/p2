package eu.siacs.p2.fcm;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import de.gultsch.xmpp.addr.adapter.Adapter;
import eu.siacs.p2.Configuration;
import eu.siacs.p2.PushService;
import eu.siacs.p2.pojo.Target;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FcmPushService implements PushService {

    private static final String BASE_URL = "https://fcm.googleapis.com";

    private final FcmHttpInterface httpInterface;


    public FcmPushService() {
        final GsonBuilder gsonBuilder = new GsonBuilder();
        Adapter.register(gsonBuilder);
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);

        final Retrofit.Builder retrofitBuilder = new Retrofit.Builder();
        retrofitBuilder.baseUrl(BASE_URL);
        retrofitBuilder.addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()));

        final Retrofit retrofit = retrofitBuilder.build();

        this.httpInterface = retrofit.create(FcmHttpInterface.class);
    }

    @Override
    public boolean push(Target target) {
        final Message message = Message.createHighPriority(target, Configuration.getInstance().isCollapse());
        return push(message);
    }

    private boolean push(Message message) {
        try {
            final Response<Result> response = this.httpInterface.send(message, "key=" + Configuration.getInstance().getFcmAuthKey()).execute();
            if (response.isSuccessful()) {
                final Result result = response.body();
                return result != null && result.getSuccess() > 0;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }
}
