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
package net.whollynugatory.streamytunes.android.service;

import android.app.Notification;
import android.content.Intent;
import android.media.MediaDescription;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.media.MediaBrowserServiceCompat;

import net.whollynugatory.streamytunes.android.service.contentcatalogs.MusicLibrary;
import net.whollynugatory.streamytunes.android.service.notifications.MediaNotificationManager;
import net.whollynugatory.streamytunes.android.service.players.MediaPlayerAdapter;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class MusicService extends MediaBrowserServiceCompat {

  private static final String TAG = BaseActivity.BASE_TAG + MusicService.class.getSimpleName();

  public static final String CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED";
  public static final String CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT";
  public static final String CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT";
  public static final String EXTRA_CONTENT_STYLE_GROUP_TITLE_HINT = "android.media.browse.CONTENT_STYLE_GROUP_TITLE_HINT";
  public static final int CONTENT_STYLE_LIST_ITEM_HINT_VALUE = 1;
  public static final int CONTENT_STYLE_GRID_ITEM_HINT_VALUE = 2;
  public static final int CONTENT_STYLE_CATEGORY_LIST_ITEM_HINT_VALUE = 3;
  public static final int CONTENT_STYLE_CATEGORY_GRID_ITEM_HINT_VALUE = 4;

  private MediaSessionCompat mSession;
  private PlayerAdapter mPlayback;
  private MediaNotificationManager mMediaNotificationManager;
  private MediaSessionCallback mCallback;
  private boolean mServiceInStartedState;

  @Override
  public void onCreate() {
    super.onCreate();

    Log.d(TAG, "++onCreate()");
    mSession = new MediaSessionCompat(this, MusicService.class.getSimpleName());
    mCallback = new MediaSessionCallback();
    mSession.setCallback(mCallback);
    mSession.setFlags(
      MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
        MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS |
        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
    setSessionToken(mSession.getSessionToken());

    mMediaNotificationManager = new MediaNotificationManager(this);
    mPlayback = new MediaPlayerAdapter(this, new MediaPlayerListener());
  }

  @Override
  public void onDestroy() {

    Log.d(TAG, "++onDestroy())");
    mMediaNotificationManager.onDestroy();
    mPlayback.stop();
    mSession.release();
  }

  @Override
  public MediaBrowserServiceCompat.BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, Bundle rootHints) {

    Log.d(TAG, "++onGetRoot(String, int, Bundle)");
    Bundle extras = new Bundle();
    extras.putBoolean(CONTENT_STYLE_SUPPORTED, true);
    extras.putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_GRID_ITEM_HINT_VALUE);
    extras.putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_LIST_ITEM_HINT_VALUE);
    return new BrowserRoot(MusicLibrary.getRoot(), rootHints);
  }

  @Override
  public void onLoadChildren(@NonNull final String parentMediaId, @NonNull final Result<List<MediaBrowserCompat.MediaItem>> result) {

    Log.d(TAG, "++onLoadChildren(String, Result<List<MediaBrowserCompat.MediaItem>>)");
    result.sendResult(MusicLibrary.getMediaItems());
  }

  @Override
  public void onTaskRemoved(Intent rootIntent) {
    super.onTaskRemoved(rootIntent);

    Log.d(TAG, "++onTaskRemoved(Intent)");
    stopSelf();
  }

  private MediaBrowser.MediaItem createBrowsableMediaItem(String mediaId, String folderName, Uri iconUri) {

    Log.d(TAG, "++createBrowsableMediaItem(String, String, Uri)");
    MediaDescription.Builder mediaDescriptionBuilder = new MediaDescription.Builder();
    mediaDescriptionBuilder.setMediaId(mediaId);
    mediaDescriptionBuilder.setTitle(folderName);
    mediaDescriptionBuilder.setIconUri(iconUri);
    Bundle extras = new Bundle();
    extras.putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_LIST_ITEM_HINT_VALUE);
    extras.putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_GRID_ITEM_HINT_VALUE);
    mediaDescriptionBuilder.setExtras(extras);
    return new MediaBrowser.MediaItem(mediaDescriptionBuilder.build(), MediaBrowser.MediaItem.FLAG_BROWSABLE);
  }

  private MediaBrowser.MediaItem createMediaItem(String mediaId, String folderName, Uri iconUri) {

    Log.d(TAG, "++createMediaItem(String, String, Uri)");
    MediaDescription.Builder mediaDescriptionBuilder = new MediaDescription.Builder();
    mediaDescriptionBuilder.setMediaId(mediaId);
    mediaDescriptionBuilder.setTitle(folderName);
    mediaDescriptionBuilder.setIconUri(iconUri);
    Bundle extras = new Bundle();
    extras.putString(EXTRA_CONTENT_STYLE_GROUP_TITLE_HINT, "Songs");
    mediaDescriptionBuilder.setExtras(extras);
    return new MediaBrowser.MediaItem(mediaDescriptionBuilder.build(), MediaBrowser.MediaItem.FLAG_PLAYABLE);
  }

  /*
      Private Class(es)
   */
  private class MediaSessionCallback extends MediaSessionCompat.Callback {

    private final String TAG = BaseActivity.BASE_TAG + MediaSessionCallback.class.getSimpleName();

    private final List<MediaSessionCompat.QueueItem> mPlaylist = new ArrayList<>();
    private int mQueueIndex = -1;
    private MediaMetadataCompat mPreparedMedia;

    @Override
    public void onAddQueueItem(MediaDescriptionCompat description) {

      Log.d(TAG, "++onAddQueueItem(" + description.getTitle() + ")");
      mPlaylist.add(new MediaSessionCompat.QueueItem(description, description.hashCode()));
      mQueueIndex = (mQueueIndex == -1) ? 0 : mQueueIndex;
      mSession.setQueue(mPlaylist);
    }

    @Override
    public void onRemoveQueueItem(MediaDescriptionCompat description) {

      Log.d(TAG, "++onRemoveQueueItem(" + description.getTitle() + ")");
      mPlaylist.remove(new MediaSessionCompat.QueueItem(description, description.hashCode()));
      mQueueIndex = (mPlaylist.isEmpty()) ? -1 : mQueueIndex;
      mSession.setQueue(mPlaylist);
    }

    @Override
    public void onPrepare() {

      Log.d(TAG, "++onPrepare()");
      if (mQueueIndex < 0 && mPlaylist.isEmpty()) {
        // nothing to play
        return;
      }

      final String mediaId = mPlaylist.get(mQueueIndex).getDescription().getMediaId();
      mPreparedMedia = MusicLibrary.getMetadata(mediaId);
      mSession.setMetadata(mPreparedMedia);
      if (!mSession.isActive()) {
        mSession.setActive(true);
      }
    }

    @Override
    public void onPlay() {

      Log.d(TAG, "++onPlay()");
      if (!isReadyToPlay()) {
        Log.w(TAG, "Nothing to play.");
        return;
      }

      if (mPreparedMedia == null) {
        onPrepare();
      }

      mPlayback.playFromMedia(mPreparedMedia);
    }

    @Override
    public void onPause() {

      Log.d(TAG, "++onPause()");
      mPlayback.pause();
    }

    @Override
    public void onStop() {

      Log.d(TAG, "++onStop()");
      mPlayback.stop();
      mSession.setActive(false);
    }

    @Override
    public void onSkipToNext() {

      Log.d(TAG, "++onSkipToNext()");
      if (mPlaylist.size() > 0) {
        mQueueIndex = (++mQueueIndex % mPlaylist.size());
        mPreparedMedia = null;
        onPlay();
      }
    }

    @Override
    public void onSkipToPrevious() {

      Log.d(TAG, "++onSkipToPrevious()");
      mQueueIndex = mQueueIndex > 0 ? mQueueIndex - 1 : mPlaylist.size() - 1;
      mPreparedMedia = null;
      onPlay();
    }

    @Override
    public void onSeekTo(long pos) {

      Log.d(TAG, "++onSeekTo(long)");
      mPlayback.seekTo(pos);
    }

    private boolean isReadyToPlay() {

      Log.d(TAG, "++isReadyToPlay(long)");
      return (!mPlaylist.isEmpty());
    }
  }

  private class MediaPlayerListener extends PlaybackInfoListener {

    private final String TAG = BaseActivity.BASE_TAG + MediaPlayerListener.class.getSimpleName();

    private final ServiceManager mServiceManager;

    MediaPlayerListener() {

      Log.d(TAG, "++MediaPlayerListener()");
      mServiceManager = new ServiceManager();
    }

    @Override
    public void onPlaybackStateChange(PlaybackStateCompat state) {

      Log.d(TAG, "++onPlaybackStateChange(PlaybackStateCompat)");
      mSession.setPlaybackState(state);

      switch (state.getState()) {
        case PlaybackStateCompat.STATE_PLAYING:
          mServiceManager.moveServiceToStartedState(state);
          break;
        case PlaybackStateCompat.STATE_PAUSED:
          mServiceManager.updateNotificationForPause(state);
          break;
        case PlaybackStateCompat.STATE_STOPPED:
          mServiceManager.moveServiceOutOfStartedState(state);
          break;
        case PlaybackStateCompat.STATE_BUFFERING:
        case PlaybackStateCompat.STATE_CONNECTING:
        case PlaybackStateCompat.STATE_ERROR:
        case PlaybackStateCompat.STATE_FAST_FORWARDING:
        case PlaybackStateCompat.STATE_NONE:
        case PlaybackStateCompat.STATE_REWINDING:
        case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
        case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
        case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM:
          break;
      }
    }

    class ServiceManager {

      private void moveServiceToStartedState(PlaybackStateCompat state) {

        Notification notification = mMediaNotificationManager.getNotification(mPlayback.getCurrentMedia(), state, getSessionToken());

        if (!mServiceInStartedState) {
          ContextCompat.startForegroundService(MusicService.this, new Intent(MusicService.this, MusicService.class));
          mServiceInStartedState = true;
        }

        startForeground(BaseActivity.NOTIFICATION_ID, notification);
      }

      private void updateNotificationForPause(PlaybackStateCompat state) {

        stopForeground(false);
        Notification notification = mMediaNotificationManager.getNotification(mPlayback.getCurrentMedia(), state, getSessionToken());
        mMediaNotificationManager.getNotificationManager().notify(BaseActivity.NOTIFICATION_ID, notification);
      }

      private void moveServiceOutOfStartedState(PlaybackStateCompat state) {

        stopForeground(true);
        stopSelf();
        mServiceInStartedState = false;
      }
    }
  }
}
