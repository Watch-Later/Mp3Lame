package com.lame.mp3.ui;

import android.content.Context;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.lame.mp3.AudioConstant;
import com.lame.mp3.R;
import com.lame.mp3.audio.ComposeAudio;
import com.lame.mp3.audio.DecodeEngine;
import com.lame.mp3.audiorecord.AudioManageEvent;
import com.lame.mp3.audiorecord.AudioRecordManager;
import com.lame.mp3.listener.ComposeAudioInterface;
import com.lame.mp3.listener.DecodeOperateInterface;
import com.lame.mp3.utils.MP3Recorder;

/**
 * 声音合成demo
 */
public class MusicSynthesisActivity extends AppCompatActivity implements DecodeOperateInterface,
        ComposeAudioInterface {

    private String bgmPath, name;
    private long duration;
    private AudioManager mAudioManager;
    private int volume;
    private AudioRecordManager mAudioRecordManager;
    private long totalTime = -1l;
    private int colorTextOn, colorTextOff;
    private String uploadPcmName, firstComposeFileName, composedFileName;
    private TextView tv_timer;
    /**
     * 将bgm指定长度进行转码pcm的位置
     */
    private String decodeFileName;
    private String bgId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_synthesis);

        initViews();
    }

    private void initViews() {
        tv_timer = findViewById(R.id.tv_timer);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioRecordManager = new AudioRecordManager(AudioRecordManager.AUDIO_FORMAT.WAV);
        mAudioRecordManager.setAudioManageEvent(new AudioManageEvent() {

            @Override public void start() {
            }

            @Override public void onTimeChange(long time) {
                totalTime = time;
            }

            @Override public void completeEncode() {
                new DecodeAsyncTask().execute();
            }

            @Override public void completeRecord() {
                if (totalTime < MP3Recorder.MIN_DURATION) {
                    mAudioRecordManager.deleteRecordFile();
                }
            }

            @Override public void onError(int code) {
            }
        });
    }

    @Override public void updateDecodeProgress(int decodeProgress) {

    }

    @Override public void decodeSuccess() {
        new ComposeAsyncTask().execute();
    }

    @Override public void decodeFail() {

    }

    @Override public void updateComposeProgress(int composeProgress) {

    }

    @Override public void composeSuccess() {
    }

    @Override public void composeFail() {

    }

    class DecodeAsyncTask extends AsyncTask<Void, Integer, String> {

        @Override protected void onPreExecute() {
            super.onPreExecute();
            if (!mAudioRecordManager.isRecordFileExist()) {
                cancel(true);
            }
        }

        @Override protected String doInBackground(Void... voids) {
            DecodeEngine.getInstance()
                    .beginDecodeMusicFile(bgmPath, decodeFileName, 0,
                            (int) (totalTime / AudioConstant.OneSecond) + +AudioConstant.MusicCutEndOffset,
                            MusicSynthesisActivity.this);
            return null;
        }

    }

    class ComposeAsyncTask extends AsyncTask<Void, Integer, String> {
        @Override protected void onPreExecute() {
            super.onPreExecute();
            if (mAudioRecordManager.getEncodeFilePath() == null) {
                cancel(true);
                return;
            }
            firstComposeFileName = mAudioRecordManager.getEncodeFilePath();
            composedFileName = firstComposeFileName.substring(0, firstComposeFileName.lastIndexOf("."))
                    + "composed" + AudioConstant.MusicSuffix;
        }

        @Override protected String doInBackground(Void... voids) {
            ComposeAudio.composeAudio(firstComposeFileName, decodeFileName, composedFileName, false,
                    AudioConstant.VoiceWeight, AudioConstant.VoiceBackgroundWeight,
                    -1 * AudioConstant.MusicCutEndOffset / 2 * AudioConstant.RecordDataNumberInOneSecond,
                    MusicSynthesisActivity.this);
            return null;
        }
    }

    @Override protected void onDestroy() {
        if (mAudioRecordManager != null) {
            mAudioRecordManager.release();
        }
        super.onDestroy();
    }

    @Override protected void onPause() {
        mAudioRecordManager.stop();
        mAudioRecordManager.deleteRecordFile();
        super.onPause();
    }
}
