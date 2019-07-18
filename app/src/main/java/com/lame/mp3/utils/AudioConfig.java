package com.lame.mp3.utils;

import com.lame.mp3.AudioConstant;

/**
 * created by wangguoqun at 2019-07-13
 * 不管什么场景的配置统一用这个类来封装
 */
public class AudioConfig {

    public String format= AudioConstant.PcmSuffix;
    int volumeThreshold;
    long sliceLength = -1l;

    ADD_HEAD_MODE headMode = ADD_HEAD_MODE.ORIGNAL;

    /**
     * 添加头部的方式：创建新的文件、在原有的数据上添加头部
     */
    enum ADD_HEAD_MODE {
        NEW, ORIGNAL
    }

    public AudioConfig setFormat(String format) {
        this.format = format;
        return this;
    }

    public AudioConfig setVolumeThreshold(int volumeThreshold) {
        this.volumeThreshold = volumeThreshold;
        return this;
    }

    public AudioConfig setSliceLength(long sliceLength) {
        this.sliceLength = sliceLength;
        return this;
    }

    public AudioConfig setHeadMode(ADD_HEAD_MODE headMode) {
        this.headMode = headMode;
        return this;
    }
}
