package com.lame.mp3;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import android.util.Base64;
import android.util.Log;
import com.lame.mp3.audio.PcmToWav;
import com.lame.mp3.listener.ConvertOperateInterface;
import com.lame.mp3.utils.RecordFilePathHelper;
import java.io.File;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.lame.mp3", appContext.getPackageName());
    }

    @Test
    public void convertToWav() {
        PcmToWav pcmToWav = new PcmToWav();
        Context appContext = InstrumentationRegistry.getTargetContext();
        String src = RecordFilePathHelper.getRecordDir(appContext) + "test.pcm";
        String targetSrc = RecordFilePathHelper.createRecordFile(appContext, AudioConstant.WavSuffix);
        File targetFile = new File(targetSrc);
        if (!targetFile.exists()) {
            try {
                targetFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        pcmToWav.convert(src, targetSrc, new ConvertOperateInterface() {
            @Override public void convertSuccess() {
                Log.i("tag", "convertSuccess");
            }

            @Override public void convertFail() {
                Log.i("tag", "convertFail");
            }
        });
    }

    @Test
    public void convertTest() {
        String str = "NtVRO2XpoNTZ6OAI93SfH7Z0dGvBF4dJMCdrGxTh4uv7oymnzrwhMtuFHykyfKeP";
        byte[] bytes = str.getBytes();
        //System.out.println(Base64.getEncoder().encodeToString(bytes));
        Log.i("tag", Base64.encodeToString(bytes,Base64.DEFAULT));
    }
}
