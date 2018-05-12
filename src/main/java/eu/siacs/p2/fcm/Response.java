package eu.siacs.p2.fcm;

import com.google.gson.annotations.SerializedName;

public class Response {

    private int success;
    private int failure;
    @SerializedName("multicast_id")
    private long multicastId;


    public int getSuccess() {
        return success;
    }

    public int getFailure() {
        return failure;
    }
}
