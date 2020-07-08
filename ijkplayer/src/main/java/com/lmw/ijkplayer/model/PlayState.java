package com.lmw.ijkplayer.model;

public enum PlayState {
    IDIE(100),PREPARING(101),PLAYING(102),PAUSE(103);

    private int state;

    PlayState(int state) {
        this.state = state;
    }

}
