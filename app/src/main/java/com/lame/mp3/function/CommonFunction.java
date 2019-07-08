package com.lame.mp3.function;

import android.os.Looper;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * created by wangguoqun at 2019-07-01
 */

public class CommonFunction {

    public static String convertToTime(long duration) {
        if (duration < 0) {
            return "00:00";
        }
        int minute = (int) (duration / 1000 / 60);
        int seconds = (int) (duration / 1000 - minute * 60);
        StringBuilder sb = new StringBuilder();
        if (minute < 10) {
            sb.append("0" + minute);
        } else {
            sb.append(minute);
        }
        sb.append(":");
        if (seconds < 10) {
            sb.append("0" + seconds);
        } else {
            sb.append(seconds);
        }
        return sb.toString();
    }

    public static String GetDate() {
        long time = System.currentTimeMillis();
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        String date = simpleDateFormat.format(time);
        return date;
    }

    public static String GetDate(long time) {
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        String date = simpleDateFormat.format(time);
        return date;
    }

    public static boolean notEmpty(CharSequence text) {
        return !isEmpty(text);
    }

    public static boolean isEmpty(CharSequence text) {
        if (text == null || text.length() == 0) {
            return true;
        }

        return false;
    }

    public static byte[] GetBytes(short shortValue, boolean bigEnding) {
        byte[] byteArray = new byte[2];

        if (bigEnding) {
            byteArray[1] = (byte) (shortValue & 0x00ff);
            shortValue >>= 8;
            byteArray[0] = (byte) (shortValue & 0x00ff);
        } else {
            byteArray[0] = (byte) (shortValue & 0x00ff);
            shortValue >>= 8;
            byteArray[1] = (byte) (shortValue & 0x00ff);
        }

        return byteArray;
    }

    public static short GetShort(byte firstByte, byte secondByte, boolean bigEnding) {
        short shortValue = 0;

        if (bigEnding) {
            shortValue |= (firstByte & 0x00ff);
            shortValue <<= 8;
            shortValue |= (secondByte & 0x00ff);
        } else {
            shortValue |= (secondByte & 0x00ff);
            shortValue <<= 8;
            shortValue |= (firstByte & 0x00ff);
        }

        return shortValue;
    }

    public static byte[] AverageShortByteArray(byte firstShortHighByte, byte firstShortLowByte,
                                               byte secondShortHighByte, byte secondShortLowByte,
                                               boolean bigEnding) {
        short firstShort =
                CommonFunction.GetShort(firstShortHighByte, firstShortLowByte, bigEnding);
        short secondShort =
                CommonFunction.GetShort(secondShortHighByte, secondShortLowByte, bigEnding);
        return CommonFunction.GetBytes((short) (firstShort / 2 + secondShort / 2), bigEnding);
    }

    public static short AverageShort(byte firstShortHighByte, byte firstShortLowByte,
                                     byte secondShortHighByte, byte secondShortLowByte,
                                     boolean bigEnding) {
        short firstShort =
                CommonFunction.GetShort(firstShortHighByte, firstShortLowByte, bigEnding);
        short secondShort =
                CommonFunction.GetShort(secondShortHighByte, secondShortLowByte, bigEnding);
        return (short) (firstShort / 2 + secondShort / 2);
    }

    public static short WeightShort(byte firstShortHighByte, byte firstShortLowByte,
                                    byte secondShortHighByte, byte secondShortLowByte,
                                    float firstWeight, float secondWeight, boolean bigEnding) {
        short firstShort =
                CommonFunction.GetShort(firstShortHighByte, firstShortLowByte, bigEnding);
        short secondShort =
                CommonFunction.GetShort(secondShortHighByte, secondShortLowByte, bigEnding);
        return (short) (firstShort * firstWeight + secondShort * secondWeight);
    }

    public static boolean IsInMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static byte[] GetByteBuffer(short[] shortArray, boolean bigEnding) {
        return GetByteBuffer(shortArray, shortArray.length, bigEnding);
    }

    public static byte[] GetByteBuffer(short[] shortArray, int shortArrayLength,
                                       boolean bigEnding) {
        int actualShortArrayLength = shortArray.length;

        if (shortArrayLength > actualShortArrayLength) {
            shortArrayLength = actualShortArrayLength;
        }

        short shortValue;
        byte[] byteArray = new byte[2 * shortArrayLength];

        for (int i = 0; i < shortArrayLength; i++) {
            shortValue = shortArray[i];

            if (bigEnding) {
                byteArray[i * 2 + 1] = (byte) (shortValue & 0x00ff);
                shortValue >>= 8;
                byteArray[i * 2] = (byte) (shortValue & 0x00ff);
            } else {
                byteArray[i * 2] = (byte) (shortValue & 0x00ff);
                shortValue >>= 8;
                byteArray[i * 2 + 1] = (byte) (shortValue & 0x00ff);
            }
        }

        return byteArray;
    }

}
