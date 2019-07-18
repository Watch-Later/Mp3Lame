package com.lame.mp3.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.lame.mp3.R;
import com.lame.mp3.utils.PcmFileToMp3;
import com.lame.mp3.utils.RecordFilePathHelper;

public class ConvertToMp3TestActivity extends AppCompatActivity {

    private PcmFileToMp3 mPcmFileToMp3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convert_to_mp3_test);
        /**
         * 直接将文件放相应的目录下，test.pcm在asset文件夹下
         * todo test.pcm有点问题
         */
        String pcmFileName = RecordFilePathHelper.getRecordDir(this) + "test.pcm";
        String mp3FileName = RecordFilePathHelper.getRecordDir(this) + "result.mp3";

        mPcmFileToMp3 = new PcmFileToMp3(pcmFileName, mp3FileName);
        findViewById(R.id.btn_convert).setOnClickListener(v -> {
            mPcmFileToMp3.convertToMp3();
        });
    }
}
