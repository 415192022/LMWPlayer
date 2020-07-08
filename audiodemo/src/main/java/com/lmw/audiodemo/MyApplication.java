package com.lmw.audiodemo;

import android.app.Application;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CoreLib.instance.install(this);
    }
}
