package com.lame.mp3.utils;

import android.media.AudioRecord;
import android.util.Log;
import com.lame.mp3.AudioConstant;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MP3Recorder extends BaseRecord {

    private static final String TAG = MP3Recorder.class.getSimpleName();

    /**
     * 以下三项为默认配置参数。Google Android文档明确表明只有以下3个参数是可以在所有设备上保证支持的。
     */
    //======================Lame Default Settings=====================
    private static final int DEFAULT_LAME_MP3_QUALITY = 7;
    /**
     * 与DEFAULT_CHANNEL_CONFIG相关，因为是mono单声，所以是1
     */
    private static final int DEFAULT_LAME_IN_CHANNEL = 1;
    /**
     * Encoded bit rate. MP3 file will be encoded with bit rate 32kbps
     */
    private static final int DEFAULT_LAME_MP3_BIT_RATE = 32;

    //==================================================================

    /**
     * 自定义 每160帧作为一个周期，通知一下需要进行编码
     */
    private static final int FRAME_COUNT = 160;
    private AudioRecord mAudioRecord = null;
    private int mBufferSize;
    private short[] mPCMBuffer;
    private DataEncodeThread mEncodeThread;

    private int mMaxSize;

    private ArrayList<Short> dataList;

    private SliceVoiceManager mSliceVoiceManager;

    public MP3Recorder() {
        AUDIO_SUFFIX = AudioConstant.RecordSuffix;
    }

    /**
     * Default constructor. Setup recorder with default sampling rate 1 channel,
     * 16 bits pcm
     *
     * @param recordFile target file
     */
    public MP3Recorder(File recordFile) {
        mRecordFile = recordFile;
    }

    /**
     * Start recording. Create an encoding thread. Start record from this
     * thread.
     *
     * @throws IOException initAudioRecorder throws
     */
    public void start() {
        if (isRecording) {
            return;
        }
        isRecording = true;
        sendStartRecord();
        if (mSliceVoiceManager != null) {
            mSliceVoiceManager.reset();
        }
        try {
            initAudioRecorder();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mAudioRecord.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "录音初始化错误，检查是否有录音权限");
            sendErrorMsg(AudioConstant.ERROR_NO_PERMISSION);
        }
        new Thread() {
            @Override
            public void run() {
                startTime = System.currentTimeMillis();
                //设置线程权限
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                while (isRecording) {
                    int readSize = mAudioRecord.read(mPCMBuffer, 0, mBufferSize);
                    if (readSize > 0) {
                        sliceVoice(mPCMBuffer);
                        mEncodeThread.addTask(mPCMBuffer, readSize);
                        calculateRealVolume(mPCMBuffer, readSize);
                        sendData(mPCMBuffer, readSize);
                        sendTimeChangeMsg();
                    }
                }
                // release and finalize audioRecord
                if (mAudioRecord != null && mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                    mAudioRecord.stop();
                    mAudioRecord.release();
                }
                if (mSliceVoiceManager != null) {
                    mSliceVoiceManager.complete();
                }
                mAudioRecord = null;
                // stop the encoding thread and try to wait
                // until the thread finishes its job
                mEncodeThread.sendStopMessage();
            }
        }.start();
    }

    /**
     * @param buffer buffer
     * @param readSize readSize
     */
    private void calculateRealVolume(short[] buffer, int readSize) {
        double sum = 0;
        for (int i = 0; i < readSize; i++) {
            sum += buffer[i] * buffer[i];
        }
        if (readSize > 0) {
            double amplitude = sum / readSize;
            mVolume = (int) (10 * Math.log10(amplitude));
        }
    }

    private void sendData(short[] shorts, int readSize) {
        if (dataList != null) {
            int length = readSize / 300;
            short resultMax = 0, resultMin = 0;
            for (short i = 0, k = 0; i < length; i++, k += 300) {
                for (short j = k, max = 0, min = 1000; j < k + 300; j++) {
                    if (shorts[j] > max) {
                        max = shorts[j];
                        resultMax = max;
                    } else if (shorts[j] < min) {
                        min = shorts[j];
                        resultMin = min;
                    }
                }
                dataList.add(resultMax);
            }
        }
    }

    private void sliceVoice(short[] shorts) {
        if (mSliceVoiceManager == null) return;
        if (mSliceVoiceManager.judgeToStart(mVolume, startTime)) {
            mSliceVoiceManager.writeData(shorts);
        }
    }

    /**
     * 设置数据的获取显示，设置最大的获取数，一般都是控件大小/线的间隔offset
     *
     * @param dataList 数据
     * @param maxSize 最大个数
     */
    public void setDataList(ArrayList<Short> dataList, int maxSize) {
        this.dataList = dataList;
        this.mMaxSize = maxSize;
    }

    private int mVolume;

    /**
     * @return 真实音量
     */
    public int getRealVolume() {
        return mVolume;
    }

    /**
     * 获取相对音量。 超过最大值时取最大值。
     *
     * @return 音量
     */
    public int getVolume() {
        return mVolume;
    }

    /**
     * Initialize audio recorder
     */
    private void initAudioRecorder() throws IOException {
        mBufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLING_RATE, DEFAULT_CHANNEL_CONFIG,
                DEFAULT_AUDIO_FORMAT.getAudioFormat());

        int bytesPerFrame = DEFAULT_AUDIO_FORMAT.getBytesPerFrame();
        /* Get number of samples. Calculate the buffer size
         * (round up to the factor of given frame size)
         * 使能被整除，方便下面的周期性通知
         * */
        int frameSize = mBufferSize / bytesPerFrame;
        if (frameSize % FRAME_COUNT != 0) {
            frameSize += (FRAME_COUNT - frameSize % FRAME_COUNT);
            mBufferSize = frameSize * bytesPerFrame;
        }

        /* Setup audio recorder */
        mAudioRecord = new AudioRecord(DEFAULT_AUDIO_SOURCE, DEFAULT_SAMPLING_RATE, DEFAULT_CHANNEL_CONFIG,
                DEFAULT_AUDIO_FORMAT.getAudioFormat(), mBufferSize);

        mPCMBuffer = new short[mBufferSize];
        /*
         * Initialize lame buffer
         * mp3 sampling rate is the same as the recorded pcm sampling rate
         * The bit rate is 32kbps
         *
         */
        LameUtil.init(DEFAULT_SAMPLING_RATE, DEFAULT_LAME_IN_CHANNEL, DEFAULT_SAMPLING_RATE, DEFAULT_LAME_MP3_BIT_RATE,
                DEFAULT_LAME_MP3_QUALITY);
        // Create and run thread used to encode data
        // The thread will
        mEncodeThread = new DataEncodeThread(this, mRecordFile, mBufferSize);
        //		mEncodeThread = new DataEncodeThread(mRecordFile, mBufferSize);
        mEncodeThread.start();
        mAudioRecord.setRecordPositionUpdateListener(mEncodeThread, mEncodeThread.getHandler());
        mAudioRecord.setPositionNotificationPeriod(FRAME_COUNT);
        mVolume = 0;
    }

    public void release() {
        isRecording = false;
        if (mHander != null) mHander.removeCallbacksAndMessages(null);
        if (mEncodeThread != null) {
            mEncodeThread.release();
        }
    }

    public boolean isRecordFileExist() {
        if (mRecordFile != null && mRecordFile.exists()) {
            return true;
        }
        return false;
    }

    public File getRecordFile() {
        return mRecordFile;
    }

    public void callBack() {
        sendCompleteRecord();
    }

    @Override
    public void setSliceVoiceManager(SliceVoiceManager sliceVoiceManager) {
        mSliceVoiceManager = sliceVoiceManager;
    }

    @Override public void stopRecording() {
        isRecording = false;
    }
}