package com.lmw.lmwplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lmw.lmwplayer.R;
import com.lmw.lmwplayer.audio.AudioPlayService;
import com.lmw.lmwplayer.audio.MusicModel;

public class MusicActivity extends AppCompatActivity {

    private AudioPlayService.PlayBinder mBinder;
    private AudioConnection mConnection;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        Intent intent = new Intent(this, AudioPlayService.class);
        startService(intent);
        bindService();

        findViewById(R.id.btn_float).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askForPermission();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (mBinder != null) mBinder.hideFloatView();
        unbindService(mConnection);
        Intent intent = new Intent(this, AudioPlayService.class);
        stopService(intent);
        super.onDestroy();
    }

    private void bindService() {
        mConnection = new AudioConnection();
        Intent intent = new Intent(this, AudioPlayService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);
    }

    private class AudioConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (AudioPlayService.PlayBinder) service;
            mBinder.setPlayListener(new AudioPlayService.PlayListener() {
                @Override
                public void onProgress(long currentPosition, long duration, int position) {
                    Log.d("lmw", "currentPosition " + currentPosition + "-----duration" + duration);
                }

                @Override
                public void onBufferingUpdate(int percent, int position) {
                    Log.d("lmw", "percent " + percent);
                }

                @Override
                public void onAudioChange(MusicModel model, int position) {

                }


                @Override
                public void onStateChange(boolean isNowPlaying, int position) {
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder = null;
        }
    }

    private final int OVERLAY_PERMISSION_REQ_CODE = 0x0001;

    /**
     * 请求用户给予悬浮窗的权限
     */
    private void askForPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                    , Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
        } else {
            if (mBinder != null) {
                mBinder.showFloatView();
                mBinder.play("https://oijmns1ch.qnssl.com/antiwork_today.mp3");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "权限授予失败，无法开启悬浮窗", Toast.LENGTH_SHORT).show();
            } else {
                if (mBinder != null) {
                    mBinder.showFloatView();
                    mBinder.play("https://oijmns1ch.qnssl.com/antiwork_today.mp3");
                }
            }
        }
    }
}
