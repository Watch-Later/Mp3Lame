package com.lame.mp3.utils;

import android.os.AsyncTask;
import android.util.Log;
import com.lame.mp3.AudioConstant;
import com.lame.mp3.audio.PcmToWav;
import com.lame.mp3.function.CommonFunction;
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
    private long sliceLength = -1l;
    private long startTime;
    private int VOLUME_THRESHOLD = 40;
    private String dataBase64;
    private boolean isSlicing = false;
    private boolean isComplete;
    private long duration;
    private List<Data> mDataList = new ArrayList<>();
    private String fileName;
    private File mSliceFile;
    private String dir;

    public static final int SLICE_MODE_ALL = 1;
    public static final int SLICE_MODE_PART = 2;
    public int mode = SLICE_MODE_PART;
    private BaseRecord mBaseRecord;
    private long byteRate;
    private boolean isAddHeader;

    public SliceVoiceManager(BaseRecord baseRecord, AudioConfig config) {
        mBaseRecord = baseRecord;
        this.sliceLength = config.sliceLength;
        if (sliceLength > 0l) {
            mode = SLICE_MODE_PART;
        } else {
            mode = SLICE_MODE_ALL;
        }
        if (config.format == AudioConstant.WavSuffix) {
            isAddHeader = true;
            byteRate = (BaseRecord.DEFAULT_AUDIO_FORMAT == PCMFormat.PCM_16BIT ? 16 : 8)
                    * BaseRecord.DEFAULT_SAMPLING_RATE * 1 / 8;
        } else {
            isAddHeader = false;
        }
        this.VOLUME_THRESHOLD = config.volumeThreshold;
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
            createSliceFile();
            if (mSliceFile.exists()) {
                saveSliceDataToFile();
            }
        } else if (mode == SLICE_MODE_PART) {
            //测试，将数据保存文件
            //createWavFile();
            new EncodeAsyncTask().execute();
        }
    }

    private void saveSliceDataToFile() {
        try {
            OutputStream os = new FileOutputStream(mSliceFile);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            DataOutputStream dos = new DataOutputStream(bos);
            if (mDataList.isEmpty()) {
                return;
            }
            int length = 0;
            for (int i = 0; i < mDataList.size(); i++) {
                length += mDataList.get(i).rawData.length;
            }
            if (isAddHeader) {
                byte[] header = PcmToWav.getWavHeader(length * 2, length * 2 + 36, BaseRecord.DEFAULT_SAMPLING_RATE,
                        1, byteRate);
                dos.write(header, 0, header.length);
            }
            for (int i = 0; i < mDataList.size(); i++) {
                Data data = mDataList.get(i);
                short[] buffer = data.getRawData();
                for (int j = 0; j < buffer.length; j++) {
                    dos.writeShort(Short.reverseBytes(buffer[j]));
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
        int length = 0;
        for (int i = 0; i < mDataList.size(); i++) {
            length += mDataList.get(i).rawData.length;
        }

        byte[] bytes = new byte[length * 2];
        int index = 0;
        for (int i = 0; i < mDataList.size(); i++) {
            short[] shorts = mDataList.get(i).getRawData();
            for (int j = 0; j < shorts.length; j++) {
                byte[] tmpByte = CommonFunction.GetBytes(shorts[j], false);
                bytes[j * 2 + index] = tmpByte[0];
                bytes[j * 2 + 1 + index] = tmpByte[1];
            }
            index += shorts.length * 2;
        }
        if (isAddHeader) {
            byte[] header = PcmToWav.getWavHeader(length * 2, length * 2 + 36, BaseRecord.DEFAULT_SAMPLING_RATE,
                    1, byteRate);

            byte[] result = combineBytes(header, bytes);
            //测试，将数据保存成文件
            //writeBytesToFile(result);
            dataBase64 = android.util.Base64.encodeToString(result, android.util.Base64.NO_WRAP);
        } else {
            dataBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP);
        }
        return dataBase64;
    }

    public byte[] combineBytes(byte[] data1, byte[] data2) {
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
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

    private void savePcmToWav() {
        try {
            OutputStream os = new FileOutputStream(mSliceFile);
            BufferedOutputStream bos = new BufferedOutputStream(os);
            DataOutputStream dos = new DataOutputStream(bos);
            if (mDataList.isEmpty()) {
                return;
            }
            int length = 0;
            for (int i = 0; i < mDataList.size(); i++) {
                length += mDataList.get(i).rawData.length;
            }
            byte[] header = PcmToWav.getWavHeader(length * 2, length * 2 + 36, BaseRecord.DEFAULT_SAMPLING_RATE,
                    1, byteRate);
            dos.write(header, 0, header.length);
            for (int i = 0; i < mDataList.size(); i++) {
                Data data = mDataList.get(i);
                short[] shorts = data.getRawData();
                for (int j = 0; j < shorts.length; j++) {
                    dos.writeShort(Short.reverseBytes(shorts[j]));
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

    private void writeBytesToFile(byte[] bytes) {
        try {
            FileOutputStream os = new FileOutputStream(mSliceFile);
            Log.i("tag", "fdafasdf:" + mSliceFile.getAbsolutePath());
            os.write(bytes, 0, bytes.length);
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getSliceFile() {
        return mSliceFile;
    }

    public static byte[] shortToByteArray(short s) {
        return new byte[] {(byte) (s & 0x00FF), (byte) ((s & 0xFF00) >> 8)};
    }

    public void reset() {
        isComplete = false;
        isSlicing = false;
        mDataList.clear();
        dataBase64 = null;
        mSliceFile = null;
    }

    private void createSliceFile() {
        fileName = dir + System.currentTimeMillis() + (isAddHeader ? AudioConstant.WavSuffix : AudioConstant.PcmSuffix);
        mSliceFile = new File(fileName);
        try {
            mSliceFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createWavFile() {
        fileName = dir + System.currentTimeMillis() + AudioConstant.WavSuffix;
        mSliceFile = new File(fileName);
        try {
            mSliceFile.createNewFile();
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
