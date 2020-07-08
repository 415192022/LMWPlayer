package com.lmw.videodemo;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.lmw.ijkplayer.model.SwitchVideoModel;
import com.lmw.ijkplayer.video.SmartPickVideo;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;

import java.util.ArrayList;
import java.util.List;

public class AloneActivity extends AppCompatActivity {

    private String url_high = "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4";
    private String url_low = "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4";

    private String cover_url = "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4";

    private SmartPickVideo videoPlayer;
    private OrientationUtils orientationUtils;
    private List<SwitchVideoModel> videoModelList;
    private boolean audioPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alone);
        init();
    }


    private void init() {
        videoPlayer = findViewById(R.id.video_player);
        orientationUtils = new OrientationUtils(this, videoPlayer);

        //配置两种视频源
        videoModelList = new ArrayList<>();
        videoModelList.add(new SwitchVideoModel("标清", url_low));
        videoModelList.add(new SwitchVideoModel("超清", url_high));


        setPlayerData();
    }

    private void setPlayerData() {
        if (videoPlayer == null || videoModelList == null || videoModelList.isEmpty()) return;

        //配置视频
        videoPlayer.setUp(videoModelList, true, videoModelList.get(0).getName());

        //设置封面
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        Glide.with(this).load(cover_url).into(imageView);
        videoPlayer.setThumbImageView(imageView);

        //设置全屏
        videoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orientationUtils.resolveByClick();
                videoPlayer.startWindowFullscreen(AloneActivity.this, true, true);
            }
        });

        //是否可以滑动调整
        videoPlayer.setIsTouchWiget(true);

        //分辨率
        videoPlayer.getSwitchSize().setVisibility(videoModelList.size() > 1 ? View.VISIBLE : View.GONE);


        //设置返回按键功能
        videoPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //监听全屏恢复
        videoPlayer.setVideoAllCallBack(new GSYSampleCallBack() {
            @Override
            public void onQuitFullscreen(String url, Object... objects) {
                super.onQuitFullscreen(url, objects);
                if (orientationUtils != null) {
                    orientationUtils.backToProtVideo();
                }
            }
        });

        //自动播放
        if (audioPlay) {
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
        if (orientationUtils != null) {
            orientationUtils.backToProtVideo();
        }
        if (GSYVideoManager.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }
}
