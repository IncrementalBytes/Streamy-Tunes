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

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

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
    new MediaSearchAsync(this, MediaRepository.getInstance(StreamyTunesDatabase.getInstance(this).mediaDao())).execute();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    Log.d(TAG, "++onDestroy()");
  }

  /*
    Public Method(s)
   */
  public void mediaSearchComplete() {

    Log.d(TAG, "++mediaSearchComplete()");
    Intent intent = new Intent(SyncActivity.this, MainActivity.class);
    startActivity(intent);
    finish();
  }
}
