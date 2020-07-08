package com.lmw.audiodemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

import com.lmw.audiodemo.manager.AlonePlayManager;
import com.lmw.audiodemo.manager.WorksPlayManager;


/**
 * 来电/耳机拔出时暂停播放
 * 其实这个跟通知处理逻辑一样
 */
public class AudioEarPhoneReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action != null && action.length() > 0) {
            switch (action) {
                //来电/耳机拔出时暂停播放
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    AlonePlayManager.getInstance().pause();
                    WorksPlayManager.getInstance().pause();
                    break;
                default:
                    break;
            }
        }
    }


}
