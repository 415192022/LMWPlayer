package com.lmw.lmwplayer;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;

import com.lmw.ijkplayer.PlayPickActivity;
import com.lmw.ijkplayer.model.SwitchVideoModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void playVideo(View view) {
        String source1 = "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4";
        String name1 = "普通";
        SwitchVideoModel switchVideoModel = new SwitchVideoModel(name1, source1);

        String source2 = "http://vfx.mtime.cn/Video/2019/03/19/mp4/190319212559089721.mp4";
        String name2 = "清晰";
        SwitchVideoModel switchVideoModel2 = new SwitchVideoModel(name2, source2);

        goToVideoPickPlayer(this, view, switchVideoModel, switchVideoModel2);
    }

    public void playAudio(View view) {
        Intent intent = new Intent(this, com.lmw.lmwplayer.MusicActivity.class);
        startActivity(intent);
    }


    /**
     * 跳转到视频播放
     *
     * @param activity activity
     * @param view     view
     */
    public static void goToVideoPickPlayer(Activity activity, View view, SwitchVideoModel... model) {
        Intent intent = new Intent(activity, PlayPickActivity.class);
        intent.putExtra(PlayPickActivity.VIDEO_COVER, "http://img.hb.aicdn.com/5c5bc9303fd409d4f7c54dd835a9d9afab361849109d66-lFqkP3_fw658");
        intent.putExtra(PlayPickActivity.VIDEO_CACHE_WITH_PLAY, false);
        intent.putParcelableArrayListExtra(PlayPickActivity.VIDEO_URL, new ArrayList<>(Arrays.asList(model)));

        Pair pair = new Pair<>(view, PlayPickActivity.IMG_TRANSITION);
        ActivityOptionsCompat activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity, pair);
        ActivityCompat.startActivity(activity, intent, activityOptions.toBundle());
    }

    private void playVideo() {
        //PlayerView video_view = findViewById(R.id.video_view);
        //video_view.setVideoPath("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
        //video_view.start();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"), "video/mp4");
        startActivity(intent);
    }

    private void playMusic() {
        try {
            IjkMediaPlayer ijkMediaPlayer1 = new IjkMediaPlayer();
            ijkMediaPlayer1.setDataSource(this, Uri.parse("https://oijmns1ch.qnssl.com/antiwork_today.mp3"));
            ijkMediaPlayer1.setAudioStreamType(AudioManager.STREAM_MUSIC);
            ijkMediaPlayer1.prepareAsync();
            ijkMediaPlayer1.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(IMediaPlayer iMediaPlayer) {
                    Log.d("lmw", "onSeekComplete");
                }
            });
            ijkMediaPlayer1.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(IMediaPlayer iMediaPlayer) {
                    iMediaPlayer.release();
                    Log.d("lmw", "onCompletion");
                }
            });
            ijkMediaPlayer1.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                    Log.d("lmw", "Error = " + i + "----" + i1);
                    return false;
                }
            });
            ijkMediaPlayer1.setOnBufferingUpdateListener(new IMediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int i) {
                    Log.d("lmw", "Buffer = " + i);
                }
            });
            ijkMediaPlayer1.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(IMediaPlayer iMediaPlayer) {
                    Log.d("lmw", "onPrepared");
                    iMediaPlayer.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
