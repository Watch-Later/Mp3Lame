package com.lame.mp3;

/**
 * created by wangguoqun at 2019-07-01
 */
public class AudioConstant {
    public static final boolean Debug = BuildConfig.DEBUG;

    public static final int NoExistIndex = -1;

    public static final int OneSecond = 1000;

    public static final int RecordSampleRate = 44100; // 采样率
    public static final int RecordByteNumber = 2; // 采样率
    public static final int RecordChannelNumber = 1;  // 与DEFAULT_CHANNEL_CONFIG相关，因为是mono单声，所以是1
    public static final int RecordDataNumberInOneSecond =
            RecordSampleRate * RecordByteNumber * RecordChannelNumber;
    // 与DEFAULT_CHANNEL_CONFIG相关，因为是mono单声，所以是1
    public static final int BehaviorSampleRate = 44100; // 采样率
    public static final int LameMp3Quality = 7; // Lame Default Settings
    public static final int LameBehaviorChannelNumber = RecordChannelNumber;
    // 与DEFAULT_CHANNEL_CONFIG相关，因为是mono单声，所以是1
    public static final int lameRecordBitRate = 64;
    // Encoded bit rate. MP3 file will be encoded with bit rate 64kbps
    public static final int LameBehaviorBitRate = 128;

    public static final int MusicCutEndOffset = 2;

    public static final int MaxDecodeProgress = 50;
    public static final int NormalMaxProgress = 100;

    public static final int RecordVolumeMaxRank = 9;

    public static final int ThreadPoolCount = 5;

    public static final float VoiceWeight = 1.8f;
    public static final float VoiceBackgroundWeight = 0.6f;

    public static final String MusicSuffix = ".mp3";
    public static final String LyricSuffix = ".lrc";
    public static final String RecordSuffix = ".mp3";
    public static final String PcmSuffix = ".pcm";

    public static final int ERROR_UNKNOW = 0;
    public static final int ERROR_FILE_NOT_FOUND = 1;
    public static final int ERROR_NO_PERMISSION = 2;
    public static final int ERROR_FILE_NOT_EFFECTIVE = 3;
    public static final int ERROR_SHORT_TIME = 4;
}