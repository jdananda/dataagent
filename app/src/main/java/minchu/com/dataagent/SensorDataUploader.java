package minchu.com.dataagent;

import android.util.Base64;
import android.util.Log;

import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLEncoder;

import java.net.HttpURLConnection;
import java.util.concurrent.Executors;

/**
 * Created by Deva on 11/20/2016.
 */
public class SensorDataUploader {
    private static final String REST_URL_SUFFIX = "/deva.php";
    private final String hostName;
    private final String port;

    public SensorDataUploader(String hostName, String port) {
        this.hostName = hostName;
        this.port = port;
    }

    public void upload(final long timestamp, final byte[] rawBytes) {
        Executors.newSingleThreadExecutor().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    postData(rawBytes, timestamp);
                } catch (Exception e) {
                    Log.i(SensorDataUploader.class.getName(), "Unable to upload data");
                }
            }
        });

    }

    private void postData(byte[] bytes, long timestamp) throws Exception {
        StringBuilder params = new StringBuilder("bytes=");
        params.append(new String(Base64.encode(bytes, Base64.DEFAULT)));
        params.append("&timestamp=");
        params.append(URLEncoder.encode(String.valueOf(timestamp), "UTF-8"));

        String url = "http://" + hostName + ":" + port + REST_URL_SUFFIX + "?" + params.toString();
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Accept-Language", "UTF-8");

        con.setDoOutput(true);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(con.getOutputStream());
        outputStreamWriter.flush();

        int responseCode = con.getResponseCode();
        Log.i(SensorDataUploader.class.getName(), "Sending 'POST' request to URL : " + url);
        Log.i(SensorDataUploader.class.getName(), "POST params : " + params);
        Log.i(SensorDataUploader.class.getName(), "Response code : " + responseCode);
    }
}
