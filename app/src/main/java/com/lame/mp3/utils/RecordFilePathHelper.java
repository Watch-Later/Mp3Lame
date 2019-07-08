package com.lame.mp3.utils;

import android.content.Context;
import java.io.File;

/**
 * created by wangguoqun at 2019-06-27
 */
public class RecordFilePathHelper {

    public static String createRecordFile(Context context) {
        return createRecordFile(context, "");
    }

    public static String createRecordFile(Context context, String suffix) {
        String dir = getRecordDir(context);
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return dir + System.currentTimeMillis() + suffix;
    }

    public static String getRecordDir(Context context) {
        return context.getExternalFilesDir("").getAbsolutePath()
                + File.separator + "record" + File.separator;
    }

    public static String getFolderPath(Context context) {
        return context.getExternalFilesDir("").getAbsolutePath()
                + File.separator;
    }

    public static String getDownloadPath(Context context) {
        String dir = context.getExternalFilesDir("").getAbsolutePath()
                + File.separator + "download" + File.separator;
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return dir;
    }
}
