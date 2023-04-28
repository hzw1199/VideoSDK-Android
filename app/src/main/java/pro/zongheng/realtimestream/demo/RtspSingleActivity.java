package pro.zongheng.realtimestream.demo;

import static pro.zongheng.realtimestream.demo.utils.Constants.PREF_RTSP_URI;
import static pro.zongheng.realtimestream.demo.utils.ImageUtils.saveBitmap;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.wuadam.fflibrary.FFJNI;
import com.wuadam.fflibrary.listeners.FFListener;
import com.wuadam.fflibrary.listeners.FFListenerManager;
import com.wuadam.medialibrary.MediaHelper;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import pro.zongheng.realtimestream.demo.utils.Constants;

public class RtspSingleActivity extends AppCompatActivity {

    private EditText etUri;
    private CheckBox cbTcp;
    private TextView tvOperate;
    private TextView tvDecodeMode;
    private FrameLayout fl;
    private SurfaceView surface;

    private Button btnShot;
    private Button btnStartRecord;
    private Button btnStopRecord;
    private Spinner spDecodeMode;

    private MediaHelper mediaHelper;
    private boolean isPlaying = false;
    private int decodeMode;
    private FFListenerManager ffListenerManager;
    /**
     *  support 5 channels, from 1 to 5
     */
    private final int DECODE_CHANNEL = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtsp_single);

        etUri = findViewById(R.id.et_uri);
        cbTcp = findViewById(R.id.cb_tcp);
        tvOperate = findViewById(R.id.tv_operate);
        tvDecodeMode = findViewById(R.id.tv_decode_mode);
        fl = findViewById(R.id.fl);
        surface = findViewById(R.id.surface);

        btnShot = findViewById(R.id.btn_shot);
        btnStartRecord = findViewById(R.id.btn_start_record);
        btnStopRecord = findViewById(R.id.btn_stop_record);
        spDecodeMode = findViewById(R.id.sp_decode_mode);

        String packageName = MainApplication.applicationContext.getPackageName();
        SharedPreferences sp = MainApplication.applicationContext.getSharedPreferences(packageName + "_preferences", MODE_PRIVATE);
        String uri = sp.getString(PREF_RTSP_URI, "rtsp://127.0.0.1:8554/main");
        if (!TextUtils.isEmpty(uri)) {
            etUri.setText(uri);
        }

        // decode mode start
        // 0. FFmpeg with hw decoder, direct render in SurfaceView
        // 1. FFmpeg with hw/sw decoder, yuv2rgb with GL, render in SurfaceView
        // 2. FFmpeg with sw decoder, yuv2rgb with sws_scale, render in SurfaceView
        // 3. Find frames with FFmpeg, call MediaCodec by JNI to decode, direct render in SurfaceView
        String[] decodeModeArr = new String[]{"FFmpeg-Direct", "FFmpeg-GL", "FFmpeg-SwDecode-SwsScale", "FFmpeg-Jni-MediaCodec"};
        ArrayAdapter<String> decodeModeAdapter = new ArrayAdapter<String>(this, R.layout.item_select, decodeModeArr);
        decodeModeAdapter.setDropDownViewResource(R.layout.item_dropdown);
        spDecodeMode.setAdapter(decodeModeAdapter);

        decodeMode = sp.getInt(Constants.PREF_DECODE_MODE_RTSP, Constants.DECODE_MODE_RTSP_FF_DIRECT);
        spDecodeMode.setSelection(decodeMode);

        spDecodeMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != decodeMode) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(RtspSingleActivity.this);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt(Constants.PREF_DECODE_MODE_RTSP, position);
                    editor.apply();

                    AlertDialog.Builder builder = new AlertDialog.Builder(RtspSingleActivity.this);
                    builder.setMessage(R.string.restart_to_work).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    }).setCancelable(false).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        // decode mode ends

        ffListenerManager = FFListenerManager.addListener(MainApplication.applicationContext, ffListener);
        switch (decodeMode) {
            case Constants.DECODE_MODE_RTSP_FF_DIRECT:
                mediaHelper = new MediaHelper(MediaHelper.DECODE_MODE.FF_DIRECT_SURFACE_PATH, null, surface, null, null, DECODE_CHANNEL);
                break;
            case Constants.DECODE_MODE_RTSP_FF_GL:
                mediaHelper = new MediaHelper(MediaHelper.DECODE_MODE.FF_GL_SURFACE_PATH, null, surface, null, null, DECODE_CHANNEL);
                break;
            case Constants.DECODE_MODE_RTSP_FF_SW_SWS:
                mediaHelper = new MediaHelper(MediaHelper.DECODE_MODE.FF_SWS_SURFACE_PATH, null, surface, null, null, DECODE_CHANNEL);
                break;
            case Constants.DECODE_MODE_RTSP_FF_JNI_MEDIACODEC:
                mediaHelper = new MediaHelper(MediaHelper.DECODE_MODE.FF_NDK_MEDIACODEC_SURFACE_PATH, null, surface, null, null, DECODE_CHANNEL);
                break;
        }

        tvOperate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlaying) {
                    isPlaying = false;
                    tvOperate.setText(R.string.play);
                    mediaHelper.stopPlayFile();
                } else {
                    String uri = etUri.getText().toString().trim();
                    if (!TextUtils.isEmpty(uri) && uri.startsWith("rtsp://")) {
                        boolean res = mediaHelper.playFile(uri);
                        if (!res) {
                            Toast.makeText(RtspSingleActivity.this, R.string.surface_unavailable, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        isPlaying = true;
                        tvOperate.setText(R.string.stop);
                        tvDecodeMode.setText(R.string.decode_mode_hw);


                        String packageName = MainApplication.applicationContext.getPackageName();
                        SharedPreferences sp = MainApplication.applicationContext.getSharedPreferences(packageName + "_preferences", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString(PREF_RTSP_URI, uri);
                        editor.apply();
                    } else {
                        Toast.makeText(RtspSingleActivity.this, R.string.url_error, Toast.LENGTH_SHORT).show();
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

        cbTcp.setChecked(mediaHelper.isRtspTcp());
        cbTcp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mediaHelper.setRtspTcp(isChecked);
            }
        });
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
        isPlaying = false;
        tvOperate.setText(R.string.play);
        ffListenerManager.removeListener();
        mediaHelper.destroy();
    }

    public void onClick(View view) {
        if (view == btnShot) {
            switch (mediaHelper.getDecodeMode()) {
                case FF_SWS_SURFACE_PATH:
                case FF_GL_SURFACE_PATH: {
                    String path = MainApplication.applicationContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/shot";
                    File fileDir = new File(path);
                    fileDir.mkdirs();
                    File file = new File(fileDir, new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + ".jpg");
                    try {
                        file.createNewFile();
                        // Retrieve the result via FFListener.onShotFrame
                        FFJNI.shotFrame(file.getAbsolutePath(), DECODE_CHANNEL);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
                case FF_NDK_MEDIACODEC_SURFACE_PATH:
                case FF_DIRECT_SURFACE_PATH: {
                    // 直接渲染到Surface上的情况，无法从buffer中提取图像，只能从Surface上提取
                    Bitmap bitmap = Bitmap.createBitmap(mediaHelper.VIDEO_WIDTH, mediaHelper.VIDEO_HEIGHT, Bitmap.Config.ARGB_8888);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        PixelCopy.request(
                                surface, bitmap, new PixelCopy.OnPixelCopyFinishedListener() {
                                    @Override
                                    public void onPixelCopyFinished(int copyResult) {
                                        if (copyResult == PixelCopy.SUCCESS) {
                                            Toast.makeText(MainApplication.applicationContext, R.string.take_photo_success, Toast.LENGTH_SHORT)
                                                    .show();
                                            saveBitmap(bitmap);
                                        } else {
                                            Toast.makeText(MainApplication.applicationContext, R.string.take_photo_fail, Toast.LENGTH_SHORT)
                                                    .show();
                                        }
                                    }
                                }, new Handler(Looper.getMainLooper())
                        );
                    } else {
                        Toast.makeText(MainApplication.applicationContext, getString(R.string.take_photo_tip, mediaHelper.getDecodeMode().name()), Toast.LENGTH_SHORT)
                                .show();
                    }
                }
                break;
            }
        } else if (view == btnStartRecord) {
            String path = MainApplication.applicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath() + "/record";
            switch (mediaHelper.getDecodeMode()) {
                case FF_SWS_SURFACE_PATH:
                case FF_GL_SURFACE_PATH:
                case FF_DIRECT_SURFACE_PATH:
                    File fileDir = new File(path);
                    fileDir.mkdirs();
                    File file = new File(fileDir, new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + ".mp4");
                    try {
                        file.createNewFile();
                        FFJNI.startRecordVideo(file.getAbsolutePath(), DECODE_CHANNEL);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case FF_NDK_MEDIACODEC_SURFACE_PATH:
                    // Not supported
                    break;
            }
        } else if (view == btnStopRecord) {
            switch (mediaHelper.getDecodeMode()) {
                case FF_SWS_SURFACE_PATH:
                case FF_GL_SURFACE_PATH:
                case FF_DIRECT_SURFACE_PATH:
                    // Retrieve the record result via FFListener.onRecordVideo
                    FFJNI.stopRecord(DECODE_CHANNEL);
                    break;
                case FF_NDK_MEDIACODEC_SURFACE_PATH:
                    // Not supported
                    break;
            }
        }
    }
    private FFListener ffListener = new FFListener() {
        @Override
        public void onMediaFormat(String format, int width, int height, long bitRate, int handler) {
            if (handler == DECODE_CHANNEL) {
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
        }

        @Override
        public void onDowngradeToSwDecode(int handler) {
            tvDecodeMode.setText(R.string.decode_mode_sw);
        }
    };
}