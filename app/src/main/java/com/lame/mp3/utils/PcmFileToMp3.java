package com.lame.mp3.utils;

import android.util.Log;
import com.lame.mp3.function.CommonFunction;
import com.lame.mp3.listener.DataEncodeCallback;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * created by wangguoqun at 2019-07-13
 */
public class PcmFileToMp3 implements DataEncodeCallback {

    private String pcmFileName;

    private String mp3FileName;
    private DataEncodeThread mDataEncodeThread;

    public PcmFileToMp3(String pcmFileName, String mp3FileName) {
        this.pcmFileName = pcmFileName;
        this.mp3FileName = mp3FileName;
        LameUtil.init(BaseRecord.DEFAULT_SAMPLING_RATE, MP3Recorder.DEFAULT_LAME_IN_CHANNEL,
                MP3Recorder.DEFAULT_SAMPLING_RATE, MP3Recorder.DEFAULT_LAME_MP3_BIT_RATE,
                MP3Recorder.DEFAULT_LAME_MP3_QUALITY);
        try {
            mDataEncodeThread = new DataEncodeThread(this, new File(mp3FileName), 0);
            mDataEncodeThread.start();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void convertToMp3() {
        FileInputStream fis;
        try {
            File pcmFile = new File(pcmFileName);
            fis = new FileInputStream(pcmFile);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ByteArrayOutputStream bos = new ByteArrayOutputStream((int) pcmFile.length());
            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len = 0;
            while ((len = bis.read(buffer, 0, buf_size)) != -1) {
                bos.write(buffer, 0, len);
                short[] shorts = getShorts(buffer, true);
                mDataEncodeThread.addTask(shorts, len);
            }
            mDataEncodeThread.sendStopMessage();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private short[] getShorts(byte[] bytes, boolean bigEnding) {
        short[] shorts = new short[bytes.length / 2];
        for (int i = 0; i < shorts.length; i++) {
            short tmpShort = CommonFunction.GetShort(bytes[i * 2], bytes[i * 2 + 1], bigEnding);
            shorts[i] = (tmpShort);
        }
        return shorts;
    }

    @Override public void callback() {
        Log.i("tag", "转mp3完毕，" + mp3FileName);
    }
}
