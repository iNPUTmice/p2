package eu.siacs.p2.fcm;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.gultsch.xmpp.addr.adapter.Adapter;
import eu.siacs.p2.Configuration;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;

public class FcmService {

    private static final FcmService INSTANCE = new FcmService();

    private static final URL FCM_URL;

    private final GsonBuilder gsonBuilder;

    static {
        try {
            FCM_URL = new URL("https://fcm.googleapis.com/fcm/send");
        } catch (MalformedURLException e) {
            throw new AssertionError("FCM URL wrong");
        }
    }

    private FcmService() {
        this.gsonBuilder = new GsonBuilder();
        Adapter.register(gsonBuilder);
    }

    public static FcmService getInstance() {
        return INSTANCE;
    }

    public boolean push(Message message) {
        final Gson gson = gsonBuilder.create();
        try {
            HttpsURLConnection urlConnection = (HttpsURLConnection) FCM_URL.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Authorization", "key=" + Configuration.getInstance().getFcmAuthKey());
            urlConnection.setDoOutput(true);
            OutputStreamWriter outputStream = new OutputStreamWriter(urlConnection.getOutputStream());
            gson.toJson(message, outputStream);
            outputStream.flush();
            outputStream.close();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            Response response = gson.fromJson(bufferedReader,Response.class);
            boolean success;
            if (urlConnection.getResponseCode() == 200) {
                success = response.getSuccess() > 0;
            } else {
                success = false;
            }
            bufferedReader.close();
            return success;
        } catch (Exception e) {
            return false;
        }
    }

}
