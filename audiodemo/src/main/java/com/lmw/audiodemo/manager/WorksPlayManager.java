package com.lmw.audiodemo.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.lmw.audiodemo.CoreLib;
import com.lmw.audiodemo.listener.WorksPlayListener;
import com.lmw.audiodemo.model.Works;
import com.lmw.audiodemo.service.WorksPlayService;

import java.util.List;

public class WorksPlayManager {

    private static final String TAG = "WorksPlayManager";

    private static WorksPlayManager instance;

    private WorksPlayService mPlayService;
    private boolean mIsBound;
    private WorksServiceConnection mConnection;


    private WorksPlayManager() {
    }

    public static WorksPlayManager getInstance() {
        if (instance == null) {
            synchronized (WorksPlayManager.class) {
                if (instance == null) {
                    instance = new WorksPlayManager();
                }
            }
        }
        return instance;
    }

    public void setWorksPlayListener(WorksPlayListener worksPlayListener) {
        if (checkNull()) return;
        mPlayService.setWorksPlayListener(worksPlayListener);
    }

    public void setListData(List<Works> worksList) {
        if (checkNull()) return;
        if (!equalList(mPlayService.getWorksList(), worksList)) {
            mPlayService.initWorksList(worksList);
        }
    }

    public void setLooping(boolean looping) {
        if (checkNull()) return;
        mPlayService.setLooping(looping);
    }

    public boolean isLooping() {
        return !checkNull() && mPlayService.isLooping();
    }

    public void playSong(int position) {
        if (checkNull()) return;
        mPlayService.playSong(position);
    }

    public void pause() {
        if (checkNull()) return;
        mPlayService.pause();
    }

    public void playOrPause() {
        if (checkNull()) return;
        mPlayService.playOrPause();
    }

    public boolean isPlaying() {
        return !checkNull() && mPlayService.isPlaying();
    }

    public void nextSong() {
        if (checkNull()) return;
        mPlayService.nextSong();
    }

    public void preSong() {
        if (checkNull()) return;
        mPlayService.preSong();
    }

    public void seekTo(long l) {
        if (checkNull()) return;
        mPlayService.seekTo(l);
    }

    public boolean isInitData() {
        return !checkNull() && mPlayService.getWorksList().size() > 0;
    }

    public void resume() {
        if (checkNull()) return;
        mPlayService.resume();
    }

    private boolean equalList(List<Works> list1, List<Works> list2) {
        if (list1.size() != list2.size()) return false;
        for (int i = 0; i < list1.size(); i++) {
            if (list1.get(i).opusId != list2.get(i).opusId) return false;
        }
        return true;
    }


    public void startService() {
        if (!mIsBound || mPlayService == null) {
            Intent intent = new Intent(CoreLib.instance.getContext(), WorksPlayService.class);
            mConnection = new WorksServiceConnection();
            mIsBound = CoreLib.instance.getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void stopService() {
        if (mIsBound && mPlayService != null) {
            if (mPlayService.isPlaying()) {
                mPlayService.pause();
                mPlayService.stop();
            }
            CoreLib.instance.getContext().unbindService(mConnection);
            mConnection = null;
            mPlayService = null;
            mIsBound = false;
        }
    }

    public void clickFloatView(){
        if(checkNull()) return;
        Log.e(TAG,"点击浮标按钮");
    }


    private class WorksServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayService = ((WorksPlayService.WorksPlayBinder) service).getWorksPlayService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private boolean checkNull() {
        return !mIsBound || mPlayService == null;
    }

}
