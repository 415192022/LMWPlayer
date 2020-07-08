package com.lmw.audiodemo;

import android.content.Context;

public enum  CoreLib {
    instance;
    private Context context;

    public void install(Context context){
        this.context = context.getApplicationContext();
    }

    public Context getContext(){
        return context;
    }
}
