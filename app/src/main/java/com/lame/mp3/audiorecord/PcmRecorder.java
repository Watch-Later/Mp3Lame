package com.lame.mp3.audiorecord;

import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;
import com.lame.mp3.AudioConstant;
import com.lame.mp3.audio.PcmToWav;
import com.lame.mp3.utils.AudioConfig;
import com.lame.mp3.utils.BaseRecord;
import com.lame.mp3.utils.PCMFormat;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PcmRecorder extends BaseRecord {

    private AudioTrack mAudioTrack;
    private short[] buffer;
    private long endTime;
    private File mWavFile;
    private boolean isAddHeader;
    int audioRecordMinBufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLING_RATE, DEFAULT_CHANNEL_CONFIG,
            DEFAULT_AUDIO_FORMAT.getAudioFormat());
    private long byteRate;

    public PcmRecorder() {
        AUDIO_SUFFIX = AudioConstant.PcmSuffix;
    }

    public PcmRecorder(AudioConfig config) {
        if (config.format == AudioConstant.WavSuffix) {
            AUDIO_SUFFIX = config.format;
            isAddHeader = true;
            byteRate = (BaseRecord.DEFAULT_AUDIO_FORMAT == PCMFormat.PCM_16BIT ? 16 : 8)
                    * BaseRecord.DEFAULT_SAMPLING_RATE * 1 / 8;
        } else {
            isAddHeader = false;
        }
    }

    public void start() {
        Thread thread = new Thread(() -> {
            try {
                isRecording = true;
                sendStartRecord();
                OutputStream os = new FileOutputStream(mRecordFile);
                BufferedOutputStream bos = new BufferedOutputStream(os);
                DataOutputStream dos = new DataOutputStream(bos);
                AudioRecord audioRecord =
                        new AudioRecord(DEFAULT_AUDIO_SOURCE, DEFAULT_SAMPLING_RATE, DEFAULT_CHANNEL_CONFIG,
                                DEFAULT_AUDIO_FORMAT.getAudioFormat(), audioRecordMinBufferSize);
                buffer = new short[audioRecordMinBufferSize];
                audioRecord.startRecording();
                startTime = System.currentTimeMillis();
                isRecording = true;
                while (isRecording) {
                    int bufferedReadResult = audioRecord.read(buffer, 0, audioRecordMinBufferSize);
                    if (bufferedReadResult > 0) {
                        for (int i = 0; i < bufferedReadResult; i++) {
                            dos.writeShort(Short.reverseBytes(buffer[i]));
                        }
                        long v = 0;
                        for (int i = 0; i < buffer.length; i++) {
                            v += buffer[i] * buffer[i];
                        }
                        double mean = v / (double) bufferedReadResult;
                        double volume = 10 * Math.log10(mean);
                        //获取录音的声音分贝值
                        Log.i("tag", "分贝值:" + volume);
                        duration = System.currentTimeMillis() - startTime;
                        Log.i("time", "totalTime:" + duration);
                    }
                    sendTimeChangeMsg();
                }
                audioRecord.stop();
                dos.close();
                audioRecord.release();
                endTime = System.currentTimeMillis();
                sendCompleteRecord();
                if (isAddHeader) {
                    convertToWav();
                }
                Log.i("time", "endTime:" + endTime);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    private void convertToWav() {
        if (!isRecordFileExist()) {
            return;
        }
        createmWavFile();
        FileInputStream in = null;
        DataOutputStream dos = null;
        long totalAudioLen;
        try {
            in = new FileInputStream(mRecordFile);
            dos = new DataOutputStream(new FileOutputStream(mWavFile));

            totalAudioLen = in.getChannel().size();
            //由于不包括RIFF和WAV
            byte[] header = PcmToWav.getWavHeader(totalAudioLen, totalAudioLen + 36, DEFAULT_SAMPLING_RATE,
                    1, byteRate);
            dos.write(header, 0, header.length);
            int length = 0;
            int buffer_size = 1024;
            byte[] buffer = new byte[buffer_size];
            while ((length = in.read(buffer, 0, buffer_size)) != -1) {
                dos.write(buffer, 0, length);
            }
            in.close();
            dos.close();
            sendCompleteEncode();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createmWavFile() {
        String path = mRecordFile.getAbsolutePath();
        String dir = path.substring(0, path.lastIndexOf(File.separator) + 1);
        String wavPath = dir + System.currentTimeMillis() + AudioConstant.WavSuffix;
        mWavFile = new File(wavPath);
        try {
            mWavFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    @Override public File getEncodeFile() {
        return mWavFile;
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
