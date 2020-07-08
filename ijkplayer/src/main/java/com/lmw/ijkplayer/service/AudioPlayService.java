package com.lmw.ijkplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.util.Patterns;

import androidx.annotation.Nullable;

import com.lmw.ijkplayer.listener.AudioPlayListener;
import com.lmw.ijkplayer.manager.AudioFocusManager;
import com.lmw.ijkplayer.model.PlayState;


import java.io.FileDescriptor;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class AudioPlayService extends Service implements IMediaPlayer.OnPreparedListener, IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnCompletionListener, IMediaPlayer.OnErrorListener, AudioPlay {
    private static final String TAG = "AudioPlayService";

    private IjkMediaPlayer mMediaPlayer;

    private AudioPlayListener mAudioPlayListener;

    private AudioFocusManager mAudioFocusManager;

    //保存暂停时播放进度
    private long mCurrentPlayPosition;

    //当前播放状态
    private PlayState mPlayState = PlayState.IDIE;

    //准备完成自动播放
    private boolean isAutoPlaying = true;

    //音量
    private float volume = 1.0f;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new AudioPlayBinder();
    }

    /**
     * 添加播放监听
     *
     * @param audioPlayListener
     */
    public void setAudioPlayListener(AudioPlayListener audioPlayListener) {
        mAudioPlayListener = audioPlayListener;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new IjkMediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);


        mAudioFocusManager = new AudioFocusManager(this);

    }

    /**
     * 准备完成
     *
     * @param iMediaPlayer
     */
    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        resume();
        //ijkplayer准备完成后会自动播放 无法控制 这里手动暂停控制
        //https://github.com/bilibili/ijkplayer/issues/4200
        if (!isAutoPlaying) pause();
    }

    /**
     * 更新缓存
     *
     * @param iMediaPlayer
     * @param percent
     */
    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
        if (mAudioPlayListener != null) mAudioPlayListener.onBufferingUpdate(percent);
    }

    /**
     * 播放完成
     *
     * @param iMediaPlayer
     */
    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        if (mAudioPlayListener != null) mAudioPlayListener.onComplete();
    }

    /**
     * 播放错误
     *
     * @param iMediaPlayer
     * @param what
     * @param extra
     * @return
     */
    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
        if (mAudioPlayListener != null) mAudioPlayListener.onError("MediaPlayer error " + what);
        return false;
    }

    /**
     * 添加播放Url
     *
     * @param path
     */
    @Override
    public void initDataSource(String path) {
        try {
            mCurrentPlayPosition = 0;
            if (mMediaPlayer == null) throw new Exception("MediaPlayer is null");
            mMediaPlayer.reset();
            if (Patterns.WEB_URL.matcher(path).matches()) {
                //uri
                mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(path));
            } else {
                //filePath
                mMediaPlayer.setDataSource(path);
            }
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepareAsync();
            mPlayState = PlayState.PREPARING;
            if (mAudioPlayListener != null) mAudioPlayListener.onPreparing();
        } catch (Exception e) {
            e.printStackTrace();
            mPlayState = PlayState.IDIE;
            if (mAudioPlayListener != null) mAudioPlayListener.onError(e.getMessage());
        }
    }

    /**
     * 播放本地音频
     * @param fileDescriptor
     */
    public void initDataSource(FileDescriptor fileDescriptor) {
        try {
            mCurrentPlayPosition = 0;
            if (mMediaPlayer == null) throw new Exception("MediaPlayer is null");
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(fileDescriptor);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.prepareAsync();
            mPlayState = PlayState.PREPARING;
            if (mAudioPlayListener != null) mAudioPlayListener.onPreparing();
        } catch (Exception e) {
            e.printStackTrace();
            mPlayState = PlayState.IDIE;
            if (mAudioPlayListener != null) mAudioPlayListener.onError(e.getMessage());
        }
    }

  @Override
    public void resume() {
        if (mMediaPlayer == null) return;
        if (!requestAudioFocus()) return;
        play();
    }

    /**
     * 播放
     */
    @Override
    public void play() {
        if (mMediaPlayer == null) return;
        mMediaPlayer.start();
        if (mCurrentPlayPosition > 0) {
            seekTo(mCurrentPlayPosition);
        }
        mPlayState = PlayState.PLAYING;
        startUpdatePlayProgress();
        if (mAudioPlayListener != null) mAudioPlayListener.onPlaying();
    }

    /**
     * 暂停
     */
    @Override
    public void pause() {
        if (mMediaPlayer == null) return;
        mCurrentPlayPosition = mMediaPlayer.getCurrentPosition();
        mMediaPlayer.pause();
        mPlayState = PlayState.PAUSE;
        stopUpdatePlayProgress();
        if (mAudioPlayListener != null) mAudioPlayListener.onPause();
    }

    /**
     * 播放或暂停
     */
    @Override
    public void playOrPause() {
        switch (mPlayState) {
            case IDIE:
                Log.w(TAG, "please initDataSource");
                break;
            case PAUSE:
                play();
                break;
            case PLAYING:
                pause();
                break;
            case PREPARING:
                resume();
                break;
        }
    }

    /**
     * 停止
     */
    @Override
    public void stop() {
        if (mMediaPlayer == null) return;
        mMediaPlayer.stop();
        mCurrentPlayPosition = 0;
        if (mAudioPlayListener != null) mAudioPlayListener.onComplete();
        abandonAudioFocus();
        mPlayState = PlayState.IDIE;
    }

    /**
     * 滑动进度
     *
     * @param position
     */
    @Override
    public void seekTo(long position) {
        if (mMediaPlayer == null) return;
        mCurrentPlayPosition = position;
        mMediaPlayer.seekTo(position);
    }

    /**
     * 重置
     */
    @Override
    public void reset() {
        if (mMediaPlayer == null) return;
        if (isPlaying()) {
            pause();
        }
        stop();
    }

    /**
     * 是否在播放
     *
     * @return
     */
    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    /**
     * 是否是准备状态
     *
     * @return
     */
    public boolean isPreparing() {
        return mMediaPlayer != null && mPlayState == PlayState.PREPARING;
    }

    /**
     * 是否是暂停状态
     *
     * @return
     */
    public boolean isPause() {
        return mMediaPlayer != null && mPlayState == PlayState.PAUSE;
    }


    /**
     * 设置是否自动播放
     *
     * @param autoPlaying
     */
    public void setAutoPlaying(boolean autoPlaying) {
        isAutoPlaying = autoPlaying;
    }

    /**
     * 设置是否循环播放
     *
     * @param looping
     */
//    public void setLooping(boolean looping) {
//        if (mMediaPlayer != null) mMediaPlayer.setLooping(looping);
//    }


    /**
     * 设置音量
     *
     * @param volume volume
     */
    public void setVolume(float volume, boolean save) {
        if (null == mMediaPlayer) return;
        if (save) this.volume = volume;
        mMediaPlayer.setVolume(volume, volume);
    }

    /**
     * 静音
     */
    public void mute() {
        if (null == mMediaPlayer) return;
        mMediaPlayer.setVolume(0, 0);
    }

    /**
     * 取消静音
     */
    public void unMute() {
        if (null == mMediaPlayer) return;
        mMediaPlayer.setVolume(volume, volume);
    }

    /**
     * 获取音乐时长
     *
     * @return
     */
    public long getDuration() {
        if (mMediaPlayer == null || mPlayState == PlayState.IDIE) return 0;
        return mMediaPlayer.getDuration();
    }

    /**
     * 获取当前播放位置
     *
     * @return
     */
    public long getCurrentPlayPosition() {
        if (mMediaPlayer == null || mPlayState == PlayState.IDIE) return 0;
        return mMediaPlayer.getCurrentPosition();
    }


    //------------------------------------------音乐焦点获取-----------------------------------------------

    /**
     * 获取焦点
     */
    private boolean requestAudioFocus() {
        if (mAudioFocusManager == null) return false;
        return mAudioFocusManager.requestAudioFocus();
    }

    /**
     * 放弃音乐焦点
     */
    private void abandonAudioFocus() {
        if (mAudioFocusManager == null) return;
        mAudioFocusManager.abandonAudioFocus();
    }
    //-----------------------------------------------------------------------------------------


    //-------------------------------------- 定时更新进度 -------------------------------------------
    private Handler mProgressHandler = null;
    private Runnable mProgressRunnable = new Runnable() {
        @Override
        public void run() {
            updatePlayProgress();
        }
    };

    private void startUpdatePlayProgress() {
        if (mProgressHandler == null)
            mProgressHandler = new Handler(Looper.getMainLooper());
        mProgressHandler.postDelayed(mProgressRunnable, 300L);
    }

    private void stopUpdatePlayProgress() {
        if (mProgressHandler != null)
            mProgressHandler.removeCallbacks(mProgressRunnable);
    }

    private void updatePlayProgress() {
        if (isPlaying() && mAudioPlayListener != null) {
            long currentPosition = mMediaPlayer.getCurrentPosition();
            long duration = mMediaPlayer.getDuration();
            mAudioPlayListener.onProgress(currentPosition, duration);
        }
        mProgressHandler.postDelayed(mProgressRunnable, 300L);
    }
    //-----------------------------------------------------------------------------------------


    @Override
    public void onDestroy() {
        removeListener();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        stopUpdatePlayProgress();
        mProgressHandler = null;
        super.onDestroy();
    }

    public void removeListener() {
        mAudioPlayListener = null;
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnErrorListener(null);
            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.setOnBufferingUpdateListener(null);
            mMediaPlayer.setOnPreparedListener(null);
        }
    }

    public class AudioPlayBinder extends Binder {
        public AudioPlayService getAudioPlayService() {
            return AudioPlayService.this;
        }
    }
}
