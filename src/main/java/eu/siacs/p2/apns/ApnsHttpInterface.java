package eu.siacs.p2.apns;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApnsHttpInterface {

    @Headers({
            "apns-push-type: alert",
            "apns-priority: 10"
    })
    @POST("/3/device/{token}")
    Call<Result> sendAlert(@Path("token") String token, @Body Notification notification);

    @Headers({
            "apns-push-type: background",
            "apns-priority: 5"
    })
    @POST("/3/device/{token}")
    Call<Result> sendBackground(@Path("token") String token, @Body Notification notification);
}
