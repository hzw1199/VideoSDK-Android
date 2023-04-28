package pro.zongheng.realtimestream.demo;


import static pro.zongheng.realtimestream.demo.utils.ImageUtils.saveBitmap;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.PixelCopy;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.wuadam.fflibrary.FFJNI;
import com.wuadam.fflibrary.listeners.FFListener;
import com.wuadam.fflibrary.listeners.FFListenerManager;
import com.wuadam.medialibrary.BitRateHelper;
import com.wuadam.medialibrary.H264Extractor;
import com.wuadam.medialibrary.H264Saver;
import com.wuadam.medialibrary.MediaHelper;
import com.wuadam.medialibrary.MediaListener;
import com.wuadam.medialibrary.MuxerUtil;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import pro.zongheng.realtimestream.demo.utils.Constants;
import pro.zongheng.realtimestream.demo.utils.ImageUtils;
import pro.zongheng.realtimestream.demo.utils.PermissionHelper;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private MediaHelper mediaHelper;
    private MuxerUtil muxerUtil;
    private FFListenerManager ffListenerManager;
    private BitRateHelper bitRateHelperVideo;
    private H264Saver h264Saver;
    private VideoMock videoMock;
    private PermissionHelper permissionHelper;

    private ViewGroup rootView;
    private SurfaceView surface;
    private TextureView texture;
    private TextView tvBitrateVideo;
    private TextView widgetMap;
    private Button btnShot;
    private Button btnStartRecord;
    private Button btnStopRecord;
    private Button btnHwDecoder;
    private SwitchCompat swMockVideo;
    private Spinner spDecodeMode;
    private SwitchCompat swHwDecode;
    private Button btnRtsp;
    private Button btnRtspMulti;

    private boolean isMapMini = true;

    private int decodeMode;

    private int mapWidgetHeight;
    private int mapWidgetWidth;
    private int mapWidgetMarginRight;
    private int mapWidgetMarginBottom;

    private int deviceWidth;
    private int deviceHeight;

    private int videoWidgetWidth;
    private int videoWidgetHeight;
    private MediaFormat mMediaFormat;
    private boolean isMediaCodecPlaying = false;
    private boolean isMediaCodecRecordFoundIFrame = false;

    /**
     *  support 5 channels, from 1 to 5
     */
    private final int DECODE_CHANNEL = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mapWidgetHeight = (int) getResources().getDimension(R.dimen.mini_map_height);
        mapWidgetWidth = (int) getResources().getDimension(R.dimen.mini_map_width);
        mapWidgetMarginRight = (int) getResources().getDimension(R.dimen.mini_map_margin_right);
        mapWidgetMarginBottom = (int) getResources().getDimension(R.dimen.mini_map_margin_bottom);

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        final Display display = windowManager.getDefaultDisplay();
        Point outPoint = new Point();
        display.getRealSize(outPoint);
        deviceHeight = outPoint.y;
        deviceWidth = outPoint.x;

        rootView = findViewById(R.id.root_view);
        surface = findViewById(R.id.surface);
        texture = findViewById(R.id.texture);
        tvBitrateVideo = findViewById(R.id.tv_bitrate_video);
        widgetMap = findViewById(R.id.widget_map);
        btnShot = findViewById(R.id.btn_shot);
        btnStartRecord = findViewById(R.id.btn_start_record);
        btnStopRecord = findViewById(R.id.btn_stop_record);
        btnHwDecoder = findViewById(R.id.btn_hw_decoder);
        swMockVideo = findViewById(R.id.sw_mock_video);
        spDecodeMode = findViewById(R.id.sp_decode_mode);
        swHwDecode = findViewById(R.id.sw_hw_decode);
        btnRtsp = findViewById(R.id.btn_rtsp);
        btnRtspMulti = findViewById(R.id.btn_rtsp_multi);

        // decode mode start
        // 0. FFmpeg with hw decoder, direct render in SurfaceView
        // 1. FFmpeg with hw/sw decoder, yuv2rgb with GL, render in SurfaceView
        // 2. MediaCodec with hw decoder, direct render in SurfaceView
        // 3. MediaCodec with hw decoder, direct render in TextureView
        String[] decodeModeArr = new String[]{"FFmpeg-SurfaceView", "FFmpeg-GL-SurfaceView", "MediaCodec-SurfaceView", "MediaCodec-TextureView"};
        ArrayAdapter<String> decodeModeAdapter = new ArrayAdapter<String>(this, R.layout.item_select, decodeModeArr);
        decodeModeAdapter.setDropDownViewResource(R.layout.item_dropdown);
        spDecodeMode.setAdapter(decodeModeAdapter);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        decodeMode = sp.getInt(Constants.PREF_DECODE_MODE, Constants.DECODE_MODE_FF_SURFACE);
        spDecodeMode.setSelection(decodeMode);
        if (decodeMode != Constants.DECODE_MODE_FF_GL_SURFACE && decodeMode != Constants.DECODE_MODE_FF_SURFACE) {
            btnHwDecoder.setVisibility(View.GONE);
            swHwDecode.setVisibility(View.GONE);
        }
        if (decodeMode == Constants.DECODE_MODE_MEDIACODEC_TEXTURE) {
            surface.setVisibility(View.GONE);
        } else {
            texture.setVisibility(View.GONE);
        }

        spDecodeMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != decodeMode) {
                    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt(Constants.PREF_DECODE_MODE, position);
                    editor.apply();

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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

        switch (decodeMode) {
            case Constants.DECODE_MODE_FF_SURFACE:
                mediaHelper = new MediaHelper(MediaHelper.DECODE_MODE.FF_DIRECT_SURFACE, null, surface, null, null, DECODE_CHANNEL);
                break;
            case Constants.DECODE_MODE_FF_GL_SURFACE:
                mediaHelper = new MediaHelper(MediaHelper.DECODE_MODE.FF_GL_SURFACE, null, surface, null, null, DECODE_CHANNEL);
                break;
            case Constants.DECODE_MODE_MEDIACODEC_SURFACE:
                mediaHelper = new MediaHelper(MediaHelper.DECODE_MODE.MEDIACODEC_SURFACE, null, surface, null, null, DECODE_CHANNEL, 1920, 1080, 30);
                // Other video profile
//                mediaHelper = new MediaHelper(MediaHelper.DECODE_MODE.MEDIACODEC_SURFACE, null, surface, null, null, DECODE_CHANNEL, 240, 320, 25);
                break;
            case Constants.DECODE_MODE_MEDIACODEC_TEXTURE:
                mediaHelper = new MediaHelper(MediaHelper.DECODE_MODE.MEDIACODEC_TEXTURE, texture, null, null, null, DECODE_CHANNEL, 1920, 1080, 30);
                // Other video profile
//                mediaHelper = new MediaHelper(MediaHelper.DECODE_MODE.MEDIACODEC_TEXTURE, texture, null, null, null, DECODE_CHANNEL, 240, 320, 25);
                break;
        }
        mediaHelper.setListener(mediaListener);

        ffListenerManager = FFListenerManager.addListener(this, ffListener);

        bitRateHelperVideo = new BitRateHelper();
        bitRateHelperVideo.setListener(bitRateListenerVideo);

        String path = MainApplication.applicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath() + "/record";
        h264Saver = new H264Saver(path);

        permissionHelper = new PermissionHelper(this);

        // Get whether hardware decoding now (default value is true)
        swHwDecode.setChecked(FFJNI.isHwDecode());
        swHwDecode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(Constants.PREF_IS_HW_DECODE, isChecked);
                editor.apply();

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.restart_to_work).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        System.exit(0);
                    }
                }).setCancelable(false).show();
            }
        });

        swMockVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    videoMock = new VideoMock(mediaHelper);
                    videoMock.start();
                } else {
                    if (videoMock != null) {
                        videoMock.destroy();
                        videoMock = null;
                    }
                }
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

        permissionHelper.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ffListenerManager.removeListener();
        FFJNI.stop(DECODE_CHANNEL);
        h264Saver.stop();
        if (videoMock != null) {
            videoMock.destroy();
            videoMock = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] paramArrayOfInt) {
        super.onRequestPermissionsResult(requestCode, permissions, paramArrayOfInt);
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, paramArrayOfInt);
    }

    public void onClick(View view) {
        if ((view == surface || view == texture) && !isMapMini) {
            // 地图缩小，视频变大
            //reorder widgets
            if (decodeMode == Constants.DECODE_MODE_MEDIACODEC_TEXTURE) {
                texture.setTranslationZ(1);
            } else {
                surface.setTranslationZ(1);
            }

            //resize widgets
            resizeMap(false);
            resizeVideo(true);
            //disable user login widget on map
//            widgetMap.getUserAccountLoginWidget().setVisibility(View.GONE);
            isMapMini = true;
        } else if (view == widgetMap && isMapMini) {
            // 地图变大，视频缩小
            //reorder widgets
            if (decodeMode == Constants.DECODE_MODE_MEDIACODEC_TEXTURE) {
                texture.setTranslationZ(4);
            } else {
                surface.setTranslationZ(4);
            }

            //resize widgets
            resizeMap(true);
            resizeVideo(false);
            //enable user login widget on map
//            widgetMap.getUserAccountLoginWidget().setVisibility(View.VISIBLE);
            isMapMini = false;
        }
        else if (view == btnShot) {
            switch (mediaHelper.getDecodeMode()) {
                case FF_GL_SURFACE: {
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
                case FF_DIRECT_SURFACE:
                case MEDIACODEC_SURFACE: {
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
                case MEDIACODEC_TEXTURE: {
                    Bitmap bitmap = texture.getBitmap();
                    if (bitmap != null) {
                        Toast.makeText(MainApplication.applicationContext, R.string.take_photo_success, Toast.LENGTH_SHORT)
                                .show();
                        saveBitmap(bitmap);
                    } else {
                        Toast.makeText(MainApplication.applicationContext, R.string.take_photo_fail, Toast.LENGTH_SHORT)
                                .show();
                    }
                }
                break;
            }
        } else if (view == btnStartRecord) {
            String path = MainApplication.applicationContext.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath() + "/record";
            switch (mediaHelper.getDecodeMode()) {
                case FF_GL_SURFACE:
                case FF_DIRECT_SURFACE:
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
                case MEDIACODEC_SURFACE:
                case MEDIACODEC_TEXTURE: {
                    if (muxerUtil != null) {
                        Toast.makeText(MainApplication.applicationContext, R.string.record_ing, Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    if (isMediaCodecPlaying && mMediaFormat != null) {
                        muxerUtil = new MuxerUtil(path);
                        muxerUtil.addVideoTrack(mMediaFormat);
                        muxerUtil.start();
                    } else {
                        Toast.makeText(MainApplication.applicationContext, R.string.record_not_playing, Toast.LENGTH_SHORT)
                                .show();
                    }
                }
                break;
            }
        } else if (view == btnStopRecord) {
            switch (mediaHelper.getDecodeMode()) {
                case FF_GL_SURFACE:
                case FF_DIRECT_SURFACE:
                    // Retrieve the record result via FFListener.onRecordVideo
                    FFJNI.stopRecord(DECODE_CHANNEL);
                    break;
                case MEDIACODEC_SURFACE:
                case MEDIACODEC_TEXTURE: {
                    if (muxerUtil != null) {
                        muxerUtil.stop();
                        final String path = muxerUtil.getFilePath();
                        muxerUtil = null;
                        Toast.makeText(MainActivity.this, R.string.record_success, Toast.LENGTH_SHORT).show();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                ImageUtils.save2Album(path, "rts-library", System.currentTimeMillis() + ".mp4", true);
                            }
                        }).start();
                    }
                }
                break;
            }
        } else if (view == btnHwDecoder) {
            String info = FFJNI.avcodecinfo();
            Toast.makeText(MainActivity.this, info, Toast.LENGTH_LONG).show();
            Log.d("codec info", info);
        } else if (view == btnRtsp) {
            Intent intent = new Intent(this, RtspSingleActivity.class);
            startActivity(intent);
        } else if (view == btnRtspMulti) {
            Intent intent = new Intent(this, RtspMultiActivity.class);
            startActivity(intent);
        }
    }

    private void setVideoLayout(int videoWidth, int videoHeight) {
        float aspectRatio = ((float) rootView.getWidth()) / rootView.getHeight();
        float aspectRatioNew = ((float) videoWidth) / videoHeight;
        View viewToChange = mediaHelper.getDecodeMode() == MediaHelper.DECODE_MODE.MEDIACODEC_TEXTURE? texture: surface;
        if (aspectRatio > aspectRatioNew) {
            float realWidth = ((float) (rootView.getHeight())) * aspectRatioNew;
            if (isMapMini) {
                ViewGroup.LayoutParams layoutParams = viewToChange.getLayoutParams();
                layoutParams.width = (int) realWidth;
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                viewToChange.requestLayout();
            }

            videoWidgetWidth = (int) realWidth;
            videoWidgetHeight = rootView.getHeight();
        } else {
            float realHeight = ((float) (rootView.getWidth())) / aspectRatioNew;
            if (isMapMini) {
                ViewGroup.LayoutParams layoutParams = viewToChange.getLayoutParams();
                layoutParams.height = (int) realHeight;
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                viewToChange.requestLayout();
            }

            videoWidgetWidth = rootView.getWidth();
            videoWidgetHeight = (int) realHeight;
        }
    }

    // For MEDIACODEC_SURFACE and MEDIACODEC_TEXTURE, to record H264 stream
    private MediaListener mediaListener = new MediaListener() {
        @Override
        public void onConfigure(MediaFormat mediaFormat) {
            mMediaFormat = mediaFormat;

            // For MEDIACODEC_SURFACE and MEDIACODEC_TEXTURE, to record H264 stream, you need to set SPS and PPS parameters
            // So if you need to record, we recommend using FF_DIRECT_SURFACE or FF_GL_SURFACE
            // TODO Use your own video parameters
            byte[] sps = {
                    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01, (byte)0x67,
                    (byte)0x64, (byte)0x00, (byte)0x1F, (byte)0xAC, (byte)0xB4,
                    (byte)0x02, (byte)0x80, (byte)0x2D, (byte)0xD8, (byte)0x08,
                    (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x03, (byte)0x00,
                    (byte)0x80, (byte)0x00, (byte)0x00, (byte)0x1E, (byte)0x07,
                    (byte)0x8C, (byte)0x19, (byte)0x50};
            byte[] pps = {(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x01,
                    (byte)0x68, (byte)0xEF, (byte)0x32, (byte)0xC8, (byte)0xB0};
            mMediaFormat.setByteBuffer("csd-0", ByteBuffer.wrap(sps));
            mMediaFormat.setByteBuffer("csd-1", ByteBuffer.wrap(pps));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setVideoLayout(mMediaFormat.getInteger(MediaFormat.KEY_WIDTH), mMediaFormat.getInteger(MediaFormat.KEY_HEIGHT));
                }
            });
        }

        @Override
        public void onStart() {
            isMediaCodecPlaying = true;
        }

        @Override
        public void onFrameData(H264Extractor.SyncFrame syncFrame) {
            if (muxerUtil != null && muxerUtil.isStart()) {
                if (!isMediaCodecRecordFoundIFrame) {
                    if (!syncFrame.isIframe) {
                        // First frame must be I frame
                        return;
                    } else {
                        isMediaCodecRecordFoundIFrame = true;
                    }
                }
                muxerUtil.writeVideoSampleData(syncFrame.byteBuffer, syncFrame.byteBuffer.capacity(), 30);
            }
        }

        @Override
        public void onBitRate(float bitRate) {

        }

        @Override
        public void onRelease() {
            isMediaCodecPlaying = false;
            isMediaCodecRecordFoundIFrame = false;
        }
    };

    private FFListener ffListener = new FFListener() {
        public void onMediaFormat(String format, int width, int height, long bitRate, int handler) {
            if (handler == DECODE_CHANNEL) {
                setVideoLayout(width, height);
                mediaHelper.updateVideoSize(width, height);
            }
        }
    };

    private BitRateHelper.OnBitRateListener bitRateListenerVideo = new BitRateHelper.OnBitRateListener() {
        @Override
        public void onBitRate(long bitRate, String readable) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvBitrateVideo.setText(readable);
                }
            });
        }
    };

    private void resizeMap(boolean isEnlarge) {
        if (isEnlarge) {
            // enlarge
            ResizeAnimation enlargeAnimation = new ResizeAnimation(true, widgetMap, mapWidgetWidth, mapWidgetHeight, deviceWidth, deviceHeight, 0, 0);
            widgetMap.startAnimation(enlargeAnimation);
        } else {
            // shrink
            ResizeAnimation shrinkAnimation = new ResizeAnimation(false, widgetMap, deviceWidth, deviceHeight, mapWidgetWidth, mapWidgetHeight, mapWidgetMarginRight, mapWidgetMarginBottom);
            widgetMap.startAnimation(shrinkAnimation);
        }
    }

    private void resizeVideo(boolean isEnlarge) {
        if (decodeMode == Constants.DECODE_MODE_MEDIACODEC_TEXTURE) {
            if (isEnlarge) {
                // enlarge
                ResizeAnimation enlargeAnimation = new ResizeAnimation(true, texture, mapWidgetWidth, mapWidgetHeight, videoWidgetWidth, videoWidgetHeight, 0, 0);
                texture.startAnimation(enlargeAnimation);
            } else {
                // shrink
                ResizeAnimation shrinkAnimation = new ResizeAnimation(false, texture, videoWidgetWidth, videoWidgetHeight, mapWidgetWidth, mapWidgetHeight, mapWidgetMarginRight, mapWidgetMarginBottom);
                texture.startAnimation(shrinkAnimation);
            }
        } else {
            if (isEnlarge) {
                // enlarge
                ResizeAnimation enlargeAnimation = new ResizeAnimation(true, surface, mapWidgetWidth, mapWidgetHeight, videoWidgetWidth, videoWidgetHeight, 0, 0);
                surface.startAnimation(enlargeAnimation);
            } else {
                // shrink
                ResizeAnimation shrinkAnimation = new ResizeAnimation(false, surface, videoWidgetWidth, videoWidgetHeight, mapWidgetWidth, mapWidgetHeight, mapWidgetMarginRight, mapWidgetMarginBottom);
                surface.startAnimation(shrinkAnimation);
            }
        }
    }

    /**
     * Animation to change the size of a view.
     */
    private static class ResizeAnimation extends Animation {

        private static final int DURATION = 300;

        private boolean isEnlarge;
        private View view;
        private int toHeight;
        private int fromHeight;
        private int toWidth;
        private int fromWidth;
        private int marginRight;
        private int marginBottom;

        private ResizeAnimation(boolean isEnlarge, View v, int fromWidth, int fromHeight, int toWidth, int toHeight, int marginRight, int marginBottom) {
            this.isEnlarge = isEnlarge;
            this.toHeight = toHeight;
            this.toWidth = toWidth;
            this.fromHeight = fromHeight;
            this.fromWidth = fromWidth;
            view = v;
            this.marginRight = marginRight;
            this.marginBottom = marginBottom;
            setDuration(DURATION);
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float height = (toHeight - fromHeight) * interpolatedTime + fromHeight;
            float width = (toWidth - fromWidth) * interpolatedTime + fromWidth;
            ConstraintLayout.LayoutParams p = (ConstraintLayout.LayoutParams) view.getLayoutParams();
            p.height = (int) height;
            p.width = (int) width;
            p.rightMargin = marginRight;
            p.bottomMargin = marginBottom;

            if (this.isEnlarge) {
                if (interpolatedTime == 1) {
                    ConstraintSet set = new ConstraintSet();
                    set.clone((ConstraintLayout) view.getParent());
                    set.connect(view.getId(), ConstraintSet.TOP, R.id.root_view, ConstraintSet.TOP, 0);
                    set.connect(view.getId(), ConstraintSet.START, R.id.root_view, ConstraintSet.START, 0);
                    set.applyTo((ConstraintLayout) view.getParent());
                }
            } else {
                ConstraintSet set = new ConstraintSet();
                set.clone((ConstraintLayout) view.getParent());
                set.clear(view.getId(), ConstraintSet.TOP);
                set.clear(view.getId(), ConstraintSet.START);
                set.applyTo((ConstraintLayout) view.getParent());
            }

            view.requestLayout();
        }
    }
}