package com.lmw.audiodemo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.lmw.audiodemo.manager.WorksPlayManager;


public class EarphoneControlReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event == null || event.getAction() != KeyEvent.ACTION_UP) {
            return;
        }
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            case KeyEvent.KEYCODE_HEADSETHOOK:
                WorksPlayManager.getInstance().playOrPause();
                break;
            case KeyEvent.KEYCODE_MEDIA_NEXT:
                WorksPlayManager.getInstance().nextSong();
                break;
            case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                WorksPlayManager.getInstance().preSong();
                break;
            default:
                break;
        }
    }
}
