package com.lame.mp3.audiorecord;

/**
 * created by wangguoqun at 2019-06-29
 */
public interface OnAudioPlayListener {
    void onStart();

    void onStop();

    void onProgress(int progress, long position, long duration);
}
