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
package net.whollynugatory.streamytunes.android;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.media.AudioFocusRequestCompat;
import androidx.media.AudioManagerCompat;
import androidx.media.session.MediaButtonReceiver;

import net.whollynugatory.streamytunes.android.db.MediaDetails;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MediaPlayerService extends Service implements
  MediaPlayer.OnCompletionListener,
  MediaPlayer.OnPreparedListener,
  MediaPlayer.OnErrorListener,
  MediaPlayer.OnSeekCompleteListener,
  MediaPlayer.OnInfoListener,
  MediaPlayer.OnBufferingUpdateListener,
  AudioManager.OnAudioFocusChangeListener {

  private final static String TAG = BaseActivity.BASE_TAG + MediaPlayerService.class.getSimpleName();

  public MutableLiveData<PlaybackStatus> MusicServiceState = new MutableLiveData<>(PlaybackStatus.STOPPED);
  public MutableLiveData<MediaDetails> CurrentMediaDetails = new MutableLiveData<>();

  private final static String AUDIO_PLAYER_TAG = "PlayerService";
  private final static String CHANNEL_ID = "my_channel_01";
  private final static int NOTIFICATION_ID = 1;

  private final IBinder iBinder = new LocalBinder();

  private MediaDetails mActiveMediaDetails = new MediaDetails();
  private int mAudioIndex = -1;
  private ArrayList<MediaDetails> mAudioList;
  private AudioManager mAudioManager;
  private MediaPlayer mMediaPlayer;
  private MediaSessionCompat mMediaSessionCompat;
  private MediaSessionManager mMediaSessionManager;
  private boolean mOngoingCall = false;
  private PhoneStateListener mPhoneStateListener;
  private int mResumePosition;
  private TelephonyManager mTelephonyManager;
  private MediaControllerCompat.TransportControls mTransportControls;

  private final BroadcastReceiver BecomingNoisyReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {

      Log.d(TAG, "++BecomingNoisyReceiver()");
      pauseMedia();
//      buildNotification(PlaybackStatus.PAUSED);
    }
  };

  private final BroadcastReceiver PauseAudio = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {

      Log.d(TAG, "++PauseAudio()");
      pauseMedia();
//      buildNotification(PlaybackStatus.PAUSED);
    }
  };

  private final BroadcastReceiver PlayAudio = new BroadcastReceiver() {

    @Override
    public void onReceive(Context context, Intent intent) {

      Log.d(TAG, "++PlayAudio()");
      // TODO: if player is paused we want to execute isPlaying branch
      if (mMediaPlayer.isPlaying()) {
        stopMedia();
        stopSelf();
        initMediaPlayer();
      } else {
        updateMetaData();
        playMedia();
//        buildNotification(PlaybackStatus.PLAYING);
      }
    }
  };

  @Override
  public void onAudioFocusChange(int focusState) {

    Log.d(TAG, "++onAudioFocusChange(int)");
    switch (focusState) {
      case AudioManager.AUDIOFOCUS_GAIN:
        if (mMediaPlayer == null) {
          initMediaPlayer();
        } else if (!mMediaPlayer.isPlaying()) {
          mMediaPlayer.start();
        }

        mMediaPlayer.setVolume(1.0f, 1.0f);
        break;
      case AudioManager.AUDIOFOCUS_LOSS:
        if (mMediaPlayer.isPlaying()) {
          mMediaPlayer.stop();
        }

        mMediaPlayer.release();
        mMediaPlayer = null;
        break;
      case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
        if (mMediaPlayer.isPlaying()) {
          mMediaPlayer.pause();
        }

        break;
      case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
        if (mMediaPlayer.isPlaying()) {
          mMediaPlayer.setVolume(0.1f, 0.1f);
        }

        break;
    }
  }

  @Override
  public IBinder onBind(Intent intent) {

    Log.d(TAG, "++onBind(Intent)");
    return iBinder;
  }

  @Override
  public void onBufferingUpdate(MediaPlayer mp, int percent) {

    Log.d(TAG, "++onBufferingUpdate(MediaPlayer, int)");
  }

  @Override
  public void onCompletion(MediaPlayer mp) {

    Log.d(TAG, "++onCompletion(MediaPlayer)");
    if (mAudioList.size() > 1) {
      if (mAudioIndex == mAudioList.size() - 1) {
        // TODO: enable repeat
        stopMedia();
        stopSelf();
      } else {
        mActiveMediaDetails = mAudioList.get(++mAudioIndex);
      }

      PreferenceUtils.setAudioIndex(getApplicationContext(), mAudioIndex);
      initMediaPlayer();
    } else {
      stopMedia();
      stopSelf();
    }
  }

  @Override
  public void onCreate() {
    super.onCreate();

    Log.d(TAG, "++onCreate()");
    callStateListener();
    registerBecomingNoisyReceiver();
    registerPauseAudio();
    registerPlayAudio();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    Log.d(TAG, "++onDestroy()");
    if (mMediaPlayer != null) {
      stopMedia();
      mMediaPlayer.release();
    }

    if (mMediaSessionCompat != null) {
      mMediaSessionCompat.release();
    }

    if (!removeAudioFocus()) {
      stopSelf();
    }

    if (mPhoneStateListener != null) {
      mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
    }

    removeNotification();
    unregisterReceiver(BecomingNoisyReceiver);
    unregisterReceiver(PauseAudio);
    unregisterReceiver(PlayAudio);
    PreferenceUtils.clearCachedAudioList(getApplicationContext());
  }

  @Override
  public boolean onError(MediaPlayer mp, int what, int extra) {

    Log.d(TAG, "++onError(MediaPlayer, int, int)");
    switch (what) {
      case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
        Log.e(TAG, "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
        break;
      case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
        Log.e(TAG, "MEDIA ERROR SERVER DIED " + extra);
        break;
      case MediaPlayer.MEDIA_ERROR_UNKNOWN:
        Log.e(TAG, "MEDIA ERROR UNKNOWN " + extra);
        break;
    }

    return false;
  }

  @Override
  public boolean onInfo(MediaPlayer mp, int what, int extra) {

    Log.d(TAG, "++onInfo(MediaPlayer, int, int)");
    // TODO: invoked to communicate some info
    return false;
  }

  @Override
  public void onPrepared(MediaPlayer mp) {

    Log.d(TAG, "++onPrepared(MediaPlayer)");
    playMedia();
  }

  @Override
  public void onSeekComplete(MediaPlayer mp) {

    Log.d(TAG, "++onSeekComplete(MediaPlayer)");
    //TODO: invoked indicating the completion of a seek operation
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    Log.d(TAG, "++onStartCommand(Intent, int, int)");
    if (!requestAudioFocus()) {
      stopSelf();
    }

    if (mMediaSessionManager == null) {
      try {
        initMediaSession();
        initMediaPlayer();
      } catch (Exception e) {
        Log.e(TAG, "Failed to initialize media player.", e);
        stopSelf();
      }

//      buildNotification(PlaybackStatus.PLAYING);
    }

    if (mMediaSessionCompat != null) {
      MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent);
    } else {
      Log.w(TAG, "MediaSession was null, defaulting to handleIncomingActions");
      String actionString = intent.getAction();
      if (actionString.equalsIgnoreCase(BaseActivity.ACTION_PLAY)) {
        mTransportControls.play();
      } else if (actionString.equalsIgnoreCase(BaseActivity.ACTION_PAUSE)) {
        mTransportControls.pause();
      } else if (actionString.equalsIgnoreCase(BaseActivity.ACTION_NEXT)) {
        mTransportControls.skipToNext();
      } else if (actionString.equalsIgnoreCase(BaseActivity.ACTION_PREVIOUS)) {
        mTransportControls.skipToPrevious();
      }
    }

    return super.onStartCommand(intent, flags, startId);
  }

  /*
    Private Method(s)
   */
  private void buildNotification(PlaybackStatus playbackStatus) {

    Log.d(TAG, "++buildNotification(PlaybackStatus)");
    int notificationAction = android.R.drawable.ic_media_pause;
    PendingIntent play_pauseAction = null;

    if (playbackStatus == PlaybackStatus.PLAYING) {
      MusicServiceState.setValue(PlaybackStatus.PLAYING);
      play_pauseAction = playbackAction(1);
    } else if (playbackStatus == PlaybackStatus.PAUSED) {
      MusicServiceState.setValue(PlaybackStatus.PAUSED);
      notificationAction = android.R.drawable.ic_media_play;
      play_pauseAction = playbackAction(0);
    }

    Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification_dark); // TODO: replace with your own image

    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
      .setShowWhen(false)
      .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
        .setMediaSession(mMediaSessionCompat.getSessionToken())
        .setShowActionsInCompactView(0, 1, 2))
      .setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null))
      .setLargeIcon(largeIcon)
      .setSmallIcon(android.R.drawable.stat_sys_headset)
      .setContentText(mActiveMediaDetails.ArtistName)
      .setContentTitle(mActiveMediaDetails.AlbumName)
      .setContentInfo(mActiveMediaDetails.Title)
      .setOngoing(true)
      .setChannelId(CHANNEL_ID)
      .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
      .addAction(notificationAction, "pause", play_pauseAction)
      .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));
    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
  }

  private void callStateListener() {

    Log.d(TAG, "++callStateListener()");
    mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
    mPhoneStateListener = new PhoneStateListener() {

      @Override
      public void onCallStateChanged(int state, String incomingNumber) {

        Log.d(TAG, "++onCallStateChanged(int, String)");
        switch (state) {
          case TelephonyManager.CALL_STATE_OFFHOOK:
          case TelephonyManager.CALL_STATE_RINGING:
            if (mMediaPlayer != null) {
              pauseMedia();
              mOngoingCall = true;
            }

            break;
          case TelephonyManager.CALL_STATE_IDLE:
            if (mMediaPlayer != null) {
              if (mOngoingCall) {
                mOngoingCall = false;
                resumeMedia();
              }
            }

            break;
        }
      }
    };

    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
  }

  private void initMediaPlayer() {

    Log.d(TAG, "++initMediaPlayer()");
    try {
      mAudioList = PreferenceUtils.getAudioList(getApplicationContext());
      mAudioIndex = PreferenceUtils.getAudioIndex(getApplicationContext());
      if (mAudioIndex != -1 && mAudioIndex < mAudioList.size()) {
        mActiveMediaDetails = mAudioList.get(mAudioIndex);
      } else {
        Log.w(TAG, "Audio index out of range.");
        stopSelf();
      }
    } catch (NullPointerException e) {
      Log.e(TAG, "Audio list is not ready.", e);
      stopSelf();
    }

    mMediaPlayer = new MediaPlayer();
    mMediaPlayer.setOnCompletionListener(this);
    mMediaPlayer.setOnErrorListener(this);
    mMediaPlayer.setOnPreparedListener(this);
    mMediaPlayer.setOnBufferingUpdateListener(this);
    mMediaPlayer.setOnSeekCompleteListener(this);
    mMediaPlayer.setOnInfoListener(this);
    mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    mMediaPlayer.reset();
    mMediaPlayer.setAudioAttributes(
      new AudioAttributes
        .Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build());
    try {
      mMediaPlayer.setDataSource(
        getApplicationContext(),
        Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + mActiveMediaDetails.MediaId));
      Log.d(TAG, "Setting active source: " + mActiveMediaDetails.toString());
      updateMetaData();
      CurrentMediaDetails.setValue(mActiveMediaDetails);
      MusicServiceState.setValue(PlaybackStatus.LOADING);
      mMediaPlayer.prepareAsync();
    } catch (IOException e) {
      e.printStackTrace();
      stopSelf();
    }
  }

  private void initMediaSession() {

    Log.d(TAG, "++initMediaSession()");
    if (mMediaSessionManager != null) {
      return;
    }

    mMediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
    mMediaSessionCompat = new MediaSessionCompat(getApplicationContext(), AUDIO_PLAYER_TAG);
    mTransportControls = mMediaSessionCompat.getController().getTransportControls();
    mMediaSessionCompat.setActive(true);

    mMediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {

      @Override
      public boolean onMediaButtonEvent(Intent mediaButtonEvent) {

        Log.d(TAG, "++onMediaButtonEvent(Intent)");
        return super.onMediaButtonEvent(mediaButtonEvent);
      }

      @Override
      public void onPlay() {
        super.onPlay();

        Log.d(TAG, "++onPlay()");
        resumeMedia();
//        buildNotification(PlaybackStatus.PLAYING);
      }

      @Override
      public void onPause() {
        super.onPause();

        Log.d(TAG, "++onPause()");
        pauseMedia();
//        buildNotification(PlaybackStatus.PAUSED);
      }

      @Override
      public void onSkipToNext() {
        super.onSkipToNext();

        Log.d(TAG, "++onSkipToNext()");
        int currentAudioIndex = PreferenceUtils.getAudioIndex(getApplicationContext());
        List<MediaDetails> audioList = PreferenceUtils.getAudioList(getApplicationContext());
        if (currentAudioIndex == audioList.size() - 1) {
          currentAudioIndex = 0; // TODO: add repeat functionality, for now just loop to beginning of audio list
        } else {
          ++currentAudioIndex;
        }

        PreferenceUtils.setAudioIndex(getApplicationContext(), currentAudioIndex);
        playMedia();
//        buildNotification(PlaybackStatus.PLAYING);
      }

      @Override
      public void onSkipToPrevious() {
        super.onSkipToPrevious();

        Log.d(TAG, "++onSkipToPrevious()");
        int currentAudioIndex = PreferenceUtils.getAudioIndex(getApplicationContext());
        List<MediaDetails> audioList = PreferenceUtils.getAudioList(getApplicationContext());
        if (currentAudioIndex == 0) {
          currentAudioIndex = audioList.size() - 1; // TODO: add repeat functionality, for now just loop to end of audio list
        } else {
          --currentAudioIndex;
        }

        PreferenceUtils.setAudioIndex(getApplicationContext(), currentAudioIndex);
        playMedia();
//        buildNotification(PlaybackStatus.PLAYING);
      }

      @Override
      public void onStop() {
        super.onStop();

        Log.d(TAG, "++onStop()");
        removeNotification();
        stopSelf();
      }

      @Override
      public void onSeekTo(long position) {
        super.onSeekTo(position);

        Log.d(TAG, "++onSeekTo()");
        // TODO: enable seek functionality
      }
    });
  }

  private PendingIntent playbackAction(int actionNumber) {

    Log.d(TAG, "++playbackAction(int)");
    Intent playbackAction = new Intent(this, MediaPlayerService.class);
    switch (actionNumber) {
      case 0:
        playbackAction.setAction(BaseActivity.ACTION_PLAY);
        return PendingIntent.getService(this, actionNumber, playbackAction, 0);
      case 1:
        playbackAction.setAction(BaseActivity.ACTION_PAUSE);
        return PendingIntent.getService(this, actionNumber, playbackAction, 0);
      case 2:
        playbackAction.setAction(BaseActivity.ACTION_NEXT);
        return PendingIntent.getService(this, actionNumber, playbackAction, 0);
      case 3:
        playbackAction.setAction(BaseActivity.ACTION_PREVIOUS);
        return PendingIntent.getService(this, actionNumber, playbackAction, 0);
      default:
        break;
    }

    Log.w(TAG, "playbackAction will be null");
    return null;
  }

  private void playMedia() {

    Log.d(TAG, "++playMedia()");
    MusicServiceState.setValue(PlaybackStatus.PLAYING);
    mMediaPlayer.start();
  }

  private void pauseMedia() {

    Log.d(TAG, "++pauseMedia()");
    if (mMediaPlayer.isPlaying()) {
      mMediaPlayer.pause();
      MusicServiceState.setValue(PlaybackStatus.PAUSED);
      mResumePosition = mMediaPlayer.getCurrentPosition();
    } else {
      Log.w(TAG, "Media not playing, cannot pause. Bad call?");
    }
  }

  private void registerBecomingNoisyReceiver() {

    Log.d(TAG, "++registerBecomingNoisyReceiver()");
    IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    registerReceiver(BecomingNoisyReceiver, intentFilter);
  }

  private void registerPauseAudio() {

    Log.d(TAG, "++registerPauseAudio()");
    IntentFilter filter = new IntentFilter(BaseActivity.ACTION_PAUSE);
    registerReceiver(PauseAudio, filter);
  }

  private void registerPlayAudio() {

    Log.d(TAG, "++registerPlayAudio()");
    IntentFilter filter = new IntentFilter(BaseActivity.ACTION_PLAY);
    registerReceiver(PlayAudio, filter);
  }

  private boolean removeAudioFocus() {

    Log.d(TAG, "++removeAudioFocus()");
    int result = AudioManagerCompat.abandonAudioFocusRequest(
      mAudioManager,
      new AudioFocusRequestCompat.Builder(AudioManagerCompat.AUDIOFOCUS_GAIN).build());
    return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
  }

  private void removeNotification() {

    Log.d(TAG, "++removeNotification()");
    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.cancel(NOTIFICATION_ID);
  }

  private boolean requestAudioFocus() {

    Log.d(TAG, "++requestAudioFocus()");
    mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    int result = mAudioManager.requestAudioFocus(
      new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
          new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        )
        .setAcceptsDelayedFocusGain(true)
        .setOnAudioFocusChangeListener(focusChange -> {
          // TODO: handle focus change
        }).build()
    );

    return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
  }

  private void resumeMedia() {

    Log.d(TAG, "++resumeMedia()");
    if (!mMediaPlayer.isPlaying()) {
      mMediaPlayer.seekTo(mResumePosition);
      mMediaPlayer.start();
    }
  }

  private void stopMedia() {

    Log.d(TAG, "++stopMedia()");
    MusicServiceState.setValue(PlaybackStatus.STOPPED);
    if (mMediaPlayer == null) {
      return;
    }

    if (mMediaPlayer.isPlaying()) {
      mMediaPlayer.stop();
    }

    mMediaPlayer.release();
  }

  private void updateMetaData() {

    Log.d(TAG, "++updateMetaData()");

    Bitmap albumArt = Utils.loadImageFromStorage(getApplicationContext(), mActiveMediaDetails.ArtistId, mActiveMediaDetails.AlbumId);
    mMediaSessionCompat.setMetadata(new MediaMetadataCompat.Builder()
      .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
      .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mActiveMediaDetails.ArtistName)
      .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mActiveMediaDetails.AlbumName)
      .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mActiveMediaDetails.Title)
      .build());
  }

  public class LocalBinder extends Binder {

    public MediaPlayerService getService() {

      return MediaPlayerService.this;
    }
  }
}
