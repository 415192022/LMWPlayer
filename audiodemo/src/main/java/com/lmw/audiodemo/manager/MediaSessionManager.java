package com.lmw.audiodemo.manager;

import android.media.MediaMetadata;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.lmw.audiodemo.service.WorksPlayService;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MediaSessionManager {

    private static final String TAG = "MediaSessionManager";
    private static final long MEDIA_SESSION_ACTIONS = PlaybackState.ACTION_PLAY
            | PlaybackState.ACTION_PAUSE
            | PlaybackState.ACTION_PLAY_PAUSE
            | PlaybackState.ACTION_SKIP_TO_NEXT
            | PlaybackState.ACTION_SKIP_TO_PREVIOUS
            | PlaybackState.ACTION_STOP
            | PlaybackState.ACTION_SEEK_TO;

    private WorksPlayService mPlayService;
    private MediaSession mMediaSession;
    private MediaSession.Callback callback = new MediaSession.Callback() {
        @Override
        public void onPlay() {
            mPlayService.playOrPause();
        }

        @Override
        public void onPause() {
            mPlayService.playOrPause();
        }

        @Override
        public void onSkipToNext() {
            mPlayService.nextSong();
        }

        @Override
        public void onSkipToPrevious() {
            mPlayService.preSong();
        }

        @Override
        public void onStop() {
            mPlayService.stop();
        }

        @Override
        public void onSeekTo(long pos) {
            mPlayService.seekTo((int) pos);
        }
    };

    public MediaSessionManager(WorksPlayService playService) {
        mPlayService = playService;
        setupMediaSession();
    }


    private void setupMediaSession() {
        mMediaSession = new MediaSession(mPlayService, TAG);
        mMediaSession.setFlags(MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS | MediaSession.FLAG_HANDLES_MEDIA_BUTTONS);
        mMediaSession.setCallback(callback);
        mMediaSession.setActive(true);
    }

    public void updatePlaybackState() {
        int state = (mPlayService.isPlaying() ||
                mPlayService.isPreparing()) ? PlaybackState.STATE_PLAYING :
                PlaybackState.STATE_PAUSED;
        mMediaSession.setPlaybackState(
                new PlaybackState.Builder()
                        .setActions(MEDIA_SESSION_ACTIONS)
                        .setState(state, mPlayService.getCurrentPlayPosition(), 1)
                        .build());
    }

    public void updateMetaData(String url) {
        if (url == null) {
            mMediaSession.setMetadata(null);
            return;
        }

        MediaMetadata.Builder metaData = new MediaMetadata.Builder()
                .putString(MediaMetadata.METADATA_KEY_ART_URI, url);
        mMediaSession.setMetadata(metaData.build());
    }

    public void release() {
        mMediaSession.setCallback(null);
        mMediaSession.setActive(false);
        mMediaSession.release();
    }
}
