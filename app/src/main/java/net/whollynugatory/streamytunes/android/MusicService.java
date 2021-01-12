package net.whollynugatory.streamytunes.android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.RemoteControlClient;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;

import net.whollynugatory.streamytunes.android.db.MediaDetails;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;
import net.whollynugatory.streamytunes.android.ui.BaseActivity.ServiceState;
import net.whollynugatory.streamytunes.android.ui.MainActivity;

import java.io.IOException;
import java.util.ArrayList;

public class MusicService extends Service implements
  MediaPlayer.OnCompletionListener,
  MediaPlayer.OnPreparedListener,
  MediaPlayer.OnErrorListener {
//  MediaPlayer.OnErrorListener,
//  MusicFocusable,
//  PrepareMusicRetrieverTask.MusicRetrieverPreparedListener

  // The tag we put on debug messages
  private final static String TAG = BaseActivity.BASE_TAG + MusicService.class.getSimpleName();

  private final static String CHANNEL_ID = "net.whollynugatory.streamytunes.android";
  private final static String CHANNEL_NAME = "Random Music Player";

  // These are the Intent actions that we are prepared to handle. Notice that the fact these
  // constants exist in our class is a mere convenience: what really defines the actions our
  // service can handle are the <action> tags in the <intent-filters> tag for our service in
  // AndroidManifest.xml.
//  public static final String ACTION_TOGGLE_PLAYBACK = "net.whollynugatory.streamytunes.android.TOGGLE_PLAYBACK";
//  public static final String ACTION_PLAY = "net.whollynugatory.streamytunes.android.PLAY";
//  public static final String ACTION_PAUSE = "net.whollynugatory.streamytunes.android.PAUSE";
//  public static final String ACTION_STOP = "net.whollynugatory.streamytunes.android.STOP";
//  public static final String ACTION_SKIP = "net.whollynugatory.streamytunes.android.SKIP";
//  public static final String ACTION_REWIND = "net.whollynugatory.streamytunes.android.REWIND";
//  public static final String ACTION_URL = "net.whollynugatory.streamytunes.android.URL";

  // The volume we set the media player to when we lose audio focus, but are allowed to reduce
  // the volume instead of stopping playback.
  public static final float DUCK_VOLUME = 0.1f;

  // our media player
  MediaPlayer mPlayer = null;

  private MediaDetails mActiveMediaDetails;

  // our AudioFocusHelper object, if it's available (it's available on SDK level >= 8)
  // If not available, this will be null. Always check for null before using!
//  AudioFocusHelper mAudioFocusHelper = null;

  ServiceState mState = ServiceState.Stopped;

  // if in Retrieving mode, this flag indicates whether we should start playing immediately
  // when we are ready or not.
  boolean mStartPlayingAfterRetrieve = false;

  // if mStartPlayingAfterRetrieve is true, this variable indicates the URL that we should
  // start playing when we are ready. If null, we should play a random song from the device
  Uri mWhatToPlayAfterRetrieve = null;

  enum PauseReason {
    UserRequest,  // paused by user request
    FocusLoss,    // paused because of audio focus loss
  }

  ;

  // why did we pause? (only relevant if mState == State.Paused)
  PauseReason mPauseReason = PauseReason.UserRequest;

  // do we have audio focus?
  enum AudioFocus {
    NoFocusNoDuck,    // we don't have audio focus, and can't duck
    NoFocusCanDuck,   // we don't have focus, but can play at a low volume ("ducking")
    Focused           // we have full audio focus
  }

  AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;

  // title of the song we are currently playing
  String mSongTitle = "";

  // whether the song we are playing is streaming from the network
//  boolean mIsStreaming = false;

  // Wifi lock that we hold when streaming files from the internet, in order to prevent the
  // device from shutting off the Wifi radio
//  WifiManager.WifiLock mWifiLock;

  // The ID we use for the notification (the onscreen alert that appears at the notification
  // area at the top of the screen as an icon -- and as text as well if the user expands the
  // notification area).
  final int NOTIFICATION_ID = 1;

  // Our instance of our MusicRetriever, which handles scanning for media and
  // providing titles and URIs as we need.
//  MusicRetriever mRetriever;

  // our RemoteControlClient object, which will use remote control APIs available in
  // SDK level >= 14, if they're available.
  RemoteControlClientCompat mRemoteControlClientCompat;

  // Dummy album art we will pass to the remote control (if the APIs are available).
  Bitmap mDummyAlbumArt;

  // The component name of MusicIntentReceiver, for use with media button and remote control
  // APIs
  ComponentName mMediaButtonReceiverComponent;

  AudioManager mAudioManager;
  NotificationManager mNotificationManager;

//  Notification mNotification = null;

  /**
   * Makes sure the media player exists and has been reset. This will create the media player
   * if needed, or reset the existing media player if one already exists.
   */
  void createMediaPlayerIfNeeded() {

    Log.d(TAG, "++createMediaPlayerIfNeeded()");
    if (mPlayer == null) {
      mPlayer = new MediaPlayer();

      // Make sure the media player will acquire a wake-lock while playing. If we don't do
      // that, the CPU might go to sleep while the song is playing, causing playback to stop.
      //
      // Remember that to use this, we have to declare the android.permission.WAKE_LOCK
      // permission in AndroidManifest.xml.
      mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);

      // we want the media player to notify us when it's ready preparing, and when it's done
      // playing:
      mPlayer.setOnPreparedListener(this);
      mPlayer.setOnCompletionListener(this);
      mPlayer.setOnErrorListener(this);
    } else
      mPlayer.reset();
  }

  @Override
  public void onCreate() {

    Log.d(TAG, "+onCreate()");
    // Create the Wifi lock (this does not acquire the lock, this just creates it)
//    mWifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
//      .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

    // Create the retriever and start an asynchronous task that will prepare it.
//    mRetriever = new MusicRetriever(getContentResolver());
//    (new PrepareMusicRetrieverTask(mRetriever, this)).execute();

    // create the Audio Focus Helper, if the Audio Focus feature is available (SDK 8 or above)
//    if (android.os.Build.VERSION.SDK_INT >= 8)
//      mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);
//    else
      mAudioFocus = AudioFocus.Focused; // no focus feature, so we always "have" audio focus

    mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.ic_song_dark);

    mMediaButtonReceiverComponent = new ComponentName(this, MusicIntentReceiver.class);
  }

  /**
   * Called when we receive an Intent. When we receive an intent sent to us via startService(),
   * this is the method that gets called. So here we react appropriately depending on the
   * Intent's action, which specifies what is being requested of us.
   */
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    Log.d(TAG, "++onStartCommand(Intent, int, int)");
    String action = intent.getAction();
//    if (action.equals(BaseActivity.ACTION_TOGGLE_PLAYBACK)) processTogglePlaybackRequest();
//    else if (action.equals(BaseActivity.ACTION_PLAY)) processPlayRequest();
    switch (action) {
      case BaseActivity.ACTION_PLAY:
        processPlayRequest();
        break;
      case BaseActivity.ACTION_PAUSE:
        processPauseRequest();
        break;
      case BaseActivity.ACTION_NEXT:
        processNextRequest();
        break;
      case BaseActivity.ACTION_PREVIOUS:
        processPreviousRequest();
        break;
    }
//    else if (action.equals(BaseActivity.ACTION_URL)) processAddRequest(intent);

    return START_NOT_STICKY; // Means we started the service, but don't want it to
    // restart in case it's killed.
  }

//  void processTogglePlaybackRequest() {
//    if (mState == State.Paused || mState == State.Stopped) {
//      processPlayRequest();
//    } else {
//      processPauseRequest();
//    }
//  }

  void processPlayRequest() {

    Log.d(TAG, "++processPlayRequest()");
    tryToGetAudioFocus();

    // actually play the song
    if (mState == ServiceState.Stopped) {
      // If we're stopped, just go ahead to the next song and start playing
      playNextSong();
    } else if (mState == ServiceState.Paused) {
      // If we're paused, just continue playback and restore the 'foreground service' state.
      mState = ServiceState.Playing;
      setUpAsForeground(mSongTitle + " (playing)");
      configAndStartMediaPlayer();
    }

    // Tell any remote controls that our playback state is 'playing'.
    if (mRemoteControlClientCompat != null) {
      mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
    }
  }

  void processPauseRequest() {

    Log.d(TAG, "++processPauseRequest()");
    if (mState == ServiceState.Playing) {
      // Pause media player and cancel the 'foreground service' state.
      mState = ServiceState.Paused;
      mPlayer.pause();
      relaxResources(false); // while paused, we always retain the MediaPlayer
      // do not give up audio focus
    }

    // Tell any remote controls that our playback state is 'paused'.
    if (mRemoteControlClientCompat != null) {
      mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
    }
  }

  void processPreviousRequest() {

    Log.d(TAG, "++processPreviousRequest()");
    if (mState == ServiceState.Playing || mState == ServiceState.Paused) {
      // TODO: playPreviousSong()
      //  mPlayer.seekTo(0);
    }
  }

  void processNextRequest() {

    Log.d(TAG, "++processNextRequest()");
    if (mState == ServiceState.Playing || mState == ServiceState.Paused) {
      tryToGetAudioFocus();
      playNextSong();
    }
  }

  /**
   * Releases resources used by the service for playback. This includes the "foreground service"
   * status and notification, the wake locks and possibly the MediaPlayer.
   *
   * @param releaseMediaPlayer Indicates whether the Media Player should also be released or not
   */
  void relaxResources(boolean releaseMediaPlayer) {

    Log.d(TAG, "relaxResources(boolean)");
    // stop being a foreground service
    stopForeground(true);

    // stop and release the Media Player, if it's available
    if (releaseMediaPlayer && mPlayer != null) {
      mPlayer.reset();
      mPlayer.release();
      mPlayer = null;
    }

    // we can also release the Wifi lock, if we're holding it
//    if (mWifiLock.isHeld()) mWifiLock.release();
  }

  void giveUpAudioFocus() {

    Log.d(TAG, "giveUpAudioFocus()");
//    if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null
//      && mAudioFocusHelper.abandonFocus())
    if (mAudioFocus == AudioFocus.Focused) {
      mAudioFocus = AudioFocus.NoFocusNoDuck;
    }
  }

  /**
   * Reconfigures MediaPlayer according to audio focus settings and starts/restarts it. This
   * method starts/restarts the MediaPlayer respecting the current audio focus state. So if
   * we have focus, it will play normally; if we don't have focus, it will either leave the
   * MediaPlayer paused or set it to a low volume, depending on what is allowed by the
   * current focus settings. This method assumes mPlayer != null, so if you are calling it,
   * you have to do so from a context where you are sure this is the case.
   */
  void configAndStartMediaPlayer() {

    Log.d(TAG, "configAndStartMediaPlayer()");
    if (mAudioFocus == AudioFocus.NoFocusNoDuck) {
      // If we don't have audio focus and can't duck, we have to pause, even if mState
      // is State.Playing. But we stay in the Playing state so that we know we have to resume
      // playback once we get the focus back.
      if (mPlayer.isPlaying()) mPlayer.pause();
      return;
    } else if (mAudioFocus == AudioFocus.NoFocusCanDuck)
      mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);  // we'll be relatively quiet
    else
      mPlayer.setVolume(1.0f, 1.0f); // we can be loud

    if (!mPlayer.isPlaying()) mPlayer.start();
  }

//  void processAddRequest(Intent intent) {
//    // user wants to play a song directly by URL or path. The URL or path comes in the "data"
//    // part of the Intent. This Intent is sent by {@link MainActivity} after the user
//    // specifies the URL/path via an alert box.
//    if (mState == State.Retrieving) {
//      // we'll play the requested URL right after we finish retrieving
//      mWhatToPlayAfterRetrieve = intent.getData();
//      mStartPlayingAfterRetrieve = true;
//    } else if (mState == State.Playing || mState == State.Paused || mState == State.Stopped) {
//      Log.d(TAG, "Playing from URL/path: " + intent.getData().toString());
//      tryToGetAudioFocus();
//      playNextSong();
//    }
//  }

  void tryToGetAudioFocus() {

    Log.d(TAG, "++tryToGetAudioFocus()");
//    if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
//      && mAudioFocusHelper.requestFocus())
      if (mAudioFocus != AudioFocus.Focused) {
        mAudioFocus = AudioFocus.Focused;
      }
  }

  void playNextSong() {

    Log.d(TAG, "++playNextSong()");
    mState = ServiceState.Stopped;
    relaxResources(false); // release everything except MediaPlayer
    try {
      // audio index and audio list should be set by caller
      int currentIndex = PreferenceUtils.getAudioIndex(getApplicationContext());
      ArrayList<MediaDetails> mediaDetailsArrayList = PreferenceUtils.getAudioList(getApplicationContext());
      mActiveMediaDetails = mediaDetailsArrayList.get(currentIndex);

      // set the source of the media player a a content URI
      createMediaPlayerIfNeeded();
//      mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC); // deprecated
      mPlayer.setDataSource(
        getApplicationContext(),
        Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + mActiveMediaDetails.MediaId));

      mSongTitle = mActiveMediaDetails.Title;

      mState = ServiceState.Preparing;
      setUpAsForeground(mSongTitle + " (loading)");

      // Use the media button APIs (if available) to register ourselves for media button events
//      MediaButtonHelper.registerMediaButtonEventReceiverCompat(mAudioManager, mMediaButtonReceiverComponent);

      // Use the remote control APIs (if available) to set the playback state
      if (mRemoteControlClientCompat == null) {
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.setComponent(mMediaButtonReceiverComponent);
        mRemoteControlClientCompat = new RemoteControlClientCompat(PendingIntent.getBroadcast(this, 0, intent, 0));
        RemoteControlHelper.registerRemoteControlClient(mAudioManager, mRemoteControlClientCompat);
      }

//      mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING); // deprecated
//      mRemoteControlClientCompat.setTransportControlFlags(
//        RemoteControlClient.FLAG_KEY_MEDIA_PLAY | // deprecated
//          RemoteControlClient.FLAG_KEY_MEDIA_PAUSE | // deprecated
//          RemoteControlClient.FLAG_KEY_MEDIA_NEXT | // deprecated
//          RemoteControlClient.FLAG_KEY_MEDIA_STOP); // deprecated

      // Update the remote controls
      mRemoteControlClientCompat.editMetadata(true)
        .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, mActiveMediaDetails.ArtistName)
        .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, mActiveMediaDetails.AlbumName)
        .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, mActiveMediaDetails.Title)
//        .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION, playingItem.getDuration())
        // TODO: fetch real item artwork
        .putBitmap(RemoteControlClientCompat.MetadataEditorCompat.METADATA_KEY_ARTWORK, mDummyAlbumArt)
        .apply();

      // starts preparing the media player in the background. When it's done, it will call
      // our OnPreparedListener (that is, the onPrepared() method on this class, since we set
      // the listener to 'this').
      //
      // Until the media player is prepared, we *cannot* call start() on it!
      mPlayer.prepareAsync();
    } catch (IOException ex) {
      Log.e(TAG, "IOException playing next song.", ex);
      ex.printStackTrace();
    }
  }

  /**
   * Called when media player is done playing current song.
   */
  public void onCompletion(MediaPlayer player) {

    Log.d(TAG, "++onCompletion(MediaPlayer)");
    ArrayList<MediaDetails> mediaDetailsArrayList = PreferenceUtils.getAudioList(getApplicationContext());
    if (mediaDetailsArrayList.size() > 1) {
      int currentIndex = PreferenceUtils.getAudioIndex(getApplicationContext());
      if (++currentIndex < mediaDetailsArrayList.size()) {
        PreferenceUtils.setAudioIndex(getApplicationContext(), currentIndex);
      }

      playNextSong();
    } else {
      mState = ServiceState.Stopped;
      relaxResources(true);
      giveUpAudioFocus();
    }
  }

  /**
   * Called when media player is done preparing.
   */
  public void onPrepared(MediaPlayer player) {

    Log.d(TAG, "++onPrepared(MediaPlayer)");
    // The media player is done preparing. That means we can start playing!
    mState = ServiceState.Playing;
    updateNotification(mSongTitle + " (playing)");
    configAndStartMediaPlayer();
  }

  /**
   * Updates the notification.
   */
  void updateNotification(String text) {

    Log.d(TAG, "++updateNotification(String)");
    PendingIntent pi = PendingIntent.getActivity(
      getApplicationContext(),
      0,
      new Intent(
        getApplicationContext(),
        MainActivity.class),
      PendingIntent.FLAG_UPDATE_CURRENT);
//    mNotification.setLatestEventInfo(getApplicationContext(), "RandomMusicPlayer", text, pi);
//    mNotificationManager.notify(NOTIFICATION_ID, mNotification);

    NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
    notificationChannel.setLightColor(Color.BLUE);
    notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(notificationChannel);

    Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification_dark); // TODO: replace with your own image
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
//      .setShowWhen(false)
//      .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
//        .setMediaSession(mMediaSessionCompat.getSessionToken())
//        .setShowActionsInCompactView(0, 1, 2))
      .setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null))
      .setLargeIcon(largeIcon)
      .setSmallIcon(android.R.drawable.stat_sys_headset)
      .setContentIntent(pi)
      .setContentText(mActiveMediaDetails.ArtistName)
      .setContentTitle(mActiveMediaDetails.AlbumName)
      .setContentInfo(mActiveMediaDetails.Title)
      .setOngoing(true)
      .setChannelId(CHANNEL_ID);
//      .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
//      .addAction(notificationAction, "pause", play_pauseAction)
//      .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));
    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
  }

  /**
   * Configures service as a foreground service. A foreground service is a service that's doing
   * something the user is actively aware of (such as playing music), and must appear to the
   * user as a notification. That's why we create the notification here.
   */
  void setUpAsForeground(String text) {

    Log.d(TAG, "++setUpAsForeground(String)");
    PendingIntent pi = PendingIntent.getActivity(
      getApplicationContext(),
      0,
      new Intent(
        getApplicationContext(),
        MainActivity.class),
      PendingIntent.FLAG_UPDATE_CURRENT);

    NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
    notificationChannel.setLightColor(Color.BLUE);
    notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
    ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(notificationChannel);

//    mNotification = new Notification();
//    mNotification.tickerText = text;
//    mNotification.icon = R.drawable.ic_play_dark; // deprecated
//    mNotification.flags |= Notification.FLAG_ONGOING_EVENT;
//    mNotification.setLatestEventInfo(getApplicationContext(), "RandomMusicPlayer", text, pi);

    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
      .setColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimary, null))
      .setContentIntent(pi)
      .setOngoing(true)
      .setTicker(text)
      .setChannelId(CHANNEL_ID);
//      .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
//      .addAction(notificationAction, "pause", play_pauseAction)
//      .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

    startForeground(NOTIFICATION_ID, notificationBuilder.build());
  }

  /**
   * Called when there's an error playing media. When this happens, the media player goes to
   * the Error state. We warn the user about the error and reset the media player.
   */
  public boolean onError(MediaPlayer mp, int what, int extra) {

    Toast.makeText(getApplicationContext(), "Media player error! Resetting.",
      Toast.LENGTH_SHORT).show();
    Log.e(TAG, "Error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));

    mState = ServiceState.Stopped;
    relaxResources(true);
    giveUpAudioFocus();
    return true; // true indicates we handled the error
  }

//  public void onGainedAudioFocus() {
//    Toast.makeText(getApplicationContext(), "gained audio focus.", Toast.LENGTH_SHORT).show();
//    mAudioFocus = AudioFocus.Focused;
//
//    // restart media player with new focus settings
//    if (mState == State.Playing)
//      configAndStartMediaPlayer();
//  }
//
//  public void onLostAudioFocus(boolean canDuck) {
//    Toast.makeText(getApplicationContext(), "lost audio focus." + (canDuck ? "can duck" :
//      "no duck"), Toast.LENGTH_SHORT).show();
//    mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;
//
//    // start/restart/pause media player with new focus settings
//    if (mPlayer != null && mPlayer.isPlaying())
//      configAndStartMediaPlayer();
//  }
//
//  public void onMusicRetrieverPrepared() {
//    // Done retrieving!
//    mState = State.Stopped;
//
//    // If the flag indicates we should start playing after retrieving, let's do that now.
//    if (mStartPlayingAfterRetrieve) {
//      tryToGetAudioFocus();
//      playNextSong();
//    }
//  }

  @Override
  public void onDestroy() {

    Log.d(TAG, "++onDestroy()");
    mState = ServiceState.Stopped;
    relaxResources(true);
    giveUpAudioFocus();
  }

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }
}
