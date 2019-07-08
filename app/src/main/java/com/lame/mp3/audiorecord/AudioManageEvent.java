package com.lame.mp3.audiorecord;

/**
 * Created by kuyuzhiqi on 2017/4/14.
 */

public interface AudioManageEvent {

    void start();

    void onTimeChange(long time);

    /**
     * 录音后剪裁要编码
     */
    void completeEncode();

    /**
     *录制完毕
     */
    void completeRecord();

    void onError(int code);
}