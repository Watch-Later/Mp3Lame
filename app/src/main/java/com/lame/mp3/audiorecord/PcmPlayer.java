package com.lame.mp3.audiorecord;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * created by wangguoqun at 2019-06-29
 */
public class PcmPlayer {

    public static final int MSG_PLAY_START = 101;
    public static final int MSG_PLAY_END = 102;
    private volatile boolean isPlaying;
    private AudioTrack mAudioTrack;
    private Handler mHandler;
    private long totalTime = -1l;
    private File mRecordFile;
    private OnAudioPlayListener mOnAudioPlayListener;

    public PcmPlayer(OnAudioPlayListener onAudioPlayListener) {
        mOnAudioPlayListener = onAudioPlayListener;
        mHandler = new Handler() {
            @Override public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case MSG_PLAY_START:
                        if (mOnAudioPlayListener != null) {
                            mOnAudioPlayListener.onStart();
                        }
                        break;
                    case MSG_PLAY_END:
                        if (mOnAudioPlayListener != null) {
                            mOnAudioPlayListener.onStop();
                        }
                        break;
                }
            }
        };
    }

    public void playRecord() {
        if (mRecordFile == null || !mRecordFile.exists() || isPlaying) {
            return;
        }
        new Thread(new Runnable() {
            @Override public void run() {
                if (mHandler != null) {
                    Message msg = Message.obtain();
                    msg.what = MSG_PLAY_START;
                    mHandler.sendMessage(msg);
                }
                isPlaying = true;
                int musicLength = (int) (mRecordFile.length() / 2);
                short[] music = new short[musicLength];
                try {
                    InputStream is = new FileInputStream(mRecordFile);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    DataInputStream dis = new DataInputStream(bis);
                    int i = 0;
                    while (dis.available() > 0) {
                        music[i] = dis.readShort();
                        i++;
                    }
                    dis.close();
                    mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 16000,
                            AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, musicLength * 2,
                            AudioTrack.MODE_STREAM);
                    mAudioTrack.setNotificationMarkerPosition(musicLength);
                    mAudioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
                        @Override public void onMarkerReached(AudioTrack track) {
                            isPlaying = false;
                            sendMessageEnd();
                        }

                        @Override public void onPeriodicNotification(AudioTrack track) {
                        }
                    });
                    mAudioTrack.play();
                    mAudioTrack.write(music, 0, musicLength);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendMessageEnd() {
        if (mHandler != null) {
            Message msg = Message.obtain();
            msg.what = MSG_PLAY_END;
            mHandler.sendMessage(msg);
        }
    }

    public void playRecord(String fileName) {
        mRecordFile = new File(fileName);
        playRecord();
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void stop() {
        isPlaying = false;
        sendMessageEnd();
        if (mAudioTrack == null || mAudioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
            return;
        }
        if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            mAudioTrack.pause();
            mAudioTrack.flush();
        }
    }

    public void release() {
        stop();
        if (mAudioTrack != null) {
            mAudioTrack.release();
            mAudioTrack = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mRecordFile = null;
    }
}
