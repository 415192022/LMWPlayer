package com.lmw.audiodemo.utils;

import android.util.TypedValue;

import com.lmw.audiodemo.CoreLib;

public enum  DensityUtils {
    INSTANCE;


    public int dp2px(float dpVal){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpVal, CoreLib.instance.getContext().getResources().getDisplayMetrics());
    }
}
