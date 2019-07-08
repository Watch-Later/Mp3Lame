package com.lame.mp3.listener;

/**
 * created by wangguoqun at 2019-07-01
 */
public interface ComposeAudioInterface {
    void updateComposeProgress(int composeProgress);

    void composeSuccess();

    void composeFail();
}
