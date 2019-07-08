package com.lame.mp3.listener;

/**
 * created by wangguoqun at 2019-07-01
 */
 public interface DecodeOperateInterface {
     void updateDecodeProgress(int decodeProgress);

     void decodeSuccess();

     void decodeFail();
}
