package com.lmw.lmwplayer.audio;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.lmw.lmwplayer.R;
import com.shuyu.gsyvideoplayer.utils.NetworkUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class AudioPlayService extends FloatViewService implements
        IMediaPlayer.OnPreparedListener, IMediaPlayer.OnCompletionListener
        , IMediaPlayer.OnErrorListener, IMediaPlayer.OnBufferingUpdateListener {

    //MediaPlayer
    private IjkMediaPlayer mMediaPlayer;
    //音频管理对象
    private AudioManager mAudioManager;
    //浮窗View
    private View mFloatView;
    //Handler
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化MediaPlayer,设置监听事件
        mMediaPlayer = new IjkMediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnBufferingUpdateListener(this);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        //初始化音频管理对象
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        startSendProgress();
    }

    @Override
    public void onDestroy() {
        //释放资源
        if (null != mMediaPlayer) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        stopSendProgress();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        removeAllListener();
        return super.onUnbind(intent);
    }

    @Override
    protected View getFloatView() {
        mFloatView = LayoutInflater.from(getApplication()).inflate(R.layout.view_audio_float_view, null);
        setFloatClickListener();
        return mFloatView;
    }

    @Override
    protected void onClose() {
        pause();
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        //准备加载的时候
        resume();
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
        updateBuffering(percent);
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        if (mMediaPlayer.isLooping()) {
            return;
        }
        if (!NetworkUtils.isAvailable(getApplication())) {
            pause();
            return;
        }
        nextSong();
    }

    @Override
    public boolean onError(IMediaPlayer iMediaPlayer, int what, int extra) {
        return false;
    }

    private void floatViewPlay(String picUrl) {
        if (mFloatView == null) return;
        ImageView ivPic = mFloatView.findViewById(R.id.ivPic);
        ImageView ivAnim = mFloatView.findViewById(R.id.ivAnim);
        ImageView ivPause = mFloatView.findViewById(R.id.ivPause);
        ivAnim.setVisibility(View.VISIBLE);
        ivPause.setVisibility(View.GONE);
        //Glide.with(getApplication()).load(picUrl).into(ivPic);
        ivAnim.setImageResource(R.drawable.audio_view_anim);
        AnimationDrawable animationDrawable = (AnimationDrawable) ivAnim.getDrawable();
        animationDrawable.start();
    }

    private void floatViewPause() {
        if (mFloatView == null) return;
        ImageView ivAnim = mFloatView.findViewById(R.id.ivAnim);
        ImageView ivPause = mFloatView.findViewById(R.id.ivPause);
        ivAnim.setVisibility(View.GONE);
        ivPause.setVisibility(View.VISIBLE);
    }

    private void setFloatClickListener() {
        setFloatViewClickListener(new FloatViewClickListener() {
            @Override
            public void onClick(View floatView) {
            }
        });
    }

    //-------------------------------------播放相关----------------------------------------

    //音乐列表
    private List<MusicModel> musicsList = new ArrayList<>();
    private int musicsListSize;
    private MusicModel model = new MusicModel();
    //播放的位置
    private int position = 0;
    //记录音量
    private float volume = 1;
    //记录当前播放时间
    private long currentTime = 0;
    //是否不在播放状态
    private boolean isNotPlaying = true;

    private void initPlay(List<MusicModel> list) {
        if (!equalList(musicsList, list)) {
            musicsList.clear();
            musicsList.addAll(list);
            musicsListSize = musicsList.size();
            mMediaPlayer.reset();
            currentTime = 0;
            position = 0;
        }
    }

    private boolean equalList(List<MusicModel> list1, List<MusicModel> list2) {
        if (list1.size() != list2.size()) return false;
        for (int i = 0; i < list1.size(); i++) {
            if (list1.get(i).id != list2.get(i).id) return false;
        }
        return true;
    }

    /**
     * 播放
     */
    private void playSong(int newPosition) {
        if (null == musicsList || musicsList.size() == 0) return;
        //由滑动操作传递过来的歌曲position，如果跟当前的播放的不同的话，就将MediaPlayer重置
        if (position != newPosition && newPosition < musicsListSize) {
            mMediaPlayer.reset();
            currentTime = 0;
            position = newPosition;
        }
        if (null != musicsList && 0 < musicsList.size()) model = musicsList.get(position);
        //currentTime>0并且不在播放状态，直接播放
        if (currentTime > 0 && isNotPlaying) {
            resume();
        }
        if (currentTime <= 0) {
            if (null != model) {
                updateMusicModel(model, position);
                play(model.url);
            }
        }
    }

    /**
     * 下一首
     */
    private void nextSong() {
        currentTime = 0;
        if (position < 0) {
            position = 0;
        }
        if (musicsListSize > 0) {
            position++;
            if (position < musicsListSize) {//当前歌曲的索引小于歌曲集合的长度
                model = musicsList.get(position);
            } else {
                position = 0;
                model = musicsList.get(position);
            }
            updateMusicModel(model, position);
            play(model.url);
        }
    }

    /**
     * 上一首
     */
    private void preSong() {
        currentTime = 0;
        if (position < 0) {
            position = 0;
        }
        if (musicsListSize > 0) {
            position--;
            if (position >= 0) {//大于等于0的情况
                model = musicsList.get(position);
            } else {
                model = musicsList.get(0);//小于0时，播放第一首歌
            }
            updateMusicModel(model, position);
            play(model.url);
        }
    }

    /**
     * 音乐播放
     *
     * @param musicUrl musicUrl
     */
    private void play(String musicUrl) {
        if (null == mMediaPlayer) return;
        mMediaPlayer.reset();//停止音乐后，不重置的话就会崩溃
        try {
            if (Patterns.WEB_URL.matcher(musicUrl).matches()) {
                //uri
                mMediaPlayer.setDataSource(getApplicationContext(), Uri.parse(musicUrl));
            } else {
                //filePath
                mMediaPlayer.setDataSource(musicUrl);
            }
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 音乐暂停
     */
    private void pause() {
        if (null == mMediaPlayer) return;
        mMediaPlayer.pause();
        isNotPlaying = true;
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                updatePlayState();
                floatViewPause();
            }
        });
    }

    /**
     * 音乐继续播放
     */
    private void resume() {
        if (null == mMediaPlayer) return;
        //请求音频焦点
        requestAudioFocus();
        mMediaPlayer.start();
        isNotPlaying = false;
        if (currentTime > 0) {
            mMediaPlayer.seekTo(currentTime);
        }
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                updatePlayState();
                floatViewPlay(model.coverUrl);
            }
        });
    }

    /**
     * 停止音乐
     */
    private void stop() {
        if (null == mMediaPlayer) return;
        mMediaPlayer.stop();
        isNotPlaying = true;
        currentTime = 0;//停止音乐，将当前播放时间置为0
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                updatePlayState();
                floatViewPause();
            }
        });
    }

    /**
     * 设置音量
     *
     * @param volume volume
     */
    private void setVolume(float volume) {
        if (null == mMediaPlayer) return;
        this.volume = volume;
        mMediaPlayer.setVolume(volume, volume);
    }

    /**
     * 静音
     */
    private void mute() {
        if (null == mMediaPlayer) return;
        mMediaPlayer.setVolume(0, 0);
    }

    /**
     * 静音
     */
    private void unMute() {
        if (null == mMediaPlayer) return;
        mMediaPlayer.setVolume(volume, volume);
    }

    private void setLooping(boolean looping) {
        if (null == mMediaPlayer) return;
        mMediaPlayer.setLooping(looping);
    }

    /**
     * 进度
     */
    private void seekTo(long msec) {
        if (null == mMediaPlayer) return;
        mMediaPlayer.seekTo(msec);
    }

    private long getCurrentPosition() {
        if (null == mMediaPlayer) return 0;
        return mMediaPlayer.getCurrentPosition();
    }

    private long getDuration() {
        if (null == mMediaPlayer) return 0;
        return mMediaPlayer.getDuration();
    }

    private boolean isPlaying() {
        return null != mMediaPlayer && mMediaPlayer.isPlaying();
    }

    /**
     * 是否在播放
     * （不同于isPlaying()，isNowPlaying是同步即时的，是根据用户的操作逻辑产生的）
     */
    private boolean isNowPlaying() {
        return null != mMediaPlayer && !isNotPlaying;
    }


    //-------------------------------------进度相关------------------------------------------

    //定时发送进度
    private Handler mHandler = null;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            updateSeekBarProgress();
            mHandler.postDelayed(this, 1000);
        }
    };

    private void startSendProgress() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        mHandler.postDelayed(mRunnable, 1000);
    }

    private void stopSendProgress() {
        mHandler.removeCallbacks(mRunnable);
        mHandler = null;
    }

    //-------------------------------------焦点相关------------------------------------------

    //音频焦点监听处理
    AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    //获取音频焦点
                    resume();
                    setVolume(volume);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    //永久失去 音频焦点
                    pause();
                    abandonFocus();//放弃音频焦点
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    //暂时失去 音频焦点，并会很快再次获得。必须停止Audio的播放，但是因为可能会很快再次获得AudioFocus，这里可以不释放Media资源
                    pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    //暂时失去 音频焦点 ，但是可以继续播放，不过要在降低音量。
                    setVolume(volume / 3);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 申请音频焦点
     */
    private void requestAudioFocus() {
        if (null != onAudioFocusChangeListener) {
            int result = mAudioManager.requestAudioFocus(onAudioFocusChangeListener
                    , AudioManager.STREAM_MUSIC //STREAM_xxx，在AudioStream的裁决机制中并未有什么实际意义
                    , AudioManager.AUDIOFOCUS_GAIN);
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                //请求音频焦点成功
            } else if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                //请求音频焦点失败
            }
        }
    }

    /**
     * 放弃音频焦点
     */
    private void abandonFocus() {
        if (null != onAudioFocusChangeListener) {
            mAudioManager.abandonAudioFocus(onAudioFocusChangeListener);
        }
    }

    //-------------------------------------IBinder------------------------------------------

    public class PlayBinder extends Binder {

        public void showFloatView() {
            AudioPlayService.this.showFloatView();
        }

        public void hideFloatView() {
            AudioPlayService.this.hideFloatView();
        }

        public boolean isShowFloatView() {
            return AudioPlayService.this.isShowFloatView();
        }

        public void initPlay(List<MusicModel> list) {
            AudioPlayService.this.initPlay(list);
        }

        public void playSong(int position) {
            AudioPlayService.this.playSong(position);
        }

        public void play(String musicUrl) {
            AudioPlayService.this.play(musicUrl);
        }

        public void pause() {
            AudioPlayService.this.pause();
        }

        public void resume() {
            AudioPlayService.this.resume();
        }

        public void stop() {
            AudioPlayService.this.stop();
        }

        public void setLooping(boolean looping) {
            AudioPlayService.this.setLooping(looping);
        }

        public void seekTo(long msec) {
            AudioPlayService.this.seekTo(msec);
        }

        public void abandonFocus() {
            AudioPlayService.this.abandonFocus();
        }

        public long getCurrentPosition() {
            return AudioPlayService.this.getCurrentPosition();
        }

        public long getDuration() {
            return AudioPlayService.this.getDuration();
        }

        public boolean isPlaying() {
            return AudioPlayService.this.isPlaying();
        }

        public boolean isNowPlaying() {
            return AudioPlayService.this.isNowPlaying();
        }

        public void setPlayListener(PlayListener playListener) {
            AudioPlayService.this.setPlayListener(playListener);
        }
    }

    //-------------------------------------CallBack------------------------------------------

    private PlayListener playListener;

    public interface PlayListener {
        void onProgress(long currentPosition, long duration, int position);

        void onBufferingUpdate(int percent, int position);

        void onAudioChange(MusicModel model, int position);

        void onStateChange(boolean isNowPlaying, int position);
    }

    public void setPlayListener(final PlayListener playListener) {
        this.playListener = playListener;
    }

    public void removeAllListener() {
        this.playListener = null;
        this.floatViewClickListener = null;
    }

    /**
     * 通知缓冲进度
     */
    private void updateBuffering(int percent) {
        if (playListener != null) {
            playListener.onBufferingUpdate(percent, position);
        }
    }

    /**
     * 通知播放状态
     */
    private void updatePlayState() {
        if (playListener != null && mMediaPlayer != null) {
            playListener.onStateChange(isNowPlaying(), position);
        }
    }

    /**
     * 通知换歌
     *
     * @param model    model
     * @param position position
     */
    private void updateMusicModel(MusicModel model, int position) {
        if (playListener != null) {
            playListener.onAudioChange(model, position);
        }
    }

    /**
     * 通知进度条进度
     */
    private void updateSeekBarProgress() {
        if (playListener != null && mMediaPlayer != null) {
            currentTime = mMediaPlayer.getCurrentPosition();
            long duration = mMediaPlayer.getDuration();
            playListener.onProgress(currentTime, duration, position);
        }
    }
}