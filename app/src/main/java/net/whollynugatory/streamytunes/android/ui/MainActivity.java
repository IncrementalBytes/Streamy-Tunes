package net.whollynugatory.streamytunes.android.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.snackbar.Snackbar;

import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.Utils;
import net.whollynugatory.streamytunes.android.db.views.AlbumDetails;
import net.whollynugatory.streamytunes.android.db.views.ArtistDetails;
import net.whollynugatory.streamytunes.android.db.views.SongDetails;
import net.whollynugatory.streamytunes.android.ui.fragments.AlbumsFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.ArtistsFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.PlayerFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.SongsFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.SummaryFragment;
import net.whollynugatory.streamytunes.android.ui.fragments.UserSettingsFragment;

import java.util.ArrayList;
import java.util.Collection;

public class MainActivity extends AppCompatActivity implements
  AlbumsFragment.OnAlbumListener,
  ArtistsFragment.OnArtistListener,
  PlayerFragment.OnPlayerListener,
  SongsFragment.OnSongListener,
  SummaryFragment.OnSummaryListener {

  private static final String TAG = Utils.BASE_TAG + MainActivity.class.getSimpleName();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    setContentView(R.layout.activity_main);
    Toolbar mainToolbar = findViewById(R.id.main_toolbar);
    setSupportActionBar(mainToolbar);

    checkForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, Utils.REQUEST_STORAGE_PERMISSIONS);
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
      replaceFragment(SummaryFragment.newInstance());
    } else if (item.getItemId() == R.id.action_main_settings) {
      replaceFragment(UserSettingsFragment.newInstance());
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    Log.d(TAG, "++onRequestPermissionsResult(int, String[], int[])");
    if (requestCode == Utils.REQUEST_STORAGE_PERMISSIONS) {
      checkForPermission(Manifest.permission.READ_EXTERNAL_STORAGE, Utils.REQUEST_STORAGE_PERMISSIONS);
    }
  }

  /*
    Fragment Callback(s)
   */
  @Override
  public void onAlbumClicked(AlbumDetails albumDetails) {

    Log.d(TAG, "++onAlbumClicked(AlbumDetails)");
    replaceFragment(SongsFragment.newInstance(albumDetails));
  }

  @Override
  public void onArtistClicked(ArtistDetails artistDetails) {

    Log.d(TAG, "++onArtistClicked(ArtistDetails)");
    replaceFragment(SongsFragment.newInstance(new ArrayList<>(artistDetails.Albums.values())));
  }

  @Override
  public void onPlayerSongComplete() {

    Log.d(TAG, "++onPlayerSongComplete()");
  }

  @Override
  public void onSongClicked(Collection<SongDetails> songDetailsCollection) {

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

  /*
    Private Methods
   */
  private void checkForPermission(String permissionName, int permissionId) {

    Log.d(TAG, "++checkForWritePermission(String, int)");
    if (ContextCompat.checkSelfPermission(this, permissionName) != PackageManager.PERMISSION_GRANTED) {
      if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissionName)) {
        String requestMessage = getString(R.string.permission_default);
        if (permissionId == Utils.REQUEST_STORAGE_PERMISSIONS) {
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
      if (permissionId == Utils.REQUEST_STORAGE_PERMISSIONS) {
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
