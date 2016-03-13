package comfallblank.github.gankgirl.utils;

import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by fallb on 2016/3/12.
 */
public class Downloader {
    final static String TAG = "Downloader";

    public static byte[] getBytes(String url) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().bytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getString(String url) {
        byte[] jsonBytes = getBytes(url);
        if (jsonBytes == null) {
            return null;
        }
        return new String(jsonBytes);
    }
}
