package eu.siacs.p2.apns;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApnsHttpInterface {

    @Headers({"apns-push-type: alert", "apns-priority: 10"})
    @POST("/3/device/{token}")
    Call<Void> sendAlert(
            @Path("token") String token,
            @Header("apns-topic") String topic,
            @Body Notification notification);

    @Headers({"apns-push-type: background", "apns-priority: 5"})
    @POST("/3/device/{token}")
    Call<Void> sendBackground(
            @Path("token") String token,
            @Header("apns-topic") String topic,
            @Body Notification notification);
}
