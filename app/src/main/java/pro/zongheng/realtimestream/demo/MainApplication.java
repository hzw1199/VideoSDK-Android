package pro.zongheng.realtimestream.demo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.wuadam.fflibrary.FFJNI;
import com.wuadam.fflibrary.listeners.FFListener;
import com.wuadam.fflibrary.listeners.FFListenerManager;
import com.wuadam.medialibrary.MediaHelper;

import pro.zongheng.realtimestream.demo.utils.Constants;
import pro.zongheng.realtimestream.demo.utils.ImageUtils;

/**
 * @Description:
 * @Author: zongheng
 * @Date: 2021/12/6 5:39 下午
 */
public class MainApplication extends Application {
    private static final String TAG = MainApplication.class.getSimpleName();
    public static Context applicationContext;
    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = this;
        MediaHelper.init(this);
        FFJNI.init();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isHwDecode = sharedPreferences.getBoolean(Constants.PREF_IS_HW_DECODE, true);

        /*
         * Set whether to hardware decode (default value is true). This method needs to be called before SurfaceView is created to take effect.
         * @param isHw
         * @return Whether the setting is successful
         */
        FFJNI.setHwDecode(isHwDecode);
        FFListenerManager.addListener(MainApplication.applicationContext, ffListener);
    }

    FFListener ffListener = new FFListener() {
        // Retrieving result after calling FFJNI.shotFrame
        @Override
        public void onShotFrame(String path, boolean success, int handler) {
            Toast.makeText(applicationContext, success? R.string.take_photo_success: R.string.take_photo_fail, Toast.LENGTH_SHORT).show();
            if (success) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ImageUtils.save2Album(path, "rts-library", System.currentTimeMillis() + ".jpg", false);
                    }
                }).start();
            }
        }

        // Retrieving record result after calling FFJNI.stopRecord
        @Override
        public void onRecordVideo(String path, boolean success, int handler) {
            Toast.makeText(applicationContext, success? R.string.record_success: R.string.record_fail, Toast.LENGTH_SHORT).show();
            if (success) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        ImageUtils.save2Album(path, "rts-library", System.currentTimeMillis() + ".mp4", true);
                    }
                }).start();
            }
        }

        @Override
        public void onDowngradeToSwDecode(int handler) {
            Toast.makeText(applicationContext, getString(R.string.sw_decode, handler), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onSpsPps(byte[] sps, byte[] pps, int handler) {
            StringBuilder stringBuilder = new StringBuilder(sps.length);
            for (int i = 0; i<sps.length; i++) {
                stringBuilder.append(String.format("%02X ", sps[i]));
            }
            Log.d(TAG, "onSpsPpsAnnexB sps: " + stringBuilder.toString());

            stringBuilder = new StringBuilder(pps.length);
            for (int i = 0; i<pps.length; i++) {
                stringBuilder.append(String.format("%02X ", pps[i]));
            }
            Log.d(TAG, "onSpsPpsAnnexB pps: " + stringBuilder.toString());
        }
    };
}
