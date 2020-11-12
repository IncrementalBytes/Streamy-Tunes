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
package net.whollynugatory.streamytunes.android;

import android.os.AsyncTask;
import android.util.Log;

import net.whollynugatory.streamytunes.android.db.entity.MediaEntity;
import net.whollynugatory.streamytunes.android.db.repository.MediaRepository;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;
import net.whollynugatory.streamytunes.android.ui.MainActivity;

import java.lang.ref.WeakReference;

public class UpdateRowAsync extends AsyncTask<Void, Void, Void> {

  private static final String TAG = BaseActivity.BASE_TAG + UpdateRowAsync.class.getSimpleName();

  private final WeakReference<MainActivity> mWeakReference;

  private final MediaRepository mRepository;
  private MediaEntity mMediaEntity;

  public UpdateRowAsync(MainActivity context, MediaRepository repository, MediaEntity mediaEntity) {

    mWeakReference = new WeakReference<>(context);
    mRepository = repository;
    mMediaEntity = mediaEntity;
  }

  @Override
  protected Void doInBackground(final Void... params) {

    mRepository.updateMedia(mMediaEntity);
    return null;
  }

  protected void onPostExecute(Void nothingReally) {

    Log.d(TAG, "++onPostExecute()");
    MainActivity activity = mWeakReference.get();
    if (activity == null) {
      Log.e(TAG, "MainActivity is null or detached.");
      return;
    }

    activity.mediaEntityUpdated();
  }
}
