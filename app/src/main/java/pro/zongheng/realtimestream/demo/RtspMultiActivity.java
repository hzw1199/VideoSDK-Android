package pro.zongheng.realtimestream.demo;


import static pro.zongheng.realtimestream.demo.utils.Constants.PREF_MULTI_RTSP_URI_1;
import static pro.zongheng.realtimestream.demo.utils.Constants.PREF_MULTI_RTSP_URI_2;
import static pro.zongheng.realtimestream.demo.utils.Constants.PREF_MULTI_RTSP_URI_3;
import static pro.zongheng.realtimestream.demo.utils.Constants.PREF_MULTI_RTSP_URI_4;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wuadam.fflibrary.listeners.FFListener;
import com.wuadam.fflibrary.listeners.FFListenerManager;
import com.wuadam.medialibrary.MediaHelper;

public class RtspMultiActivity extends AppCompatActivity {

    private Spinner spChannel;
    private EditText etUri;
    private CheckBox cbTcp;
    private TextView tvOperate;
    private TextView tvDecodeMode;
    private FrameLayout fl1;
    private FrameLayout fl2;
    private FrameLayout fl3;
    private FrameLayout fl4;

    private SurfaceView vv1;
    private SurfaceView vv2;
    private SurfaceView vv3;
    private SurfaceView vv4;


    private MediaHelper mediaHelper1;
    private MediaHelper mediaHelper2;
    private MediaHelper mediaHelper3;
    private MediaHelper mediaHelper4;

    private boolean isHw1 = true;
    private boolean isHw2 = true;
    private boolean isHw3 = true;
    private boolean isHw4 = true;

    private boolean isTcp1 = false;
    private boolean isTcp2 = false;
    private boolean isTcp3 = false;
    private boolean isTcp4 = false;

    private boolean isPlaying1 = false;
    private boolean isPlaying2 = false;
    private boolean isPlaying3 = false;
    private boolean isPlaying4 = false;

    private FFListenerManager ffListenerManager;
    /**
     *  support 5 channels, from 1 to 5
     */
    private final int DECODE_CHANNEL1 = 2;
    private final int DECODE_CHANNEL2 = 3;
    private final int DECODE_CHANNEL3 = 4;
    private final int DECODE_CHANNEL4 = 5;

    private int selectedChannel = DECODE_CHANNEL1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtsp_multi);

        spChannel = findViewById(R.id.sp_channel);
        etUri = findViewById(R.id.et_uri);
        cbTcp = findViewById(R.id.cb_tcp);
        tvOperate = findViewById(R.id.tv_operate);
        tvDecodeMode = findViewById(R.id.tv_decode_mode);
        fl1 = findViewById(R.id.fl1);
        fl2 = findViewById(R.id.fl2);
        fl3 = findViewById(R.id.fl3);
        fl4 = findViewById(R.id.fl4);
        vv1 = findViewById(R.id.vv1);
        vv2 = findViewById(R.id.vv2);
        vv3 = findViewById(R.id.vv3);
        vv4 = findViewById(R.id.vv4);

        String[] channels = getResources().getStringArray(R.array.channel);
        ArrayAdapter<String> jpAmAdapter = new ArrayAdapter<String>(this, R.layout.item_select_light, channels);
        jpAmAdapter.setDropDownViewResource(R.layout.item_dropdown);
        spChannel.setAdapter(jpAmAdapter);

        spChannel.setSelection(0);
        spChannel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedChannel = position + 2;

                String prefKey = PREF_MULTI_RTSP_URI_1;
                boolean isTcp = isTcp1;
                boolean isHw = isHw1;
                boolean isPlaying = isPlaying1;
                switch (selectedChannel) {
                    case DECODE_CHANNEL1:
                        prefKey = PREF_MULTI_RTSP_URI_1;
                        isTcp = isTcp1;
                        isHw = isHw1;
                        isPlaying = isPlaying1;
                        break;
                    case DECODE_CHANNEL2:
                        prefKey = PREF_MULTI_RTSP_URI_2;
                        isTcp = isTcp2;
                        isHw = isHw2;
                        isPlaying = isPlaying2;
                        break;
                    case DECODE_CHANNEL3:
                        prefKey = PREF_MULTI_RTSP_URI_3;
                        isTcp = isTcp3;
                        isHw = isHw3;
                        isPlaying = isPlaying3;
                        break;
                    case DECODE_CHANNEL4:
                        prefKey = PREF_MULTI_RTSP_URI_4;
                        isTcp = isTcp4;
                        isHw = isHw4;
                        isPlaying = isPlaying4;
                        break;
                }

                String packageName = MainApplication.applicationContext.getPackageName();
                SharedPreferences sp = MainApplication.applicationContext.getSharedPreferences(packageName + "_preferences", MODE_PRIVATE);
                String uri = sp.getString(prefKey, "rtsp://127.0.0.1:8554/main");
                if (!TextUtils.isEmpty(uri)) {
                    etUri.setText(uri);
                }

                cbTcp.setOnCheckedChangeListener(null);
                cbTcp.setChecked(isTcp);
                setTcpListener();

                tvDecodeMode.setText(isHw ? R.string.decode_mode_hw : R.string.decode_mode_sw);

                tvOperate.setText(isPlaying? R.string.stop : R.string.play);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String packageName = MainApplication.applicationContext.getPackageName();
        SharedPreferences sp = MainApplication.applicationContext.getSharedPreferences(packageName + "_preferences", MODE_PRIVATE);
        String uri = sp.getString(PREF_MULTI_RTSP_URI_1, "rtsp://127.0.0.1:8554/main");
        if (!TextUtils.isEmpty(uri)) {
            etUri.setText(uri);
        }

        ffListenerManager = FFListenerManager.addListener(MainApplication.applicationContext, ffListener);
        mediaHelper1 = new MediaHelper(MediaHelper.DECODE_MODE.FF_DIRECT_SURFACE_PATH, null, vv1, null, null, DECODE_CHANNEL1);
        mediaHelper2 = new MediaHelper(MediaHelper.DECODE_MODE.FF_DIRECT_SURFACE_PATH, null, vv2, null, null, DECODE_CHANNEL2);
        mediaHelper3 = new MediaHelper(MediaHelper.DECODE_MODE.FF_DIRECT_SURFACE_PATH, null, vv3, null, null, DECODE_CHANNEL3);
        mediaHelper4 = new MediaHelper(MediaHelper.DECODE_MODE.FF_DIRECT_SURFACE_PATH, null, vv4, null, null, DECODE_CHANNEL4);

        tvOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isPlaying = false;
                MediaHelper mediaHelper = mediaHelper1;
                switch (selectedChannel) {
                    case DECODE_CHANNEL1:
                        isPlaying = isPlaying1;
                        mediaHelper = mediaHelper1;
                        break;
                    case DECODE_CHANNEL2:
                        isPlaying = isPlaying2;
                        mediaHelper = mediaHelper2;
                        break;
                    case DECODE_CHANNEL3:
                        isPlaying = isPlaying3;
                        mediaHelper = mediaHelper3;
                        break;
                    case DECODE_CHANNEL4:
                        isPlaying = isPlaying4;
                        mediaHelper = mediaHelper4;
                        break;
                }

                if (isPlaying) {
                    setIsPlaying(false);
                    tvOperate.setText(R.string.play);
                    mediaHelper.stopPlayFile();
                } else {
                    String uri = etUri.getText().toString().trim();
                    if (!TextUtils.isEmpty(uri) && uri.startsWith("rtsp://")) {
                        mediaHelper.playFile(uri);
                        setIsPlaying(true);
                        tvOperate.setText(R.string.stop);
                        tvDecodeMode.setText(R.string.decode_mode_hw);


                        String prefKey = PREF_MULTI_RTSP_URI_1;
                        switch (selectedChannel) {
                            case DECODE_CHANNEL1:
                                prefKey = PREF_MULTI_RTSP_URI_1;
                                break;
                            case DECODE_CHANNEL2:
                                prefKey = PREF_MULTI_RTSP_URI_2;
                                break;
                            case DECODE_CHANNEL3:
                                prefKey = PREF_MULTI_RTSP_URI_3;
                                break;
                            case DECODE_CHANNEL4:
                                prefKey = PREF_MULTI_RTSP_URI_4;
                                break;
                        }
                        String packageName = MainApplication.applicationContext.getPackageName();
                        SharedPreferences sp = MainApplication.applicationContext.getSharedPreferences(packageName + "_preferences", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(prefKey, uri);
                        editor.apply();
                    } else {
                        Toast.makeText(RtspMultiActivity.this, R.string.url_error, Toast.LENGTH_SHORT).show();
                    }
                }

                tvOperate.setEnabled(false);
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (tvOperate != null) {
                            tvOperate.setEnabled(true);
                        }
                    }
                }, 1000);
            }
        });

        setTcpListener();
    }

    private void setTcpListener() {
        cbTcp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                switch (selectedChannel) {
                    case DECODE_CHANNEL1:
                        isTcp1 = isChecked;
                        mediaHelper1.setRtspTcp(isChecked);
                        break;
                    case DECODE_CHANNEL2:
                        isTcp2 = isChecked;
                        mediaHelper2.setRtspTcp(isChecked);
                        break;
                    case DECODE_CHANNEL3:
                        isTcp3 = isChecked;
                        mediaHelper3.setRtspTcp(isChecked);
                        break;
                    case DECODE_CHANNEL4:
                        isTcp4 = isChecked;
                        mediaHelper4.setRtspTcp(isChecked);
                        break;
                }
            }
        });
    }

    private void setIsPlaying(boolean isPlaying) {
        switch (selectedChannel) {
            case DECODE_CHANNEL1:
                isPlaying1 = isPlaying;
                break;
            case DECODE_CHANNEL2:
                isPlaying2 = isPlaying;
                break;
            case DECODE_CHANNEL3:
                isPlaying3 = isPlaying;
                break;
            case DECODE_CHANNEL4:
                isPlaying4 = isPlaying;
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Hide both the navigation bar and the status bar.
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cbTcp.setOnCheckedChangeListener(null);
        isPlaying1 = false;
        isPlaying2 = false;
        isPlaying3 = false;
        isPlaying4 = false;
        tvOperate.setText(R.string.play);
        ffListenerManager.removeListener();
        mediaHelper1.destroy();
        mediaHelper2.destroy();
        mediaHelper3.destroy();
        mediaHelper4.destroy();
    }

    private FFListener ffListener = new FFListener() {
        @Override
        public void onMediaFormat(String format, int width, int height, long bitRate, int handler) {
            SurfaceView surface = null;
            FrameLayout fl = null;
            MediaHelper mediaHelper = null;
            switch (handler) {
                case DECODE_CHANNEL1:
                    surface = vv1;
                    fl = fl1;
                    mediaHelper = mediaHelper1;
                    break;
                case DECODE_CHANNEL2:
                    surface = vv2;
                    fl = fl2;
                    mediaHelper = mediaHelper2;
                    break;
                case DECODE_CHANNEL3:
                    surface = vv3;
                    fl = fl3;
                    mediaHelper = mediaHelper3;
                    break;
                case DECODE_CHANNEL4:
                    surface = vv4;
                    fl = fl4;
                    mediaHelper = mediaHelper4;
                    break;
            }
            ViewGroup.LayoutParams layoutParams = surface.getLayoutParams();
            float aspectRatio = ((float) fl.getWidth()) / fl.getHeight();
            float aspectRatioNew = ((float) width) / height;
            if (aspectRatio > aspectRatioNew) {
                float realWidth = ((float) (fl.getHeight())) * aspectRatioNew;
                layoutParams.width = (int) realWidth;
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            } else {
                float realHeight = ((float) (fl.getWidth())) / aspectRatioNew;
                layoutParams.height = (int) realHeight;
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            }
            surface.requestLayout();
            mediaHelper.updateVideoSize(width, height);
        }

        @Override
        public void onDowngradeToSwDecode(int handler) {
            switch (handler) {
                case DECODE_CHANNEL1:
                    isHw1 = false;
                    break;
                case DECODE_CHANNEL2:
                    isHw2 = false;
                    break;
                case DECODE_CHANNEL3:
                    isHw3 = false;
                    break;
                case DECODE_CHANNEL4:
                    isHw4 = false;
                    break;
            }
            if (selectedChannel == handler) {
                tvDecodeMode.setText(R.string.decode_mode_sw);
            }
        }
    };
}