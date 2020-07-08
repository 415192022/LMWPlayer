package com.lmw.ijkplayer.video;

import android.content.Context;
import android.util.AttributeSet;

import com.lmw.ijkplayer.R;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

/**
 * 简单ui的播放
 */
public class SimpleControlVideo extends StandardGSYVideoPlayer {


    public SimpleControlVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public SimpleControlVideo(Context context) {
        super(context);
    }

    public SimpleControlVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.simple_control_video;
    }

    @Override
    protected void touchSurfaceMoveFullLogic(float absDeltaX, float absDeltaY) {
        super.touchSurfaceMoveFullLogic(absDeltaX, absDeltaY);
        //不给触摸快进，如果需要，屏蔽下方代码即可
        mChangePosition = false;

        //不给触摸音量，如果需要，屏蔽下方代码即可
        mChangeVolume = false;

        //不给触摸亮度，如果需要，屏蔽下方代码即可
        mBrightness = false;
    }

//    @Override
//    protected void touchDoubleUp() {
//        //super.touchDoubleUp();
//        //不需要双击暂停
//    }

    @Override
    protected void changeUiToPlayingShow() {
        super.changeUiToPlayingShow();
        if (mListener != null) {
            mListener.onPlay();
        }
    }

    @Override
    protected void changeUiToPauseShow() {
        super.changeUiToPauseShow();
        if (mListener != null) {
            mListener.onPause();
        }
    }

    @Override
    protected void changeUiToCompleteShow() {
        super.changeUiToCompleteShow();
        if (mListener != null) {
            mListener.onComplete();
        }
    }

    public void setMute(boolean on) {
        if (on) {
            GSYVideoManager.instance().setNeedMute(true);
        } else {
            GSYVideoManager.instance().setNeedMute(false);
        }
    }

    public boolean isMute() {
        return GSYVideoManager.instance().isNeedMute();
    }

    //--------------------------------------Listener------------------------------------------------

    public ChangeStateListener mListener;

    public void setChangeStateListener(ChangeStateListener listener) {
        mListener = listener;
    }

    public interface ChangeStateListener {
        void onPlay();

        void onPause();

        void onComplete();
    }
}
