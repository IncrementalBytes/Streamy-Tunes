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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import net.whollynugatory.streamytunes.android.MediaPlayerService;
import net.whollynugatory.streamytunes.android.PlaybackStatus;
import net.whollynugatory.streamytunes.android.PlaylistAsync;
import net.whollynugatory.streamytunes.android.PreferenceUtils;
import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.UpdateRowAsync;
import net.whollynugatory.streamytunes.android.Utils;
import net.whollynugatory.streamytunes.android.db.MediaDetails;
import net.whollynugatory.streamytunes.android.db.StreamyTunesDatabase;
import net.whollynugatory.streamytunes.android.db.entity.MediaEntity;
import net.whollynugatory.streamytunes.android.db.entity.PlaylistEntity;
import net.whollynugatory.streamytunes.android.db.repository.MediaRepository;
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

//  private ServiceState mCurrentState;
  private PlaybackStatus mCurrentPlaybackStatus;

  private View mPlayerIncludeView;
  private ImageView mPlayerAlbumImage;
  private TextView mPlayerSongText;
  private TextView mPlayerAlbumText;
  private ImageButton mPlayerPreviousImage;
  private ImageButton mPlayerPlayPauseImage;
  private ImageButton mPlayerNextImage;

  private MediaPlayerService mPlayerService;
  private boolean mServiceBound = false;
  private ServiceConnection mServiceConnection;

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    Log.d(TAG, "++onActivityResult(int, int, Intent)");
    switch (requestCode) {
      case BaseActivity.REQUEST_SYNC:
      if (resultCode == RESULT_OK) {
        Log.d(TAG, "Result OK from other activity.");
      }

      break;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    setContentView(R.layout.activity_main);

//    mCurrentState = ServiceState.Preparing;

    mPlayerIncludeView = findViewById(R.id.main_include_player);
    mPlayerAlbumImage = findViewById(R.id.player_image_album);
    mPlayerAlbumText = findViewById(R.id.player_text_details);
    mPlayerSongText = findViewById(R.id.player_text_song);

    mPlayerNextImage = findViewById(R.id.player_image_next);
    mPlayerNextImage.setOnClickListener(v -> {

        List<MediaDetails> mediaDetailsList = PreferenceUtils.getAudioList(this);
        if (mediaDetailsList.size() > 1) {
          checkForPermission(BaseActivity.ACTION_NEXT);
        }
      });

    mPlayerPlayPauseImage = findViewById(R.id.player_image_play_pause);
    mPlayerPlayPauseImage.setOnClickListener(v -> {

        if (mCurrentPlaybackStatus.equals(PlaybackStatus.PLAYING)) {
          checkForPermission(ACTION_PAUSE);
        } else {
          checkForPermission(ACTION_PLAY);
        }
      });

    mPlayerPreviousImage = findViewById(R.id.player_image_prev);
    mPlayerPreviousImage.setOnClickListener(v -> {

      List<MediaDetails> mediaDetailsList = PreferenceUtils.getAudioList(this);
      if (mediaDetailsList.size() > 1) {
        checkForPermission(BaseActivity.ACTION_PREVIOUS);
      }
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
          replaceFragment(SummaryFragment.newInstance());
          return true;
        case R.id.navigation_audiobook:
//          PreferenceUtils.setIsAudiobook(this);
//          return true;
        case R.id.navigation_podcast:
//          PreferenceUtils.setIsPodcast(this);
          Snackbar.make(
            findViewById(R.id.main_fragment_container),
            getString(R.string.not_yet_implemented),
            Snackbar.LENGTH_LONG).show();
          return true;
      }

      return false;
    });

    mServiceConnection = new ServiceConnection() {

      @Override
      public void onServiceConnected(ComponentName name, IBinder service) {

        Log.d(TAG, "++onServiceConnected(ComponentName, IBinder)");
        MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
        mPlayerService = binder.getService();
        mServiceBound = true;
//        mPlayerService.registerClient(getParent());
        Log.d(TAG, "Service Bound");

        final Observer<PlaybackStatus> statusObserver = playbackStatus -> {

          if (playbackStatus.equals(PlaybackStatus.PAUSED) || playbackStatus.equals(PlaybackStatus.STOPPED)) {
            if (mPlayerPlayPauseImage != null) {
              mPlayerPlayPauseImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_dark, null));
            }
          } else if (playbackStatus.equals(PlaybackStatus.PLAYING)) {
            if (mPlayerPlayPauseImage != null) {
              mPlayerPlayPauseImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause_dark, null));
            }
          }

          mCurrentPlaybackStatus = playbackStatus;
        };

        mPlayerService.MusicServiceState.observe(MainActivity.this, statusObserver);

        final Observer<MediaDetails> mediaObserver = mediaDetails -> {

          if (mediaDetails.MediaId != BaseActivity.UNKNOWN_ID) {
            Bitmap albumArt = Utils.loadImageFromStorage(getApplicationContext(), mediaDetails.ArtistId, mediaDetails.AlbumId);
            if (albumArt != null) {
              mPlayerAlbumImage.setImageBitmap(albumArt);
            }

            mPlayerAlbumText.setText(mediaDetails.AlbumName);
            mPlayerSongText.setText(mediaDetails.Title);
          }
        };

        mPlayerService.CurrentMediaDetails.observe(MainActivity.this, mediaObserver);
      }

      @Override
      public void onServiceDisconnected(ComponentName name) {

        Log.d(TAG, "++onServiceDisconnected(ComponentName)");
        mServiceBound = false;
      }
    };

    replaceFragment(SummaryFragment.newInstance());
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
    if (mServiceBound) {
      unbindService(mServiceConnection);
      mPlayerService.stopSelf();
    }
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
    if (requestCode == BaseActivity.REQUEST_PHONE_PERMISSIONS) {
      checkForPermission(); // TODO: add feedback on permission denied
    }
  }

  @Override
  public void onRestoreInstanceState(@NonNull  Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);

    mServiceBound = savedInstanceState.getBoolean("ServiceState");
  }

  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {

    savedInstanceState.putBoolean("ServiceState", mServiceBound);
    super.onSaveInstanceState(savedInstanceState);
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
  public void onMediaClicked() {

    Log.d(TAG, "++onMediaClicked()");
    checkForPermission();
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
  private void checkForPermission() {

    Log.d(TAG, "++checkForPermission()");
    checkForPermission("");
  }

  private void checkForPermission(String action) {

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
              new String[]{Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WAKE_LOCK},
              BaseActivity.REQUEST_PHONE_PERMISSIONS))
          .show();
      } else {
        ActivityCompat.requestPermissions(
          this,
          new String[]{Manifest.permission.FOREGROUND_SERVICE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WAKE_LOCK},
          BaseActivity.REQUEST_PHONE_PERMISSIONS);
      }
    } else {
      int currentAudioIndex = PreferenceUtils.getAudioIndex(this);
      List<MediaDetails> audioList = PreferenceUtils.getAudioList(this);
      MediaDetails currentMediaDetails = audioList.get(currentAudioIndex);

      /*
        MediaPlayerService implementation
       */
      if (!mServiceBound) {
        Log.w(TAG, "Service was not bound before calling permission check.");
        Intent playerIntent = new Intent(this, MediaPlayerService.class);
        startService(playerIntent);
        bindService(playerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
      } else {
        Intent broadcastIntent = new Intent(BaseActivity.ACTION_PLAY);
        switch (action) {
          case ACTION_NEXT:
            if (currentAudioIndex == audioList.size() - 1) {
              currentAudioIndex = 0; // TODO: add repeat functionality, for now just loop to beginning of audio list
            } else {
              ++currentAudioIndex;
            }

            PreferenceUtils.setAudioIndex(getApplicationContext(), currentAudioIndex);
            broadcastIntent = new Intent(BaseActivity.ACTION_PLAY);
            break;
          case ACTION_PAUSE:
            broadcastIntent = new Intent(BaseActivity.ACTION_PAUSE);
            break;
          case ACTION_PREVIOUS:
            if (currentAudioIndex == 0) {
              currentAudioIndex = audioList.size() - 1; // TODO: add repeat functionality, for now just loop to end of audio list
            } else {
              --currentAudioIndex;
            }

            PreferenceUtils.setAudioIndex(getApplicationContext(), currentAudioIndex);
            broadcastIntent = new Intent(BaseActivity.ACTION_PLAY);
            break;
        }

        sendBroadcast(broadcastIntent);
//      if (!mServiceBound) {
//        Intent playerIntent = new Intent(this, MediaPlayerService.class);
//        startService(playerIntent);
//        bindService(playerIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
//      } else {
//        Intent broadcastIntent = new Intent(BaseActivity.BROADCAST_PLAY_AUDIO);
//        sendBroadcast(broadcastIntent);
//      }
      }

      mPlayerIncludeView.setVisibility(View.VISIBLE);

      /*
        MusicService implementation
       */
//      Intent serviceIntent = new Intent(BaseActivity.ACTION_PLAY);
//      switch (action) {
//        case BaseActivity.ACTION_NEXT:
//          if (audioList.size() > 1) {
//            mPlayerNextImage.setEnabled(true);
//            currentAudioIndex++;
//            if (currentAudioIndex > audioList.size()) {
//              currentAudioIndex = 0;
//            }
//          } else {
//            mPlayerNextImage.setEnabled(false);
//          }
//
//          currentMediaDetails = audioList.get(currentAudioIndex);
//          mPlayerPlayPauseImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause_dark, null));
//          mCurrentState = ServiceState.Playing;
//          break;
//        case BaseActivity.ACTION_PAUSE:
//          mPlayerPlayPauseImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_play_dark, null));
//          serviceIntent = new Intent(BaseActivity.ACTION_PAUSE);
//          mCurrentState = ServiceState.Paused;
//          break;
//        case BaseActivity.ACTION_PLAY:
//          mPlayerPlayPauseImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause_dark, null));
//          mCurrentState = ServiceState.Playing;
//          break;
//        case BaseActivity.ACTION_PREVIOUS:
//          if (audioList.size() > 1) {
//            mPlayerPreviousImage.setEnabled(true);
//            currentAudioIndex--;
//            if (currentAudioIndex < 0) {
//              currentAudioIndex = audioList.size() - 1;
//            }
//          } else {
//            mPlayerPreviousImage.setEnabled(false);
//          }
//
//          currentMediaDetails = audioList.get(currentAudioIndex);
//          mPlayerPlayPauseImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause_dark, null));
//          mCurrentState = ServiceState.Playing;
//          break;
//        default:
//          mCurrentState = ServiceState.Playing;
//          mPlayerPlayPauseImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_pause_dark, null));
//          break;
//      }
//
//      if (currentMediaDetails != null) {
//        Bitmap albumArt = Utils.loadImageFromStorage(this, currentMediaDetails.ArtistId, currentMediaDetails.AlbumId);
//        if (albumArt != null) {
//          mPlayerAlbumImage.setImageBitmap(albumArt);
//        }
//
//        mPlayerAlbumText.setText(currentMediaDetails.AlbumName);
//        mPlayerSongText.setText(currentMediaDetails.Title);
//      }
//
//      if (!action.isEmpty()) {
//        serviceIntent = new Intent(action);
//      }
//
//      mPlayerIncludeView.setVisibility(View.VISIBLE);
//      serviceIntent.setPackage(this.getPackageName());
//      startService(serviceIntent);
    }
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
}
