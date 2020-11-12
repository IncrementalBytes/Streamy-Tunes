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

import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;

import net.whollynugatory.streamytunes.android.db.entity.MediaEntity;
import net.whollynugatory.streamytunes.android.db.repository.MediaRepository;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;
import net.whollynugatory.streamytunes.android.ui.SyncActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;

public class MediaSearchAsync extends AsyncTask<Void, Void, Void> {

  private static final String TAG = BaseActivity.BASE_TAG + MediaSearchAsync.class.getSimpleName();

  private final WeakReference<SyncActivity> mWeakReference;

  private final MediaRepository mRepository;

  public MediaSearchAsync(SyncActivity context, MediaRepository repository) {

    mWeakReference = new WeakReference<>(context);
    mRepository = repository;
  }

  @Override
  protected Void doInBackground(final Void... params) {

    getContent(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
    return null;
  }

  protected void onPostExecute(Void nothingReally) {

    Log.d(TAG, "++onPostExecute()");
    SyncActivity activity = mWeakReference.get();
    if (activity == null) {
      Log.e(TAG, "SyncActivity is null or detached.");
      return;
    }

    activity.mediaSearchComplete();
  }

  private void getContent(Uri contentSource) {

    Log.d(TAG, "++getContent(Uri)");
    String sortOrder = MediaStore.Audio.Media.YEAR + " DESC";
    String[] projection = new String[]{
      MediaStore.Audio.Media._ID,
      MediaStore.Audio.Media.ALBUM,
      MediaStore.Audio.Media.ALBUM_ID,
      MediaStore.Audio.Media.ARTIST,
      MediaStore.Audio.Media.ARTIST_ID,
      MediaStore.Audio.Media.TITLE,
      MediaStore.Audio.Media.TRACK,
      MediaStore.Audio.Media.YEAR,
      MediaStore.Audio.Media.IS_AUDIOBOOK,
      MediaStore.Audio.Media.IS_MUSIC,
      MediaStore.Audio.Media.IS_PODCAST
    };

    try (Cursor cursor = mWeakReference.get().getContentResolver().query(
      contentSource,
      projection,
      null,
      null,
      sortOrder)) {

      assert cursor != null;
      while (cursor.moveToNext()) {
        MediaEntity mediaEntity = new MediaEntity();
        try {
          mediaEntity.Id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
          mediaEntity.AlbumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
          mediaEntity.AlbumName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
          mediaEntity.ArtistId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
          mediaEntity.ArtistName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
          mediaEntity.IsAudiobook = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_AUDIOBOOK)) != 0;
          mediaEntity.IsExternal = contentSource.equals(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
          mediaEntity.IsMusic = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)) != 0;
          mediaEntity.IsPodcast = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_PODCAST)) != 0;
          mediaEntity.Title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
          mediaEntity.TrackNumber = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK));
          mediaEntity.Year = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR));
          saveToAppStorage(mediaEntity);
          mRepository.insertMedia(mediaEntity);
        } catch (Exception e) {
          Log.e(TAG, "Failed to write media entity.", e);
        }
      }
    }
  }

  private void saveToAppStorage(MediaEntity mediaEntity) {

    ContextWrapper cw = new ContextWrapper(mWeakReference.get());
    File directory = cw.getDir(mWeakReference.get().getString(R.string.album), Context.MODE_PRIVATE);
    File destinationPath = new File(directory, mediaEntity.ArtistId + "-" + mediaEntity.AlbumId + ".jpg");
    try (FileOutputStream fos = new FileOutputStream(destinationPath)) {
      Uri localSource = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + mediaEntity.Id);
      mWeakReference.get().getContentResolver().loadThumbnail(
        localSource,
        new Size(640, 480), null)
        .compress(Bitmap.CompressFormat.PNG, 100, fos);
    } catch (Exception e) {
      Log.e(TAG, "Failed to save image", e);
    }
  }
}
