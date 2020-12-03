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

import net.whollynugatory.streamytunes.android.db.entity.PlaylistEntity;
import net.whollynugatory.streamytunes.android.db.repository.MediaRepository;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;
import net.whollynugatory.streamytunes.android.ui.MainActivity;

import java.lang.ref.WeakReference;

public class PlaylistAsync extends AsyncTask<Void, Void, Void> {

  private static final String TAG = BaseActivity.BASE_TAG + PlaylistAsync.class.getSimpleName();

  private final boolean mAddToPlaylist;
  private final PlaylistEntity mPlaylistEntity;
  private final MediaRepository mRepository;
  private final WeakReference<MainActivity> mWeakReference;

  public PlaylistAsync(MainActivity context, MediaRepository repository, PlaylistEntity playlistEntity, boolean addToPlaylist) {

    mAddToPlaylist = addToPlaylist;
    mPlaylistEntity = playlistEntity;
    mRepository = repository;
    mWeakReference = new WeakReference<>(context);
  }

  @Override
  protected Void doInBackground(final Void... params) {

    if (mAddToPlaylist) {
      mRepository.insertPlaylist(mPlaylistEntity);
    } else {
      mRepository.deletePlaylist(mPlaylistEntity);
    }

    return null;
  }

  protected void onPostExecute(Void nothingReally) {

    Log.d(TAG, "++onPostExecute()");
    MainActivity activity = mWeakReference.get();
    if (activity == null) {
      Log.e(TAG, "MainActivity is null or detached.");
      return;
    }

    activity.playlistUpdated();
  }
}
