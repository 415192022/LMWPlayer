package com.lmw.audiodemo.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.lmw.audiodemo.listener.WorksPlayListener;
import com.lmw.audiodemo.manager.FloatServiceManager;
import com.lmw.audiodemo.manager.MediaSessionManager;
import com.lmw.audiodemo.model.Works;
import com.lmw.audiodemo.receiver.AudioEarPhoneReceiver;
import com.lmw.audiodemo.receiver.EarphoneControlReceiver;
import com.lmw.ijkplayer.listener.AudioPlayListener;
import com.lmw.ijkplayer.service.AudioPlayService;

import java.util.ArrayList;
import java.util.List;

public class WorksPlayService extends AudioPlayService {
    private static final String TAG = "WorksPlayService";
    private List<Works> worksList = new ArrayList<>();
    private int mPosition;
    private Works mCurWorks;
    private boolean mIsLooping;
    private int mListSize;

    private boolean mIsInitData;
    private WorksPlayListener mWorksPlayListener;
    private MediaSessionManager mMediaSessionManager;
    private AudioManager mAudioManager;

    /**
     * 来电/耳机拔出时暂停播放
     * 在播放时调用，在暂停时注销
     */
    private final AudioEarPhoneReceiver mNoisyReceiver = new AudioEarPhoneReceiver();
    private final IntentFilter mFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);


    public void setWorksPlayListener(WorksPlayListener mWorksPlayListener) {
        this.mWorksPlayListener = mWorksPlayListener;
    }

    /**
     * 加载歌曲
     *
     * @param works
     */
    public void initWorksList(List<Works> works) {
        worksList.clear();
        worksList.addAll(works);
        mIsInitData = true;
        reset();
        mPosition = 0;
        mCurWorks = null;
        mListSize = works.size();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setAudioPlayListener(new WorksAudioPlayListener());
        registerReceiver();
    }

    /**
     * 播放
     *
     * @param position
     */
    public void playSong(int position) {
        if (mListSize <= 0) return;
        if (position < 0 || position >= mListSize) position = 0;
        this.mPosition = position;
        mIsInitData = false;
        changeWorks();
        setAutoPlaying(true);
        initDataSource(mCurWorks.voiceUrl);
    }

    /**
     * 下一首
     */
    public void nextSong() {
        if (mListSize <= 0) return;
        if (mPosition >= mListSize - 1) {
            mPosition = 0;
        } else {
            mPosition++;
        }
        playSong(mPosition);
    }

    /**
     * 上一首
     */
    public void preSong() {
        if (mListSize <= 0) return;
        if (mPosition <= 0) {
            mPosition = mListSize - 1;
        } else {
            mPosition--;
        }
        playSong(mPosition);
    }

    /**
     * 切换歌曲
     */
    private void changeWorks() {
        mCurWorks = worksList.get(mPosition);
        if (mWorksPlayListener != null) mWorksPlayListener.onWorksChange(mCurWorks, mPosition);
    }

    public Works getPlayingWorks() {
        return mCurWorks;
    }

    public int getPlayingPosition() {
        return mPosition;
    }

    public List<Works> getWorksList() {
        return worksList;
    }

    public void setLooping(boolean isLooping) {
        this.mIsLooping = isLooping;
    }

    public boolean isLooping(){
        return mIsLooping;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        worksList.clear();
        return new WorksPlayBinder();
    }

    public class WorksPlayBinder extends Binder {
        public WorksPlayService getWorksPlayService() {
            return WorksPlayService.this;
        }
    }


    private class WorksAudioPlayListener implements AudioPlayListener {

        @Override
        public void onError(String msg) {
            Log.e(TAG, "playError " + msg);
        }


        @Override
        public void onPreparing() {

        }

        @Override
        public void onPlaying() {
            if (mMediaSessionManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                mMediaSessionManager.updatePlaybackState();
            if (mWorksPlayListener != null) mWorksPlayListener.onPlayStateChange(true, mPosition);
            FloatServiceManager.getInstance().floatViewPlay(mCurWorks.coverUrl);
        }

        @Override
        public void onPause() {
            if (mMediaSessionManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                mMediaSessionManager.updatePlaybackState();
            if (mWorksPlayListener != null) mWorksPlayListener.onPlayStateChange(false, mPosition);
            FloatServiceManager.getInstance().floatViewPause();
        }

        @Override
        public void onComplete() {
            if (mWorksPlayListener != null) mWorksPlayListener.onPlayStateChange(false, mPosition);
            FloatServiceManager.getInstance().floatViewPause();
            if (mIsInitData) return;
            if (mIsLooping) {
                playSong(mPosition);
            } else {
                nextSong();
            }
        }

        @Override
        public void onProgress(long currentPosition, long duration) {
            if (mWorksPlayListener != null)
                mWorksPlayListener.onProgress(currentPosition, duration, mPosition);
        }

        @Override
        public void onBufferingUpdate(int percent) {
            if (percent >= 95) percent = 100;
            if (mWorksPlayListener != null)
                mWorksPlayListener.onBufferingUpdate(percent, mPosition);
        }
    }


    @Override
    public void onDestroy() {
        unRegisterReceiver();
        super.onDestroy();
    }


    private void registerReceiver() {
        registerReceiver(mNoisyReceiver, mFilter);
        //注册线控监听 5.0前后监控方式不同
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mMediaSessionManager = new MediaSessionManager(this);
        } else {
            mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            ComponentName name = new ComponentName(getPackageName(), EarphoneControlReceiver.class.getName());
            mAudioManager.registerMediaButtonEventReceiver(name);
        }
    }

    private void unRegisterReceiver() {
        unregisterReceiver(mNoisyReceiver);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mMediaSessionManager != null) mMediaSessionManager.release();
        } else {
            ComponentName name = new ComponentName(getPackageName(), EarphoneControlReceiver.class.getName());
            if (mAudioManager != null) mAudioManager.unregisterMediaButtonEventReceiver(name);
        }
    }

}
