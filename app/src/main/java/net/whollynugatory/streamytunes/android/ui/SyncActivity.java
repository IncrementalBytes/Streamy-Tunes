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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import net.whollynugatory.streamytunes.android.MediaSearchAsync;
import net.whollynugatory.streamytunes.android.R;
import net.whollynugatory.streamytunes.android.db.StreamyTunesDatabase;
import net.whollynugatory.streamytunes.android.db.repository.MediaRepository;

public class SyncActivity extends BaseActivity {

  private static final String TAG = BaseActivity.BASE_TAG + SyncActivity.class.getSimpleName();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d(TAG, "++onCreate(Bundle)");
    setContentView(R.layout.activity_sync);
    checkForPermission(); // TODO: add feedback on permission denied
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    Log.d(TAG, "++onDestroy()");
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

    Log.d(TAG, "++onRequestPermissionsResult(int, String[], int[])");
    if (requestCode == BaseActivity.REQUEST_STORAGE_PERMISSIONS) {
      checkForPermission(); // TODO: add feedback on permission denied
    }
  }

  /*
    Public Method(s)
   */
  public void mediaSearchComplete() {

    Log.d(TAG, "++mediaSearchComplete()");
    setResult(RESULT_OK);
    finish();
  }

  /*
    Private Method(s)
   */
  private boolean checkForPermission() {

    Log.d(TAG, "++checkForPermission(String, int)");
    if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) ||
      (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_MEDIA_LOCATION) != PackageManager.PERMISSION_GRANTED)){
      if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_MEDIA_LOCATION)) {
        String requestMessage = getString(R.string.permission_storage);
        Snackbar.make(
          findViewById(R.id.main_fragment_container), // TODO: update layout to support this
          requestMessage,
          Snackbar.LENGTH_INDEFINITE)
          .setAction(
            getString(R.string.ok),
            view -> ActivityCompat.requestPermissions(
              SyncActivity.this,
              new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_MEDIA_LOCATION},
              BaseActivity.REQUEST_STORAGE_PERMISSIONS))
          .show();
      } else {
        ActivityCompat.requestPermissions(
          this,
          new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.ACCESS_MEDIA_LOCATION},
          BaseActivity.REQUEST_STORAGE_PERMISSIONS);
      }
    } else {
      Log.d(TAG, "Permissions granted: " + Manifest.permission.READ_EXTERNAL_STORAGE + ", " + Manifest.permission.ACCESS_MEDIA_LOCATION);
      new MediaSearchAsync(this, MediaRepository.getInstance(StreamyTunesDatabase.getInstance(this).mediaDao())).execute();
      return true;
    }

    return false;
  }
}
