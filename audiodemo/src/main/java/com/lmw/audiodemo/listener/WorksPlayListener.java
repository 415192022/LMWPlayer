package com.lmw.audiodemo.listener;

import com.lmw.audiodemo.model.Works;

public interface WorksPlayListener {

    void onProgress(long currentPosition, long duration, int position);

    void onBufferingUpdate(int percent, int position);

    void onWorksChange(Works model, int position);

    void onPlayStateChange(boolean isPlaying, int position);
}
