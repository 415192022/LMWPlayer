package com.lmw.ijkplayer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Transition;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.bumptech.glide.Glide;
import com.lmw.ijkplayer.R;
import com.lmw.ijkplayer.listener.OnTransitionListener;
import com.lmw.ijkplayer.model.SwitchVideoModel;
import com.lmw.ijkplayer.video.SmartPickVideo;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;

import java.util.ArrayList;


/**
 * 单独的视频播放页面
 */
public class PlayPickActivity extends AppCompatActivity {

    public final static String VIDEO_URL = "video_url";
    public final static String VIDEO_TITLE = "video_title";
    public final static String VIDEO_COVER = "video_cover";
    public final static String VIDEO_AUTO_PLAY = "video_auto_play";
    public final static String VIDEO_CACHE_WITH_PLAY = "video_cache_with_play";
    public final static String IMG_TRANSITION = "img_transition";

    SmartPickVideo videoPlayer;
    OrientationUtils orientationUtils;
    private ArrayList<SwitchVideoModel> mVideoList = new ArrayList<>();
    private String mTitle;
    private String mCover;
    private boolean mAutoPlay;
    private boolean mCacheWithPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_pick);
        hindBar();
        videoPlayer = findViewById(R.id.video_player);
        mTitle = getIntent().getStringExtra(VIDEO_TITLE);
        mCover = getIntent().getStringExtra(VIDEO_COVER);
        mAutoPlay = getIntent().getBooleanExtra(VIDEO_AUTO_PLAY, false);
        mCacheWithPlay = getIntent().getBooleanExtra(VIDEO_CACHE_WITH_PLAY, true);
        mVideoList = getIntent().getParcelableArrayListExtra(VIDEO_URL);
        init();
    }

    private void init() {
        videoPlayer.setUp(mVideoList, mCacheWithPlay, mTitle);

        //增加封面
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        Glide.with(this).load(mCover).into(imageView);
        videoPlayer.setThumbImageView(imageView);

        //增加title
        videoPlayer.getTitleTextView().setVisibility(View.VISIBLE);

        //设置返回键
        videoPlayer.getBackButton().setVisibility(View.VISIBLE);

        //分辨率切换
        videoPlayer.getSwitchSize().setVisibility(mVideoList.size() > 1 ? View.VISIBLE : View.GONE);

        //设置旋转
        orientationUtils = new OrientationUtils(this, videoPlayer);

        //设置全屏按键功能,这是使用的是选择屏幕，而不是全屏
        videoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orientationUtils.resolveByClick();
            }
        });

        //是否可以滑动调整
        videoPlayer.setIsTouchWiget(true);

        //设置返回按键功能
        videoPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //过渡动画
        initTransition();

        if (mAutoPlay) {
            videoPlayer.startPlayLogic();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        videoPlayer.onVideoPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        videoPlayer.onVideoResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }

    @Override
    public void onBackPressed() {
        //先返回正常状态
        if (orientationUtils.getScreenType() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            videoPlayer.getFullscreenButton().performClick();
            return;
        }
        //释放所有
        videoPlayer.setVideoAllCallBack(null);
        GSYVideoManager.releaseAllVideos();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.onBackPressed();
        } else {
            new Handler().postDelayed(new Runnable() {
                @SuppressLint("PrivateResource")
                @Override
                public void run() {
                    finish();
                    overridePendingTransition(R.anim.abc_fade_in, R.anim.abc_fade_out);
                }
            }, 500);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        hindBar();
    }

    private void initTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
            ViewCompat.setTransitionName(videoPlayer, IMG_TRANSITION);
            startPostponedEnterTransition();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean addTransitionListener() {
        Transition transition = getWindow().getSharedElementEnterTransition();
        if (transition != null) {
            transition.addListener(new OnTransitionListener() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    videoPlayer.startPlayLogic();
                    transition.removeListener(this);
                }
            });
            return true;
        }
        return false;
    }

    private void hindBar() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) supportActionBar.hide();
    }
}
