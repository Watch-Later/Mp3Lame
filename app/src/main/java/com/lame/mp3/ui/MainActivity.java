package com.lame.mp3.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.lame.mp3.AudioConstant;
import com.lame.mp3.R;
import com.lame.mp3.audiorecord.AudioRecordManager;
import com.lame.mp3.utils.RecordFilePathHelper;

/**
 * 测试用的
 */
public class MainActivity extends AppCompatActivity {

    private AudioRecordManager mAudioRecordManager;

    public static void start(Context context) {
        context.startActivity(new Intent(context, MainActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn_start).setOnClickListener(v -> {
            String path = RecordFilePathHelper.createRecordFile(this, AudioConstant.PcmSuffix);
            mAudioRecordManager.start(path);
            Log.i("tag", "path:" + path);
        });
        findViewById(R.id.btn_stop).setOnClickListener(v -> {
            mAudioRecordManager.stop();
        });
        mAudioRecordManager = new AudioRecordManager(AudioRecordManager.AUDIO_FORMAT.PCM);
    }
}
