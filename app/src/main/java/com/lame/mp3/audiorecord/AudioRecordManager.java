package com.lame.mp3.audiorecord;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.lame.mp3.AudioConstant;
import com.lame.mp3.R;
import com.lame.mp3.utils.AudioConfig;
import com.lame.mp3.utils.BaseRecord;
import com.lame.mp3.utils.MP3Recorder;
import com.lame.mp3.utils.SliceVoiceManager;
import java.io.File;


public class AudioRecordManager {

    private AudioManageEvent audioManageEvent;
    private BaseRecord mBaseRecord;
    private SliceVoiceManager mSliceVoiceManager;

    /**
     * MP3_SLICE_ALL  语音转文字
     * MP3_SLICE_PART 上传数据分析
     * PCM_HIGH_RATE 合成mp3
     */
    public enum AUDIO_FORMAT {MP3, SLICE_ALL_WAV, SLICE_PART_WAV, PCM, WAV}

    private AUDIO_FORMAT audioFormat = AUDIO_FORMAT.MP3;

    public AudioRecordManager(AUDIO_FORMAT audioFormat) {
        this.audioFormat = audioFormat;
        AudioConfig config = new AudioConfig();
        switch (audioFormat) {
            case MP3:
                mBaseRecord = new MP3Recorder();
                break;
            case SLICE_ALL_WAV:
                mBaseRecord = new MP3Recorder();
                config.setFormat(AudioConstant.WavSuffix);
                mSliceVoiceManager = new SliceVoiceManager(mBaseRecord, config);
                break;
            case SLICE_PART_WAV:
                mBaseRecord = new MP3Recorder();
                config.setFormat(AudioConstant.WavSuffix).setSliceLength(4l * 1000).setVolumeThreshold(40);
                mSliceVoiceManager = new SliceVoiceManager(mBaseRecord, config);
                break;
            case PCM:
                mBaseRecord = new PcmRecorder();
                break;
            case WAV:
                config.setFormat(AudioConstant.WavSuffix);
                mBaseRecord = new PcmRecorder(config);
                break;
        }
        mBaseRecord.setHander(mHandler);
        if (mSliceVoiceManager != null) {
            mBaseRecord.setSliceVoiceManager(mSliceVoiceManager);
        }
        setMaxDuration(60 * 1000);
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BaseRecord.MSG_ERROR:
                    if (null != audioManageEvent) {
                        audioManageEvent.onError((Integer) msg.obj);
                    }
                    break;
                case BaseRecord.MSG_START_RECORD:
                    if (null != audioManageEvent) {
                        audioManageEvent.start();
                    }
                    break;
                case BaseRecord.MSG_TIME_CHANGE:
                    long time = (long) msg.obj;
                    if (time > BaseRecord.MAX_DURATION * 1000) {
                        stop();
                    }
                    if (null != audioManageEvent) audioManageEvent.onTimeChange(time);
                    break;
                case BaseRecord.MSG_COMPLETE_RECORD:
                    if (null != audioManageEvent) audioManageEvent.completeRecord();
                    break;
                case BaseRecord.MSG_COMPLETE_ENCODING:
                    if (null != audioManageEvent) audioManageEvent.completeEncode();
                    break;
                case BaseRecord.MSG_FILE_NOT_EXIST:
                    Log.i("tag", "录音文件不存在");
                    break;
            }
        }
    };

    public void start(String fileName) {
        Boolean isRecording = mBaseRecord.isRecording();
        if (null == mBaseRecord || (isRecording != null && isRecording)) return;
        if (mSliceVoiceManager != null) {
            mSliceVoiceManager.setDir(fileName.substring(0, fileName.lastIndexOf("/") + 1));
        }
        mBaseRecord.start(fileName);
    }

    public void stop() {
        if (null == mBaseRecord || !isRecordFileExist()) return;

        if (mBaseRecord.isRecording()) mBaseRecord.stopRecording();
    }

    public boolean isRecording() {
        return mBaseRecord.isRecording();
    }

    public boolean isRecordFileExist() {
        return mBaseRecord.isRecordFileExist();
    }

    public void release() {
        mBaseRecord.release();
        audioManageEvent = null;
    }

    public String getFilePath() {
        if (mBaseRecord.isRecordFileExist()) {
            return mBaseRecord.getRecordFile().getAbsolutePath();
        }
        return "";
    }

    public String getSliceFilePath() {
        if (mSliceVoiceManager != null) {
            return mSliceVoiceManager.getSliceFile().getAbsolutePath();
        }
        return null;
    }

    public String getEncodeFilePath() {
        if (mBaseRecord == null) {
            return null;
        }
        File file = mBaseRecord.getEncodeFile();
        if (file != null && file.exists()) {
            return file.getAbsolutePath();
        }
        return null;
    }

    public long getSliceVoiceDuration() {
        if (mSliceVoiceManager != null) {
            return mSliceVoiceManager.getDuration();
        }
        return 0;
    }

    public String getSliceVoiceBase64() {
        if (mSliceVoiceManager != null) {
            return mSliceVoiceManager.getDataBase64();
        }
        return "";
    }

    public long getDuration() {
        return mBaseRecord.getDuration();
    }

    public void deleteRecordFile() {
        mBaseRecord.deleteRecordFile();
    }

    public void setAudioManageEvent(AudioManageEvent audioManageEvent) {
        this.audioManageEvent = audioManageEvent;
    }

    public void setMaxDuration(long maxDuration) {
        if (mBaseRecord == null || maxDuration < 0l) {
            return;
        }
        mBaseRecord.setMaxDuration(maxDuration);
    }

    public static int getErrorText(int code) {
        int id;
        switch (code) {
            case AudioConstant.ERROR_UNKNOW:
                id = R.string.str_unknown_error;
                break;
            case AudioConstant.ERROR_FILE_NOT_FOUND:
                id = R.string.record_file_not_found;
                break;
            case AudioConstant.ERROR_NO_PERMISSION:
                id = R.string.no_record_voice_permission;
                break;
            case AudioConstant.ERROR_FILE_NOT_EFFECTIVE:
                id = R.string.effective_voice_too_short;
                break;
            default:
                id = R.string.str_unknown_error;
                break;
        }
        return id;
    }
}
