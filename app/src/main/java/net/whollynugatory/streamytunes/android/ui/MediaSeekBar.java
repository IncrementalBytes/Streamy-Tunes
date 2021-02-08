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
package net.whollynugatory.streamytunes.android.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;

import androidx.appcompat.widget.AppCompatSeekBar;

public class MediaSeekBar extends AppCompatSeekBar {

  private static final String TAG = BaseActivity.BASE_TAG + MediaSeekBar.class.getSimpleName();

  private ControllerCallback mControllerCallback;
  private boolean mIsTracking = false;
  private MediaControllerCompat mMediaController;
  private ValueAnimator mProgressAnimator;

  private final OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
      mIsTracking = true;
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

      mMediaController.getTransportControls().seekTo(getProgress());
      mIsTracking = false;
    }
  };

  public MediaSeekBar(Context context) {
    super(context);
    super.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
  }

  public MediaSeekBar(Context context, AttributeSet attrs) {
    super(context, attrs);
    super.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
  }

  public MediaSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    super.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
  }

  @Override
  public final void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {

    throw new UnsupportedOperationException("Cannot add listeners to a MediaSeekBar");
  }

  public void setMediaController(final MediaControllerCompat mediaController) {

    Log.d(TAG, "++setMediaController(MediaControllerCompat)");
    if (mediaController != null) {
      mControllerCallback = new ControllerCallback();
      mediaController.registerCallback(mControllerCallback);
    } else if (mMediaController != null) {
      mMediaController.unregisterCallback(mControllerCallback);
      mControllerCallback = null;
    }

    mMediaController = mediaController;
  }

  public void disconnectController() {

    Log.d(TAG, "++disconnectController()");
    if (mMediaController != null) {
      mMediaController.unregisterCallback(mControllerCallback);
      mControllerCallback = null;
      mMediaController = null;
    }
  }

  private class ControllerCallback extends MediaControllerCompat.Callback implements ValueAnimator.AnimatorUpdateListener {

    @Override
    public void onSessionDestroyed() {
      super.onSessionDestroyed();
    }

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat state) {
      super.onPlaybackStateChanged(state);

      // if there's an ongoing animation, stop it now.
      if (mProgressAnimator != null) {
        mProgressAnimator.cancel();
        mProgressAnimator = null;
      }

      final int progress = state != null ? (int) state.getPosition() : 0;
      setProgress(progress);

      // If the media is playing then the seekbar should follow it, and the easiest
      // way to do that is to create a ValueAnimator to update it so the bar reaches
      // the end of the media the same time as playback gets there (or close enough).
      if (state != null && state.getState() == PlaybackStateCompat.STATE_PLAYING) {
        final int timeToEnd = (int) ((getMax() - progress) / state.getPlaybackSpeed());
        mProgressAnimator = ValueAnimator.ofInt(progress, getMax()).setDuration(timeToEnd);
        mProgressAnimator.setInterpolator(new LinearInterpolator());
        mProgressAnimator.addUpdateListener(this);
        mProgressAnimator.start();
      }
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat metadata) {
      super.onMetadataChanged(metadata);

      final int max = metadata != null ? (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION) : 0;
      setProgress(0);
      setMax(max);
    }

    @Override
    public void onAnimationUpdate(final ValueAnimator valueAnimator) {

      // if the user is changing the slider, cancel the animation.
      if (mIsTracking) {
        valueAnimator.cancel();
        return;
      }

      final int animatedIntValue = (int) valueAnimator.getAnimatedValue();
      setProgress(animatedIntValue);
    }
  }
}
