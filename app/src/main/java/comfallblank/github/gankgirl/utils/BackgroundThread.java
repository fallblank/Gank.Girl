package comfallblank.github.gankgirl.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import comfallblank.github.gankgirl.model.Meizi;

/**
 * Created by fallb on 2016/3/13.
 */
public class BackgroundThread extends HandlerThread {
    private final static String TAG = "BackgroundThread";
    private final static String BASE_URL = "http://gank.io/api/random/data/福利/";

    public final static int MSG_FAILED = 0;
    public final static int MSG_SUCCESS = 2;
    public final static int MSG_REFLESH = 3;
    public final static int MSG_REQUEST_MEIZI = 4;
    public final static int MSG_REQUEST_BITMAP = 5;

    private ArrayList<Meizi> mDates;
    private Map<ImageView, String> mRequestMap = Collections.synchronizedMap(new HashMap<ImageView, String>());
    /**
     * 一次加载多少数据，默认10个
     */
    private int mCount = 20;

    private Handler mSelfHandler;
    private Handler mResHandler;
    private Linstener mLinstener;
    private LruCache<String, byte[]> mBitMapCache;


    public interface Linstener {
        void loadfinished(ImageView imageView, Bitmap bitmap);
    }

    public void setLinstener(Linstener linstener) {
        mLinstener = linstener;
    }

    public BackgroundThread(Handler resHandler, ArrayList<Meizi> dates) {
        super(TAG);
        mResHandler = resHandler;
        mDates = dates;
        initBitmapCache();
    }

    private void initBitmapCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        mBitMapCache = new LruCache<String, byte[]>(cacheSize) {
            @Override
            protected int sizeOf(String key, byte[] value) {
                return value.length / 1024;
            }
        };
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();
        mSelfHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_REQUEST_MEIZI:
                        loadGank();
                        break;
                    case MSG_REQUEST_BITMAP:
                        loadBitmap((ImageView) msg.obj);
                        break;
                    default:
                        break;
                }
            }
        };
    }

    public void requestBitmap(ImageView imageView, Meizi meizi) {
        byte[] bytes = mBitMapCache.get(meizi.getUrl());
        if (bytes != null) {
            final Bitmap bitmap = decodeBitMap(imageView, bytes);
            mLinstener.loadfinished(imageView, bitmap);
        } else {
            mRequestMap.put(imageView, meizi.getUrl());
            mSelfHandler.obtainMessage(MSG_REQUEST_BITMAP, imageView).sendToTarget();
        }
    }


    private void loadBitmap(final ImageView imageView) {
        final String url = mRequestMap.get(imageView);
        byte[] bytes = Downloader.getBytes(url);
        mBitMapCache.put(url, bytes);
        final Bitmap bitmap = decodeBitMap(imageView, bytes);
        mResHandler.post(new Runnable() {
            @Override
            public void run() {
                mLinstener.loadfinished(imageView, bitmap);
            }
        });
        mRequestMap.remove(imageView);
    }

    public void requestGank() {
        final Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (mSelfHandler != null) {
                    mSelfHandler.obtainMessage(MSG_REQUEST_MEIZI).sendToTarget();
                    timer.cancel();
                }
            }
        };
        if (mSelfHandler == null) {
            timer.schedule(task, 200, 200);
        } else {
            mSelfHandler.obtainMessage(MSG_REQUEST_MEIZI).sendToTarget();
        }
    }

    private void loadGank() {
        String json = Downloader.getString(BASE_URL + mCount);
        //BUG?
        if (json == null) {
            mResHandler.obtainMessage(MSG_FAILED).sendToTarget();
            return;
        }
        mDates.clear();
        parse(json);
        mResHandler.obtainMessage(MSG_SUCCESS).sendToTarget();
    }

    private void parse(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONArray array = root.getJSONArray("results");
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                String url = object.getString("url");
                Meizi meizi = new Meizi(url);
                mDates.add(meizi);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeBitMap(ImageView imageView, byte[] bytes) {
        int width = imageView.getWidth(), height = imageView.getHeight();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        options.inSampleSize = calculateInSampleSize(options, width, height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

}
