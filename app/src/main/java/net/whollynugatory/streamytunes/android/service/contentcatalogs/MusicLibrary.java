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
package net.whollynugatory.streamytunes.android.service.contentcatalogs;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.util.Log;

import net.whollynugatory.streamytunes.android.Utils;
import net.whollynugatory.streamytunes.android.ui.BaseActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class MusicLibrary {

  private static final String TAG = BaseActivity.BASE_TAG + MusicLibrary.class.getSimpleName();

  private static final TreeMap<String, MediaMetadataCompat> mMediaMap = new TreeMap<>();
  private static final HashMap<String, File> mAlbumResourceMap = new HashMap<>();
  private static final HashMap<String, String> mMusicFileNameMap = new HashMap<>();

  /*
    Public Method(s)
   */
  public static Bitmap getAlbumBitmap(String mediaId) {

    Log.d(TAG, "++getAlbumBitmap(Context, String)");
    if (mAlbumResourceMap.containsKey(mediaId)) {
      return Utils.loadImageFromStorage(mAlbumResourceMap.get(mediaId));
    }

    return null;
  }

  public static void setContent(Context context) {

    Log.d(TAG, "++setContent(Context)");
    String sortOrder = MediaStore.Audio.Media.YEAR + " DESC";
    String[] projection = new String[]{
      MediaStore.Audio.Media._ID,
      MediaStore.Audio.Media.ALBUM,
      MediaStore.Audio.Media.ALBUM_ID,
      MediaStore.Audio.Media.ARTIST,
      MediaStore.Audio.Media.ARTIST_ID,
      MediaStore.Audio.Media.TITLE,
      MediaStore.Audio.Media.TRACK,
      MediaStore.Audio.Media.DURATION,
      MediaStore.Audio.Media.YEAR,
      MediaStore.Audio.Media.IS_AUDIOBOOK,
      MediaStore.Audio.Media.IS_MUSIC,
      MediaStore.Audio.Media.IS_PODCAST
    };

    try (Cursor cursor = context.getContentResolver().query(
      MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
      projection,
      null,
      null,
      sortOrder)) {

      assert cursor != null;
      while (cursor.moveToNext()) {
        long artistId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID));
        long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
        String mediaId = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
        mMediaMap.put(
          mediaId,
          new MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaId)
            .putLong(MediaStore.Audio.Media.ALBUM_ID, albumId)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)))
            .putLong(MediaStore.Audio.Media.ARTIST_ID, artistId)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)))
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
            .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)))
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)))
            .build());

        mMusicFileNameMap.put(mediaId, title); // TODO: this isn't the filename

        // save the album bitmap for later
        File pathToBitmap = Utils.saveImageToStorage(context, artistId, albumId, mediaId);
        if (pathToBitmap != null) {
          mAlbumResourceMap.put(mediaId, pathToBitmap);
        }
      }
    }
  }

  public static List<MediaBrowserCompat.MediaItem> getMediaItems() {

    Log.d(TAG, "++getMediaItems()");
    List<MediaBrowserCompat.MediaItem> result = getMediaItemsFromDatabase();

    for (MediaMetadataCompat metadata : mMediaMap.values()) {
      result.add(new MediaBrowserCompat.MediaItem(metadata.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
    }

    return result;
  }

  public static MediaMetadataCompat getMetadata(String mediaId) {

    Log.d(TAG, "++getMetadata(Context, String)");
    MediaMetadataCompat metadataWithoutBitmap = mMediaMap.get(mediaId);
    Bitmap albumArt = getAlbumBitmap(mediaId);

    MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder();
    builder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, metadataWithoutBitmap.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
    builder.putLong(MediaStore.Audio.Media.ALBUM_ID, metadataWithoutBitmap.getLong(MediaStore.Audio.Media.ALBUM_ID));
    builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, metadataWithoutBitmap.getString(MediaMetadataCompat.METADATA_KEY_ALBUM));
    builder.putLong(MediaStore.Audio.Media.ARTIST_ID, metadataWithoutBitmap.getLong(MediaStore.Audio.Media.ARTIST_ID));
    builder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, metadataWithoutBitmap.getString(MediaMetadataCompat.METADATA_KEY_ARTIST));
    builder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, metadataWithoutBitmap.getString(MediaMetadataCompat.METADATA_KEY_TITLE));
    builder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, metadataWithoutBitmap.getLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER));
    builder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, metadataWithoutBitmap.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
    builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt);

    return builder.build();
  }

  public static String getMusicFilename(String mediaId) {

    Log.d(TAG, "++getMusicFilename(String)");
    return mMusicFileNameMap.containsKey(mediaId) ? mMusicFileNameMap.get(mediaId) : null;
  }

  public static String getRoot() {

    return "root";
  }

  /*
    Private Method(s)
   */
  private static List<MediaBrowserCompat.MediaItem> getMediaItemsFromDatabase() {

    Log.d(TAG, "++getMediaItemsFromDatabase()");
    List<MediaBrowserCompat.MediaItem> mediaItemList = new ArrayList<>();

    return mediaItemList;
  }
}
