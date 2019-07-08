package com.lame.mp3.utils;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import com.lame.mp3.AudioConstant;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * created by wangguoqun at 2019-06-19
 */
public class SliceVoiceManager {
    private long sliceLength;
    private long startTime;
    private int VOLUME_THRESHOLD = 40;
    private String dataBase64;
    private boolean isSlicing = false;
    private boolean isComplete;
    private long duration;
    private List<Data> mDataList = new ArrayList<>();
    private String fileName;
    private File pcmFile;
    private String dir;

    public static final int SLICE_MODE_ALL = 1;
    public static final int SLICE_MODE_PART = 2;
    public int mode = SLICE_MODE_PART;
    private BaseRecord mBaseRecord;

    public SliceVoiceManager(BaseRecord baseRecord, long sliceLength, int VOLUME_THRESHOLD) {
        this.mBaseRecord = baseRecord;
        this.sliceLength = sliceLength;
        this.VOLUME_THRESHOLD = VOLUME_THRESHOLD;
        mode = SLICE_MODE_PART;
    }

    public SliceVoiceManager(int mode, String fileName) {
        this.mode = mode;
        this.fileName = fileName;
    }

    public SliceVoiceManager(BaseRecord baseRecord, int mode) {
        this.mBaseRecord = baseRecord;
        this.mode = mode;
    }

    public boolean judgeToStart(int volume, long recordStartTime) {
        if (isComplete) {
            return false;
        }
        if (isSlicing) {
            if (mode == SLICE_MODE_ALL) {
                return true;
            } else if (mode == SLICE_MODE_PART) {
                duration = System.currentTimeMillis() - startTime;
                if (System.currentTimeMillis() - startTime > sliceLength) {
                    isSlicing = false;
                    complete();
                    return false;
                }
            }
            return true;
        }
        if (volume >= VOLUME_THRESHOLD) {
            long recordDuration = System.currentTimeMillis() - recordStartTime;
            if (recordDuration + sliceLength > MP3Recorder.MAX_DURATION) {
                Log.i("tag", "有效声音片段太短");
                return false;
            }
            isSlicing = true;
            startTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public void writeData(short[] buffer) {
        short[] dataBuffer = new short[buffer.length];
        for (int i = 0; i < buffer.length; i++) {
            dataBuffer[i] = buffer[i];
        }
        mDataList.add(new Data(dataBuffer));
    }

    private class Data {
        public Data(short[] rawData) {
            this.rawData = rawData;
        }

        private short[] rawData;

        public short[] getRawData() {
            return rawData;
        }
    }

    public void complete() {
        if (isComplete) {
            return;
        }
        isComplete = true;
        duration = System.currentTimeMillis() - startTime;
        if (duration < sliceLength) {
            Log.i("tag", "有效声音片段太短");
            if (mBaseRecord != null) {
                mBaseRecord.sendErrorMsg(AudioConstant.ERROR_FILE_NOT_EFFECTIVE);
            }
            return;
        }
        if (mode == SLICE_MODE_ALL) {
            createPcmFile();
            if (pcmFile.exists()) {
                savePcmToFile();
            }
        } else if (mode == SLICE_MODE_PART) {
            new EncodeAsyncTask().execute();
        }
    }

    private void savePcmToFile() {
        try {
            OutputStream os = new FileOutputStream(pcmFile);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            DataOutputStream dos = new DataOutputStream(bos);
            if (mDataList.isEmpty()) {
                return;
            }
            for (int i = 0; i < mDataList.size(); i++) {
                Data data = mDataList.get(i);
                short[] buffer = data.getRawData();
                for (int j = 0; j < buffer.length; j++) {
                    dos.writeShort(buffer[j]);
                }
            }
            dos.flush();
            dos.close();
            if (mBaseRecord != null) {
                mBaseRecord.sendCompleteEncode();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (mBaseRecord != null) {
                mBaseRecord.sendErrorMsg(AudioConstant.ERROR_FILE_NOT_FOUND);
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (mBaseRecord != null) {
                mBaseRecord.sendErrorMsg(AudioConstant.ERROR_UNKNOW);
            }
        }
    }

    public String encryptToBase64() {
        Log.i("tag", "开始时间" + System.currentTimeMillis());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mDataList.size(); i++) {
            short[] data = mDataList.get(i).getRawData();
            byte[] bytes = new byte[data.length * 2];
            for (int j = 0; j < data.length; j++) {
                bytes[j * 2] = shortToByteArray(data[i])[0];
                bytes[j * 2 + 1] = shortToByteArray(data[i])[1];
            }
            sb.append(Base64.encodeToString(bytes, Base64.DEFAULT));
        }
        dataBase64 = sb.toString();
        Log.i("tag", "结束时间" + dataBase64);
        return dataBase64;
    }

    class EncodeAsyncTask extends AsyncTask<Void, Integer, String> {

        @Override protected String doInBackground(Void... voids) {
            encryptToBase64();
            return "";
        }

        @Override protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (mBaseRecord != null) {
                mBaseRecord.sendCompleteEncode();
            }
        }
    }

    public File getPcmFile() {
        return pcmFile;
    }

    public static byte[] shortToByteArray(short s) {
        byte[] targets = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((s >>> offset) & 0xff);
        }
        return targets;
    }

    public void reset() {
        isComplete = false;
        isSlicing = false;
        mDataList.clear();
        dataBase64 = null;
        pcmFile = null;
    }

    private void createPcmFile() {
        fileName = dir + System.currentTimeMillis() + AudioConstant.PcmSuffix;
        pcmFile = new File(fileName);
        try {
            pcmFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getDataBase64() {
        return dataBase64;
    }

    public boolean isSlicing() {
        return isSlicing;
    }

    public long getDuration() {
        return duration;
    }
}
