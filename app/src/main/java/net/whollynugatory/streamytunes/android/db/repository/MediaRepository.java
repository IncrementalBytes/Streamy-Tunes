/*
 * Copyright 2019 Ryan Ward
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
package net.whollynugatory.streamytunes.android.db.repository;

import android.util.Log;

import net.whollynugatory.streamytunes.android.db.StreamyTunesDatabase;
import net.whollynugatory.streamytunes.android.db.dao.MediaDao;
import net.whollynugatory.streamytunes.android.db.entity.MediaEntity;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;

import java.util.List;

import androidx.lifecycle.LiveData;

public class MediaRepository {

  private static final String TAG = BaseActivity.BASE_TAG + MediaRepository.class.getSimpleName();

  private static volatile MediaRepository INSTANCE;

  private final MediaDao mMediaDao;

  private MediaRepository(MediaDao mediaDao) {

    mMediaDao = mediaDao;
  }

  public static MediaRepository getInstance(final MediaDao mediaDao) {

    if (INSTANCE == null) {
      synchronized (MediaRepository.class) {
        if (INSTANCE == null) {
          Log.d(TAG, "++getInstance(Context)");
          INSTANCE = new MediaRepository(mediaDao);
        }
      }
    }

    return INSTANCE;
  }

  public void deleteMedia(String mediaId) {

    StreamyTunesDatabase.databaseWriteExecutor.execute(() -> mMediaDao.delete(mediaId));
  }

  public LiveData<List<MediaEntity>> getAll() {

    return mMediaDao.getAll();
  }

  public LiveData<List<MediaEntity>> getAllAudiobooks() {

    return mMediaDao.getAllAudiobooks();
  }

  public LiveData<List<MediaEntity>> getAllMusic() {

    return mMediaDao.getAllMusic();
  }

  public LiveData<List<MediaEntity>> getAllPodcasts() {

    return mMediaDao.getAllPodcasts();
  }

  public void insertMedia(MediaEntity mediaEntity) {

    StreamyTunesDatabase.databaseWriteExecutor.execute(() -> mMediaDao.insert(mediaEntity));
  }

  public void updateMedia(MediaEntity mediaEntity) {

    StreamyTunesDatabase.databaseWriteExecutor.execute(() -> mMediaDao.update(mediaEntity));
  }
}
