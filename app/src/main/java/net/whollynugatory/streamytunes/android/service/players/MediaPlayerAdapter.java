/*
 * Copyright 2021 Ryan Ward
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package net.whollynugatory.streamytunes.android.service.players;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import net.whollynugatory.streamytunes.android.service.PlaybackInfoListener;
import net.whollynugatory.streamytunes.android.service.PlayerAdapter;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;
import net.whollynugatory.streamytunes.android.ui.MainActivity;

/**
 * Exposes the functionality of the {@link MediaPlayer} and implements the {@link PlayerAdapter}
 * so that {@link MainActivity} can control music playback.
 */
public final class MediaPlayerAdapter extends PlayerAdapter {

  private static final String TAG = BaseActivity.BASE_TAG + MediaPlayerAdapter.class.getSimpleName();

  private final Context mContext;
  private final PlaybackInfoListener mPlaybackInfoListener;

  private MediaMetadataCompat mCurrentMedia;
  private boolean mCurrentMediaPlayedToCompletion;
  private String mMediaId;
  private MediaPlayer mMediaPlayer;
  private int mState;

  // work-around for a MediaPlayer bug related to the behavior of MediaPlayer.seekTo() while not playing
  private int mSeekWhileNotPlaying = -1;

  public MediaPlayerAdapter(Context context, PlaybackInfoListener listener) {
    super(context);

    Log.d(TAG, "++MediaPlayerAdapter(Context, PlaybackInfoListener)");
    mContext = context.getApplicationContext();
    mPlaybackInfoListener = listener;
  }

  /*
    Override Implementation(s)
   */
  @Override
  public MediaMetadataCompat getCurrentMedia() {
    return mCurrentMedia;
  }

  @Override
  public boolean isPlaying() {

    Log.d(TAG, "++isPlaying()");
    return mMediaPlayer != null && mMediaPlayer.isPlaying();
  }

  @Override
  protected void onPlay() {

    Log.d(TAG, "onPlay()");
    if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
      mMediaPlayer.start();
      setNewState(PlaybackStateCompat.STATE_PLAYING);
    }
  }

  @Override
  protected void onPause() {

    Log.d(TAG, "++onPause()");
    if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
      mMediaPlayer.pause();
      setNewState(PlaybackStateCompat.STATE_PAUSED);
    }
  }

  @Override
  public void onStop() {

    Log.d(TAG, "++onStop()");
    setNewState(PlaybackStateCompat.STATE_STOPPED);
    release();
  }

  @Override
  public void playFromMedia(MediaMetadataCompat metadata) {

    Log.d(TAG, "++playFromMedia(MediaMetadataCompat");
    mCurrentMedia = metadata;
    final String mediaId = metadata.getDescription().getMediaId();
//    String fileName = MusicLibrary.getMusicFilename(mediaId);
//    if (fileName != null) {
//      playFile(fileName);
      playFile(mediaId);
//    } else {
//      Log.w(TAG, "Could not find file with mediaId: " + mediaId);
//    }
  }

  @Override
  public void seekTo(long position) {

    Log.d(TAG, "++seekTo(long)");
    if (mMediaPlayer != null) {
      if (!mMediaPlayer.isPlaying()) {
        mSeekWhileNotPlaying = (int) position;
      }

      mMediaPlayer.seekTo((int) position);

      // Set the state (to the current state) because the position changed and should be reported to clients.
      setNewState(mState);
    }
  }

  @Override
  public void setVolume(float volume) {

    Log.d(TAG, "++setVolume(float)");
    if (mMediaPlayer != null) {
      mMediaPlayer.setVolume(volume, volume);
    }
  }

  /*
    Private Method(s)
   */
  private void playFile(String mediaId) {

    Log.d(TAG, "++playFile(String)");
    boolean mediaChanged = (!mediaId.equals(mMediaId));
    if (mCurrentMediaPlayedToCompletion) {
      // Last audio file was played to completion, the resourceId hasn't changed, but the
      // player was released, so force a reload of the media file for playback.
      mediaChanged = true;
      mCurrentMediaPlayedToCompletion = false;
    }

    if (!mediaChanged) {
      if (!isPlaying()) {
        play();
      }

      return;
    } else {
      release();
    }

    mMediaId = mediaId;
    initializeMediaPlayer();

    try {
//      AssetFileDescriptor assetFileDescriptor = mContext.getAssets().openFd(mFilename);
//      mMediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(), assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
      mMediaPlayer.setDataSource(
        mContext,
        Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + mMediaId));
    } catch (Exception e) {
      throw new RuntimeException("Failed to open file: " + mMediaId, e);
    }

    try {
      mMediaPlayer.prepare();
    } catch (Exception e) {
      throw new RuntimeException("Failed to open file: " + mMediaId, e);
    }

    play();
  }

  /*
    Private Method(s)
   */
  /**
   * Once the {@link MediaPlayer} is released, it can't be used again, and another one has to be
   * created. In the onStop() method of the {@link MainActivity} the {@link MediaPlayer} is
   * released. Then in the onStart() of the {@link MainActivity} a new {@link MediaPlayer}
   * object has to be created. That's why this method is private, and called by load(int) and
   * not the constructor.
   */
  private void initializeMediaPlayer() {

    Log.d(TAG, "++initializeMediaPlayer()");
    if (mMediaPlayer == null) {
      mMediaPlayer = new MediaPlayer();
      mMediaPlayer.setOnCompletionListener(mediaPlayer -> {

        mPlaybackInfoListener.onPlaybackCompleted();
        setNewState(PlaybackStateCompat.STATE_PAUSED);
      });
    }
  }

  private void release() {

    Log.d(TAG, "++release()");
    if (mMediaPlayer != null) {
      mMediaPlayer.release();
      mMediaPlayer = null;
    }
  }

  private void setNewState(@PlaybackStateCompat.State int newPlayerState) {

    Log.d(TAG, "++setNewState(int)");
    mState = newPlayerState;

    // Whether playback goes to completion, or whether it is stopped, the mCurrentMediaPlayedToCompletion is set to true.
    if (mState == PlaybackStateCompat.STATE_STOPPED) {
      mCurrentMediaPlayedToCompletion = true;
    }

    // Work around for MediaPlayer.getCurrentPosition() when it changes while not playing.
    final long reportPosition;
    if (mSeekWhileNotPlaying >= 0) {
      reportPosition = mSeekWhileNotPlaying;

      if (mState == PlaybackStateCompat.STATE_PLAYING) {
        mSeekWhileNotPlaying = -1;
      }
    } else {
      reportPosition = mMediaPlayer == null ? 0 : mMediaPlayer.getCurrentPosition();
    }

    final PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder();
    stateBuilder.setActions(getAvailableActions());
    stateBuilder.setState(mState, reportPosition, 1.0f, SystemClock.elapsedRealtime());
    mPlaybackInfoListener.onPlaybackStateChange(stateBuilder.build());
  }

  /**
   * Set the current capabilities available on this session. Note: If a capability is not
   * listed in the bitmask of capabilities then the MediaSession will not handle it. For
   * example, if you don't want ACTION_STOP to be handled by the MediaSession, then don't
   * included it in the bitmask that's returned.
   */
  @PlaybackStateCompat.Actions
  private long getAvailableActions() {

    long actions = PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
      | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
      | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
      | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
    switch (mState) {
      case PlaybackStateCompat.STATE_STOPPED:
        actions |= PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_PAUSE;
        break;
      case PlaybackStateCompat.STATE_PLAYING:
        actions |= PlaybackStateCompat.ACTION_STOP | PlaybackStateCompat.ACTION_PAUSE | PlaybackStateCompat.ACTION_SEEK_TO;
        break;
      case PlaybackStateCompat.STATE_PAUSED:
        actions |= PlaybackStateCompat.ACTION_PLAY | PlaybackStateCompat.ACTION_STOP;
        break;
      default:
        actions |= PlaybackStateCompat.ACTION_PLAY
          | PlaybackStateCompat.ACTION_PLAY_PAUSE
          | PlaybackStateCompat.ACTION_STOP
          | PlaybackStateCompat.ACTION_PAUSE;
    }

    return actions;
  }
}
