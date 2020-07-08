package com.lmw.ijkplayer.listener;


public interface AudioPlayListener {

    void onError(String msg);

    void onPreparing();

    void onPlaying();

    void onPause();

    void onComplete();

    void onProgress(long currentPosition,long duration);

    void onBufferingUpdate(int percent);

}
