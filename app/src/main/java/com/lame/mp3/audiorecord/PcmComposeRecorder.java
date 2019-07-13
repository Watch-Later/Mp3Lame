package com.lame.mp3.audiorecord;

/**
 * created by wangguoqun at 2019-07-01
 */

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import com.lame.mp3.AudioConstant;
import com.lame.mp3.utils.BaseRecord;
import com.lame.mp3.utils.PCMFormat;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 数据采集的方式同mp3，采集后的pcm数据只能用于合成，不能用于播放
 */
public class PcmComposeRecorder extends BaseRecord {

    private AudioTrack mAudioTrack;
    private long endTime;
    private int audioRecordBufferSize;
    private static final PCMFormat pcmFormat = PCMFormat.PCM_16BIT;
    private final static int sampleDuration = 100;
    private static final int FRAME_COUNT = 160;

    private short[] audioRecordBuffer;
    private AudioRecord audioRecord = null;
    private int realSampleDuration;
    private int realSampleNumberInOneDuration;

    public PcmComposeRecorder() {
        AUDIO_SUFFIX = ".pcm";
        initAudioRecord();
    }

    private void initAudioRecord() {
        int audioRecordMinBufferSize = AudioRecord
                .getMinBufferSize(AudioConstant.RecordSampleRate, AudioFormat.CHANNEL_IN_MONO,
                        pcmFormat.getAudioFormat());

        audioRecordBufferSize = AudioConstant.RecordSampleRate * pcmFormat.getBytesPerFrame() / (1000 / sampleDuration);

        if (audioRecordMinBufferSize > audioRecordBufferSize) {
            audioRecordBufferSize = audioRecordMinBufferSize;
        }

        /* Get number of samples. Calculate the buffer size
         * (round up to the factor of given frame size)
         * 使能被整除，方便下面的周期性通知
         * */
        int bytesPerFrame = pcmFormat.getBytesPerFrame();
        int frameSize = audioRecordBufferSize / bytesPerFrame;

        if (frameSize % FRAME_COUNT != 0) {
            frameSize += (FRAME_COUNT - frameSize % FRAME_COUNT);
            audioRecordBufferSize = frameSize * bytesPerFrame;
        }

        audioRecordBuffer = new short[audioRecordBufferSize];

        double sampleNumberInOneMicrosecond = (double) AudioConstant.RecordSampleRate / 1000;

        realSampleDuration = audioRecordBufferSize * 1000 /
                (AudioConstant.RecordSampleRate * pcmFormat.getBytesPerFrame());

        realSampleNumberInOneDuration = (int) (sampleNumberInOneMicrosecond * realSampleDuration);
    }

    public void start() {
        Thread thread = new Thread(() -> {
            try {
                isRecording = true;
                audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                        AudioConstant.RecordSampleRate, AudioFormat.CHANNEL_IN_MONO,
                        pcmFormat.getAudioFormat(), audioRecordBufferSize);

                try {
                    audioRecord.startRecording();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                sendStartRecord();
                OutputStream os = new FileOutputStream(mRecordFile);
                BufferedOutputStream bos = new BufferedOutputStream(os);
                DataOutputStream dos = new DataOutputStream(bos);
                audioRecord.startRecording();
                startTime = System.currentTimeMillis();
                isRecording = true;
                while (isRecording) {
                    int audioRecordReadDataSize = audioRecord.read(audioRecordBuffer, 0, audioRecordBufferSize);
                    if (audioRecordReadDataSize > 0) {
                        byte[] outputByteArray = getByteBuffer(audioRecordBuffer, audioRecordReadDataSize, false);
                        bos.write(outputByteArray);
                        duration = System.currentTimeMillis() - startTime;
                        sendTimeChangeMsg();
                    }
                }
                audioRecord.stop();
                dos.close();
                audioRecord.release();
                endTime = System.currentTimeMillis();
                sendCompleteRecord();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public static byte[] getByteBuffer(short[] shortArray, int shortArrayLength,
            boolean bigEnding) {
        int actualShortArrayLength = shortArray.length;

        if (shortArrayLength > actualShortArrayLength) {
            shortArrayLength = actualShortArrayLength;
        }

        short shortValue;
        byte[] byteArray = new byte[2 * shortArrayLength];

        for (int i = 0; i < shortArrayLength; i++) {
            shortValue = shortArray[i];

            if (bigEnding) {
                byteArray[i * 2 + 1] = (byte) (shortValue & 0x00ff);
                shortValue >>= 8;
                byteArray[i * 2] = (byte) (shortValue & 0x00ff);
            } else {
                byteArray[i * 2] = (byte) (shortValue & 0x00ff);
                shortValue >>= 8;
                byteArray[i * 2 + 1] = (byte) (shortValue & 0x00ff);
            }
        }

        return byteArray;
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

    @Override
    public void stopRecording() {
        isRecording = false;
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
