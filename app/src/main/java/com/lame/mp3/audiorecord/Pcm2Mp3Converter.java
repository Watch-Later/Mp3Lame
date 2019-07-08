package com.lame.mp3.audiorecord;

import android.media.AudioRecord;
import android.os.Handler;
import android.os.Message;
import com.lame.mp3.utils.BaseRecord;
import com.lame.mp3.utils.DataEncodeThread;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * created by wangguoqun at 2019-06-29
 */
public class Pcm2Mp3Converter {

    public static final int MSG_END = 1;
    public static final int MSG_ERROR = 2;
    private Handler mHandler;

    public Pcm2Mp3Converter() {
        mHandler = new Handler() {
            @Override public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_END:
                        break;
                    case MSG_ERROR:
                        break;
                }
            }
        };
    }

    public void startConvert(String fileName) {
        File pcmFile = new File(fileName);
        int bufferSize = AudioRecord.getMinBufferSize(BaseRecord.DEFAULT_SAMPLING_RATE, BaseRecord.DEFAULT_CHANNEL_CONFIG,
                BaseRecord.DEFAULT_AUDIO_FORMAT.getAudioFormat());
        try {
            DataEncodeThread dataEncodeThread =new DataEncodeThread(pcmFile,bufferSize);
            dataEncodeThread.start();
        } catch (FileNotFoundException e) {
            sendMsgError();
            e.printStackTrace();
        }
    }

    private void sendMsgError() {
        if (mHandler == null) {
            return;
        }
        Message msg = Message.obtain();
        msg.what = MSG_ERROR;
        mHandler.sendMessage(msg);
    }
}
