package com.lame.mp3;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import com.lame.mp3.utils.LameUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LameUtil lameUtil =new LameUtil();
    }
}
