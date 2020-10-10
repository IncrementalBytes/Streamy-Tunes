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

import net.whollynugatory.streamytunes.android.PreferenceUtils;
import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.db.models.AlbumDetails;
import net.whollynugatory.streamytunes.android.db.models.ArtistDetails;
import net.whollynugatory.streamytunes.android.db.models.AuthorDetails;
import net.whollynugatory.streamytunes.android.db.models.MediaDetails;
import net.whollynugatory.streamytunes.android.ui.fragments.AlbumsFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.ArtistsFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.PlayerFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.MediaFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.SummaryFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.UserSettingsFragment;

import java.util.ArrayList;
import java.util.Collection;

public class MainActivity extends BaseActivity implements
  AlbumsFragment.OnAlbumListener,
  ArtistsFragment.OnArtistListener,
  PlayerFragment.OnPlayerListener,
  MediaFragment.OnMediaListener,
  SummaryFragment.OnSummaryListener {

  private static final String TAG = BaseActivity.BASE_TAG + MainActivity.class.getSimpleName();

  private BottomNavigationView mNavigationView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    setContentView(R.layout.activity_main);
    Toolbar mainToolbar = findViewById(R.id.main_toolbar);
    mNavigationView = findViewById(R.id.main_nav_bottom);

    setSupportActionBar(mainToolbar);

    mNavigationView.setOnNavigationItemSelectedListener(menuItem -> {

      Log.d(TAG, "++onNavigationItemSelectedListener(MenuItem)");
      switch (menuItem.getItemId()) {
        case R.id.navigation_music:
          PreferenceUtils.setIsMusic(this);
          replaceFragment(SummaryFragment.newInstance());
          return true;
        case R.id.navigation_audiobook:
          PreferenceUtils.setIsAudiobook(this);
          replaceFragment(SummaryFragment.newInstance());
          return true;
        case R.id.navigation_podcast:
          PreferenceUtils.setIsPodcast(this);
          replaceFragment(SummaryFragment.newInstance());
          return true;
      }

      return false;
    });

    checkForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, BaseActivity.REQUEST_STORAGE_PERMISSIONS);
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
      // TODO: disable sync menu
      replaceFragment(SummaryFragment.newInstance());
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    Log.d(TAG, "++onRequestPermissionsResult(int, String[], int[])");
    if (requestCode == BaseActivity.REQUEST_STORAGE_PERMISSIONS) {
      checkForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, BaseActivity.REQUEST_STORAGE_PERMISSIONS);
    }
  }

  /*
    Fragment Callback(s)
   */
  @Override
  public void onAlbumClicked(AlbumDetails albumDetails) {

    Log.d(TAG, "++onAlbumClicked(AlbumDetails)");
    replaceFragment(MediaFragment.newInstance(albumDetails));
  }

  @Override
  public void onArtistClicked(ArtistDetails artistDetails) {

    Log.d(TAG, "++onArtistClicked(ArtistDetails)");
    replaceFragment(MediaFragment.newInstance(new ArrayList<>(artistDetails.Albums.values())));
  }

  @Override
  public void onPlayerSongComplete() {

    Log.d(TAG, "++onPlayerSongComplete()");
  }

  @Override
  public void onMediaClicked(Collection<MediaDetails> songDetailsCollection) {

    Log.d(TAG, "++onSongClicked(List<SongDetails>)");
    replaceFragment(PlayerFragment.newInstance(new ArrayList<>(songDetailsCollection)));
  }

  @Override
  public void onSummaryAlbumsClicked(Collection<AlbumDetails> albumDetailsList) {

    Log.d(TAG, "++onSummaryAlbumsClicked(Collection<AlbumDetails>)");
    replaceFragment(AlbumsFragment.newInstance(new ArrayList<>(albumDetailsList)));
  }

  @Override
  public void onSummaryArtistsClicked(Collection<ArtistDetails> artistDetailsList) {

    Log.d(TAG, "++onSummaryArtistsClicked(Collection<ArtistDetails>)");
    replaceFragment(ArtistsFragment.newInstance(new ArrayList<>(artistDetailsList)));
  }

  @Override
  public void onSummaryAudiobooksClicked(Collection<MediaDetails> audiobookDetailsCollection) {

    Log.d(TAG, "++onSummaryPlaylistsClicked(onSummaryAudiobooksClicked(Collection<MediaDetails>)");
  }

  @Override
  public void onSummaryAuthorsClicked(Collection<AuthorDetails> authorDetailsCollection) {

    Log.d(TAG, "++onSummaryAuthorsClicked(Collection<AuthorDetails>)");
  }

  @Override
  public void onSummaryPlaylistsClicked() {

    Log.d(TAG, "++onSummaryPlaylistsClicked()");
  }

  @Override
  public void onSummaryPodcastsClicked(Collection<MediaDetails> podcastDetailsCollection) {

    Log.d(TAG, "++onSummaryPodcastsClicked(Collection<MediaDetails>)");
  }

  /*
      Private Methods
     */
  private void checkForPermission(String permissionName, int permissionId) {

    Log.d(TAG, "++checkForWritePermission(String, int)");
    if (ContextCompat.checkSelfPermission(this, permissionName) != PackageManager.PERMISSION_GRANTED) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionName)) {
        String requestMessage = getString(R.string.permission_default);
        if (permissionId == BaseActivity.REQUEST_STORAGE_PERMISSIONS) {
          requestMessage = getString(R.string.permission_storage);
        }

        Snackbar.make(
          findViewById(R.id.main_fragment_container),
          requestMessage,
          Snackbar.LENGTH_INDEFINITE)
          .setAction(
            getString(R.string.ok),
            view -> ActivityCompat.requestPermissions(
              MainActivity.this,
              new String[]{permissionName},
              permissionId))
          .show();
      } else {
        ActivityCompat.requestPermissions(this, new String[]{permissionName}, permissionId);
      }
    } else {
      Log.d(TAG, "Permission granted: " + permissionName);
      if (permissionId == BaseActivity.REQUEST_STORAGE_PERMISSIONS) {
        replaceFragment(SummaryFragment.newInstance());
      }
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
}
