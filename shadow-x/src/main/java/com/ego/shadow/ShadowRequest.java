package com.ego.shadow;

import android.util.Log;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ShadowRequest {

    public static String get(StringBuilder url) {
        return get(url.toString());
    }

    public static String get(final String url) {

        FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return request(url);
            }
        });

        new Thread(task).start();

        try {
            String json = task.get();
            return json;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String request(String url){
        Log.e("Request", url);

        InputStream stream = null;
        HttpURLConnection connection = null;

        StringBuilder json = new StringBuilder();

        try {
            URL u = new URL(url);
            connection = (HttpURLConnection) u.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            stream = connection.getInputStream();

            byte[] buf = new byte[1024];
            int count = 0;
            while ((count = stream.read(buf)) != -1) {
                json.append(new String(buf, 0, count, Charset.forName("utf-8")));
            }

            stream.close();
            Log.e("Response", json.toString());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }

            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return json.toString();
    }


}
