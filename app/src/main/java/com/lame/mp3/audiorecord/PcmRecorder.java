package com.lame.mp3.audiorecord;

import android.media.AudioRecord;
import android.media.AudioTrack;
import android.util.Log;
import com.lame.mp3.AudioConstant;
import com.lame.mp3.utils.BaseRecord;
import com.lame.mp3.utils.PCMFormat;
import com.lame.mp3.utils.PcmToWav;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

public class PcmRecorder extends BaseRecord {

    private AudioTrack mAudioTrack;
    private short[] buffer;
    private long endTime;
    private File mWavFile;
    private boolean isAddHeader;
    int audioRecordMinBufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLING_RATE, DEFAULT_CHANNEL_CONFIG,
            DEFAULT_AUDIO_FORMAT.getAudioFormat());

    public PcmRecorder() {
        AUDIO_SUFFIX = AudioConstant.PcmSuffix;
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
        long byteRate = (DEFAULT_AUDIO_FORMAT == PCMFormat.PCM_16BIT ? 16 : 8) * DEFAULT_SAMPLING_RATE * 1 / 8;
        try {
            in = new FileInputStream(mRecordFile);
            dos = new DataOutputStream(new FileOutputStream(mWavFile));
            byte[] bytes = new byte[in.available()];//in.available()是得到文件的字节数
            int length = bytes.length;
            while (length != 1) {
                long i = in.read(bytes, 0, bytes.length);
                if (i == -1) {
                    break;
                }
                length -= i;
            }
            int dataLength = bytes.length;
            int shortlength = dataLength / 2;
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes, 0, dataLength);
            ShortBuffer shortBuffer = byteBuffer.order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();//此处设置大小端
            short[] shorts = new short[shortlength];
            shortBuffer.get(shorts, 0, shortlength);

            totalAudioLen = in.getChannel().size();
            //由于不包括RIFF和WAV
            byte[] header = PcmToWav.getWavHeader(totalAudioLen, totalAudioLen + 36, DEFAULT_SAMPLING_RATE,
                    1, byteRate);
            dos.write(header, 0, header.length);
            for (int i = 0; i < shorts.length; i++) {
                dos.writeShort(shorts[i]);
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
        if (isAddHeader) {
            return mWavFile;
        } else {
            return mRecordFile;
        }
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

    public void setAddHeader(boolean addHeader) {
        isAddHeader = addHeader;
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
