package com.lmw.ijkplayer.service;

public interface AudioPlay {

    void initDataSource(String path);

    void resume();

    void play();

    void pause();

    void playOrPause();

    void stop();

    void seekTo(long position);

    void reset();
}
