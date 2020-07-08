package com.lmw.audiodemo.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.lmw.audiodemo.CoreLib;
import com.lmw.ijkplayer.listener.AudioPlayListener;
import com.lmw.ijkplayer.service.AudioPlayService;

public class AlonePlayManager {
    private static final String TAG = "AlonePlayManager";

    private static AlonePlayManager instance;

    private AudioPlayService mPlayService;
    private boolean mIsBound;
    private AloneServiceConnection mConnection;

    private String mAloneUrl;
    private AudioPlayListener mAudioPlayListener;
    private Boolean mAutoPlay;

    private AlonePlayManager() {
    }

    public static AlonePlayManager getInstance() {
        if (instance == null) {
            synchronized (AlonePlayManager.class) {
                if (instance == null) {
                    instance = new AlonePlayManager();
                }
            }
        }
        return instance;
    }

    public void startService() {
        if (!mIsBound || mPlayService == null) {
            Intent intent = new Intent(CoreLib.instance.getContext(), AudioPlayService.class);
            mConnection = new AloneServiceConnection();
            mIsBound = CoreLib.instance.getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void stopService() {
        if (mIsBound && mPlayService != null) {
            if (mPlayService.isPlaying()) {
                mPlayService.pause();
                mPlayService.stop();
            }
            CoreLib.instance.getContext().unbindService(mConnection);
            mConnection = null;
            mPlayService = null;
            mIsBound = false;
        }
    }


    private class AloneServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayService = ((AudioPlayService.AudioPlayBinder) service).getAudioPlayService();
            if (mAloneUrl != null && mPlayService != null) {
                playAlone(mAloneUrl, mAutoPlay, mAudioPlayListener);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }


    public void playAlone(String url, boolean autoPlay, AudioPlayListener audioPlayListener) {
        if (mAudioPlayListener != null) {
            mAudioPlayListener.onComplete();
        }
        mAloneUrl = url;
        mAudioPlayListener = audioPlayListener;
        mAutoPlay = autoPlay;
        if (!mIsBound || mPlayService == null) {
            startService();
            return;
        }
        if (isPlaying()) {
            mPlayService.pause();
            mPlayService.stop();
        }
        mPlayService.setAutoPlaying(mAutoPlay);
        mPlayService.setAudioPlayListener(audioPlayListener);
        mPlayService.initDataSource(url);
    }

    public void playAlone(String url, AudioPlayListener audioPlayListener) {
        playAlone(url, false, audioPlayListener);
    }

    public void playPause() {
        if (!mIsBound || mPlayService == null) return;
        mPlayService.playOrPause();
    }


    public boolean isPlaying() {
        return mIsBound && mPlayService != null && mPlayService.isPlaying();
    }

    public void setVolume(float volume) {
        if (!mIsBound || mPlayService == null) return;
        mPlayService.setVolume(volume, false);
    }

    public boolean isPause() {
        return mIsBound && mPlayService != null && mPlayService.isPause();
    }

    public void pause() {
        if (!mIsBound || mPlayService == null) return;
        mPlayService.pause();
    }


    public void release() {
        if (!mIsBound || mPlayService == null) return;
        if (isPlaying()) {
            mPlayService.pause();
            mPlayService.stop();
        }
        mAloneUrl = null;
        mAudioPlayListener = null;
        stopService();
    }

    public void seekTo(long l) {
        if (!mIsBound || mPlayService == null) return;
        mPlayService.seekTo(l);
    }


}
