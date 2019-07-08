package com.lame.mp3.utils;

import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.File;
import java.io.IOException;

/**
 * created by wangguoqun at 2019-06-29
 */
public abstract class BaseRecord {

    //16K采集率
    public static final int DEFAULT_SAMPLING_RATE = 16000;
    public static final int HIGH_SAMPLING_RATE = 44100;
    //格式
    public static final int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    /**
     * 下面是对此的封装
     */
    public static final PCMFormat DEFAULT_AUDIO_FORMAT = PCMFormat.PCM_16BIT;
    //=======================AudioRecord Default Settings=======================
    public static final int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;

    public static final int MSG_ERROR = 0;
    public static final int MSG_START_RECORD = 1;
    public static final int MSG_TIME_CHANGE = 2;
    public static final int MSG_COMPLETE_ENCODING = 3;
    public static final int MSG_COMPLETE_RECORD = 4;
    public static final int MSG_FILE_NOT_EXIST = 5;

    //加入录音时间限制
    public static final long MAX_DURATION = 60 * 1000;
    public static final long MIN_DURATION = 5 * 1000;
    protected static String AUDIO_SUFFIX;

    protected volatile boolean isRecording;

    protected long startTime;

    protected Handler mHander;

    protected File mRecordFile;

    public void setHander(Handler hander) {
        mHander = hander;
    }

    public abstract void stopRecording();

    public abstract void start();

    public abstract boolean isRecordFileExist();

    public abstract File getRecordFile();

    public Boolean isRecording() {
        if (mRecordFile == null || !mRecordFile.exists()) {
            return null;
        }
        return isRecording;
    }

    public abstract void release();

    public void setSliceVoiceManager(SliceVoiceManager sliceVoiceManager) {

    }

    public void start(String fileName) {
        mRecordFile = new File(fileName);
        if (!mRecordFile.exists()) {
            try {
                mRecordFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                if (null == mHander) return;
                Message msg = Message.obtain();
                msg.what = MSG_FILE_NOT_EXIST;
                mHander.sendMessage(msg);
                return;
            }
        }
        start();
    }

    protected void sendErrorMsg(int code) {
        if (null == mHander) return;
        Message msg = Message.obtain();
        msg.what = MSG_ERROR;
        msg.obj = code;
        mHander.sendMessage(msg);
        Log.i("tag", "发生错误: " + code);
    }

    protected void sendStartRecord() {
        if (null == mHander) return;
        Message msg = Message.obtain();
        msg.what = MSG_START_RECORD;
        mHander.sendMessage(msg);
    }

    protected void sendTimeChangeMsg() {
        if (null == mHander) return;
        long duration = System.currentTimeMillis() - startTime;
        Message msg = new Message();
        msg.what = MSG_TIME_CHANGE;
        msg.obj = duration;
        mHander.sendMessage(msg);
        if (duration >= MAX_DURATION) isRecording = false;
    }

    protected void sendCompleteRecord() {
        if (null == mHander) return;
        Message msg = Message.obtain();
        msg.what = MSG_COMPLETE_RECORD;
        mHander.sendMessage(msg);
    }

    protected void sendCompleteEncode() {
        if (null == mHander) return;
        Message msg = Message.obtain();
        msg.what = MSG_COMPLETE_ENCODING;
        mHander.sendMessage(msg);
    }

    public void deleteRecordFile() {
        if (mRecordFile != null && mRecordFile.exists()) {
            mRecordFile.delete();
        }
        mRecordFile = null;
    }
}
