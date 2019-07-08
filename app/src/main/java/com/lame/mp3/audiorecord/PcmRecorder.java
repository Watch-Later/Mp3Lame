package com.lame.mp3.audiorecord;

import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;
import com.lame.mp3.AudioConstant;
import com.lame.mp3.utils.BaseRecord;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PcmRecorder extends BaseRecord {

    private AudioTrack mAudioTrack;
    private short[] buffer;
    private long totalTime = -1l, endTime;


    public PcmRecorder() {
        AUDIO_SUFFIX = AudioConstant.PcmSuffix;
    }

    public void start() {
        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    isRecording = true;
                    sendStartRecord();
                    OutputStream os = new FileOutputStream(mRecordFile);
                    BufferedOutputStream bos = new BufferedOutputStream(os);
                    DataOutputStream dos = new DataOutputStream(bos);
                    int audioRecordMinBufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLING_RATE, DEFAULT_CHANNEL_CONFIG,
                            DEFAULT_AUDIO_FORMAT.getAudioFormat());

                    AudioRecord audioRecord =
                            new AudioRecord(DEFAULT_AUDIO_SOURCE, DEFAULT_SAMPLING_RATE, DEFAULT_CHANNEL_CONFIG,
                                    DEFAULT_AUDIO_FORMAT.getAudioFormat(), audioRecordMinBufferSize);
                    buffer = new short[audioRecordMinBufferSize];
                    audioRecord.startRecording();
                    startTime = System.currentTimeMillis();
                    isRecording = true;
                    while (isRecording) {
                        int bufferedReadResult = audioRecord.read(buffer, 0, audioRecordMinBufferSize);
                        if(bufferedReadResult>0){
                            for (int i = 0; i < bufferedReadResult; i++) {
                                dos.writeShort(buffer[i]);
                            }
                            long v = 0;
                            for (int i = 0; i < buffer.length; i++) {
                                v += buffer[i] * buffer[i];
                            }
                            double mean = v / (double) bufferedReadResult;
                            double volume = 10 * Math.log10(mean);
                            //获取录音的声音分贝值
                            Log.i("tag", "分贝值:" + volume);
                            totalTime = System.currentTimeMillis() - startTime;
                            Log.i("time", "totalTime:" + totalTime);
                        }
                        sendTimeChangeMsg();
                    }
                    audioRecord.stop();
                    dos.close();
                    audioRecord.release();
                    endTime = System.currentTimeMillis();
                    sendCompleteRecord();
                    Log.i("time", "endTime:" + endTime);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override public boolean isRecordFileExist() {
        if (mRecordFile != null && mRecordFile.exists()) {
            return true;
        }
        return false;
    }

    @Override public File getRecordFile() {
        return mRecordFile;
    }


    public Boolean isRecording() {
        if (mRecordFile == null || !mRecordFile.exists()) {
            return null;
        }
        return isRecording;
    }

    @Override
    public void stopRecording() {
        isRecording = false;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void deleteRecordFile() {
        if (mRecordFile != null && mRecordFile.exists()) {
            mRecordFile.delete();
        }
        mRecordFile = null;
    }

    public void release() {
        stopRecording();
        if (mAudioTrack != null) {
            mAudioTrack.release();
            mAudioTrack = null;
        }
        mRecordFile = null;
    }
}
