package com.lmw.audiodemo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.lmw.audiodemo.manager.AlonePlayManager;
import com.lmw.ijkplayer.listener.AudioPlayListener;

public class AloneActivity extends AppCompatActivity {
    private static final String TAG = "AloneActivity";

    private String url = "https://oijmns1ch.qnssl.com/antiwork_today.mp3";

    private SeekBar seekBar;
    private Button btnPlayPause;
    private TextView tvTime;
    private MyAudioPlayListener myAudioPlayListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alone);
        seekBar = findViewById(R.id.seekBar);
        btnPlayPause = findViewById(R.id.btn_playOrPause);
        tvTime = findViewById(R.id.tv_time);
        myAudioPlayListener = new MyAudioPlayListener();

        AlonePlayManager.getInstance().playAlone(url, myAudioPlayListener);

        btnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlonePlayManager.getInstance().playPause();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AlonePlayManager.getInstance().seekTo(seekBar.getProgress());
            }
        });


    }

    @Override
    protected void onDestroy() {
        AlonePlayManager.getInstance().release();
        super.onDestroy();
    }

    private class MyAudioPlayListener implements AudioPlayListener {

        @Override
        public void onError(String msg) {
            Log.w(TAG, "onError  " + msg);
        }

        @Override
        public void onPreparing() {
            Log.w(TAG, "onPreparing");
        }

        @Override
        public void onPlaying() {
            Log.w(TAG, "onPlaying");
        }

        @Override
        public void onPause() {
            Log.w(TAG, "onPause");
        }

        @Override
        public void onComplete() {
            Log.w(TAG, "onComplete");
        }

        @Override
        public void onProgress(long currentPosition, long duration) {
            seekBar.setMax((int) duration);
            seekBar.setProgress((int) currentPosition);

            tvTime.setText(currentPosition + "/" + duration);
        }

        @Override
        public void onBufferingUpdate(int percent) {
            int max = seekBar.getMax();
            seekBar.setSecondaryProgress((int) Math.ceil(max * percent * 0.01));

        }
    }
}
