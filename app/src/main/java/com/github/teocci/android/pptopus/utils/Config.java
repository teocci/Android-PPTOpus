package com.github.teocci.android.pptopus.utils;

/**
 * Created by teocci.
 *
 * @author teocci@yandex.com on 2017-Jun-19
 */
public class Config
{
    public static final String LOG_PREFIX = "[SmartAudio]";

    public static int DEFAULT_PORT = 8082;

    public static int REQUEST_ALL = 100;

    public static final String KEY_OPERATION_MODE = "operation_mode";
    public static final String KEY_STATION_NAME = "station_name";
    public static final String KEY_USED_NAMES = "station_used_names";
    public static final String KEY_STATION_NAME_LIST = "station_name_list";
    public static final String KEY_FEATURE_GUIDE = "feature_guide";
    public static final String KEY_MAIN_ACTIVITY = "main_activity";
    public final static String KEY_OPEN_SOURCE_LICENSE = "open_source_license";

    public static final String KEY_PREVIEW_SIZES = "preview_sizes";

    public final static String KEY_STREAM_AUDIO = "stream_audio";
    public final static String KEY_STREAM_VIDEO = "stream_video";

    public static final String KEY_AUDIO_BITRATE = "audio_bitrate";
    public static final String KEY_AUDIO_SAMPLE_RATE = "audio_sample_rate";

    public static final String KEY_VIDEO_BITRATE = "video_bitrate";
    public static final String KEY_VIDEO_FPS = "video_fps";
    public static final String KEY_VIDEO_RESOLUTION = "video_resolution";
    public static final String KEY_VIDEO_RESOLUTION_INDEX = "video_resolution_index";

    public static final String KEY_TCP_PROTOCOL = "tcp_protocol";
    public static final String KEY_STEREO_CHANNEL = "stereo_channel";

    public static final String KEY_AUTH_USER = "auth_user";
    public static final String KEY_AUTH_PASSWORD = "auth_password";

    public static final String SERVICE_TYPE = "_smartmixer._tcp.local.";
    public static final String SERVICE_CHANNEL_NAME = "Channel_00";
    public static final String SERVICE_APP_NAME = "SmartAudio";
    public static final String SERVICE_NAME_SEPARATOR = ":";

    public static final String CLIENT_MODE = "client_mode";

    public static final String COMMAND_SEPARATOR = ";";
    public static final String PARAMETER_SEPARATOR = ":";
    public static final String VALUE_SEPARATOR = ",";

    public static final String TAG_WAKELOCK = "NewSmartAudio::wakelock";
    public static final String TAG_WIFILOCK = "NewSmartAudio::WifiLock";

    public static final String NOTIFICATION_CHANNEL_RTSP = "notification_channel_rtsp";
    public static final int NOTIFICATION_ID_RTSP = 1100;

    /**
     * Port used by default.
     */
    public static final int DEFAULT_RTSP_PORT = 8086;
}
