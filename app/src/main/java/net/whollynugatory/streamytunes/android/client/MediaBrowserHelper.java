/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.whollynugatory.streamytunes.android.client;

import android.content.ComponentName;
import android.content.Context;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaControllerCompat.Callback;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import net.whollynugatory.streamytunes.android.service.MusicService;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for a MediaBrowser that handles connecting, disconnecting,
 * and basic browsing with simplified callbacks.
 */
public class MediaBrowserHelper {

  private static final String TAG = BaseActivity.BASE_TAG + MediaBrowserHelper.class.getSimpleName();

  private final Context mContext;
  private final Class<? extends MediaBrowserServiceCompat> mMediaBrowserServiceClass;

  private final List<Callback> mCallbackList = new ArrayList<>();

  private final MediaBrowserConnectionCallback mMediaBrowserConnectionCallback;
  private final MediaControllerCallback mMediaControllerCallback;
  private final MediaBrowserSubscriptionCallback mMediaBrowserSubscriptionCallback;

  private MediaBrowserCompat mMediaBrowser;

  @Nullable
  private MediaControllerCompat mMediaController;

  public MediaBrowserHelper(Context context, Class<? extends MediaBrowserServiceCompat> serviceClass) {

    Log.d(TAG, "++MediaBrowserHelper(Context, Class<? extends MediaBrowserServiceCompat>)");
    mContext = context;
    mMediaBrowserServiceClass = serviceClass;

    mMediaBrowserConnectionCallback = new MediaBrowserConnectionCallback();
    mMediaControllerCallback = new MediaControllerCallback();
    mMediaBrowserSubscriptionCallback = new MediaBrowserSubscriptionCallback();
  }

  /*
  Public Method(s)
 */
  public MediaControllerCompat.TransportControls getTransportControls() {

    Log.d(TAG, "++getTransportControls()");
    if (mMediaController == null) {
      Log.e(TAG, "getTransportControls: MediaController is null");
      throw new IllegalStateException("MediaController is null");
    }

    return mMediaController.getTransportControls();
  }

  public void onStart() {

    Log.d(TAG, "++onStart()");
    if (mMediaBrowser == null) {
      mMediaBrowser = new MediaBrowserCompat(
          mContext,
          new ComponentName(mContext, mMediaBrowserServiceClass),
          mMediaBrowserConnectionCallback,
          null);
      mMediaBrowser.connect();
    }

    Log.d(TAG, "onStart: Creating MediaBrowser, and connecting");
  }

  public void onStop() {

    Log.d(TAG, "++onStop()");
    if (mMediaController != null) {
      mMediaController.unregisterCallback(mMediaControllerCallback);
      mMediaController = null;
    }

    if (mMediaBrowser != null && mMediaBrowser.isConnected()) {
      mMediaBrowser.disconnect();
      mMediaBrowser = null;
    }

    resetState();
    Log.d(TAG, "onStop: Releasing MediaController, Disconnecting from MediaBrowser");
  }

  public void registerCallback(Callback callback) {

    Log.d(TAG, "++registerCallback(Callback)");
    if (callback != null) {
      mCallbackList.add(callback);
      if (mMediaController != null) {
        final MediaMetadataCompat metadata = mMediaController.getMetadata();
        if (metadata != null) {
          callback.onMetadataChanged(metadata);
        }

        final PlaybackStateCompat playbackState = mMediaController.getPlaybackState();
        if (playbackState != null) {
          callback.onPlaybackStateChanged(playbackState);
        }
      }
    }
  }

  /**
   * Called after connecting with a {@link MediaBrowserServiceCompat}.
   * <p>
   * Override to perform processing after a connection is established.
   *
   * @param mediaController {@link MediaControllerCompat} associated with the connected MediaSession.
   **/
  protected void onConnected(@NonNull MediaControllerCompat mediaController) {
    Log.d(TAG, "++onConnected(MediaControllerCompat)");
  }

  /**
   * Called after loading a browsable {@link MediaBrowserCompat.MediaItem}
   *
   * @param parentId The media ID of the parent item.
   * @param children List (possibly empty) of child items.
   **/
  protected void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {

    Log.d(TAG, "++onChildrenLoaded(String, List<MediaBrowserCompat.MediaItem>)");
  }

  /**
   * Called when the {@link MediaBrowserServiceCompat} connection is lost.
   **/
  protected void onDisconnected() {
    Log.d(TAG, "++onDisconnected()");
  }

  @NonNull
  protected final MediaControllerCompat getMediaController() {

    Log.d(TAG, "++getMediaController()");
    if (mMediaController == null) {
      throw new IllegalStateException("MediaController is null!");
    }

    return mMediaController;
  }

  /*
    Private Method(s)
   */
  private void performOnAllCallbacks(@NonNull CallbackCommand command) {

    Log.d(TAG, "++performOnAllCallbacks(command)");
    for (Callback callback : mCallbackList) {
      if (callback != null) {
        command.perform(callback);
      }
    }
  }

  /**
   * The internal state of the app needs to revert to what it looks like when it started before any connections to the {@link MusicService}
   * happens via the {@link MediaSessionCompat}.
   **/
  private void resetState() {

    Log.d(TAG, "++resetState()");
    performOnAllCallbacks(callback -> callback.onPlaybackStateChanged(null));
  }

  /**
   * Helper for more easily performing operations on all listening clients.
   */
  private interface CallbackCommand {
    void perform(@NonNull Callback callback);
  }

  /**
   *  Receives callbacks from the MediaBrowser when it has successfully connected to the MediaBrowserService (MusicService).
   **/
  private class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback {

    private final String TAG = BaseActivity.BASE_TAG + MediaBrowserConnectionCallback.class.getSimpleName();

    // happens as a result of onStart()
    @Override
    public void onConnected() {

      Log.d(TAG, "++onConnected(CallbackCommand)");
      try {
        // get a MediaController for the MediaSession
        mMediaController = new MediaControllerCompat(mContext, mMediaBrowser.getSessionToken());
        mMediaController.registerCallback(mMediaControllerCallback);

        // sync existing MediaSession state to the UI
        mMediaControllerCallback.onMetadataChanged(mMediaController.getMetadata());
        mMediaControllerCallback.onPlaybackStateChanged(mMediaController.getPlaybackState());

        MediaBrowserHelper.this.onConnected(mMediaController);
      } catch (Exception e) {
        Log.e(TAG, "onConnected: Problem: ", e);
        throw new RuntimeException(e);
      }

      mMediaBrowser.subscribe(mMediaBrowser.getRoot(), mMediaBrowserSubscriptionCallback);
    }
  }

  /**
   * Receives callbacks from the MediaBrowser when the MediaBrowserService has loaded new media that is ready for playback.
   **/
  public class MediaBrowserSubscriptionCallback extends MediaBrowserCompat.SubscriptionCallback {

    private final String TAG = BaseActivity.BASE_TAG + MediaBrowserSubscriptionCallback.class.getSimpleName();

    @Override
    public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {

      Log.d(TAG, "++onChildrenLoaded(String, List<MediaBrowserCompat.MediaItem>)");
      MediaBrowserHelper.this.onChildrenLoaded(parentId, children);
    }
  }

  /**
   * Receives callbacks from the MediaController and updates the UI state, i.e.: Which is the current item, whether it's playing or paused, etc.
   **/
  private class MediaControllerCallback extends MediaControllerCompat.Callback {

    private final String TAG = BaseActivity.BASE_TAG + MediaControllerCallback.class.getSimpleName();

    @Override
    public void onMetadataChanged(final MediaMetadataCompat metadata) {

      Log.d(TAG, "++onMetadataChanged(MediaMetadataCompat)");
      performOnAllCallbacks(callback -> callback.onMetadataChanged(metadata));
    }

    @Override
    public void onPlaybackStateChanged(@Nullable final PlaybackStateCompat state) {

      Log.d(TAG, "++onPlaybackStateChanged(PlaybackStateCompat)");
      performOnAllCallbacks(callback -> callback.onPlaybackStateChanged(state));
    }

    // This might happen if the MusicService is killed while the Activity is in the foreground and onStart() has been called (but not onStop()).
    @Override
    public void onSessionDestroyed() {

      Log.d(TAG, "++onSessionDestroyed()");
      resetState();
      onPlaybackStateChanged(null);
      MediaBrowserHelper.this.onDisconnected();
    }
  }
}
