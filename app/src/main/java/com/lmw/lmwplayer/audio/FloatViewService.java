package com.lmw.lmwplayer.audio;

import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.lmw.lmwplayer.SPUtils;

public abstract class FloatViewService extends Service {

    private WindowManager.LayoutParams mParams;
    private WindowManager mWindowManager;
    private View mPhoneLayout;

    private int mScreenWidth;
    private int mScreenHeight;
    private float lastX = 0f;
    private float lastY = 0f;
    private float originX = 0f;
    private float originY = 0f;
    private int mMinX;
    private int mMaxX;
    private int mMinY;
    private int mMaxY;
    private int mGap = 20;
    private int mFloatWidth;

    private String mXKey = this.getClass().getSimpleName() + "-x";
    private String mYKey = this.getClass().getSimpleName() + "-y";

    @Override
    public void onCreate() {
        super.onCreate();
        mPhoneLayout = getFloatView();
        mPhoneLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        setFloatViewClick();
    }

    public void showFloatView() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            return;
        }
        if (isShowFloatView()) {
            return;
        }
        mWindowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        hideFloatView();
        if (mWindowManager != null) {
            mScreenWidth = mWindowManager.getDefaultDisplay().getWidth();
            mScreenHeight = mWindowManager.getDefaultDisplay().getHeight();
        }
        mParams = new WindowManager.LayoutParams();
        //type & flag
        mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            mParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        mParams.format = PixelFormat.RGBA_8888;
        mParams.gravity = Gravity.START | Gravity.TOP;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        if (mPhoneLayout == null) {
            mPhoneLayout = getFloatView();
            mPhoneLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        }
        mFloatWidth = mPhoneLayout.getMeasuredWidth();
        mMinX = mGap;
        mMaxX = mScreenWidth - mFloatWidth - mGap;
        mMinY = 0;
        mMaxY = mScreenHeight - mPhoneLayout.getMeasuredHeight();
        mParams.x = SPUtils.getInstance(this).getInt(mXKey, mMaxX);
        mParams.y = SPUtils.getInstance(this).getInt(mYKey, mMaxY - 200);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {
            mWindowManager.addView(mPhoneLayout, mParams);
        }

        mPhoneLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v == null || event == null || mParams == null) {
                    return false;
                }
                int action = event.getAction();
                boolean isIntercept = false;
                if (action == MotionEvent.ACTION_DOWN) {
                    lastX = event.getRawX();
                    lastY = event.getRawY();
                    originX = lastX;
                    originY = lastY;
                    stopAdjust();
                } else if (action == MotionEvent.ACTION_MOVE) {
                    float dx = event.getRawX() - lastX;
                    float dy = event.getRawY() - lastY;
                    int x = Math.round(mParams.x + dx);
                    int y = Math.round(mParams.y + dy);
                    if (x >= mMinX - mGap && y >= mMinY && x <= mMaxX + mGap && y <= mMaxY) {
                        mParams.x = x;
                        mParams.y = y;
                        mWindowManager.updateViewLayout(mPhoneLayout, mParams);
                        //右边缘手势
                        if (x > mMaxX && x <= mMaxX + mGap) {
                            postClose();
                        } else {
                            resetClose();
                        }
                    }
                    lastX = event.getRawX();
                    lastY = event.getRawY();
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    resetClose();
                    float lastMoveX = Math.abs(event.getRawX() - originX);
                    float lastMoveY = Math.abs(event.getRawY() - originY);
                    isIntercept = !(lastMoveX < 10 && lastMoveY < 10);
                    startAdjust();
                }
                return isIntercept;
            }
        });
    }

    public void hideFloatView() {
        if (isShowFloatView()) {
            mWindowManager.removeView(mPhoneLayout);
        }
    }

    public boolean isShowFloatView() {
        return mPhoneLayout != null && mPhoneLayout.getParent() != null;
    }

    //-------------------------------------贴边相关------------------------------------------

    private Handler mHandler = null;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            int floatLeft = mParams.x;
            int floatMid = floatLeft + mFloatWidth / 2;
            if (floatLeft < mMinX) {
                mParams.x += 20;
            } else if (floatLeft > mMaxX) {
                mParams.x -= 20;
            } else if (floatMid < mScreenWidth / 2) {
                mParams.x -= 20;
            } else if (floatMid >= mScreenWidth / 2) {
                mParams.x += 20;
            }
            if (mParams.x >= mMinX - 20 && mParams.x <= mMinX + 20) {
                mParams.x = mMinX;
            } else if (mParams.x >= mMaxX - 20 && mParams.x <= mMaxX + 20) {
                mParams.x = mMaxX;
            }
            if (isShowFloatView()) {
                mWindowManager.updateViewLayout(mPhoneLayout, mParams);
            }

            if (mParams.x == mMinX || mParams.x == mMaxX) {
                stopAdjust();
                //记录XY轴
                SPUtils.getInstance(FloatViewService.this).put(mXKey, mParams.x);
                SPUtils.getInstance(FloatViewService.this).put(mYKey, mParams.y);
            } else {
                mHandler.postDelayed(this, 5);
            }
        }
    };

    private void startAdjust() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        mHandler.post(mRunnable);
    }

    private void stopAdjust() {
        if (mHandler != null) {
            mHandler.removeCallbacks(mRunnable);
            mHandler = null;
        }
    }


    //-----------------------------------贴边关闭相关------------------------------------------

    private void postClose() {
        if (mPhoneLayout.getAlpha() == 1) {
            mPhoneLayout.setAlpha(0.5f);
            mPhoneLayout.postDelayed(closeRunnable, 1000);
        }
    }

    private void resetClose() {
        if (mPhoneLayout.getAlpha() == 0.5f) {
            mPhoneLayout.setAlpha(1);
            mPhoneLayout.removeCallbacks(closeRunnable);
        }
    }

    private Runnable closeRunnable = new Runnable() {
        @Override
        public void run() {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (vibrator != null) vibrator.vibrate(100);
            hideFloatView();
            onClose();
            mPhoneLayout.setAlpha(1);
        }
    };


    //-------------------------------------监听相关------------------------------------------

    private void setFloatViewClick() {
        if (mPhoneLayout == null) return;
        mPhoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (floatViewClickListener != null) {
                    floatViewClickListener.onClick(v);
                }
            }
        });
    }

    protected FloatViewClickListener floatViewClickListener;

    public interface FloatViewClickListener {
        void onClick(View floatView);
    }

    public void setFloatViewClickListener(FloatViewClickListener floatViewClickListener) {
        this.floatViewClickListener = floatViewClickListener;
    }


    //-------------------------------------抽象方法------------------------------------------

    protected abstract View getFloatView();

    protected abstract void onClose();
}
