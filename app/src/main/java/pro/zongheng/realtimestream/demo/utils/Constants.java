package pro.zongheng.realtimestream.demo.utils;

/**
 * @Description:
 * @Author: zongheng
 * @Date: 2022/3/7 5:19 下午
 */
public class Constants {
    public static final String PREF_IS_HW_DECODE = "PREF_IS_HW_DECODE";
    public static final String PREF_DECODE_MODE = "PREF_DECODE_MODE";
    public static final String PREF_DECODE_MODE_RTSP = "PREF_DECODE_MODE_RTSP";

    public static final String PREF_RTSP_URI = "RTSP_URI";

    public static final int DECODE_MODE_FF_SURFACE = 0;
    public static final int DECODE_MODE_FF_GL_SURFACE = 1;
    public static final int DECODE_MODE_MEDIACODEC_SURFACE = 2;
    public static final int DECODE_MODE_MEDIACODEC_TEXTURE = 3;

    public static final int DECODE_MODE_RTSP_FF_DIRECT = 0;
    public static final int DECODE_MODE_RTSP_FF_GL = 1;
    public static final int DECODE_MODE_RTSP_FF_SW_SWS = 2;
    public static final int DECODE_MODE_RTSP_FF_JNI_MEDIACODEC = 3;
}
