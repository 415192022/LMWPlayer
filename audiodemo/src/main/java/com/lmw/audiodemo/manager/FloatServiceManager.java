package com.lmw.audiodemo.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.view.View;

import com.lmw.audiodemo.CoreLib;
import com.lmw.audiodemo.service.FloatViewService;

public class FloatServiceManager {

    private static FloatServiceManager instance;

    private FloatViewService mService;
    private boolean mIsBound;
    private FloatServiceConnection mConnection;

    private FloatServiceManager() {
    }

    public static FloatServiceManager getInstance() {
        if (instance == null) {
            synchronized (FloatServiceManager.class) {
                if (instance == null) {
                    instance = new FloatServiceManager();
                }
            }
        }
        return instance;
    }

    private class FloatServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = ((FloatViewService.FloatServiceBinder) service).getFloatViewService();
            mService.setFloatViewClickListener(new FloatViewService.FloatViewClickListener() {
                @Override
                public void onClick(View floatView) {
                    WorksPlayManager.getInstance().clickFloatView();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    public void startService() {
        if (!mIsBound || mService == null) {
            Intent intent = new Intent(CoreLib.instance.getContext(), FloatViewService.class);
            mConnection = new FloatServiceConnection();
            mIsBound = CoreLib.instance.getContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void stopService() {
        if (mIsBound && mService != null) {
            mService.floatViewPause();
            mService.hideFloatView();
            CoreLib.instance.getContext().unbindService(mConnection);
            mConnection = null;
            mService = null;
            mIsBound = false;
        }
    }

    /**
     * 显示浮标
     */
    public void showFloatView() {
        if (!mIsBound || mService == null || !WorksPlayManager.getInstance().isInitData()) return;
        mService.showFloatView();
    }

    /**
     * 隐藏浮标
     */
    public void hideFloatView() {
        if (!mIsBound || mService == null) return;
        mService.hideFloatView();
    }

    /**
     * 浮标是否显示
     * @return
     */
    public boolean isShowFloatView() {
        return mIsBound && mService != null && mService.isShowFloatView();
    }

    /**
     * 切换浮标布局内图片
     * @param picUrl
     */
    public void floatViewPlay(String picUrl) {
        if (!mIsBound || mService == null) return;
        mService.floatViewPlay(picUrl);
    }

    /**
     * 浮标布局暂停状态
     */
    public void floatViewPause() {
        if (!mIsBound || mService == null) return;
        mService.floatViewPause();
    }
}
