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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import net.whollynugatory.streamytunes.android.PlaylistAsync;
import net.whollynugatory.streamytunes.android.PreferenceUtils;
import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.UpdateRowAsync;
import net.whollynugatory.streamytunes.android.Utils;
import net.whollynugatory.streamytunes.android.client.MediaBrowserHelper;
import net.whollynugatory.streamytunes.android.db.MediaDetails;
import net.whollynugatory.streamytunes.android.db.StreamyTunesDatabase;
import net.whollynugatory.streamytunes.android.db.entity.MediaEntity;
import net.whollynugatory.streamytunes.android.db.entity.PlaylistEntity;
import net.whollynugatory.streamytunes.android.db.repository.MediaRepository;
import net.whollynugatory.streamytunes.android.service.MusicService;
import net.whollynugatory.streamytunes.android.service.contentcatalogs.MusicLibrary;
import net.whollynugatory.streamytunes.android.ui.fragments.AlbumsFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.ArtistsFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.MediaFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.PlaylistFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.SummaryFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.UserSettingsFragment;

import java.util.List;

public class MainActivity extends BaseActivity implements
  AlbumsFragment.OnAlbumListener,
  ArtistsFragment.OnArtistListener,
  MediaFragment.OnMediaListener,
  PlaylistFragment.OnPlaylistListener,
  SummaryFragment.OnSummaryListener {

  private static final String TAG = BaseActivity.BASE_TAG + MainActivity.class.getSimpleName();

//  private PlaybackStatus mCurrentPlaybackStatus;

  private View mPlayerIncludeView;
  private ImageView mPlayerAlbumImage;
  private TextView mPlayerSongText;
  private TextView mPlayerAlbumText;
  private ImageButton mPlayerPlayPauseImage;
  private MediaSeekBar mSeekBarAudio;

  private MediaBrowserHelper mMediaBrowserHelper;
  private boolean mIsPlaying = false;

//  private MediaPlayerService mPlayerService;
//  private boolean mServiceBound = false;
//  private ServiceConnection mServiceConnection;

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    Log.d(TAG, "++onActivityResult(int, int, Intent)");
    switch (requestCode) {
      case BaseActivity.REQUEST_NOTIFICATION:
        if (resultCode == RESULT_OK) {
          Log.d(TAG, "Result OK from notification.");
        }

        break;
      case BaseActivity.REQUEST_PERMISSIONS:
        if (resultCode == RESULT_OK) {
          Log.d(TAG, "Result OK from permissions check.");
        }

        break;
      case BaseActivity.REQUEST_SYNC:
        if (resultCode == RESULT_OK) {
          Log.d(TAG, "Result OK from other sync activity.");
        }

        break;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    setContentView(R.layout.activity_main);

    mPlayerIncludeView = findViewById(R.id.main_include_player);
    mPlayerAlbumImage = findViewById(R.id.player_image_album);
    mPlayerAlbumText = findViewById(R.id.player_text_details);
    mPlayerSongText = findViewById(R.id.player_text_song);
    mSeekBarAudio = findViewById(R.id.player_seekbar_audio);

    ImageButton playerNextImage = findViewById(R.id.player_image_next);
    playerNextImage.setOnClickListener(v -> {

//      List<MediaDetails> mediaDetailsList = PreferenceUtils.getAudioList(this);
//      if (mediaDetailsList.size() > 1) {
//        checkForPermission(BaseActivity.ACTION_NEXT);
//      }
      mMediaBrowserHelper.getTransportControls().skipToNext();
    });

    mPlayerPlayPauseImage = findViewById(R.id.player_image_play_pause);
    mPlayerPlayPauseImage.setOnClickListener(v -> {

//      if (mCurrentPlaybackStatus.equals(PlaybackStatus.PLAYING)) {
//        checkForPermission(ACTION_PAUSE);
//      } else {
//        checkForPermission(ACTION_PLAY);
//      }
      if (mIsPlaying) {
        mMediaBrowserHelper.getTransportControls().pause();
        mPlayerPlayPauseImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_dark, null));
      } else {
        mMediaBrowserHelper.onStart();
        mMediaBrowserHelper.getTransportControls().play();
        mPlayerPlayPauseImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause_dark, null));
      }
    });

    ImageButton playerPreviousImage = findViewById(R.id.player_image_prev);
    playerPreviousImage.setOnClickListener(v -> {

//      List<MediaDetails> mediaDetailsList = PreferenceUtils.getAudioList(this);
//      if (mediaDetailsList.size() > 1) {
//        checkForPermission(BaseActivity.ACTION_PREVIOUS);
//      }
      mMediaBrowserHelper.getTransportControls().skipToPrevious();
    });

    Toolbar mainToolbar = findViewById(R.id.main_toolbar);
    BottomNavigationView navigationView = findViewById(R.id.main_nav_bottom);
    setSupportActionBar(mainToolbar);

    getSupportFragmentManager().addOnBackStackChangedListener(() -> {
      Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment_container);
      if (fragment != null) {
        String fragmentClassName = fragment.getClass().getName();
        if (fragmentClassName.equals(UserSettingsFragment.class.getName())) {
          setTitle(getString(R.string.settings));
        } else {
          setTitle(getString(R.string.app_name));
        }
      }
    });

    navigationView.setOnNavigationItemSelectedListener(menuItem -> {

      Log.d(TAG, "++onNavigationItemSelectedListener(MenuItem)");
      switch (menuItem.getItemId()) {
        case R.id.navigation_music:
          PreferenceUtils.setIsMusic(this);
//          replaceFragment(SummaryFragment.newInstance());
          return true;
        case R.id.navigation_audiobook:
//          PreferenceUtils.setIsAudiobook(this);
//          return true;
        case R.id.navigation_podcast:
//          PreferenceUtils.setIsPodcast(this);
          Snackbar.make(
            findViewById(R.id.main_fragment_container),
            getString(R.string.not_yet_implemented),
            Snackbar.LENGTH_SHORT).show();
          return true;
      }

      return false;
    });

//    MusicLibrary.setContent(this);
    mMediaBrowserHelper = new MediaBrowserConnection(this);
    mMediaBrowserHelper.registerCallback(new MediaBrowserListener());

////    mServiceConnection = new ServiceConnection() {
////
////      @Override
////      public void onServiceConnected(ComponentName name, IBinder service) {
////
////        Log.d(TAG, "++onServiceConnected(ComponentName, IBinder)");
////        MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
////        mPlayerService = binder.getService();
////        mServiceBound = true;
////        Log.d(TAG, "Service Bound");
////
////        final Observer<PlaybackStatus> statusObserver = playbackStatus -> {
////
////          if (playbackStatus.equals(PlaybackStatus.PAUSED) || playbackStatus.equals(PlaybackStatus.STOPPED)) {
////            if (mPlayerPlayPauseImage != null) {
////              mPlayerPlayPauseImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_dark, null));
////            }
////          } else if (playbackStatus.equals(PlaybackStatus.PLAYING)) {
////            if (mPlayerPlayPauseImage != null) {
////              mPlayerPlayPauseImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause_dark, null));
////            }
////          }
////
////          mCurrentPlaybackStatus = playbackStatus;
////        };
////
////        mPlayerService.MusicServiceState.observe(MainActivity.this, statusObserver);
////
////        final Observer<MediaDetails> mediaObserver = mediaDetails -> {
////
////          if (mediaDetails.MediaId != BaseActivity.UNKNOWN_ID) {
////            Bitmap albumArt = Utils.loadImageFromStorage(getApplicationContext(), mediaDetails.ArtistId, mediaDetails.AlbumId);
////            if (albumArt != null) {
////              mPlayerAlbumImage.setImageBitmap(albumArt);
////            }
////
////            mPlayerAlbumText.setText(mediaDetails.AlbumName);
////            mPlayerSongText.setText(mediaDetails.Title);
////          }
////        };
////
////        mPlayerService.CurrentMediaDetails.observe(MainActivity.this, mediaObserver);
////      }
//
//      @Override
//      public void onServiceDisconnected(ComponentName name) {
//
//        Log.d(TAG, "++onServiceDisconnected(ComponentName)");
//        mServiceBound = false;
//      }
//    };

    replaceFragment(MediaFragment.newInstance());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    Log.d(TAG, "++onCreateOptionsMenu(Menu)");
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    Log.d(TAG, "++onDestroy()");
//    if (mServiceBound) {
//      unbindService(mServiceConnection);
//      mPlayerService.stopSelf();
//    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    Log.d(TAG, "++onOptionsItemSelected(MenuItem)");
    if (item.getItemId() == R.id.action_main_home) {
      // TODO: disable home menu
      // TODO: show (if hidden) bottom navigation
      replaceFragment(SummaryFragment.newInstance());
    } else if (item.getItemId() == R.id.action_main_settings) {
      // TODO: hide bottom navigation
      // TODO: disable settings and sync menu
      replaceFragment(UserSettingsFragment.newInstance());
    } else if (item.getItemId() == R.id.action_main_sync) {
      // TODO: hide bottom navigation
      Intent intent = new Intent(this, SyncActivity.class);
      startActivityForResult(intent, BaseActivity.REQUEST_SYNC);
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    Log.d(TAG, "++onRequestPermissionsResult(int, String[], int[])");
    switch (requestCode) {
      case BaseActivity.REQUEST_NOTIFICATION:
      case BaseActivity.REQUEST_SYNC:
        break;
      case BaseActivity.REQUEST_PERMISSIONS:
        if (checkForPermission()) {
//          MusicLibrary.getContent(MainActivity.this);
//          mMediaBrowserHelper = new MediaBrowserConnection(this);
//          mMediaBrowserHelper.registerCallback(new MediaBrowserListener());
//          mMediaBrowserHelper.onStart();
        }

        break;
    }
  }

  @Override
  public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);

    Log.d(TAG, "++onRestoreInstanceState(Bundle)");
//    mServiceBound = savedInstanceState.getBoolean("ServiceState");
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {

    Log.d(TAG, "++onSaveInstanceState(Bundle)");
//    savedInstanceState.putBoolean("ServiceState", mServiceBound);
    super.onSaveInstanceState(savedInstanceState);
  }

  @Override
  public void onStart() {
    super.onStart();

    Log.d(TAG, "++onStart()");
    if (mMediaBrowserHelper != null) {
      mMediaBrowserHelper.onStart();
    }
  }

  @Override
  public void onStop() {
    super.onStop();

    Log.d(TAG, "++onStop()");
    if (mSeekBarAudio != null) {
      mSeekBarAudio.disconnectController();
    }

    if (mMediaBrowserHelper != null) {
      mMediaBrowserHelper.onStop();
    }
  }

  /*
    Fragment Callback(s)
   */
  @Override
  public void onAlbumClicked(long albumId) {

    Log.d(TAG, "++onAlbumClicked(long)");
    replaceFragment(MediaFragment.newInstanceByAlbum(albumId));
  }

  @Override
  public void onArtistClicked(long artistId) {

    Log.d(TAG, "++onArtistClicked(long)");
    replaceFragment(MediaFragment.newInstanceByArtist(artistId));
  }

  @Override
  public void onMediaAddToPlaylist(MediaEntity mediaEntity) {

    Log.d(TAG, "++onMediaAddToPlaylist(MediaEntity)");
    Snackbar.make(
      findViewById(R.id.main_fragment_container),
      getString(R.string.not_yet_implemented),
      Snackbar.LENGTH_SHORT).show();
  }

  @Override
  public void onMediaClicked(MediaDetails mediaDetails) {

    Log.d(TAG, "++onMediaClicked(MediaDetails)");
    mPlayerIncludeView.setVisibility(View.VISIBLE);
    mPlayerAlbumImage.setImageBitmap(Utils.loadImageFromStorage(this, mediaDetails.ArtistId, mediaDetails.AlbumId));
    mPlayerSongText.setText(mediaDetails.Title);
    mPlayerAlbumText.setText(mediaDetails.AlbumName);
  }

  @Override
  public void onMediaUpdateFavorites(MediaEntity mediaEntity) {

    Log.d(TAG, "++onMediaUpdateFavorites(MediaEntity)");
    PlaylistEntity playlistEntity = new PlaylistEntity();
    playlistEntity.PlaylistId = BaseActivity.DEFAULT_PLAYLIST_FAVORITES_ID;
    playlistEntity.MediaId = mediaEntity.MediaId;
    playlistEntity.PlaylistName = BaseActivity.DEFAULT_PLAYLIST_FAVORITES;
    new PlaylistAsync(
      this,
      MediaRepository.getInstance(StreamyTunesDatabase.getInstance(this).mediaDao()),
      playlistEntity,
      mediaEntity.IsFavorite).execute();
    updateRowAsync(mediaEntity);
    Snackbar.make(
      findViewById(R.id.main_fragment_container),
      mediaEntity.IsFavorite ? getString(R.string.adding_to_favorites) : getString(R.string.removing_from_favorites),
      Snackbar.LENGTH_SHORT).show();
  }

  @Override
  public void onPlaylistClicked(String playlistId) {

    Log.d(TAG, "++onPlaylistClicked(String)");
    replaceFragment(MediaFragment.newInstanceByPlaylist(playlistId));
  }

  @Override
  public void onQueryComplete(int mediaFound) {

    Log.d(TAG, "++onQueryComplete(int)");
    if (mediaFound == 0) {
      Snackbar.make(
        findViewById(R.id.main_fragment_container),
        getString(R.string.no_media_found),
        Snackbar.LENGTH_LONG)
        .setAction(R.string.sync, v -> {
          Intent intent = new Intent(MainActivity.this, SyncActivity.class);
          startActivity(intent);
          finish();
        }).show();
    }
  }

  @Override
  public void onSummaryAlbumsClicked() {

    Log.d(TAG, "++onSummaryAlbumsClicked()");
    replaceFragment(AlbumsFragment.newInstance());
  }

  @Override
  public void onSummaryArtistsClicked() {

    Log.d(TAG, "++onSummaryArtistsClicked()");
    replaceFragment(ArtistsFragment.newInstance());
  }

  @Override
  public void onSummaryPlaylistsClicked() {

    Log.d(TAG, "++onSummaryPlaylistsClicked()");
    replaceFragment(PlaylistFragment.newInstance());
  }

  @Override
  public void onSummaryAudiobooksClicked() {

    Log.d(TAG, "++onSummaryAudiobooksClicked()");
  }

  @Override
  public void onSummaryPodcastsClicked() {

    Log.d(TAG, "++onSummaryPodcastsClicked()");
  }

  /*
      Async Callback(s)
     */
  public void mediaEntityUpdated() {

    Log.d(TAG, "++mediaEntityUpdated()");
  }

  public void playlistUpdated() {

    Log.d(TAG, "++playlistUpdated()");
  }

  /*
    Private Methods
  */
  private boolean checkForPermission() {

    Log.d(TAG, "++checkForPermission(String)");
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED ||
      ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
      ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.FOREGROUND_SERVICE) ||
        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE) ||
        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WAKE_LOCK)) {
        String requestMessage = getString(R.string.permission_phone);
        Snackbar.make(
          findViewById(R.id.main_fragment_container),
          requestMessage,
          Snackbar.LENGTH_INDEFINITE)
          .setAction(
            getString(R.string.ok),
            view -> ActivityCompat.requestPermissions(
              MainActivity.this,
              new String[]{
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WAKE_LOCK},
              BaseActivity.REQUEST_PHONE_PERMISSIONS))
          .show();
      } else {
        ActivityCompat.requestPermissions(
          this,
          new String[]{
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WAKE_LOCK},
          BaseActivity.REQUEST_PHONE_PERMISSIONS);
      }
    } else {
      return true;
    }
//      int currentAudioIndex = PreferenceUtils.getAudioIndex(this);
//      List<MediaDetails> audioList = PreferenceUtils.getAudioList(this);
//      MusicLibrary.getContent(getApplicationContext());

//      if (!mServiceBound) {
//        Log.w(TAG, "Service was not bound before calling permission check.");
//        Intent playerIntent = new Intent(this, MediaPlayerService.class);
//        startService(playerIntent);
//        bindService(playerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
//      } else {
//        Intent broadcastIntent = new Intent(BaseActivity.ACTION_PLAY);
//        switch (action) {
//          case ACTION_NEXT:
//            mMediaBrowserHelper.getTransportControls().skipToNext();
//            if (currentAudioIndex == audioList.size() - 1) {
//              currentAudioIndex = 0; // TODO: add repeat functionality, for now just loop to beginning of audio list
//            } else {
//              ++currentAudioIndex;
//            }
//
//            PreferenceUtils.setAudioIndex(getApplicationContext(), currentAudioIndex);
//            broadcastIntent = new Intent(BaseActivity.ACTION_PLAY);
//            break;
//          case ACTION_PAUSE:
//            mMediaBrowserHelper.getTransportControls().pause();
//            broadcastIntent = new Intent(BaseActivity.ACTION_PAUSE);
//            break;
//          case ACTION_PLAY:
//            mMediaBrowserHelper.getTransportControls().play();
//            break;
//          case ACTION_PREVIOUS:
//            mMediaBrowserHelper.getTransportControls().skipToPrevious();
//            if (currentAudioIndex == 0) {
//              currentAudioIndex = audioList.size() - 1; // TODO: add repeat functionality, for now just loop to end of audio list
//            } else {
//              --currentAudioIndex;
//            }
//
//            PreferenceUtils.setAudioIndex(getApplicationContext(), currentAudioIndex);
//            broadcastIntent = new Intent(BaseActivity.ACTION_PLAY);
//            break;
//          default:
//            break;
//        sendBroadcast(broadcastIntent);
//      }

    return false;
  }

  private void replaceFragment(Fragment fragment) {

    Log.d(TAG, "++replaceFragment()");
    getSupportFragmentManager()
      .beginTransaction()
      .replace(R.id.main_fragment_container, fragment)
      .addToBackStack(null)
      .commit();
  }

  private void updateRowAsync(MediaEntity mediaEntity) {

    Log.d(TAG, "++updateRowAsync(MediaEntity)");
    new UpdateRowAsync(
      this,
      MediaRepository.getInstance(StreamyTunesDatabase.getInstance(this).mediaDao()), mediaEntity).execute();
  }

  /*
    Private Class(es)
   */
  /**
   * Customize the connection to our MediaBrowserServiceCompat and implement our app specific desires.
   */
  private class MediaBrowserConnection extends MediaBrowserHelper {

    private final String TAG = BaseActivity.BASE_TAG + MediaBrowserConnection.class.getSimpleName();

    private MediaBrowserConnection(Context context) {
      super(context, MusicService.class);
    }

    @Override
    protected void onConnected(@NonNull MediaControllerCompat mediaController) {

      Log.d(TAG, "++onConnected(MediaControllerCompat)");
      mSeekBarAudio.setMediaController(mediaController);
    }

    @Override
    protected void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
      super.onChildrenLoaded(parentId, children);

      Log.d(TAG, "++onChildrenLoaded(String, List<MediaBrowserCompat.MediaItem>)");
      final MediaControllerCompat mediaController = getMediaController();

      // queue up all media items for this simple sample
      for (final MediaBrowserCompat.MediaItem mediaItem : children) {
        mediaController.addQueueItem(mediaItem.getDescription());
      }

      // call prepare now so pressing play just works
      mediaController.getTransportControls().prepare();
    }
  }

  /**
   * Implementation of the {@link MediaControllerCompat.Callback} methods we're interested in.
   * <p>
   * Here would also be where one could override
   * {@code onQueueChanged(List<MediaSessionCompat.QueueItem> queue)} to get informed when items
   * are added or removed from the queue. We don't do this here in order to keep the UI
   * simple.
   */
  private class MediaBrowserListener extends MediaControllerCompat.Callback {

    private final String TAG = BaseActivity.BASE_TAG + MediaBrowserListener.class.getSimpleName();

    @Override
    public void onPlaybackStateChanged(PlaybackStateCompat playbackState) {

      Log.d(TAG, "++onPlaybackStateChanged(PlaybackStateCompat)");
      mIsPlaying = playbackState != null && playbackState.getState() == PlaybackStateCompat.STATE_PLAYING;

      if (mPlayerPlayPauseImage != null) {
        if (mIsPlaying) {
          mPlayerPlayPauseImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause_dark, null));
        } else {
          mPlayerPlayPauseImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_dark, null));
        }
      }
    }

    @Override
    public void onMetadataChanged(MediaMetadataCompat mediaMetadata) {

      Log.d(TAG, "++onMetadataChanged(MediaMetadataCompat)");
      if (mediaMetadata == null) {
        return;
      }

      mPlayerSongText.setText(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
      mPlayerAlbumText.setText(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
      mPlayerAlbumImage.setImageBitmap(MusicLibrary.getAlbumBitmap(mediaMetadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)));
    }

    @Override
    public void onSessionDestroyed() {
      super.onSessionDestroyed();
    }

    @Override
    public void onQueueChanged(List<MediaSessionCompat.QueueItem> queue) {
      super.onQueueChanged(queue);
    }
  }
}
