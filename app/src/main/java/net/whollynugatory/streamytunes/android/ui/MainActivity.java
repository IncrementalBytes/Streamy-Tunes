/*
 * Copyright 2020 Ryan Ward
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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import net.whollynugatory.streamytunes.android.PlaylistAsync;
import net.whollynugatory.streamytunes.android.PreferenceUtils;
import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.UpdateRowAsync;
import net.whollynugatory.streamytunes.android.db.MediaDetails;
import net.whollynugatory.streamytunes.android.db.StreamyTunesDatabase;
import net.whollynugatory.streamytunes.android.db.entity.MediaEntity;
import net.whollynugatory.streamytunes.android.db.entity.PlaylistEntity;
import net.whollynugatory.streamytunes.android.db.repository.MediaRepository;
import net.whollynugatory.streamytunes.android.ui.fragments.AlbumsFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.ArtistsFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.PlayerFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.MediaFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.PlaylistFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.SummaryFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.UserSettingsFragment;

import java.util.ArrayList;
import java.util.Collection;

public class MainActivity extends BaseActivity implements
  AlbumsFragment.OnAlbumListener,
  ArtistsFragment.OnArtistListener,
  MediaFragment.OnMediaListener,
  PlayerFragment.OnPlayerListener,
  PlaylistFragment.OnPlaylistListener,
  SummaryFragment.OnSummaryListener {

  private static final String TAG = BaseActivity.BASE_TAG + MainActivity.class.getSimpleName();

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    Log.d(TAG, "++onActivityResult(int, int, Intent)");
    if (requestCode == BaseActivity.REQUEST_SYNC) {
      if (resultCode != RESULT_OK) {
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    setContentView(R.layout.activity_main);
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

    checkForPermission();
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
      Intent intent = new Intent(MainActivity.this, SyncActivity.class);
      startActivity(intent);
      finish();
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    Log.d(TAG, "++onRequestPermissionsResult(int, String[], int[])");
    if (requestCode == BaseActivity.REQUEST_STORAGE_PERMISSIONS) {
      checkForPermission();
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
      Snackbar.LENGTH_LONG).show();
  }

  @Override
  public void onMediaClicked(Collection<MediaDetails> mediaDetailsCollection) {

    Log.d(TAG, "++onMediaClicked(Collection<MediaDetails>)");
    replaceFragment(PlayerFragment.newInstance(new ArrayList<>(mediaDetailsCollection)));
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
  }

  @Override
  public void onMediaUpdateVisible(MediaEntity mediaEntity) {

    Log.d(TAG, "++onMediaUpdateVisible(MediaEntity)");
    updateRowAsync(mediaEntity);
  }

  @Override
  public void onPlayerSongComplete() {

    Log.d(TAG, "++onPlayerSongComplete()");
  }

  @Override
  public void onPlaylistClicked(String playlistId) {

    Log.d(TAG, "++onPlaylistClicked(String)");
    replaceFragment(MediaFragment.newInstanceByPlaylist(playlistId));
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

    Log.d(TAG, "++checkForPermission(String, int)");
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
        String requestMessage = getString(R.string.permission_storage);
        Snackbar.make(
          findViewById(R.id.main_fragment_container),
          requestMessage,
          Snackbar.LENGTH_INDEFINITE)
          .setAction(
            getString(R.string.ok),
            view -> ActivityCompat.requestPermissions(
              MainActivity.this,
              new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
              BaseActivity.REQUEST_STORAGE_PERMISSIONS))
          .show();
      } else {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, BaseActivity.REQUEST_STORAGE_PERMISSIONS);
      }
    } else {
      Log.d(TAG, "Permission granted: " + Manifest.permission.READ_EXTERNAL_STORAGE);
      replaceFragment(SummaryFragment.newInstance());
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
